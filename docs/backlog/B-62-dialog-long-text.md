---
id: B-62
title: "Dialog: long text scrolls and the actions stay reachable"
status: done
priority: P1
size: S
stage: stage-2-components
epic: feature-components
---

# B-62 — Dialog: long text scrolls and the actions stay reachable

The Dialog's body sits in a plain Column with no scroll. Long text overprints the title and rises above the sheet, and the actions leave the window, so only Escape or a click on the screen remain (QA F05). The cause is double: there is no scroll, and `CssParagraph`'s `layout` reports a height over its constraint, so it is centred over what stands above it. A window 360 high has room for about six lines of body by arithmetic, so a phone in landscape meets this with ordinary text. [B-28](B-28-dialog.md) records no decision on length; the BottomSheet's content already scrolls.

- **The decision.** The title and the actions keep their height and the body between them scrolls within what is left, as the sheet's content does; no scrollbar and no fade, since the design draws neither.
- **Rejected.** Letting the whole sheet scroll: the actions could then be out of sight, which is what this item is to end. Cutting the text: a dialog's text is what is being agreed to.
- **Not covered.** Two long actions that overflow the sheet: [B-65](B-65-long-content.md).

- AC: with text longer than the window, title and actions are placed and on screen, the body scrolls by wheel and by keyboard, and its last line can be read; `Dialog/*` goldens hold; a test fails with the scroll removed.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatDialog.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCssText.kt`

## Findings

`verticalScroll` was the first thing tried, and `Dialog/Danger_*` went red: 157 and 166 pixels of 163 800, 0.10 % against the 0.05 % tolerance, max channel delta 7, all on the edges of the body's letters. Neither the golden nor the tolerance was touched. Bisecting: the body in a `weight` and the sheet's new foot change nothing; a clip does, whether a layer's or a `clipRect`, with the same count from either. A layer was the first guess and it was wrong: a scroll without one moved exactly the same pixels. So the text clips only when it overflows, and a text that fits draws as it did. That rules `verticalScroll` out, which always clips, and the scroll is about thirty lines of its own: a `scrollable` over an offset, its axis range in the semantics. Skia's reason for a clip changing a letter's coverage was not looked into.

Designed here (D13), as the page's sheet has no foot: a tall sheet stops `SheetInset`, 24 dp, short of the window's foot, as its sides do. The clipped last line is the only sign there is more, as the design draws no bar and no fade; between that line and the actions there is no gap of its own, as the text's padding has scrolled away. A title taller than the window is not handled: it would push the actions out as before, and only the text scrolls.

Text that overflows is a stop for the keyboard, with the kit's frame, and the arrows move it a line; PageUp and PageDown come from `scrollable`. The screen behind is still the first stop, which is [B-63](B-63-overlay-focus-and-input.md).

Rendered and looked at on both papers: tall, a window 360 high and short text. `./gradlew check`: 859 tests, 0 failed, `viddikVerify` included.

Mutants, each killed by the aimed test: no `weight` fails `the title and the actions stay on screen`; no room to scroll fails `the wheel scrolls the text`; no arrows fails `the arrows and page keys scroll the text`; no frame fails `the text shows the keyboard's frame`; no clip fails `scrolled the text stays out of the title's plate`; a box that stretches fails `text that fits leaves the sheet as high as its parts`; a stop whatever the length first survived, as the test read the wrong node, and after a rewrite fails `text that fits is no stop for the keyboard`. An always-on clip is held by `Dialog/Danger_*` themselves, as above.
