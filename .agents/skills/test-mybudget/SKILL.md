---
name: test-mybudget
description: Run the My Budget Android project's tests. Use when the user asks Codex to run tests, unit tests, Gradle test, verify test status, check regressions, or rerun the test suite after code changes.
---

# Test MyBudget

## Workflow

Run tests from the repository root `V:\repos\MyBudgetApp`.

Use Android Studio's bundled JBR when Java is not already configured in the shell:

```powershell
$env:JAVA_HOME='V:\Android\Android Studio\jbr'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat test
```

If Gradle needs to download its wrapper distribution or dependencies and the sandbox blocks network access, rerun the same command with approval/escalation rather than changing project files.

Run instrumented Android tests only when the user explicitly asks or when the task requires a device/emulator.

## Reporting

Report whether `test` passed or failed. If it fails, include the failing task, the first actionable compiler/test error, and any relevant report path if Gradle prints one.
