---
id: B-15
title: "Checkbox: the red wedge in and out"
status: open
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
