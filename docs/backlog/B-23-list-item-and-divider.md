---
id: B-23
title: "List item and Divider"
status: done
priority: P1
size: S
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-23 — List item and Divider

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `stamp`.

- **What to build.** A row with a hairline divider and a number or square marker on the left; pressed inverts the row (0 / 60). Divider: 1 or 3 dp rule, 8 dp bar for sections.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatListItem(headline, modifier, supporting, number, trailing, onClick, enabled, divider,
  interactionSource)`: a number, or without one the square marker; a trailing slot in the content
  colour (the fixtures put the mirrored Back stencil and a `PlakatSwitch` there); `onClick` null only
  shows. `PlakatDivider(modifier, weight)` with `Hair`, `Rule` and `Bar`.
- **Parity** (`ListItem_*`, four rows cropped out of the MVP page's one block; the renderer gained a
  part for the block after a heading). Nothing differs outside the words in any of the eight, and no
  whole-pixel shift lowers them; the switch inside its row is 0 px.

  | Row | Whole | Number | Headline | Supporting |
  |---|---|---|---|---|
  | Default L / D | 2.57 / 2.57 % | 19 % | 33 % | 58 % |
  | Pressed L / D | 2.49 / 2.47 % | 13 % | 38 % | 48 % |
  | Switch L / D | 1.98 / 1.94 % | — | 46 % | — |
  | Disabled L / D | 1.46 / 1.17 % | — | 34 / 27 % | — |

  All of its ink, against the 63.7 % floor. The Divider's cells are the floor's own references: the floor
  fixtures now draw `PlakatDivider`, at 0.00 % in both themes, their goldens unmoved.
- **The press**: the row's word for reduced motion is «то же», so unlike the IconButton's the 60 ms return
  stays under it (D9): `rememberPlakatPress(sameUnderReducedMotion = true)`. The row does not name a curve;
  the hard-stop is the kit's `release`.
- **The border adds to the height**: `min-height: 72px` on a `content-box` with `border-bottom: 1px` is a
  73 row, 57 without a supporting line, so the divider is laid out below the row, not over it. The page's
  pressed row is drawn without its border (72), which only shows in a picture: ink on ink live, so the fixture
  passes `divider = false`. A disabled row mutes its words and marker and keeps the divider in ink.
- **Designed here** (D13): the chevron is a consumer's slot rather than a built-in (the page mirrors Back);
  a press on a row with a trailing control presses the row, the control keeping its own target.
- **Events**: `stamp` and `release` once per gesture.
- **Mutants**: through the behaviour tests — instant under reduced motion, no inversion, the words staying
  paper past the lift (survived the first test, which read the colour only after the ground was home, and
  is killed by the stronger one), a return of 120, no border height, a rule of 2; through the goldens — a
  row of 70, a column of 26, a marker of 10, a number of 16, a regular headline, a semi-bold one without a
  line, a muted border, a bar of 6. All fourteen killed.
