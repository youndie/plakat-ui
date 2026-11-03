---
id: B-19
title: "Плакат: Круг, Баннер, Фон — generator, raskat roll, diagonal title"
status: open
priority: P1
size: L
stage: stage-2-components
epic: feature-components
blocked_by: [B-03, B-04, B-09, B-10, B-12, B-13]
---

# B-19 — Плакат: Круг, Баннер, Фон — generator, raskat roll, diagonal title

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `print / roll`.

- **What to build.** `PlakatPoster` in its three layouts (Круг 390 × 392, Баннер 390 × 160, Фон 390 × 500): a Display title rotated to −17° as a block, a composition from the generator seeded by content ([B-03](B-03-generator-port-and-numeric-parity.md)), at most one raskat shape ([B-04](B-04-raskat-port.md)), one slogan button. Appear: shapes wipe in from the left with a 30 ms cascade, then title and button (≤ 300 linear); the raskat rolls (320 hard-stop, reduce motion: final state at once). A new seed reprints without animation.
- Desktop layout of the poster is part of this item if the design draws it ([B-39](B-39-desktop-and-hover.md)).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`
