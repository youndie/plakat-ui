---
id: B-21
title: "FAB: the one round shape, riding in on the axis"
status: open
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
