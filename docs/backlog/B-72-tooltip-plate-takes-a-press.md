---
id: B-72
title: "A tooltip's plate takes a press meant for what is under it"
status: done
priority: P3
size: S
stage: stage-2-components
epic: feature-components
---

# B-72 — A tooltip's plate takes a press meant for what is under it

Found in the real window ([B-69](B-69-real-window-pass.md)). Hovered, `SIDEWAYS` shows its plate over the next button, `ON THE AXIS`. A press there, at once or 60 ms after the pointer moved onto the plate, does nothing to that button, and the plate goes. The same press with no plate fills the button.

The plate stands in a `Popup`, and a press inside a popup's bounds goes to the popup's layer and to nothing under it. The pointer leaving the anchor starts the plate's 120 ms of going, so this is a short window for a quick pointer, and the whole of the plate's time for a press with no move before it.

It is worse with the keyboard's focus on the anchor, where the plate stays as long as the focus does, and every press on what it covers does nothing.

- **The decision (the owner left it to the loop, 2026-11-07).** The plate's popup has no bounds where it is drawn on the window's own canvas: desktop, iOS and the browser. It measures 0 × 0, its position still comes from the placement and it draws its plate past that, so no press lands in its layer and each falls through. On Android a popup is a window of its own, which would draw nothing past its bounds, so it keeps its size there, and the plate takes that press. A tooltip there shows while a finger holds its anchor, or for a keyboard's focus, so little lies in reach.
- **Rejected.** Taking the plate away the moment the pointer leaves: this drops the design's wipe back and does nothing for the keyboard's focus. Forwarding the press from the popup to what lies under it: Compose has no way to do this. Drawing the plate without a popup, as it then clips to whatever its anchor stands in.
- **Checked.** In the real window, by the driver of [B-69](B-69-real-window-pass.md): the plate draws as before, and the press that did nothing now fills `ON THE AXIS`. In the test runtime, with the plate shown by the keyboard's focus, the press reaches what lies under it, and with the bounds back it does not.
- **Not exercised.** iOS and the browser, where this rests on their drawing popups on one canvas as the desktop does; Android's popup as a window comes from its known design, not from a run here.
- AC: a press on what lies under a plate reaches it, by a real press; a test fails without the fix.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTooltip.kt`
