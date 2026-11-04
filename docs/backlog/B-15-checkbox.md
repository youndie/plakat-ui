---
id: B-15
title: "Checkbox: the red wedge in and out"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-15 — Checkbox: the red wedge in and out

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `wedge-in / wedge-out`.

- **What to build.** A 24 dp square in a bar frame; the mark is a red wedge entering from the top-right corner to the stop (120 hard-stop) and leaving the same way (60 linear). Not a tick.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`

## Findings (2026-11-04)

- **API**: `PlakatCheckbox(checked, onCheckedChange, modifier, enabled, error, interactionSource)`,
  Material's shape; `onCheckedChange = null` only shows. The zone is 48 dp (`plakat/size/touch`), the
  box 24 (`plakat/size/checkbox`), the frame 3 (1 when disabled), the wedge the 18 dp triangle inside
  a 3 dp inset — the page's `clip-path: polygon(0 0, 100% 0, 100% 100%)`.
- **Parity**: Off, On, Focused, Disabled at **0.00 %** in both themes, the wedge and the focus ring
  included. Error 1.08 / 1.12 %: the box at 0, the rest is the page's word «Нужно» beside it, which
  the fixture sets (13 px Golos 600) and the component does not — the error is a form (red frame and
  hatch), the word is the caller's.
- The error hatch is the page's `repeating-linear-gradient(45deg, red 0 2px, transparent 2px 5px)`:
  `plakatHatchBrush` gained `inkShare` (default ½, the danger hatch unchanged) for its 2-in-5.
- **Motion**: the wedge moves along the diagonal out to the top-right, clipped to the inset; in 120 ms
  hard-stop, out 60 ms linear, the live lab's `translate(100%, -100%)`. The test reads the wedge's way
  off the red area of the box each frame (the visible part is a triangle of side 18 − 2d) and holds
  it to the curve; nothing shows before half-way, as on the page.
- **Focus** ring: the page's `outline: 3px; outline-offset: -3px` on the 48 zone, keyboard only.
- **Designed here** (D13): the disabled unchecked look (the muted 1 dp frame, empty — the page draws
  disabled checked only); the error look when checked (the wedge over the hatch); no press feedback,
  since the spec row has none.
- **Events**: `wedge-in` and `wedge-out` on each change of `checked`, not on the first composition.
- **Mutants**: the wedge in linearly, out in 120 ms, in from the top-left — killed by the frame
  reading (and the last by the offset test); the wedge as the other half of the square, the error
  hatch at one-in-two — killed by the goldens.
