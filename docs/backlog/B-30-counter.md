---
id: B-30
title: "Счётчик: the Isotype stat tile with a mechanical counter"
status: wip
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-31]
---

# B-30 — Счётчик: the Isotype stat tile with a mechanical counter

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `tick`.

- **What to build.** `PlakatCounter`: a big number in Rubik Mono One stretched to its box, a row of pictograms "one = N" and a caption. Digits roll per place, lowest first, 40 ms a step, the whole tick ≤ 400 ms; pictograms are added one by one.
- The Stretch page draws one more cell of it, «Счётчик / Колонна»: the number through `PlakatStretch`, 104 high, 56 wide per place, on concrete; the tick changes the digits by a change of sheet, with no scale (found in [B-31](B-31-stretch-text.md)).
- The legend's `■` and `●` are not in either font (research §1.4): they are drawn as the pictogram shapes, inline with the text.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`
