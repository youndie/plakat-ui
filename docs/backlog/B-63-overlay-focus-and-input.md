---
id: B-63
title: "Overlays and the screen change: where the keyboard and clicks go"
status: open
priority: P2
size: M
stage: stage-2-components
epic: feature-components
---

# B-63 — Overlays and the screen change: where the keyboard and clicks go

In Dialog, Sheet and Drawer the first Tab lands on the invisible screen behind them, and Enter there dismisses (QA F06). On closing, focus does not return to whatever opened them, though Menu's does (F07). Dialog has no semantics Dismiss, though Sheet and Drawer have one (F31). During a ScreenWipe the leaving screen answers clicks, so a double tap can navigate twice (F23).

- **The decision to take here.** The screen is a pointer target only, not a focus stop; focus returns to the opener as Menu's does; the leaving screen takes no input during the change. [B-36](B-36-accessibility.md) covers only the focus trap.

- AC: the first Tab in each overlay lands on a control the eye can see; focus is back on the opener after close; a reader can dismiss a dialog; a click on the leaving screen does nothing.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatDialog.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatBottomSheet.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatNavigationDrawer.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatScreenWipe.kt`
