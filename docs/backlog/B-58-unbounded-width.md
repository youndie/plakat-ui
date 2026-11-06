---
id: B-58
title: "Nothing throws under an unbounded width"
status: done
priority: P0
size: M
stage: stage-2-components
epic: feature-components
---

# B-58 — Nothing throws under an unbounded width

Counter, EmptyState, NavigationBar, Tabs and Window throw when the width they get is unbounded: in `Row(Modifier.horizontalScroll(...))`, under `Modifier.width(IntrinsicSize.Max)`, under `wrapContentWidth(unbounded = true)`. The exceptions are `Can't represent a width of 2147483647`, `... of 1073741824` and `Size(2147483647 x 12) is out of range`, and 21 cells of a sweep of every component through 11 constraint cells land in these families (QA F01). A horizontally scrolling row of stat tiles or of tabs is an ordinary layout, and [B-55](B-55-masthead-throws-by-width.md) already said that nothing in the kit throws by width.

- **The decision.** Every layout that reads `constraints.maxWidth` as its width says what it is when that is infinite: a text takes its own widest line, and a row of equal cells takes the sum of what its cells want. The content's size is the honest answer, and the same one Compose's own `Text` and `Row` give.
- **Rejected.** Falling back to a fixed width such as the screen's or a token: it invents a number the design does not state, and a scrolling row would look filled and be wrong. `coerceAtMost` on a large constant: it hides the throw and hands the consumer a row 16 000 dp wide.
- **Not covered.** What happens to text too long for a *bounded* box: [B-65](B-65-long-content.md).

- AC: the sweep of every public component through its constraint cells, unbounded width and `IntrinsicSize` included, throws nothing, and stays in the repository's tests; under an unbounded width each of the five is as wide as its content; no golden moves.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCssText.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCounter.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTabs.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatNavigationBar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatWindow.kt`

## Findings

Under no limit the answer is CSS's `max-content`, which the design's own pages would give: `CssParagraph` is as wide as its text, the pictogram row as its longest line, the window's panel as its words (measured directly rather than through intrinsics, since a consumer's picture may hold a `BoxWithConstraints`, which refuses them). A column with `fillMaxWidth` children, the EmptyState's band, would be left 0 wide by Compose, so `maxContentWidth()` gives it the width of its content first.

Tabs and NavigationBar divide what they are given, so given nothing they need a number the design does not state. Designed here (D13): a slot is the widest word with `PlakatGrid.Margin` (16 dp) each side, and never under the 48 dp target. The strip stays equal slots, as the page draws it.

The sweep, 56 subjects through 11 constraint cells, stays as `PlakatConstraintSweepTest`. It had 21 throwing cells before this and has none. A window's long compact body is one long line under no limit, so a window in a horizontal scroll with a long body is wide: that is what `max-content` says, and a consumer who wants less gives it a width.

All five were rendered in a horizontal scroll and looked at, on both papers. The bounded path is unchanged, and no golden moved. `./gradlew check`: 828 tests, 0 failed.

Mutants, each killed by the aimed test: the paragraph's width back to `constraints.maxWidth` fails `nothing throws in any cell`; the EmptyState without `maxContentWidth()` fails `an empty state given no limit keeps its band`; the pictogram row's width back to `constraints.maxWidth` fails `nothing throws in any cell`; a slot without its word fails `tabs given no limit take slots for their widest word` and the navigation bar's; a slot without its 48 dp fails `tabs given no limit still have targets of 48`; a panel without its words fails `a window given no limit is as wide as its words`.
