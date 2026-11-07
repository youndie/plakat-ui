---
id: B-63
title: "Overlays and the screen change: where the keyboard and clicks go"
status: done
priority: P2
size: M
stage: stage-2-components
epic: feature-components
---

# B-63 — Overlays and the screen change: where the keyboard and clicks go

In Dialog, Sheet and Drawer the first Tab lands on the invisible screen behind them, and Enter there dismisses (QA F06). On closing, focus does not return to whatever opened them, though Menu's does (F07). Dialog has no semantics Dismiss, though Sheet and Drawer have one (F31). During a ScreenWipe the leaving screen answers clicks, so a double tap can navigate twice (F23).

- **The decision.** The screen is a pointer target only, not a focus stop; focus returns to the opener as Menu's does; the leaving screen takes no input during the change. [B-36](B-36-accessibility.md) covers only the focus trap.

- AC: the first Tab in each overlay lands on a control the eye can see; focus is back on the opener after close; a reader can dismiss a dialog; a click on the leaving screen does nothing.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatDialog.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatBottomSheet.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatNavigationDrawer.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatScreenWipe.kt`

## Findings

Compose has no public way to read where focus stood before an overlay took it. `FocusRequester.saveFocusedChild` only sees inside the group it is attached to, an overlay is a sibling of what opened it, and `Menu` gets the behaviour for free because a `Popup` is a window of its own. Putting the three overlays in popups would change where they are placed, and tracking focus through the theme would need a layout in `PlakatTheme` or a requester on every control. So this is an addition to the public API: `returnFocusTo: FocusRequester? = null` on `PlakatDialog`, `PlakatBottomSheet` and `PlakatNavigationDrawer`, and the sample shows it. With none named, focus is left to the window, as before, so the item's AC holds only for a consumer that names the opener, and the KDoc says so. Focus returns as the close begins, not when the overlay has gone.

The screen stays a target for the pointer, and `focusProperties { canFocus = false }` takes it out of the keyboard's way. During a screen change a blocker over both screens takes the pointer, for the 320 ms or 100 ms it runs, so the new screen is out of reach too, which is shorter than a person's reaction. The keyboard is not blocked: Enter on a control of the leaving screen still fires it during those 320 ms.

`./gradlew check`: 886 tests, 0 failed. The ABI dump changes by the one parameter on each of the three.

Mutants, each killed by the aimed test: the screen a stop again fails `the first Tab lands on a control the eye can see` and `Enter on the first stop does not dismiss`; no return fails `on closing focus goes back to the opener it names`; a return with nothing opened fails `opening nothing takes nothing to the opener`; a dialog with no Dismiss fails `a reader can dismiss each of them`; no blocker fails `during a change neither screen takes a click and after it the new one does`. A guard around `requestFocus` for a requester with no node survived: it does nothing there, so the guard was removed.
