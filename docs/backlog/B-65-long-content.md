---
id: B-65
title: "Long content: what a component does with text that does not fit"
status: open
priority: P2
size: L
stage: stage-2-components
epic: feature-components
---

# B-65 — Long content: what a component does with text that does not fit

The QA pass (2026-11-06) found the same question answered differently across the kit, and sometimes not at all. ListItem's `number = "100"` prints `10`, which reads as another number (F11). Segmented wider than its box runs out of the frame with its targets (F12). A long Snackbar action leaves the message 0 wide, one letter a line (F13), with no vertical padding when text grows (F28, also ListItem). A Level or Tilted tooltip vanishes on a centred phone anchor where neither side has room (F14). Two long Dialog actions overflow the sheet (F15). Buttons cut by whole words with no sign anything is missing, and Danger wraps out of its plate (F16). Long tab and navigation labels overlap (F17). Poster's long content collides with the slogan, and Stretch shows three lines of six without saying so (F18). At font scale 2 fixed heights clip (F22). TopAppBar cuts a long title mid-letter and drops its subtitle (F29). A chip's text is cut flush with its frame (F34).

- **The decision to take here, per component.** Which of grow, wrap, ellipsis or a documented limit applies, each recorded with its reason; a limit that stays says so in the KDoc. Likely to split into several items once the first is done.

- AC: each case above has a decision in this item and a test or a golden holding it; no text overlaps a neighbour or leaves its plate silently.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatListItem.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSegmentedButton.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSnackbar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTooltip.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatButton.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatPoster.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTopAppBar.kt`
