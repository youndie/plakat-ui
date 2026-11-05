---
id: B-53
title: "Stretch in place: the poster's fourth layout and the app bar's masthead"
status: open
priority: P1
size: S
stage: stage-2-components
epic: feature-components
blocked_by: [B-19, B-25, B-31]
---

# B-53 — Stretch in place: the poster's fourth layout and the app bar's masthead

Found in [B-31](B-31-stretch-text.md): the Stretch page's «Где применяем» draws two components that no item owns.

- **Плакат / Растяжка**, the poster's fourth layout, 390 × 600: three lines on the full width at 136 / 120 / 80 (`ЧИТАЙ`, `ВЕСЬ`, `НОЯБРЬ`), a red bar of 16 between the first two, a body of 240 and a circle of 136 going off the right edge, a slogan button. No diagonal: here the vertical leads.
- **TopAppBar / Шапка**, the title on the full width at 120 (×1.6) under a line of 12 px labels, then a rule of 8. On scroll it rides up and is not scaled; the ordinary collapsed bar stays.
- Not components, so not here: «Раздел / Заголовок» is `PlakatStretch` over a rule, for the application to compose.

- AC: both cells, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the masthead rides up without scaling, read by a test; a mutant in the geometry is caught.
- Anchors: `reference/design-system/plakat-ui Растяжка.dc.html`
