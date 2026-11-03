---
id: B-09
title: "Theme: PlakatTheme for paper and black paper, reduce motion as a property, print switches"
status: open
priority: P0
size: S
stage: stage-1-materials
blocked_by: [B-08]
---

# B-09 — Theme: PlakatTheme for paper and black paper, reduce motion as a property, print switches

Two themes: «Бумага» and «Чёрная бумага», which changes four tokens and keeps the inks (research §1.5).

- **The decision and its reason.** `PlakatTheme(tokens, reduceMotion = null, texture, registration, raskat, strings)`: reduce motion defaults to the platform through `expect` and is never read inside a component, so a fixture can force it (D9); the print layers switch as the tokens' `enabled` flags; accessible names of the kit's own controls come from `strings` so an app can localise them.
- Rejected: reading the platform setting inside components.

- AC: a fixture forces reduce motion on and off; switching theme recolours the sample with no other change; the print switches toggle their layers.
- Anchors: `reference/design-system/plakat-tokens.json`
