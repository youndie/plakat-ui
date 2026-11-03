---
id: B-25
title: "TopAppBar: newspaper header, rapport strip, folding on scroll"
status: open
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-03, B-09, B-10, B-11, B-12]
---

# B-25 — TopAppBar: newspaper header, rapport strip, folding on scroll

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `fold`.

- **What to build.** Capitals over an 8 dp bar, a 24 dp rapport strip under the header seeded by the section ([B-03](B-03-generator-port-and-numeric-parity.md)); on scroll the rapport and the large title ride up and leave a 3 dp rule (linear with the scroll).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`
