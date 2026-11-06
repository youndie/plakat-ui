---
id: B-59
title: "TextField: the caret stays at the insertion point, inside the field"
status: done
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

## Findings

The toolkit's own cursor cannot carry the design's: its width and blink are fixed, and `cursorBrush` chooses only a paint. Its scroll offset cannot be read from outside either. So the field scrolls itself: `BasicTextField` sits in a `horizontalScroll`, never wider than its text, so the toolkit has nothing to scroll, and the caret is drawn in the text's own coordinates, which the same scroll moves and clips. A wheel and a finger still scroll the line, and `overscrollEffect = null` keeps Android's stretch out, since the design has no overshoot. An unbounded parent would make `horizontalScroll` throw, so the field takes `maxContentWidth()` from [B-58](B-58-unbounded-width.md).

The caret follows a frame after the text's layout, through a `LaunchedEffect`. Nothing is drawn outside the field on that frame, as the scroll clips it.

Designed here (D13): between two letters the bar is centred on the insertion point, as the Core page's `gap: 2px` is for the end of the text and would put it on the next letter. After the last letter it stays as the page draws it. The `TextField/*` goldens hold within their tolerance, not to the pixel: the scroll clips, and a clip moves the edges of letters by a channel delta of up to 7, as [B-62](B-62-dialog-long-text.md) measured on the dialog, where the longer text crossed the tolerance. A field has too few letters to cross it, and it must clip when it scrolls.

Rendered and looked at on both papers: at the end of a long line, in the middle, at the start and empty, with a button to its right that nothing paints over. `./gradlew check`: 834 tests, 0 failed.

Mutants, each killed by the aimed test: no following to the right fails `typing past the width keeps the caret inside the field`; no following to the left fails `moving the caret home scrolls the text back`; the original offset on the transformed text fails `under a mask that lengthens the text the caret stands at the mapped place`; the gap between letters fails `after the last letter the caret stands clear of it and between two it is centred on the insertion point`; the toolkit's scroll back fails `typing past the width keeps the caret inside the field`.

Not exercised: a real IME, whose composing text moves the caret by the same path, and text selection dragged past the field's edge in a real window ([B-69](B-69-real-window-pass.md)).
