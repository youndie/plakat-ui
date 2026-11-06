---
id: B-64
title: "TextField: label and error on the editable node, the jolt, the selection colour"
status: open
priority: P2
size: S
stage: stage-2-components
epic: feature-components
---

# B-64 — TextField: label and error on the editable node, the jolt, the selection colour

The label is a sibling text and the error sits on the outer Column, so a reader announces a nameless edit box (QA F08). An error cleared during its jolt leaves the field shifted, 82 pixels differing a second later, because the effect returns without resetting the shake; validation that clears on the next keystroke is the real path (F10). Selected text is the toolkit's blue, off every palette: there is no `LocalTextSelectionColors` (F26, P3, here because it is the same field).

- AC: the editable node carries the label and the error; a jolt cut short leaves nothing shifted; selection is drawn in the theme's colours.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTextField.kt`
