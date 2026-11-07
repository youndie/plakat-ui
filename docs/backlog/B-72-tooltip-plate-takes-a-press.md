---
id: B-72
title: "A tooltip's plate takes a press meant for what is under it"
status: open
priority: P3
size: S
stage: stage-2-components
epic: feature-components
---

# B-72 — A tooltip's plate takes a press meant for what is under it

Found in the real window ([B-69](B-69-real-window-pass.md)). Hovered, `SIDEWAYS` shows its plate over the next button, `ON THE AXIS`. A press there, at once or 60 ms after the pointer moved onto the plate, does nothing to that button, and the plate goes. The same press with no plate fills the button.

The plate stands in a `Popup`, and a press inside a popup's bounds goes to the popup's layer and to nothing under it. The pointer leaving the anchor starts the plate's 120 ms of going, so this is a short window for a quick pointer, and the whole of the plate's time for a press with no move before it.

- AC: a press on what lies under a plate reaches it, by a real press; a test fails without the fix.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTooltip.kt`
