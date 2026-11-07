---
id: B-65
title: "Long content: what a component does with text that does not fit"
status: done
priority: P2
size: L
stage: stage-2-components
epic: feature-components
---

# B-65 — Long content: what a component does with text that does not fit

The QA pass (2026-11-06) found the same question answered differently across the kit, and sometimes not at all. ListItem's `number = "100"` prints `10`, which reads as another number (F11). Segmented wider than its box runs out of the frame with its targets (F12). A long Snackbar action leaves the message 0 wide, one letter a line (F13), with no vertical padding when text grows (F28, also ListItem). A Level or Tilted tooltip vanishes on a centred phone anchor where neither side has room (F14). Two long Dialog actions overflow the sheet (F15). Buttons cut by whole words with no sign anything is missing, and Danger wraps out of its plate (F16). Long tab and navigation labels overlap (F17). Poster's long content collides with the slogan, and Stretch shows three lines of six without saying so (F18). At font scale 2 fixed heights clip (F22). TopAppBar cuts a long title mid-letter and drops its subtitle (F29). A chip's text is cut flush with its frame (F34).

- **The decision, per component.** Which of grow, wrap, ellipsis or a documented limit applies, each recorded below with its reason; a limit that stays says so in the KDoc.

- AC: each case above has a decision in this item and a test or a golden holding it; no text overlaps a neighbour or leaves its plate silently.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatListItem.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSegmentedButton.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSnackbar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTooltip.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatButton.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatPoster.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTopAppBar.kt`

## Findings

What each does now. All of it is designed here (D13), and all of it leaves what fits as it was, which is why no golden moved. The rule behind the choices: a number or a target grows or shares, because losing part of it says something false; a word ends in an ellipsis, because a cut at a space, which Compose does under `maxLines = 1`, looks finished.

| Case | Behaviour | Why |
|---|---|---|
| ListItem's number (F11) | The column grows past 24 | `100` as `10` is another number |
| Segmented wider than its box (F12) | Each segment gives way by its share, the words end in an ellipsis | Its targets stay in the box |
| Snackbar's long action (F13) | At most half the row, ellipsis; its 96 gives way only if half is less | The message always has the other half |
| Vertical padding (F28) | 8 dp around a Snackbar's message and a ListItem's words | Under the row's minimum it changes nothing |
| Tooltip on a centred anchor (F14) | The side with more room, the plate held in the window over its leader's end | Showing somewhere beats not showing; the leader's end can then sit under the plate |
| Dialog's actions (F15) | A `FlowRow`: too long for one line, each on its own, and `FlowRowScope` is a `RowScope`, so the API holds | A word on one line is the action's look |
| Buttons (F16) | One line and an ellipsis in all four kinds | A slogan is one line |
| EmptyState's slogan (F16) | 200 at least, growing to the column | Its 200 was the page's words' |
| Tabs, NavigationBar (F17) | A word takes its slot less 8 dp, ellipsis | Equal slots are the design's |
| TopAppBar, Masthead (F29) | The title gives way and the subtitle keeps its place; two labels take half each | |
| Chip (F34) | Ellipsis with room at its end, set by a layout and not a padding | A padding would widen a chip by the pixel its width rounds down, which turned six goldens red |
| Poster's body (F18) | Stops above the slogan, with an ellipsis, by what fits its layout; the KDoc states the limit | A sheet is a fixed composition |
| Font scale 2 (F22) | NavigationBar's strip and a Badge grow past their 64 and 20 only when their text needs it | |

Split off to [B-70](B-70-fixed-sheets-under-a-large-font.md): a TopAppBar's title and a Banner's second line at font scale 2, and a Poster's title too long for its sheet, for which the KDoc states the limit and nothing else changes. Those are drawn sheets, and how they answer is a decision of its own.

`isLineEllipsized` always answers false on Skiko, and under `softWrap = false` with `maxLines = 1` a clipped text is also cut at a space, so neither that nor a comparison of two renders told an ellipsis from a cut: both tests were tried, and the mutants survived them. The tests read the overflow set on the laid-out text, and the dots were looked at on both papers. So a test here would miss a platform not drawing them.

Rendered and looked at on both papers: every case in the table but the tooltip's, which is held by numbers. `./gradlew check`: 907 tests, 0 failed, before the tests were rewritten as above.

Mutants, each killed by the aimed test: a fixed number column, no share among segments, an action taking the whole row, a plate left off the screen, a Row for the dialog's actions, a clip on each of the buttons and the Chip, a fixed slogan, a tab word unbounded, a title and a Masthead's label not giving way, a body running on, and a fixed height on the NavigationBar and the Badge.
