---
id: B-28
title: "Dialog: the announcement"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-28 — Dialog: the announcement

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `announce`.

- **What to build.** A red title plate; the background is closed by a diagonal wipe (the MVP page: by a halftone screen) and the dialog rides down from the top on a rail (320 linear, reduce motion fade 100).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatDialog(visible, title, onDismissRequest, modifier, text, actions)`, an overlay placed last in a screen-filling
  box, and `PlakatDialogAction(text, onClick, modifier, danger, enabled)`: the kit's text and danger buttons at the dialog's metrics
  (13 px with no tracking, a rule 3 under, plate and inset of the page), so pressing and focus are the kit's. A slot, so an app may
  put any button there. The page draws only the danger dialog; a plain confirmation is the same with a plain action.
- **Parity** (`Dialog_*`, the MVP page's 390 × 420 stage, its rows under the screen): 1.47 / 1.46 %. Nothing differs outside the words:
  the screen of dots, the sheet, the cut plate and the hatch are 0 px or within a few pixels (the plate 46 px on the title's
  edges, the hatch 7). The body is 47 % of its ink, the plain word 52 %, the danger word 54–55 %, against the 63.7 % floor.
- **Residual named**: the plain action stands a pixel right of Chrome's. Chrome lays the row from its end in fractions (the
  danger action 109.35 wide, so the plain one's rule starts at 150.35, drawn from 150); whole-pixel boxes reach 151. It is the
  catalogue's "a row of buttons drifts"; each box is right, the sum is not.
- **What took it from 1.96 %**: `ButtonMetrics.wordWide`, a box as wide as the word's real width rounded rather than the ceiled
  one, which made the danger action 110 for 109; and the shared `PlakatCssText.kt`, the posters' line-by-line placement, for
  the body's 23.2 px lines and a title line shorter than its face. The Button and Poster goldens did not move for it.
- **The screen** is a tile of the page's `radial-gradient(circle, ink 0 1.6px, transparent 1.8px)` evaluated at pixel centres: at
  scale 1, 12 solid pixels of 16. Its ink is the theme's in the light theme and the page's `scrim: #0E0D0B` in the dark, which is
  no token of the design's: **a colour literal, with its source**, for a re-import to look at.
- **Motion**: one clock of 320 ms, linear, for the wipe (`PlakatWipe.Diagonal(120.dp)`) and the ride (from `-320`, whatever the
  sheet's height), both ways; the lab transitions both on the same value. Reduced motion: a 100 ms fade of both, at their places.
- **Designed here** (D13): opening also when composed visible, with its `announce`; a click on the screen and Escape ask to dismiss
  (the page draws no interaction); closed, it leaves the composition; a title of several lines takes the face's own line.
  **Not done, for [B-36](B-36-accessibility.md)**: it traps neither focus nor the system back gesture.
- **Events**: `announce` on open and on close.
- **Mutants**: through the behaviour tests — hard-stop, 200 ms, a fade of 200, a ride from 160, a slant of 60, staying composed, no
  `announce`, a screen that does not dismiss, a sheet letting clicks through, no Escape, no dialog semantics; through the goldens —
  a smaller dot, the dark screen in the theme's ink, the sheet at 90, padding 18, the title unlifted, a body line of 1.4, tracked
  actions, ceiled word widths, no cut. All twenty killed.
