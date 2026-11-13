---
id: B-80
title: "The dot screen under the kit's overlays, public for a consumer's own"
status: open
priority: P2
size: S
stage: stage-4-product
epic: feature-components
---

# B-80 — The dot screen under the kit's overlays, public for a consumer's own

Issue [#24](https://github.com/youndie/plakat-ui/issues/24), from pyatiletka. The kit closes the background under
`PlakatDialog`, `PlakatBottomSheet` and `PlakatNavigationDrawer` with a screen of ink dots (`PlakatDotScreen`, internal:
pitch 4, dots of 1.6 with an edge to 1.8, the page's `scrim: #0E0D0B` in the dark theme), wiped in on the overlay's clock.
An overlay the kit does not draw gets a scrim of the consumer's invention; the designer's note says «Затемнения в токенах
нет».

- **The decision.** A public `PlakatScrim(visible, onClick, modifier)` in a `BoxScope`: the same dot screen, closing the
  background by the same diagonal wipe on the scene's 320 ms, a fade of 100 under reduced motion, a pointer target only
  and no stop for the keyboard (B-63), with a semantics Dismiss when [onClick] is given. The kit's three overlays draw
  through it, so there is one screen. Its values stay named constants where they are: no token is invented for what the
  design's tokens do not hold, and the dark screen's colour keeps its comment.
- **Rejected.** A `Modifier.plakatScrim()`: a scrim takes clicks and has its own clock, which a modifier on someone
  else's box would hide. A token for the scrim: the vendored tokens are the design's, and D13 numbers live in the code.
- **Not covered.** Focus trapping in the consumer's overlay: the consumer's, with `returnFocusTo` as the kit's overlays.

- AC: a consumer's box over `PlakatScrim` looks as the kit's dialog's screen does at every frame of its open, on both
  papers (the dialog's goldens do not move after it draws through the public scrim); a click on it calls `onClick`; Tab
  never lands on it; the sample shows a consumer's overlay over it; the ABI dump changes by the one function; #24 is
  closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatDialog.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatBottomSheet.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatNavigationDrawer.kt`
