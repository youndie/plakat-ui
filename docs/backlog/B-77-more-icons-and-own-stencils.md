---
id: B-77
title: "Icons: More and Opens elsewhere, and stencils a consumer can make"
status: done
priority: P2
size: S
stage: stage-4-product
epic: feature-components
---

# B-77 — Icons: More and Opens elsewhere, and stencils a consumer can make

Issue [#21](https://github.com/youndie/plakat-ui/issues/21) and its comment, from pyatiletka. A header with a
`PlakatMenu` needs a "more" (⋮) stencil, and `Menu`'s three bars mean navigation. A row that opens GitHub needs "opens
elsewhere" (↗). And a consumer cannot add a stencil: `PlakatStencil`'s constructor is `internal`, so pyatiletka draws ↗ on
a `Canvas` and loses `PlakatIcon`'s sizing, tint and pressed state.

- **The decision.** `PlakatStencil` gets a public constructor, its KDoc stating the contract it already keeps: a path
  on the 24-unit grid, filled nonzero, holes counter-clockwise. The two stencils join `PlakatIcons` as the kit's own,
  designed here (D13): `More`, three 4 dp squares on the 24 grid as the issue's canvas draws them, and `OpensElsewhere`,
  the canvas's ↗. The canvas draws ↗ as a 3-wide stroke (`M13 4H20V11M20 4L10 14M17 15V20H4V7H9`, butt caps, miter
  joins), and a stencil is a fill, so it is outlined to a fill once, by the script, and checked against the stroke.
  `scripts/icons.mjs` reads a second file of the kit's own stencils beside the vendored `plakat-icons.json`, so the
  generated object stays generated and the design's file is not edited.
- **Rejected.** Writing the two into `reference/design-system/plakat-icons.json`: the vendored design is never edited.
  A `PlakatStencil.of(name, path)` factory beside an internal constructor: one more name for the same thing.
- **Not covered.** Stroke stencils in general: the set is fills.

- AC: `PlakatIcon(PlakatIcons.More)` and `PlakatIcon(PlakatIcons.OpensElsewhere)` draw at 48, 24 and 16 on both papers and
  take a button's on-colour; a consumer's `PlakatStencil("app/icon/x", "…")` draws in a `PlakatIconButton`; the outlined ↗
  is within a pixel of the stroke it came from; the icons page of the sample shows both, marked as the kit's own; the ABI
  dump changes by the constructor and the two values; #21 is closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/icons/PlakatIcon.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/icons/PlakatIcons.kt`, `scripts/icons.mjs`

## Findings (2026-11-13)

- **Built.** `PlakatStencil`'s constructor is public, its KDoc stating the contract: a fill on the 24-unit grid, nonzero,
  holes counter-clockwise, of moves, lines and unrotated arcs, anything else refused when first drawn.
  `PlakatIcons.More` and `PlakatIcons.OpensElsewhere` follow the design's sixteen; `PlakatIcons.All` stays the design's
  sixteen, so no list was added and the ABI changes by the constructor and the two values only.
- **Where the kit's own live.** `scripts/kit-icons.json`, beside the script that reads it; the vendored
  `plakat-icons.json` is untouched. Each entry is a path, or a stroke the script outlines. Its fixtures are generated
  with the design's (`Icon/More_*`, `Icon/OpensElsewhere_*` at 48, 24, 16, ink on concrete), with no reference, as the
  design draws neither.
- **Every number designed here (D13):**
  | Stencil | Path | Source |
  |---|---|---|
  | More | `M10 3H14V7H10Z`, `M10 10H14V14H10Z`, `M10 17H14V21H10Z` | pyatiletka's board canvas (`MoreGlyph`: squares of 4 at x 10, y 3, 10, 17) |
  | OpensElsewhere | the stroke `M13 4H20V11M20 4L10 14M17 15V20H4V7H9`, 3 wide, butt caps, miter joins, outlined | the issue's comment, from the repository card's canvas |
- **The outline.** `scripts/icons.mjs` offsets each polyline by half the width on both sides, butt caps at the ends,
  miter joins between (the arrow's corners are 45° and 90°, under any miter limit), and turns every polygon the same way
  so the nonzero rule fills where the head meets the shaft. The result agrees with the outline worked by hand. At 48 dp
  every filled pixel is within a pixel of the stroke drawn by Skia with the same caps and joins, and the other way round,
  and the two cover within 5 % of each other (`PlakatKitIconsTest`).
- **The sample's kit** has a group «Icons: the kit's own» with both in icon buttons and More on a FAB.
- **Mutants**, each through the generator: the miter not extended and the stroke half a unit thinner (the outline
  test), a square of More moved (`more is three squares…`). Dropping the line that turns every polygon one way changes
  nothing for this arrow — its three polygons already come out turned alike — so that line guards a later stroke, not
  this one.
