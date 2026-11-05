---
id: B-54
title: "Window insets: the kit's edges under a status bar and a gesture bar"
status: open
priority: P2
size: M
stage: stage-2-components
epic: feature-components
---

# B-54 — Window insets: the kit's edges under a status bar and a gesture bar

Found in [B-06](B-06-frame-cost-measurement.md), on a Pixel 6a, Android 17: the sample's first screen stands under the status bar (`docs/research/measurements-2026-11-05-device/raw/kit-first-screen.png`). From Android 15 an app is edge to edge. Nothing in `plakat-core` reads window insets, and [B-46](B-46-bottom-sheet.md) and [B-47](B-47-navigation-drawer.md) left the safe area out for that reason.

- **What to decide, then build.** Which components own an edge of the screen and pad themselves, as the top app bar, navigation bar, bottom sheet, drawer and snackbar do, and which leave it to the screen, with the sample's host doing its part. The design draws none of this, so it is designed here under research D13: a top app bar's paper or a navigation bar's ink running under the system's bar, with the content inside it.
- AC: on the device the kit's screens have nothing under a system bar; an inset of zero leaves every golden as it is; a test reads a component's padding under a given inset; a mutant is caught.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatBottomSheet.kt`, `bench-android/src/main/kotlin/io/github/youndie/plakat/bench/android/BenchActivity.kt`
