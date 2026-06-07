---
name: android-ui-designer
description: Design, implement, polish, or review the My Budget Android UI. Use when Codex works on Jetpack Compose screens, themes, navigation, layout, accessibility, localization, RTL/LTR behavior, transaction-entry UX, dashboard UI, reports/charts UI, or reusable Compose components for this app.
---

# Android UI Designer

Use this skill for UI/UX work in the My Budget Android app.

## Workflow

1. Read `AGENTS.md`.
2. Read `docs/ui_guidelines.md`.
3. Inspect existing Compose theme, navigation, components, and affected screens before editing.
4. For new screens or major UI changes, create a brief text wireframe or implementation outline when it helps clarify the change.
5. Implement using Jetpack Compose, Material 3, and existing project components/patterns.
6. Preserve both app themes, RTL/LTR behavior, localization expectations, and accessibility rules.
7. Avoid generated images as the source of truth for UI layout or consistency.
8. Run relevant build/tests after meaningful UI code changes when practical.
9. Update `docs/ui_guidelines.md` if a new UI decision becomes permanent.

## Design Priorities

- Keep the UI calm, clean, friendly, practical, and a little colorful.
- Optimize for quick daily use and fast transaction entry.
- Prefer dashboard-first flows and compact, scannable financial data.
- Keep primary actions visible on the relevant screen.
- Use reusable Compose components once patterns repeat, but avoid premature abstraction.
- Make accessibility and RTL support part of the implementation, not a later cleanup task.

## Review Checklist

Before finishing UI work, check:

- Text fits at typical phone sizes and with longer Hebrew/English strings.
- Buttons and controls have stable sizes and touch targets.
- Icons have content descriptions unless decorative.
- Income, expense, warning, and budget states are not communicated by color alone.
- Both default and night theme choices are respected.
- RTL mirroring is considered for layout, navigation, rows, and forms.
- Transaction and money displays are easy to scan and compare.
