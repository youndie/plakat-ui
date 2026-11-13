---
id: B-79
title: "PlakatDialog: a content slot between the text and the actions"
status: open
priority: P2
size: S
stage: stage-4-product
epic: feature-components
---

# B-79 — PlakatDialog: a content slot between the text and the actions

Issue [#23](https://github.com/youndie/plakat-ui/issues/23), from pyatiletka. `PlakatDialog` takes a title, a text and
actions. The board's appearance dialog («ВИД»: a skin and a theme, each a group of radio rows, then «ГОТОВО») needs
content between them, so the consumer draws its own surface and its own scrim; on a phone the same choice goes into
`PlakatBottomSheet`, which has a content slot.

- **The decision.** `content: (@Composable ColumnScope.() -> Unit)? = null` on `PlakatDialog`, after the text and before
  the actions, padded as the text is (`0 20px`). It scrolls with the text where the two overflow, the title and the actions
  staying on screen as [B-62](B-62-dialog-long-text.md) has them, and it clips only when it overflows, as B-62 measured a
  clip moving the goldens. Focus enters the dialog at its first control, which is now the content's where there is one
  (B-63). Designed here (D13): the page draws no dialog with content.
- **Rejected.** A second composable `PlakatDialogContent`: one dialog with an optional slot keeps one place for the
  surface, the scrim, the motion and the focus.
- **Not covered.** A scrim for the consumer's own overlays: [B-80](B-80-public-scrim.md).

- AC: a dialog with two groups of radio rows draws them between the text and the actions on both papers; tall content
  scrolls and the actions stay reachable; Tab starts at the first radio; `Dialog/*` goldens do not move with no content;
  a golden with content recorded and looked at; the ABI dump changes by the parameter; #23 is closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatDialog.kt`
