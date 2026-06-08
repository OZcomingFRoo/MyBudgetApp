# Remaining Work

This is a local planning note for follow-up work after the first Room/DataStore/Hilt data-layer implementation.

## App Foundation

- Replace the generated `Hello Android!` screen with the first real app shell.
- Add Navigation Compose and a simple hamburger/drawer structure.
- Add screens for dashboard, add transaction, history, categories, reports, and settings.
- Connect ViewModels to the new repositories.

## Transaction Flow

- Build fast add-income and add-expense entry forms.
- Use `MoneyFormatter.parseAmountMinor` for user-entered amounts.
- Let title and note remain optional.
- Let category be optional for quick entry.
- Add edit and hard-delete flows, with a confirmation dialog before delete.

## BudgetBook Flow

- Add UI for switching between BudgetBooks.
- Add UI for creating, editing, archiving, and deleting BudgetBooks.
- Keep `selectedBudgetBookId` in DataStore as the current selection.

## Categories

- Build category list and category picker UI.
- Support creating and editing custom categories.
- Archive categories instead of deleting them from normal UI flows.
- Map stored `iconName` values to Compose Material icons.

## Reports And Dashboard

- Build dashboard totals using SQL aggregate DAO queries.
- Show remaining balance as income minus expenses for the selected date period.
- Build monthly and category reports using `amountMinor` SQL aggregation.
- Divide and format minor units only at the display boundary.

## Recurring Transactions

- Build UI for creating and editing recurring transaction rules.
- Surface generated transactions in history like normal transactions.
- Decide later whether due recurring generation should show a confirmation UI or remain automatic.

## Settings

- Add theme switching for default and night themes.
- Add language switching for English US and Hebrew.
- Add RTL/LTR verification for Hebrew.

## Later

- Add home-screen widget for quick remaining-balance checks.
- Add export/import or backup.
- Consider reminders with WorkManager if needed.
- Keep cloud sync, account linking, attachments, OCR, bank sync, and multi-currency out of scope unless explicitly requested.
