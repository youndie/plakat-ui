---
id: B-31
title: "Растяжка: one Display line filling its box from cap top to baseline"
status: done
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

## Findings (2026-11-05)

- **`PlakatStretch(text, modifier, color)`**, its size from the modifier, plus `plakatStretchFactor(text, width, height)` for a text or box not known in advance. It sets the text in capitals and keeps it readable as written (`semantics.text`). Copying is not done: a drawn line has no selection, for [B-36](B-36-accessibility.md) if it is wanted.
- **The decision changed in one point: the fit is by ink, and ink is a table.** The item said to measure once with a `TextMeasurer`. A layout knows advances, and its `getPathForRange` is the selection's boxes, so nothing in common Compose gives the bounds the page's canvas gives (a left bearing of 2.5 to 12.6 of 100, which is a gap at the box's edge). Rejected: rasterising the line once and scanning it, which is an estimate at curves, depends on the platform's rasteriser and runs on every value of a counter. Instead `scripts/display-ink.mjs` reads the `glyf` boxes into `PlakatDisplayInk.kt`, `--check` in `make check`, and a test reads every row again through AWT's outlines. The layout stays one, at 100 px and density 1, scaled by the drawing's transform; `graphicsLayer` was not needed. The face's cap height is in the same table, 700.
- **The page's numbers hold exactly**: `measureText` in Chrome and the table agree to 0.01 on every line of the page (`the ink is what the page's canvas measures`); the poster's three lines stretch by 1.82, 1.56 and 1.58, within the rule, where the page's caption says «1,6–2,3»: its low end rounded, its top not reached by any line.
- **Parity**: 0.94–3.81 % of the cell, 1.7–5.7 % of ink against the text floor of 63.7 %; Latin (`POSTER`, typed into the page's sandbox) is the 3.81 and the 5.7. Red only on vertical edges, max 80 levels, and a shift of one pixel is two to four times worse: Chrome keeps each glyph's fractional x under the scale, Compose rounds it.
- **The page measures in the fallback face, unless helped.** On mount it asks for its font before the runtime has put its stylesheet in; `document.fonts.load` finds no face, answers at once, and `m()` caches every line in the fallback for good, so the page's lines overflow their boxes (46–58 % against a right fit) wherever the stylesheet is slower than React. The references add the faces before the page's first script (this page only; the other 192 did not change). The vendored page is untouched: a defect of the design, for its owner.
- **Refused at drawing**: outside 1.5–4 it throws, as the page's sketch says (`require`). Its own two bad cells, `ДА` at ×5.2 and `ЛЕНТА` at ×0.8, are held by a test, not by references: they are what must not be drawn.
- **Designed here** (D13), for the owner's review: the `require` itself, since a dynamic text in a fixed box crashes where it could have been ugly, with `plakatStretchFactor` as the way out; blank text and an empty box draw nothing and refuse nothing; a character outside the font counts as a full cell to the caps; a descender (Ц, Щ, Д) hangs out of the box as on the page, unclipped; a surrogate pair is two cells (the font has nothing past the BMP).
- **Split out**: the poster's fourth layout and the app bar's masthead, [B-53](B-53-stretch-in-place.md). «Счётчик / Колонна» is a line in [B-30](B-30-counter.md).
- **Mutants**: through the tests — cap height 720 (the AC's), advance 860, the two limits, no room for a mark, the fit ignoring the left bearing, no x offset, the baseline off, no semantics, a blank not advancing, the density not fixed, an empty box refused. All twelve killed. Setting no capitals survives and is equivalent: this font's small letters are its capitals, box for box, in Latin and Russian. Through the goldens: cap height 710, three cells over their tolerance. A measuring size of 90 changes nothing and is equivalent too: the scale cancels it.
