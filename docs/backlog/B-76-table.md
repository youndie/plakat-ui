---
id: B-76
title: "PlakatTable: rows of fixed columns under a header"
status: wip
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
