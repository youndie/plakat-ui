---
id: B-54
title: "Window insets: the kit's edges under a status bar and a gesture bar"
status: done
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

## Findings

**Designed here (D13), for the owner's review.** The design draws no system's bar, and the owner has not looked at this.

| What | Choice |
|---|---|
| Who owns an edge | `PlakatTopAppBar`, `PlakatMasthead` (the top), `PlakatNavigationBar` (the foot), `PlakatBottomSheet` (the foot), `PlakatNavigationDrawer` (top and foot) |
| Who does not | FAB, snackbar, dialog, menu, tooltip, pull-to-refresh: their screen places them, and pads them if it must. The dialog's 96 from the top already clears a status bar |
| How | a `windowInsets` parameter, the system's bars by default, zero on a desktop, and `WindowInsets(0, 0, 0, 0)` for a parent that has handled them |
| Ground | runs under the bar in the component's own colour: paper at the top, ink or concrete in the navigation bar, the sheet's and drawer's ground to their edge. So the status bar has paper behind it, and its colour on that paper is the application's |
| Height | grows by the inset, `195 + 24` |
| Sheet | its 96 from the top is unchanged, which clears a status bar |
| A target | stays the strip's: nothing is tappable under the system's bar |
| Horizontal | not handled: a display cutout in landscape or a side gesture is left out, as the drawer takes only the vertical sides |

**No golden moved**, on either module, as a desktop has no insets. The sample stands inside the safe area on a phone, at its root, so its demos' bars have nothing left to run under: the bars at a real edge are shown by `bench-android`'s `edges` mode.

**What holds what.** `PlakatWindowInsetsTest` hands each component an inset and reads its height, ground and content, as a desktop test's window has no system's bar. So **that the defaults ask for the system's own is held by no test**, only by a look: `docs/research/measurements-2026-11-05-device/raw/insets/` on a Pixel 6a, Android 17, the bars, masthead, sheet, drawer and kit, with nothing under a system's bar in any. iOS has not been seen.

**Found on the way, a new item:** [B-55](B-55-masthead-throws-by-width.md). The masthead's shot crashed the app: `PlakatStretch: "ЛИСТ" in Size(996.0, 315.0) stretches by 1.4744189, outside 1.5 to 4.0`, the page's own word at this phone's width. The shot in `raw/insets/` uses «ВЫПУСК» so as to read the insets.

**Tests.** `PlakatWindowInsetsTest`, 7. Mutants, the aimed test named among the failures: each of the five without its padding, the bar and masthead and navigation bar without their ground, and the sheet and drawer not passing the parameter on.

