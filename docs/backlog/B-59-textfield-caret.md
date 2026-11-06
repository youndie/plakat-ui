---
id: B-59
title: "TextField: the caret stays at the insertion point, inside the field"
status: open
priority: P1
size: S
stage: stage-2-components
epic: feature-components
---

# B-59 — TextField: the caret stays at the insertion point, inside the field

Once the text is wider than the field the caret leaves it: it paints over whatever stands to the right, 67 dp past the field's end in the QA's picture, and at the end of a long line none shows in the field at all (QA F02). The kit draws the caret itself at `layout.getCursorRect(...).left`, outside `BasicTextField`'s own horizontal scroll, so the scroll offset is ignored. The same line takes the original offset on the transformed layout, so under a `VisualTransformation` that changes the length it stands on the wrong letter: `123456` shown as `12-34-56` has it on the `5` at the end (F09). And the Core page's `gap: 2px` is for the end of the text; in the middle of a word the caret stands on the next letter (F27).

- **The decision.** The caret is drawn where the insertion point is on screen: through the transformation's offset mapping, moved by the field's scroll, clipped to the field. If the toolkit's own cursor can carry the design's size and colour, that is preferable to a second copy of its position; the item records which it took and why.
- **Rejected.** Disabling the horizontal scroll: a field that cannot show what is being typed is a worse defect than a misplaced bar.
- **Not covered.** Label and error semantics, the jolt and the selection colour: [B-64](B-64-textfield-semantics-jolt-selection.md).

- AC: typing past the width keeps the caret visible and inside the field, and moving it left scrolls back; nothing is drawn outside the field; under a mask the caret stands at the mapped position; `TextField/*` goldens hold; each behaviour has a test that fails with the fix reverted.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTextField.kt`
