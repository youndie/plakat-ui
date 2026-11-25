---
id: B-83
title: "PlakatDialog: a sheet 480 dp at its widest, centred in a wide window"
status: wip
priority: P2
size: S
stage: stage-4-product
epic: feature-components
blocked_by: [B-28]
---

# B-83 — PlakatDialog: a sheet 480 dp at its widest, centred in a wide window

Issue [#35](https://github.com/youndie/plakat-ui/issues/35), from pyatiletka's board on plakat-core `0.1.0.23`. The sheet
is the MVP page's `left: 24px; right: 24px` (`components/PlakatDialog.kt`, `SheetInset`), the phone's dialog: on the
board's 1280 × 800 window the appearance dialog («ВИД», two groups of radio rows and «ГОТОВО», B-79) becomes a sheet of
1232 dp with its rows at the far left. `modifier` reaches the screen-filling box, so a consumer cannot narrow the sheet
without narrowing the dot screen with it; the board keeps a sheet of its own, 480 wide.

- **The decision.** The sheet stands 24 dp off each side as before, and is 480 dp at its widest, centred; the screen still
  fills the window. One rule for every width, with no breakpoint: the sheet reaches 480 in a window of 528, and a phone's
  dialog (390 → 342) does not move. Designed here (D13), from the issue's canvas, which draws the board's dialog 480 wide.
- **Rejected.** A `maxWidth` parameter: every dialog of the kit is the same announcement, and a sheet as wide as a
  consumer likes is the 1232 dp sheet again by another road. The breakpoint at 600 dp that research D2 gives the poster: a
  poster changes its layout there, a dialog only stops growing, and a stop at 600 would make the sheet jump from 552 to
  480 on a dragged window edge.
- **Not covered.** `PlakatBottomSheet` and `PlakatMenu` in a wide window, which nobody has asked for yet.

- AC: in a 1280 × 800 window the sheet is 480 wide and centred, and a click on the screen beside it dismisses; at 390 the
  sheet is 342 at 24 a side and `Dialog/*` goldens do not move; long text wraps inside the 480 and its actions stay
  reachable; given no width it does not throw (the B-58 sweep); goldens of the wide window recorded and looked at; #35 is
  closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatDialog.kt`
