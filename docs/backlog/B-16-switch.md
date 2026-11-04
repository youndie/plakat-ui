---
id: B-16
title: "Switch: rail, carriage and the red segment"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-16 — Switch: rail, carriage and the red segment

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `lever`.

- **What to build.** The 56 dp rail (3 dp rule), the 20 × 32 carriage, the red 8 dp segment filling behind the carriage (120 hard-stop). Disabled is the muted rail and a concrete carriage (Settings demo).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`

## Findings (2026-11-04)

- **API**: `PlakatSwitch(checked, onCheckedChange, modifier, enabled, interactionSource)`, Material's
  shape; `onCheckedChange = null` only shows. The target is the 56 × 48 track itself
  (`plakat/size/switch` × `plakat/size/touch`).
- **Parity**: Off, On, Focused, Disabled at **0.00 %** in both themes. The rail is where the page puts
  it, `top: 22px` in 48 — half a pixel above the middle — and the focus ring the page's
  `outline: 3px; outline-offset: 4px` (the reference crop grew 7 px a side for it).
- **Motion**: the carriage and the segment move together, 120 ms hard-stop both ways (the live lab
  transitions `transform` and `width` with the same curve); the test reads the carriage's
  antialiased left edge each frame and holds it to `cubic-bezier(.2,.8,.9,.9)`, and the red segment
  to the edge within a pixel.
- **Designed here** (D13): disabled and off (the concrete carriage at the left end; the page draws
  disabled on only); no press feedback (the spec row has none).
- **Events**: `lever` on each change of `checked`.
