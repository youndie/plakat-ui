---
id: B-66
title: "Values a consumer can compute: NaN, zero, out of range"
status: open
priority: P2
size: S
stage: stage-2-components
epic: feature-components
---

# B-66 — Values a consumer can compute: NaN, zero, out of range

Progress and Slider throw on NaN, and `done / total` with a total of 0 is ordinary code (QA F20); a Slider with `valueRange = 1f..0f` throws too. A Counter ticking through a number past `Int.MAX_VALUE` throws `NumberFormatException`, 2 000 000 000 → 1 999 999 999 being enough (F21). `selectedIndex = -1` paints 2080 red pixels outside Tabs' box, and an empty list paints a lone bar or square (F19). A Counter with its default `perPictogram = 1` and 20 000 makes 20 000 composables and pushes its caption out (F24). `PlakatRapportFill(cell = 0.dp)` and `plakatHalftone(pitch = 0.dp)` freeze the main thread for ever (F37). `PlakatGeneratedImage` with a negative frame throws with no contract saying so (F38). A Window's `page` out of range shows empty space (F39).

- **The decision to take here.** A value that can be computed at run time is held to its range, as `PlakatStretch` holds its stretch; only an argument that can only be a programmer's error throws, and its KDoc says so. A hang is never the answer.

- AC: each value above draws something sensible or throws a documented `IllegalArgumentException`; none hangs; each has a test.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatProgress.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSlider.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTick.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTabs.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCounter.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/agit/PlakatAgit.kt`
