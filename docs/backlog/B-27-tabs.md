---
id: B-27
title: "Tabs: rubrics with a red bar riding linearly"
status: open
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-27 — Tabs: rubrics with a red bar riding linearly

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `rail`.

- **What to build.** Rubric tabs with an 8 dp red indicator that rides linearly (200), the content shifting with a hard stop.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`
