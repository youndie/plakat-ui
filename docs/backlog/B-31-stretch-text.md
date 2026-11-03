---
id: B-31
title: "Растяжка: one Display line filling its box from cap top to baseline"
status: open
priority: P1
size: S
stage: stage-2-components
epic: feature-components
blocked_by: [B-02, B-05]
---

# B-31 — Растяжка: one Display line filling its box from cap top to baseline

The Stretch page fills a box with one line of Rubik Mono One: width by fitting, height by stretching the glyphs, cap top to baseline. It is static typography; the motion rule "text does not deform" still holds.

- **The decision and its reason.** Measure the line once with a `TextMeasurer`, take the cap height from the font metric pinned in [B-02](B-02-fonts.md) (700/1000), scale the text block by `graphicsLayer` to the box. Used by the Счётчик and the posters.
- Rejected: per-glyph layout.

- AC: the Stretch page's cells match within the floor, in Latin and Cyrillic; a mutant in the cap-height constant is caught.
- Anchors: `reference/design-system/plakat-ui Растяжка.dc.html`, `reference/design-system/plakat-warp.js`
