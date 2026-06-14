# Checkpoint

This file tracks where to resume in future sessions.

## Next Focus

The main Compose UI foundation, first functional Settings pass, and first-launch onboarding
flow have been implemented.

Latest committed work:

- `09b99caa` - Add main Compose UI foundation
- `459d6fe` - Implement settings localization controls
- `2ffdd89` - Fix Hebrew runtime localization and menu icon
- `5f2d05e` - Localize application name
- `0209c37` - Implement first launch onboarding
- `628a9de` - Allow editing budget book name in settings
- `ee087e1` - Add flag language selector in settings
- `17753a0` - Add theme preview selector in settings
- Replaced the launcher icon with user-provided Shekel wallet artwork for Israeli users.

Settings now includes:

- Budget Book section for editing the current budget book name.
- Appearance, Language, Transaction Defaults, Data & Privacy, and About sections.
- Visual theme preview tiles for Default and Night, plus Use system setting.
- Runtime language switching for system language, English, and Hebrew.
- Visual language selection tiles using US and Israel flag assets, plus Use system setting.
- Hebrew Android string resources for currently visible app UI strings.
- RTL layout direction when Hebrew is selected.
- Add Transaction opens with the selected default transaction type.
- Local-only data/privacy copy and About/version information.

Onboarding now includes:

- First-launch gate controlled by `hasCompletedOnboarding`.
- Single-screen setup before the main app shell.
- Optional budget book name, prefilled as `Personal`.
- Default transaction type, theme, and language choices.
- Starting defaults: Default/light theme and Hebrew language.
- Local-only privacy note, plus Skip and Start budgeting actions.
- Automatic starter category seeding for each new budget book.

Starter categories now seed:

- Expenses: Groceries, Restaurants & Coffee, Rent / Mortgage, Utilities,
  Phone & Internet, Transportation, Health & Medical, Shopping, Entertainment,
  and Other Expense.
- Income: Salary, Freelance / Business, Tips / Cash, Refunds, Gifts Received,
  and Other Income.

Application naming now localizes through `app_name`:

- English: `My Budget`
- Hebrew: `תקציב חיסכון`

Follow-up fixes after manual device review:

- Hebrew initially only changed layout direction while text stayed English.
- The fix provides both localized `Context` and localized Compose `LocalConfiguration`.
- Hebrew resources are now also available under Android's legacy `values-iw` qualifier,
  because Android commonly maps Hebrew to `iw` internally.
- The top app bar drawer opener now uses a hamburger icon drawable instead of visible
  "Menu" text.
- App startup now waits for the first real DataStore preferences emission before choosing
  onboarding or the main UI, preventing onboarding from flashing after it was completed.
- Settings language selection now uses rounded flag tiles. The flag drawables are based on
  MIT-licensed `lipis/flag-icons` assets, with `THIRD_PARTY_NOTICES.md` added.
- Settings theme selection now uses rounded preview tiles that show each theme's background,
  active accent color, and label color.

Launcher icon update:

- User provided finalized launcher icon PNG assets under
  `C:\Users\lonez\Downloads\my_budget_shekel_android_icon_assets`.
- `README_ANDROID_ICON_ASSETS.txt` was read before applying the assets.
- The supplied `ic_launcher_preview.png` is used as the actual launcher artwork because the
  supplied foreground PNG has a checkerboard baked into the image instead of transparent
  alpha.
- Adaptive icon XML now points to the preview artwork with an empty foreground layer and a
  simple monochrome Shekel drawable for Android themed icons.
- Legacy density launcher PNGs were generated from the preview artwork for mdpi, hdpi,
  xhdpi, xxhdpi, and xxxhdpi.

Verification completed for the Settings/localization work:

- `assembleDebug` passed using Android Studio JBR because the machine's global `JAVA_HOME`
  points to an invalid Java 8 install.
- `test` passed using Android Studio JBR.
- `connectedDebugAndroidTest` was not run.

Verification completed after onboarding and Settings budget-book rename work:

- `assembleDebug` passed using Android Studio JBR.
- `test` passed using Android Studio JBR.
- `connectedDebugAndroidTest` was not run.

Verification completed after Settings selector and launcher icon polish:

- `assembleDebug` passed using Android Studio JBR after the flag language selector.
- `assembleDebug` passed using Android Studio JBR after the theme preview selector.
- `assembleDebug` passed using Android Studio JBR after replacing the launcher icon with the
  user-provided Shekel artwork.
- Manual launcher-icon review on an installed device is still recommended because launcher
  masks vary by device and Android version.

Latest rebuild before manual debugging:

- `assembleDebug` passed again with all tasks up to date.
- Use the current PowerShell session form to avoid nested quoting issues:
  `$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug`

Next, verify and polish the new onboarding and Settings flows on a device/emulator:

- Confirm first launch shows onboarding with Hebrew/default theme selected by default.
- Confirm Skip completes onboarding and opens the main app.
- Confirm Start budgeting saves a renamed budget book and opens the main app.
- Confirm returning launches skip onboarding and open Dashboard.
- Confirm Settings can rename the current budget book and preserves the new name.
- Confirm drawer navigation works across Dashboard, Add Transaction, History, Categories,
  Reports, and Settings.
- Confirm adding a transaction updates Dashboard, History, and Reports.
- Confirm Settings theme changes are readable in Default and Night modes.
- Confirm the new Settings theme preview tiles are readable in Default and Night modes.
- Confirm the new Settings language flag tiles fit in English and Hebrew / RTL.
- Continue checking Settings language changes across all visible screens, especially Hebrew
  text fit and RTL layout on phone sizes.
- Confirm the launcher icon looks correct after install on a device/emulator, including
  themed icon mode where available.
- Polish any layout, spacing, copy, RTL, or theme issues found during visual testing.

## Current Reasoning

The project now has local data, DataStore preferences, a real first-pass MyBudget UI shell,
functional Settings for theme, language, transaction defaults, and current budget book name,
plus a committed first-launch onboarding flow that leads users into concrete screens. The
Settings page now has more visual selectors for theme and language, and the launcher icon is
being moved from the Android default to a Shekel wallet concept for Israeli users.
