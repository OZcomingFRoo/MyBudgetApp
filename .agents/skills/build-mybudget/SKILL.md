---
name: build-mybudget
description: Build the My Budget Android project. Use when the user asks Codex to build, compile, assemble, verify compilation, produce a debug APK, run assembleDebug, or check whether the Android project currently builds.
---

# Build MyBudget

## Workflow

Run the build from the repository root `V:\repos\MyBudgetApp`.

Use Android Studio's bundled JBR when Java is not already configured in the shell:

```powershell
$env:JAVA_HOME='V:\Android\Android Studio\jbr'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

If Gradle needs to download its wrapper distribution or dependencies and the sandbox blocks network access, rerun the same command with approval/escalation rather than changing project files.

## Reporting

Report whether `assembleDebug` passed or failed. If it fails, include the first actionable error and the likely environment issue when applicable, such as missing Java, missing Android SDK, blocked network, or dependency resolution failure.
