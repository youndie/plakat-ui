---
id: B-62
title: "Dialog: long text scrolls and the actions stay reachable"
status: open
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
