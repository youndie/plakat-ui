---
id: B-70
title: "Fixed sheets under a large font and a long title: TopAppBar and Poster"
status: open
priority: P3
size: M
stage: stage-2-components
epic: feature-components
---

# B-70 — Fixed sheets under a large font and a long title: TopAppBar and Poster

Left over from [B-65](B-65-long-content.md), split off as it said it might be. At font scale 2 a TopAppBar's title stands over its rapport strip, whose place is a fixed distance down a bar that folds, and a Banner poster's second line goes under its button (QA F22). On any scale a Poster's title too long for its sheet runs off it or under the slogan (QA F18): Banner's under the button and cut, Wide's into the circle. The KDoc says to choose a title's words for the layout, which is a limit and not a fix.

These are drawn sheets and not boxes that grow: the bar's fold is a range of pixels, and a poster is a fixed composition with its title on the −17° axis. Whether a display title should scale with the system's font at all, or fit itself to its sheet, is a decision for what these are, and it is held by goldens the design draws.

- AC: at font scale 2 the bar's title clears its strip and a banner's second line clears its button, or the item records why not; a long title has a stated behaviour on each layout.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTopAppBar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatPoster.kt`
