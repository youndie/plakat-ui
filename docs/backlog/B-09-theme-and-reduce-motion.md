---
id: B-09
title: "Theme: PlakatTheme for paper and black paper, reduce motion as a property, print switches"
status: done
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

## Findings

- 2026-11-03. `PlakatTheme(colors, reduceMotion, print, strings)` in
  `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/theme/PlakatTheme.kt`, read through the
  `PlakatTheme` object. Reduce motion defaults to the platform through one `expect`/`actual` pair
  (Android's animator scale, iOS Reduce Motion, the browser's media query; the desktop JVM `false`),
  and a fixture forces it. `PlakatPrintSwitches` carries the texture, non-print, registration and
  raskat switches, on by default as the tokens ship them.
- `PlakatTypography` is the token scale in the kit's families: size in sp, the CSS line height with
  the half-leading split evenly, tracking in em. Compose has no `text-transform`, so each role is a
  `PlakatText` whose `text()` upper-cases where the role is set in capitals (Display, Headline,
  Label, Button) and never body text. Outside a theme, reading the typography is an error, not a
  silent default.
- `PlakatStrings` holds the words of the kit's own controls: accessible names (back, close, menu,
  clear, refresh, loading, dismiss) and the words printed beside a colour so colour never carries
  meaning alone (error, warning, info). Russian by default, as the design (English since [B-57](B-57-english-kit.md)).
- The sample takes the palette and reduce motion as parameters and sets its title in Headline.
- Mutants, each killed by `PlakatThemeTest`: an explicit reduce motion ignored, capitals dropped,
  line height left at the font size, the print switches ignored.
- The event listener for sound is B-12; the theme gains it there.
