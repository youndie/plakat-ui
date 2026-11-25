---
id: B-82
title: "PlakatTable: a selected row, and rows of 36 dp on a pointer surface"
status: wip
priority: P2
size: S
stage: stage-4-product
epic: feature-components
blocked_by: [B-76]
---

# B-82 — PlakatTable: a selected row, and rows of 36 dp on a pointer surface

Issue [#34](https://github.com/youndie/plakat-ui/issues/34), from pyatiletka's board on plakat-core `0.1.0.23`. Two things
keep the board on a table of its own. Its layout is a list beside a card: while a repository's card is open, that
repository's row is marked by a concrete ground, and `row(onClick)` has no way to say so, nor can a cell slot paint the
row. And the board is driven by a mouse: its canvas draws rows of 36 dp, twelve in view, where [B-76](B-76-table.md)'s
48 dp at the least leaves nine. The 48 is right for a row that is a button under a finger; the kit has no size for a
pointer.

- **The decision.** `row(onClick, selected = false, cells)`: a selected row stands on concrete, the words staying ink, and
  says so to a reader (`selected` in its semantics). The change is a cut, in one frame, as the radio button locks and
  hover inverts (B-38, B-39); reduced motion changes nothing. A press on a selected row inks over the concrete as over
  paper. And `PlakatTable(…, density = PlakatDensity.Touch)`, with `PlakatDensity.Pointer` giving rows of 36 dp at the
  least and the cells 4 dp off them where they grow; the header, which is no button, and the keyboard's frame do not
  change. Designed here (D13), from the issue's canvas.
- **Rejected.** A row height in dp: it would take any number, and the kit's rule is that sizes are the kit's; two named
  densities say what each is for, and are the names oldge-ui gives the same choice (`OldgeDensity`). A density in the
  theme, as oldge-ui has it: every component would then claim a pointer size, and the design draws none; the enum is
  public so that the theme can carry it later without a second type. The card's red bar for a selected row (B-21): a bar
  on the row's edge would stand against the table's first column and its words, and the canvas draws a ground.
- **Not covered.** Hover on a row, which B-76 left out for its hairs; a selected row that is not the open one (a
  multi-select table).

- AC: a selected row draws concrete under its cells on both papers and its semantics say selected; it changes in one
  frame; a press over it inks as over paper; a pointer table's text rows are 36 dp and twelve stand in 12 × 36 + the
  header; a cell taller than the row grows it with 4 dp each side; the keyboard's frame shows on a pointer row; `Table/*`
  goldens do not move with the defaults; goldens for a pointer board with a selected row recorded and looked at; the
  sample's kit shows both; #34 is closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTable.kt`, `sample/src/commonMain/kotlin/io/github/youndie/plakat/sample/Kit.kt`
