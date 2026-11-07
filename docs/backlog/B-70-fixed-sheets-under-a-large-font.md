---
id: B-70
title: "Fixed sheets under a large font and a long title: TopAppBar and Poster"
status: done
priority: P3
size: M
stage: stage-2-components
epic: feature-components
---

# B-70 — Fixed sheets under a large font and a long title: TopAppBar and Poster

Left over from [B-65](B-65-long-content.md), split off as it said it might be. At font scale 2 a TopAppBar's title stands over its rapport strip, whose place is a fixed distance down a bar that folds, and a Banner poster's second line goes under its button (QA F22). On any scale a Poster's title too long for its sheet runs off it or under the slogan (QA F18): Banner's under the button and cut, Wide's into the circle. The KDoc says to choose a title's words for the layout, which is a limit and not a fix.

These are drawn sheets and not boxes that grow: the bar's fold is a range of pixels, and a poster is a fixed composition with its title on the −17° axis. Whether a display title should scale with the system's font at all, or fit itself to its sheet, is a decision for what these are, and it is held by goldens the design draws.

- **The decision (the owner left it to the loop, 2026-11-07).** A poster's title fits itself to its sheet: it keeps the design's size when it fits, and otherwise is set smaller as a block, until its first corner reaches its room's edge. One rule answers both a long title and a large font, because a large font is just a block that is too big. Its room is the sheet, and its foot is the body's place or the slogan. On Широкий it is the left half, keeping one line and the gap for a body, and on Баннер the sheet less its margin. Растяжка already stretches each line across its width and takes the first three.
- **The bar's title** stays at the design's size in dp whatever the system's font size, both of its titles. It stands at a fixed place on a sheet that folds by a fixed range of pixels, so nothing can give it more room, and at 32 it is already large print.
- **Rejected.** Letting a title scale with the font and clip, which is what F18 and F22 were. A title that wraps, as a poster's lines are the author's words on the −17° axis. An ellipsis, as a slogan cut in the middle says something else. A minimum size, as a limit only moves the overflow to a longer title.
- **The limit that stays.** A very long title gets very small, as nothing bounds it from below except a floor of 1 %, there so that a poster squeezed to nothing does not set text at a negative size. It stays on the sheet, and its words are still the author's to choose.
- **Moved.** No golden: every fixture's title fits at the design's size.
- AC: at font scale 2 the bar's title clears its strip and a banner's second line clears its button, or the item records why not; a long title has a stated behaviour on each layout.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTopAppBar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatPoster.kt`
