# UI Guidelines

These guidelines define the intended user interface direction for My Budget. Use them as the source of truth for Android UI design decisions.

## Product Feel

The UI should feel calm, clean, friendly, practical, and a little colorful.

The app should be approachable for casual users while still feeling trustworthy for money-related tasks. Avoid flashy marketing-style layouts, childish visuals, dense enterprise dashboards, and unnecessary decoration. Use color to improve scanning and make the app feel approachable, not as visual noise.

## Layout

Use a dashboard-first layout. The home screen should open to the dashboard.

Screens should use clear sections, consistent spacing, and a consistent top app bar. Use summary cards for high-level financial totals. Use compact lists for repeated data such as transactions.

Avoid nested cards and oversized hero-style layouts. This is an everyday utility app, not a landing page.

Transaction add/edit forms should start as full-screen screens. Bottom sheets may be added later for quick entry when they clearly improve the workflow.

## Navigation

Use a simple hamburger/drawer menu for main navigation.

The drawer should include these main destinations:

- Dashboard
- Transactions / History
- Budgets
- Categories
- Reports
- Recurring Transactions
- Settings

Keep the drawer simple at first, with the app name and menu items rather than financial summaries.

Do not hide primary workflow actions inside the drawer. Each screen should expose its most common action directly, such as adding a transaction, budget, or category. Dashboard and transaction history should provide prominent access to adding a transaction.

## Transaction Entry

Transaction entry should be fast and amount-first.

Use one combined Add/Edit Transaction screen for both income and expense. Use a clear Expense/Income segmented control. Focus the amount field when opening the screen.

Required fields:

- amount
- type
- category
- date

Optional fields:

- note
- recurring settings
- reminder

Date should default to today. Category selection should be quick and visual where practical. Recurring and reminder options should be secondary or collapsed so normal one-time transactions stay fast to enter.

Edit transaction should reuse the add transaction screen. Normal transaction deletion should delete immediately with an undo snackbar. Confirmation dialogs may be used later for bulk or high-risk destructive actions.

## Dashboard

The dashboard should answer the user's immediate budgeting questions:

- how much came in
- how much went out
- how much remains
- whether the user is close to exceeding a budget

The default top metric should be remaining monthly budget. Users may later customize the top dashboard metric.

Show income, expenses, remaining budget, and net balance for the current period. Show budget progress with warning states. Show quick actions for adding expense/income and, when useful, adding a budget.

Show recent transactions with a link to full history. Show a small chart preview with a link to reports.

## Data Display

Repeated financial data should use compact, scannable rows rather than large decorative cards.

Transaction rows should show:

- category
- title, note, or payee when available
- date or group context
- amount
- income/expense direction

Transaction history should group by month by default. Users may later customize grouping or range, such as day, week, month, year, or a custom range.

Use meaningful colors for income, expenses, warnings, and budget status, but do not rely on color alone. Use labels, icons, signs, or text where needed.

Categories must have their own color and icon. Users should be able to edit category color and icon.

The Categories screen should present categories as a visual manager rather than a plain row list. Use an Expense/Income switch, a 3-column phone grid, colored circular icons, concise labels, and a Create tile. Create/edit should use a Material 3 bottom sheet with title, type, icon, and color controls. Category deletion should archive/hide the category so existing records remain intact.

Budget rows should show progress indicators. Empty states should be simple and actionable, such as showing that no transactions exist yet and offering an add action.

## Forms And Validation

Forms should be forgiving, clear, and fast.

Required fields should be obvious. Validation errors should appear near the relevant field. Amount input should accept numbers only. Amount values should not be negative; transaction type determines income or expense.

Date fields should default to today and use a date picker.

Use both validation strategies:

- disable save when the form is obviously impossible to submit
- otherwise allow save and show clear validation messages

Warn before leaving screens with unsaved changes. Split long forms into clear sections. Keep advanced or less common options collapsed by default where practical.

## Charts And Reports

Reports should stay simple and decision-oriented. Charts should help users understand spending, income, budget usage, and trends without becoming an advanced analytics system.

Useful report types include:

- category spending breakdown
- income versus expenses over time
- budget usage/progress
- spending trend over time
- recurring transaction impact later

Charts should be understandable at a glance. Use labels and legends where needed. Do not rely on color alone. Allow filtering by time range. The default report range should be the current month.

The default reports screen should use a mixed overview, with category spending and budget usage most prominent. Reports should link back to related transaction lists where practical.

## Accessibility

Accessibility is part of normal UI quality.

Use readable text sizes and maintain strong contrast in both themes. Use minimum 48dp touch targets for tappable controls. Support dynamic font scaling where practical.

Icons and important controls need screen reader labels unless they are purely decorative. Do not communicate income, expense, warning, or budget status with color alone.

Keep form errors clear and associated with the relevant field. Ensure RTL layout mirrors correctly for Hebrew. Avoid cramped layouts that break when text expands.

Financial amounts should be visually easy to compare. Charts should include labels, legends, or textual summaries so information is not trapped in color or shape alone.

## Language And Tone

Copy should be short, direct, friendly, and low-jargon.

Use plain language and short labels. Use consistent terms for income, expense, budget, category, transaction, report, and reminder.

Friendly empty states are good, but avoid silly copy. Errors should explain what to fix. Confirmations should clearly describe the action.

English copy should use US English. Hebrew copy should be natural Hebrew, not literal word-for-word translation from English.

The app should sound calm and useful, not robotic or overly formal.

## Components

Create reusable Compose components for repeated financial patterns, but avoid premature abstraction. Prefer small components that wrap Material 3 behavior and project styling rather than large custom frameworks.

Expected reusable components include:

- TransactionRow
- SummaryMetricCard
- BudgetProgressCard or BudgetProgressBar
- CategoryChip
- CategoryIcon
- AmountInput
- DatePickerField
- EmptyState
- ConfirmDialog
- UndoSnackbar pattern
- ChartSection
- FilterBar
- TimeRangeSelector
- ThemeToggle
- LanguageSelector

Components should support both themes and RTL/LTR layouts. Components with icons must have content descriptions unless decorative. Financial components should align amounts consistently for easy scanning.

## Themes

Support two app themes:

- default theme: green and white
- night theme: black and warm-yellow

Prefer Material 3 Compose theming. Keep the palette a little colorful, but avoid turning the interface into a noisy or one-note color theme.

## Localization And Direction

Support English US and Hebrew.

User-visible strings should live in Android string resources when practical. UI should support switching between LTR and RTL layouts. Hebrew layouts should mirror correctly.

## Money Display

Represent money safely using integer minor units in app logic. Do not require or display a currency symbol for now.
