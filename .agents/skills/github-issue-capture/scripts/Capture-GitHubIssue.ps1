param(
    [Parameter(Mandatory = $true)]
    [string]$Issue,

    [string]$Repo,

    [string]$OutRoot = "github-issues",

    [string]$GhPath = "gh"
)

$ErrorActionPreference = "Stop"

function Resolve-GhPath {
    param([string]$Candidate)

    if (Get-Command $Candidate -ErrorAction SilentlyContinue) {
        return (Get-Command $Candidate).Source
    }

    $workspaceGh = Join-Path (Get-Location) ".codex\tools\gh\bin\gh.exe"
    if (Test-Path $workspaceGh) {
        return $workspaceGh
    }

    throw "GitHub CLI not found. Install gh or pass -GhPath."
}

function Resolve-Repo {
    param([string]$ExplicitRepo, [string]$Gh)

    if ($ExplicitRepo) { return $ExplicitRepo }

    $remote = git remote get-url origin 2>$null
    if (-not $remote) { throw "Repo was not provided and no git origin remote was found." }

    if ($remote -match "github\.com[:/](?<owner>[^/]+)/(?<name>[^/.]+)(\.git)?$") {
        return "$($Matches.owner)/$($Matches.name)"
    }

    throw "Could not infer GitHub repo from origin: $remote"
}

function Get-IssueNumber {
    param([string]$Value)

    if ($Value -match "/issues/(?<n>\d+)") { return $Matches.n }
    if ($Value -match "#(?<n>\d+)$") { return $Matches.n }
    if ($Value -match "^(?<n>\d+)$") { return $Matches.n }

    throw "Could not parse issue number from '$Value'."
}

function Get-AttachmentUrls {
    param([string[]]$Texts)

    $urls = New-Object System.Collections.Generic.List[string]
    foreach ($text in $Texts) {
        if (-not $text) { continue }
        foreach ($match in [regex]::Matches($text, 'https://github\.com/user-attachments/assets/[A-Za-z0-9-]+')) {
            $urls.Add($match.Value)
        }
        foreach ($match in [regex]::Matches($text, '!\[[^\]]*\]\((?<url>https?://[^)]+)\)')) {
            $urls.Add($match.Groups['url'].Value)
        }
        foreach ($match in [regex]::Matches($text, '<img[^>]+src="(?<url>https?://[^"]+)"')) {
            $urls.Add($match.Groups['url'].Value)
        }
    }

    return $urls | Select-Object -Unique
}

$gh = Resolve-GhPath -Candidate $GhPath
$issueNumber = Get-IssueNumber -Value $Issue
$repoName = Resolve-Repo -ExplicitRepo $Repo -Gh $gh
$outDir = Join-Path $OutRoot "issue-$issueNumber"
$attachmentsDir = Join-Path $outDir "attachments"
New-Item -ItemType Directory -Force -Path $outDir, $attachmentsDir | Out-Null

& $gh auth status 1>$null
$issueArgs = @('issue', 'view', $issueNumber, '--repo', $repoName, '--comments', '--json', 'number,title,state,author,createdAt,updatedAt,body,comments,labels,url,assignees,milestone')
$json = & $gh @issueArgs 2>&1
if ($LASTEXITCODE -ne 0) { throw "gh issue view failed: $json" }
$jsonPath = Join-Path $outDir "issue.json"
$json | Set-Content -LiteralPath $jsonPath -Encoding UTF8
$issueData = $json | ConvertFrom-Json

$texts = @($issueData.body)
foreach ($comment in @($issueData.comments | Where-Object { $_ })) { $texts += $comment.body }
$attachmentUrls = @(Get-AttachmentUrls -Texts $texts)

$downloadNotes = New-Object System.Collections.Generic.List[string]
$token = $null
if ($attachmentUrls.Count -gt 0) {
    $token = & $gh auth token
}

for ($i = 0; $i -lt $attachmentUrls.Count; $i++) {
    $url = $attachmentUrls[$i]
    $fileName = "attachment-$($i + 1).bin"
    if ($url -match '/([^/?#]+)(?:[?#].*)?$') { $fileName = "attachment-$($i + 1)-$($Matches[1]).bin" }
    $target = Join-Path $attachmentsDir $fileName

    try {
        Invoke-WebRequest -Uri $url -Headers @{ Authorization = "Bearer $token"; Accept = "application/octet-stream" } -OutFile $target
        $length = (Get-Item $target).Length
        if ($length -le 16) {
            $content = Get-Content -Raw -LiteralPath $target -ErrorAction SilentlyContinue
            throw "Downloaded only $length bytes: $content"
        }
        $downloadNotes.Add("- $url -> attachments/$fileName ($length bytes)")
    } catch {
        $downloadNotes.Add("- $url -> FAILED: $($_.Exception.Message)")
    }
}

$labels = @($issueData.labels | ForEach-Object { $_.name }) -join ", "
if (-not $labels) { $labels = "None" }
$comments = @($issueData.comments | Where-Object { $_ })
$md = @()
$md += "# Issue #$($issueData.number): $($issueData.title)"
$md += ""
$md += "- URL: $($issueData.url)"
$md += "- Repo: $repoName"
$md += "- State: $($issueData.state)"
$md += "- Author: $($issueData.author.login)"
$md += "- Created: $($issueData.createdAt)"
$md += "- Updated: $($issueData.updatedAt)"
$md += "- Labels: $labels"
$md += ""
$md += "## Body"
$md += ""
$md += $issueData.body
$md += ""
$md += "## Attachments"
$md += ""
if ($downloadNotes.Count -gt 0) { $md += $downloadNotes } else { $md += "None found." }
$md += ""
$md += "## Comments"
$md += ""
if ($comments.Count -eq 0) {
    $md += "No comments."
} else {
    foreach ($comment in $comments) {
        $md += "### $($comment.author.login) at $($comment.createdAt)"
        $md += ""
        $md += $comment.body
        $md += ""
    }
}
$md += ""
$md += "## Agent Summary"
$md += ""
$md += "TODO: Inspect attachments and summarize the issue before implementation."

$mdPath = Join-Path $outDir "issue.md"
$md | Set-Content -LiteralPath $mdPath -Encoding UTF8

Write-Output "Captured $repoName#$issueNumber in $outDir"
Write-Output "Issue markdown: $mdPath"
Write-Output "Attachments: $attachmentsDir"
