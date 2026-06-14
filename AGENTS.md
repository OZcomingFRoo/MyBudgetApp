# AGENTS.md

This file is the main AI-agent guide for the My Budget Android project. Future agents should read it before making project changes.

## Project Purpose

My Budget is a casual Android budgeting app for managing money, tracking how much users spend, and tracking how much they earn.

The app is intended for casual personal finance usage by individuals, families/households, freelancers, and small business owners. It should help users understand spending habits, track income versus expenses, monitor how much money remains from entered income, avoid overspending, and eventually export or share records.

This is not intended to become an advanced accounting, tax, investment, or enterprise finance system unless explicitly requested.

## Product Scope

The app should eventually support:

- adding expenses
- adding income
- predefined and user-created categories
- recurring transactions
- dashboard/summary views
- charts and reports
- transaction history
- search and filters
- export
- reminders
- settings
- editing and deleting user data
- home-screen widgets for quick remaining-balance checks

Main screens should include:

- home/dashboard
- add transaction
- history
- categories
- reports/charts
- settings

Out of scope unless explicitly requested:

- bank account integration
- credit card syncing
- investment tracking
- tax/accounting reports
- multi-user collaboration
- automatic subscription detection
- OCR receipt scanning
- currency exchange
- multi-currency support
- formal budget-limit tables or budget planning models

Data should be local-only for now. Future cloud sync may be considered later, preferably through familiar account providers such as Google/Gmail or Facebook.

## Agent Permissions

Agents may edit project files to accomplish the user's task.

Agents may run local commands, including file searches, Gradle builds, tests, formatters, linters, and Git commands.

Agents may run all Git commands except pushing to origin. Do not push changes to any remote repository.

Agents must ask before installing dependencies, updating dependencies, or downloading new tooling.

No files are globally protected, but agents must preserve unrelated user changes and avoid broad refactors unless the user explicitly asks for them.

## Android Direction

The production Android namespace and Google Play application ID are
`com.ozcomingfroo.mybudget`. Do not use the old starter namespace for new
code, tests, generated references, or run configurations.

Use Jetpack Compose for UI.

Use MVVM with simple clean/layered boundaries. Keep business logic outside UI and view models when practical.

Use a feature-first package structure, with shared `data`, `domain`, and `core` packages where useful. The main app shell lives under `ui/MyBudgetApp.kt`; dedicated Compose screens live under `ui/dashboard`, `ui/transactions`, `ui/history`, `ui/categories`, `ui/reports`, and `ui/settings`, with shared UI helpers under `ui/components` and `ui/util`.

Use Room for persistent app data. The current local data model is `BudgetBook`, `Category`, `Transaction`, and `RecurringTransaction`. Do not add a formal `Budget` model unless explicitly requested.

Use DataStore for settings.

Use Hilt for dependency injection.

Use Jetpack Navigation Compose for navigation.

Use `java.time` for date and time handling.

Avoid complex abstractions until the app actually needs them. Keep early implementations simple and easy to change.

## UI Direction

Use a calm, practical UI optimized for quick daily use and fast transaction entry.

For UI design, implementation, polish, or review tasks, use the repo-local `android-ui-designer` skill and follow `docs/ui_guidelines.md`.

Prefer Material 3 Compose components.

Use a simple hamburger/drawer menu for navigation between main screens.

Categories should use a visual grid manager: Expense/Income switching, three
columns on phones, category color/icon display, category title labels, and a
bottom-sheet create/edit flow. Deleting a category should archive/hide it
rather than hard-delete it.

Support two app themes:

- default theme: green and white
- night theme: black and warm-yellow

Support English US and Hebrew. UI should support switching between LTR and RTL layouts.

Put user-visible strings in Android string resources when practical.

Money should be represented safely using integer minor units. Do not require or display a currency symbol for now.

## Build And Test Commands

Use these commands on Windows when relevant:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
.\gradlew.bat connectedDebugAndroidTest
```

Run `assembleDebug` after meaningful code changes.

Run unit tests after logic or data changes.

Run instrumented Android tests only when relevant or explicitly requested, because they require an emulator or device.

Add or update tests for meaningful behavior changes.

Use formatter or linter commands when they are configured in the project.

If a command cannot run because of missing Android SDK, missing emulator/device, dependency issues, or environment problems, report that clearly.

## Documentation Rules

`AGENTS.md` is the main AI-agent-facing documentation file.

`README.md` is user/developer-facing project documentation.

Files under `md_chat/` are brainstorming notes only. They are not authoritative project documentation unless their contents are explicitly promoted into `AGENTS.md`, `README.md`, or another formal doc.

Update `AGENTS.md` when architecture, commands, permissions, or major product decisions change.

Do not create many documentation files unless documentation is explicitly requested or clearly needed.

Use TODOs only when an answer is truly unknown. Do not invent project decisions.
