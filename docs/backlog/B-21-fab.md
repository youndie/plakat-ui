---
id: B-21
title: "FAB: the one round shape, riding in on the axis"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-11, B-12]
---

# B-21 — FAB: the one round shape, riding in on the axis

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `rail`.

- **What to build.** The red circle, the only round shape on the screen, appearing along the −17° axis from beyond the bottom-right edge (200 hard-stop).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatFab(icon, contentDescription, onClick, modifier, enabled, rideIn, interactionSource)`.
  `rideIn = false` shows it in place, for a FAB returning with its screen. The target is the 56 dp box
  of the circle.
- **Parity**: Default, Pressed and Disabled at **0.00 %** in both themes (`Fab_*`, the MVP page). The pressed
  cell draws `translate(1px, 1px)`, so the fixture's forced look and the live press agree here, unlike
  the IconButton's.
- **The ride, which the design states only in words.** The spec row says «Выезжает по оси −17° из-за
  правого нижнего края»; no number and no lab. Taken whole it cannot be met: the −17° axis rises to the
  right, so past the right edge it stands above the FAB, not below. The axis is the kit's one diagonal,
  so it is kept: the FAB starts with its left side just past the window's right edge and higher by that
  distance × tan 17°, and rides down-left to its place. The distance is measured from where it is
  placed (`positionInRoot`, the root's width), so no number is invented and any margin works.
  **For the owner's review**: if "нижний" should win, the ride would climb from below on a +17° line.
- **Designed here** (D13): the focused look, a 3 dp ink ring 3 dp off the circle as the Button's (the page
  draws no focused FAB; the golden `Fab_Focused_*` is this library's own); the ride itself as above;
  `rideIn`. The press takes `PlakatPress`, with its 60 ms hard-stop return, which the spec row does not
  name for the FAB but does for the IconButton beside it.
- **Events**: `rail` on appear; `stamp` and `release` once per gesture.
- **Mutants**: through the behaviour tests — the ride linear, the ride 120, the axis flat, the axis
  downward, half the distance, reduced motion riding, no icon shift, no `rail`; through the goldens — the
  icon ink while pressed, disabled red, a size of 54, the ring 5 off. All twelve killed.
