---
id: B-53
title: "Stretch in place: the poster's fourth layout and the app bar's masthead"
status: done
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

## Findings

**Built.** `PlakatPosterLayout.Stretch`, the poster's fourth layout, and a public `PlakatMasthead` beside `PlakatTopAppBar`, not a mode of it: the bar's row, height and strip share nothing with it but `PlakatTopAppBarState`, so the same scroll connection folds either.

**Parity**, against the floors of B-05 (shapes 0.00 %, text 63.7 % of ink): `Poster - Stretch_Light` 1.43 %, `TopAppBar - Masthead_Light` 1.57 %. Both diffs were looked at: red on glyph edges only, no box edge off; the most of it is the right-aligned end label, whose start falls on a fraction of a pixel. Dark is not drawn by the page and is not fixtured. The masthead's ring, `box-shadow: inset` on the page's cell, is the fixture's, as the page frames its cells.

**Designed here (D13), for the owner's review**, since the page draws the masthead open and says only that it rides up and an ordinary bar stays:

- the collapsed bar comes down from above in the fold's last 56, on its own paper, over what rides up;
- the 3 dp rule grows with the fraction, as the bar's does, so at 60 of 136 one row of it already stands;
- 59 collapsed, and the range of 136 that gives;
- open, the masthead has no navigation or actions: they are in the row, which is not composed until it starts coming down. Above the clip it would still be read twice and its actions would still take the keyboard's focus;
- the poster's body wipes with the title at 150 ms and its slogan at 180, as Круг's do; the page has no motion for this cell;
- the slogan's label at 14 px is the page's; the Stretch layout has no composition, no diagonal and no raskat, so `raskat` does nothing in it and a new `seed` only reprints it.

**Not a stretch of the design.** The Л's flat top stands 2 under the title's box: `PlakatStretch` fits the word's ink, and С's overshoot takes the top row. Chrome draws the same (the diff has no row there).

**Tests.** `PlakatStretchInPlaceTest`, 11 tests. Mutants, each aimed test named among the failures: masthead not riding up, rule at 0, height unfolded, title 128, bar gap 16, heading removed, row's foot 8 off, row composed always; poster line of 128, third line at 344, bar of 24, circle at 302, column of 232, slogan a step up, and the bar, circle, title, body and slogan each on the wrong clock. `.clearAndSetSemantics {}` on the lines survived at first, the test reading a heading and not that the words are read once; it now does.

A title of 112 in place of 120 does not draw a smaller title: `PlakatStretch` refuses the factor ("ЛИСТ" in 358 × 112 stretches by 1.46, outside 1.5 to 4) and throws, so it fails 7 tests rather than the one aimed, and is not counted; 128 is.
