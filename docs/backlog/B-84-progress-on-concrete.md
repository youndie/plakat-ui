---
id: B-84
title: "A linear progress on concrete loses the end of its track"
status: open
priority: P3
size: S
stage: stage-4-product
epic: feature-components
---

# B-84 — A linear progress on concrete loses the end of its track

Found in [B-82](B-82-table-selected-row-and-pointer-density.md). `PlakatLinearProgress` is a red wedge in an 8 dp bar of
concrete (`components/PlakatProgress.kt`, the MVP page's track). On a concrete ground — a table's selected row, a card — the
bar is concrete on concrete: the red part reads and the remainder does not, so 3/4 cannot be told from 3/5 without the
column's edge. The design draws the progress on paper only.

- **The decision** is open. Choices: an ink hair round the bar where its ground is concrete (a ground the theme would have
  to pass down, as `LocalPlakatContentColor` passes the words' ink); the track in `ink-muted` at a low opacity on any
  ground; or leave it, as the consumer's canvas draws it so. Whichever, nothing moves on paper.
- **Not covered.** The circular progress, whose ring is ink.

- AC: on a selected row and on a card the bar's full length is visible on both papers; the `Progress/*` goldens on paper do
  not move.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatProgress.kt`
