---
id: B-64
title: "TextField: label and error on the editable node, the jolt, the selection colour"
status: done
priority: P2
size: S
stage: stage-2-components
epic: feature-components
---

# B-64 — TextField: label and error on the editable node, the jolt, the selection colour

The label is a sibling text and the error sits on the outer Column, so a reader announces a nameless edit box (QA F08). An error cleared during its jolt leaves the field shifted, 82 pixels differing a second later, because the effect returns without resetting the shake; validation that clears on the next keystroke is the real path (F10). Selected text is the toolkit's blue, off every palette: there is no `LocalTextSelectionColors` (F26, P3, here because it is the same field).

- AC: the editable node carries the label and the error; a jolt cut short leaves nothing shifted; selection is drawn in the theme's colours.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTextField.kt`

## Findings

The editable node takes the label as its description and the error, and the label's own text clears its semantics, so it is read once. The error's message stays a text under the field, which a reader can also reach; it was on the field's outer Column before, where nothing read it with the edit box.

The jolt resets at the top of the effect: a cancelled `animateTo` leaves `shake` where it stopped, and the effect restarts on the error clearing.

Designed here (D13), as no page draws a selection: concrete under the words and red for the handles, provided by `PlakatTheme`, so a `SelectionContainer` or any `BasicTextField` under the theme gets them too. Concrete is a token that ink stays legible on, on both papers.

`./gradlew check`: 890 tests, 0 failed. One existing test moved with the semantics: `an error opens the hatch...` now asserts the error on the editable node, not the Column.

Mutants, each killed by the aimed test: no jolt reset fails `a jolt cut short by the error clearing leaves nothing shifted`; no description, no error and a label read twice each fail `the editable node carries the label and the error, and the label is not read twice`; no selection colours fail `selected text is on the palette's concrete and not the toolkit's blue`.
