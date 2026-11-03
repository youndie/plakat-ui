---
id: B-10
title: "Print primitives: cut corner, registration, hatch, wipes, hard-stop and stepped curves, non-print mask"
status: done
priority: P0
size: M
stage: stage-1-materials
blocked_by: [B-08, B-05]
---

# B-10 — Print primitives: cut corner, registration, hatch, wipes, hard-stop and stepped curves, non-print mask

Every component is built from the same few printed devices (brief «Как это ляжет в Compose», the Tokens page «ПЕЧАТЬ»).

- **The decision and its reason.** One shared set: the cut-corner shape (`polygon(0 14px, 46px 0, 100% 0, 100% 100%, 0 100%)`), the registration layer (red drawn under at the offset of D14 on Display, Headline and plates, never on body text and icons), the danger hatch (45°, 3 on / 3 off), the rectangular and diagonal wipes as clips, the paper and non-print layers, `HardStop = CubicBezierEasing(0.2, 0.8, 0.9, 0.9)` and `stepped(n)` as closed forms (D10).
- Rejected: per-component copies of these.

- AC: each primitive has a fixture matched to a page cell within the floor; the curves are read at fixed times by a test; a mutant in the cut's points is caught.
- Anchors: `reference/design-system/plakat-ui Токены.dc.html`, `reference/design-system/plakat-ui Ядро.dc.html`

## Findings

- 2026-11-03. In `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/print/`:
  `PlakatCutShape` (the pages' `polygon(0 14px, 46px 0, …)`, 46 × 14 from the tokens, any size);
  `plakatHatchBrush` / `Modifier.plakatHatch` (the 45° `repeating-linear-gradient`, placed as CSS
  places a gradient on its box, hard stops); `PlakatWipe` with `plakatWipePath` / `Modifier.plakatWipe`
  (curtains from the left, top, bottom, and the slanted one, `polygon(0 0, x 0, x − slant 100%, 0 100%)`,
  read at drawing time); `PlakatRegisteredText` (the red copy 1 dp right and down under the ink, off
  through the theme); `Modifier.plakatPrintLayer` (a tile edge to edge over the content, clipped to
  the box, at an alpha and a blend). The hard-stop and stepped curves were B-08's.
- Parity, new cells in `scripts/design-references.mjs` (it learnt crops, markup captions, `position:
  fixed` overlays and a viewport per page — the Tokens page is narrowed to 1160 px so its columns are
  whole): `Cut_Plate` 0.00 % in both themes, `Hatch_Danger` 0.00 %, `Registration_List` 1.71 % (a 40 px
  word: text, under the text floor).
- Tests: the cut's corners at densities 1, 2 and 3; every wipe empty at 0 and whole at 1; the slanted
  wipe's edge and foot at half, monotone with no jump over every millisecond of 200; registration's
  red edge present with the switch on and absent off; the print layer's tiling from the corner and
  its clip. Mutants killed: the top edge moved 2 px, the hatch period 7, registration without its
  offset, the slant's sign, the layer unclipped (the first version of that test let it live; the
  tile's dot moved to its far corner so an overflow shows).
- The tiles themselves (paper, non-print) arrive with B-07.
