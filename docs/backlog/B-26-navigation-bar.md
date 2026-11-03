---
id: B-26
title: "NavigationBar: black strip, red square behind the active icon"
status: open
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-11, B-12]
---

# B-26 — NavigationBar: black strip, red square behind the active icon

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `lock`.

- **What to build.** A 64 dp black strip (concrete in the dark theme) with stencil icons and labels always visible; the red square moves horizontally behind the active icon (120 hard-stop). Cream icon on red is 4.64:1; a red icon on dark fails contrast, which is why active is a fill (Icons page).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`
