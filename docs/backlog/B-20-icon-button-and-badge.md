---
id: B-20
title: "IconButton and Badge"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-11, B-12]
---

# B-20 — IconButton and Badge

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `stamp / tick`.

- **What to build.** A 48 dp square with a stencil icon, pressed inverts and shifts the icon 1 dp (0 / 60 hard-stop); the Badge is a red square with a number that changes like the Счётчик (40 ms a step).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatIconButton(icon, contentDescription, onClick, modifier, enabled, selected,
  interactionSource)`, a stencil rather than a slot since the design draws only stencils.
  `PlakatBadge(value, modifier)`, a null value being the dot, and `PlakatBadgedBox(value, modifier,
  content)`, the page's 48 dp box with the number at `right: 4px; top: 6px` and the dot at `8, 8`.
- **Parity** (`IconButton_*`, `Badge_*`, the MVP page). IconButton 0.00 % in all ten cells; the dot
  0.00 %. The number 2.52 %, with nothing outside the word: its two glyphs are 64.4 % of their ink
  against the 63.7 % floor, **0.7 points over**. One pixel weighs 1.1 % in a word of 58 differing
  pixels, and no whole-pixel shift lowers it (best at 0, 0); stated rather than chased.
- **A plate as wide as the real text.** First 3.95 %, a column of red on the plate's left edge:
  Compose ceils a layout's width (14 for 13.x) where Blink keeps the fraction and snaps the box. The width
  is now the number's `getLineRight` plus the padding, rounded half up. It is the target's, so the
  plate stands still through a tick.
- **The design disagrees with itself on the press.** The page's IconButton / Pressed cell has no shift,
  while its spec row says «иконка +1 dp» and its FAB / Pressed cell draws `translate(1px, 1px)`. The
  item follows the row, so the live press shifts and the test holds that; the fixture's forced look
  follows the cell, so parity still holds the colours.
- **The renderer**: a cell after a wider caption in a wrapping row starts on a fraction across, so a
  margin below a pixel now puts it on a whole one for the shot; the 130 earlier references re-rendered
  byte-identical. Captions shared with the FAB and Chip name their occurrence on the page.
- **Shared press**: `PlakatPress.kt` takes the stamp and release out of the Button (its eight tests and
  its goldens unmoved), for the FAB and chips to come.
- **Motion**: `tick` is `PlakatTick`, the MVP page's `ticks(from, to)`: digits step straight from old to
  new, lowest first, 40 ms a step, at most 400 ms in all, showing as many digits as the target has. A
  test of 999 → 0 found a real defect on the way: 27 steps of 400 / 27 ms summed short, the last digit
  stayed undone and the badge would have rested on 100; hence the epsilon.
- **Designed here** (D13): a change during a tick goes on from what shows (the page's lab restarts from
  the old target); values below zero count as zero; numbers past three digits keep every digit (the
  page's three columns stop at 999); the pressed look of a selected button inverts as any other does.
- **Events**: `stamp` and `release` once per gesture, `tick` once per change of value.
- **Mutants**: through the behaviour tests — no icon shift, the icon ink while held, the return linear,
  the return 120, the highest digit first, a step of 50, no 400 cap, no float guard, a tick from the old
  target (survived the first test, which read only the frame after the change, and is killed by the
  stronger one), reduced motion ticking; through the goldens — selected ink, the ring further in,
  disabled ink, the number 5 right, the dot 7 in, the padding 5. All sixteen killed.
