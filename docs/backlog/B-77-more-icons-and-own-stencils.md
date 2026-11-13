---
id: B-77
title: "Icons: More and Opens elsewhere, and stencils a consumer can make"
status: wip
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
