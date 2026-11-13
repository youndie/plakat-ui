---
id: B-80
title: "The dot screen under the kit's overlays, public for a consumer's own"
status: done
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

## Findings (2026-11-13)

- **Built.** `BoxScope.PlakatScrim(visible, onClick: (() -> Unit)? = null, modifier)` in `components/PlakatScrim.kt`, over
  the internal `PlakatDotScreen` the dialog, the bottom sheet and the drawer already shared, so there is one screen. It
  keeps its own clock — an `Animatable` to 1 over `PlakatDurations.SCENE`, linear, or the 100 ms fade under reduced
  motion — and leaves the composition once closed, as the dialog does. `PlakatDotScreen` gains a `modifier` and an
  optional reader's dismiss; the kit's overlays pass neither, their sheets carrying the dismiss, so their goldens and
  tests did not move.
- **No number is new.** The pitch, the dots of 1.6 and 1.8, the diagonal's 120 dp lean and the dark theme's
  `#0E0D0B` stay the dialog's named constants with their comments; no token was invented (the design's tokens hold no
  scrim).
- **Held to the dialog.** In a 390 × 700 window, the strip under where the dialog's sheet ever reaches (y 560–700) is
  the same, pixel for pixel, for `PlakatScrim(true)` and for an opening `PlakatDialog`, at 0, 16, 107, 160, 213, 304, 320
  and 400 ms, on both papers (`PlakatScrimTest`).
- **Keyboard and reader.** A click calls `onClick`; with it, the screen carries a Dismiss action. Tab never lands on
  it: with one button over it, Tab after Tab comes back to the button. The screen's semantics are cleared, so its own
  focus cannot be asked, which is why the test asks the button.
- **The sample's kit** has «Scrim, under an overlay of the app's own»: a card of the app's over the kit's scrim.
- **Mutants**, each caught: a scene of 200 ms and the wipe replaced by the fade (the frame test), no reader's dismiss
  (the click test), the screen a keyboard stop (the Tab test), the scrim never leaving (the close test).
- **ABI**: `PlakatScrim` added.
