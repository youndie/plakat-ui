---
id: B-76
title: "PlakatTable: rows of fixed columns under a header"
status: done
priority: P2
size: M
stage: stage-4-product
epic: feature-components
---

# B-76 — PlakatTable: rows of fixed columns under a header

Issue [#20](https://github.com/youndie/plakat-ui/issues/20), from pyatiletka's board. A board lists twelve rows of five
aligned columns. `PlakatListItem` is one leading, headline, supporting and trailing line and cannot align columns across
rows, so the designer's canvas assembled the board from `ListItem`, `Divider`, `LinearProgress` and `Badge`, and the
consumer lays the grid out by hand.

- **The decision.** `PlakatTable(columns: List<PlakatColumn>, modifier)` with a header row of the columns' labels in
  `label` over a 3 dp ink rule (`plakat/line/rule`), and rows added by a `row { cell { … } }` scope, separated by a hair
  rule in concrete. A column is a weight or a fixed width, with its alignment; one flexible column takes what the fixed
  ones leave. Cells are slots, so a cell holds a word, a `PlakatStatus` ([B-75](B-75-status-mark.md)) or a
  `PlakatLinearProgress`. A row may be clickable, with the kit's press and the keyboard's frame (B-60). Designed here
  (D13), from the issue's canvas: the item lists every number it sets.
- **Rejected.** A lazy table: a board's twelve rows do not need one, and a `LazyColumn` of rows built from the same row
  scope is open to a consumer. Columns measured from their content: rows would then have to be measured twice, and the
  canvas fixes the widths.
- **Not covered.** Sorting, resizing columns and a sticky header.

- AC: twelve rows of five columns stay aligned on both papers; a long word in a fixed column ends in an ellipsis and a
  number does not lose a digit (B-65's rule); given no width it does not throw (B-58's sweep includes it); a clickable row
  shows the keyboard's frame; the sample's kit shows one; goldens recorded and looked at; a mutant on the column widths and
  one on the rules are caught; #20 is closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatListItem.kt`, `sample/src/commonMain/kotlin/io/github/youndie/plakat/sample/Kit.kt`

## Findings (2026-11-13)

- **Built.** `PlakatTable(columns: List<PlakatColumn>, modifier, content: PlakatTableScope.() -> Unit)` in
  `components/PlakatTable.kt`. `PlakatColumn(label, width, weight, align, numeric)`; rows by
  `row(onClick) { cell("words"); cell { … } }`. The scope is a builder, not a composable, as `LazyListScope` is: the table
  knows every text cell before it lays anything out.
- **A number never loses a digit, and the columns stay aligned.** The item rejected measuring columns from their content
  (two passes over every row); a numeric column is the one place that needs it, and its cells are text, so the table
  measures their words once with a `TextMeasurer` and widens the column to the widest, in every row. The flexible columns
  give way, never below zero. A word in any other column ends in an ellipsis on one line (B-65's rule).
- **Given no width**, a flexible column is as wide as its widest text cell; the table is in the B-58 sweep.
- **Every number designed here (D13):**
  | What | Value | Why |
  |---|---|---|
  | header | the labels in `label`, 8 dp above and below | one grid step, as a section label stands on its rule |
  | header rule | 3 dp ink, `plakat/line/rule` | the issue: «3 dp ink rule» |
  | row | 48 dp at the least, 8 dp above and below the cells where they grow | a touch target, as a row may be a button; B-65's padding |
  | between rows | 1 dp concrete hair, none under the last | the issue: «a hair concrete rule between rows» |
  | at each side, between columns | 8 dp, `PlakatGrid.Step` and `Gutter` | a board's five columns in 390 dp |
  | a text cell | `bodyM`, one line | a board's cells are words and numbers |
  | a clickable row | the list item's press (ink ground, paper words) and the keyboard's frame | the kit's one pressable row |
  A hover look is not given: the list item's hair turning to a rule would fight the table's own hairs.
- **Goldens** `Table/Board_Light`, `Table/Board_Dark` (twelve rows of five: number, item, state, progress, days) and
  `Table/TooLong_Light` (12345 widening its column for both rows, «Electrificati…», «postpo…»), looked at. The sample's
  kit has a «Table» group.
- **Mutants**, each caught: a numeric column not widened (two tests), the weights ignored, the header's rule a hair, the
  rows' hair in ink, a hair under the last row (the rules test), the column alignment ignored (the alignment test, once
  its cells were made different widths per row — with equal widths it survived), the focus frame dropped
  (`PlakatKeyboardFocusTest`).
- **ABI**: `PlakatTable`, `PlakatColumn`, `PlakatTableScope`, `PlakatRowScope` added.
