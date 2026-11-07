---
id: B-66
title: "Values a consumer can compute: NaN, zero, out of range"
status: done
priority: P2
size: S
stage: stage-2-components
epic: feature-components
---

# B-66 — Values a consumer can compute: NaN, zero, out of range

Progress and Slider throw on NaN, and `done / total` with a total of 0 is ordinary code (QA F20); a Slider with `valueRange = 1f..0f` throws too. A Counter ticking through a number past `Int.MAX_VALUE` throws `NumberFormatException`, 2 000 000 000 → 1 999 999 999 being enough (F21). `selectedIndex = -1` paints 2080 red pixels outside Tabs' box, and an empty list paints a lone bar or square (F19). A Counter with its default `perPictogram = 1` and 20 000 makes 20 000 composables and pushes its caption out (F24). `PlakatRapportFill(cell = 0.dp)` and `plakatHalftone(pitch = 0.dp)` freeze the main thread for ever (F37). `PlakatGeneratedImage` with a negative frame throws with no contract saying so (F38). A Window's `page` out of range shows empty space (F39).

- **The decision.** A value that can be computed at run time is held to its range, as `PlakatStretch` holds its stretch; only an argument that can only be a programmer's error throws, and its KDoc says so. A hang is never the answer.

- AC: each value above draws something sensible or throws a documented `IllegalArgumentException`; none hangs; each has a test.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatProgress.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSlider.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTick.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTabs.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCounter.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/agit/PlakatAgit.kt`

## Findings

What each is held to. A NaN progress is nothing done yet, and a NaN slider stands at the start. A range with nothing in it, such as `0f..total` with a negative total, is its start alone, as a slider that throws while a total is being worked out helps no one. Negative `steps` still throw, as nothing computes those, and the KDoc says so; so do a projection's line count, which the design's rule P1 limits and its KDoc states.

An index outside a row means nothing chosen, -1 being the natural way to say so on a screen outside the navigation: no mark, for Tabs, NavigationBar, the Drawer and Segmented, which share `rememberPlakatChoiceMark` for it. The Drawer and Segmented were not in the QA's finding; they painted the same way. From nothing chosen the mark comes to its place with no run and no event, as there is nothing for it to run from. A Window or TabContent outside its pages shows the nearest, rather than an empty box.

A Counter's tick now passes over the largest Int: digit by digit, 2 000 000 000 to 1 999 999 999 goes through 2 399 999 999, and the number under way is capped there, which only the pictograms' remainder reads.

Designed here (D13): a tile draws at most 100 pictograms, eight rows of the widest kind, and what is over goes into the remainder's word: 20 000 with its default of one apiece is 100 pictograms and `+19900`. The word is held to what it says, though the design's remainder is only ever under one pictogram's worth. A Rapport's `cell` is at least 1 dp and a Halftone's `pitch` at least a pixel, each stated in its KDoc. A cell as fine as that is slow over a large box, but it ends.

`./gradlew check`: 876 tests, 0 failed, before the Drawer was added.

Mutants, each killed by the aimed test: NaN let through to a progress and a slider; an empty range let through; the tick's overflow; the mark drawn regardless on Tabs, NavigationBar and Segmented; a run and an event from nothing chosen; no event at all; every pictogram; a Window and TabContent unheld; a frame inside out; a Rapport and a Halftone of nothing. The last test of each hang fails after 30 s, as a hang cannot be interrupted, and the frozen thread then keeps its test JVM from exiting, so that regression would show as a red test followed by a job's timeout.
