---
id: B-58
title: "Nothing throws under an unbounded width"
status: open
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
