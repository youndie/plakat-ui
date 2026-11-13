---
id: B-81
title: "PlakatBanner: a warning that stays in the screen until its condition passes"
status: done
priority: P2
size: S
stage: stage-4-product
epic: feature-components
blocked_by: [B-75]
---

# B-81 — PlakatBanner: a warning that stays in the screen until its condition passes

Issue [#25](https://github.com/youndie/plakat-ui/issues/25), from pyatiletka. The board shows a stale snapshot as «ВНИМАНИЕ ·
Снимок от 11 ноября, 21:17 — нет связи с GitHub · ПОВТОРИТЬ» under its header until the connection returns.
`PlakatSnackbar` is an overlay that leaves; this one stays and takes space. The tokens name the form:
`plakat/semantic/warning` is «заливка + «ВНИМАНИЕ»».

- **The decision.** `PlakatBanner(kind, text, modifier, action: String? = null, onAction: () -> Unit = {})`, full width,
  40 dp high at its least as the issue's canvas draws it: the kind's word in `label` (the theme's strings, so an app
  localises it, as `error` already is), the caller's sentence, and a text action as the dialog's plain action is.
  Kinds are warning (ocher fill), danger (the hatch with its word, never red alone) and info (slate fill), the semantic
  kinds that warn, sharing `PlakatStatusKind` with [B-75](B-75-status-mark.md). It grows with its text rather than cutting
  it (B-65: a sentence wraps here, as it is the warning). It enters and leaves by no motion of its own: the caller's layout
  moves. Designed here (D13): the item lists every number it sets.
- **Rejected.** A snackbar that stays: a snackbar is placed over the content and a banner pushes it down, so one
  component would need two layouts. The name `PlakatAlert`: «Баннер» is also a poster's layout, but the issue and the
  board say banner, and the component lives in `components/` beside `PlakatPoster`'s layouts without a clash in Kotlin.
- **Not covered.** Dismissing it: a banner goes when its condition does.

- AC: each kind draws on both papers with the word, a long sentence wraps and the banner grows, the action is a 48 dp
  target with the keyboard's frame; danger is told from warning without colour; the theme's strings carry the kinds'
  words; the sample's kit shows all three; goldens recorded and looked at; #25 is closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSnackbar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/theme/PlakatStrings.kt`

## Findings (2026-11-13)

- **Built.** `PlakatBanner(kind: PlakatStatusKind, text, modifier, action: String? = null, onAction: () -> Unit = {})` in
  `components/PlakatBanner.kt`. A kind that does not warn — primary, success — is refused with an
  `IllegalArgumentException`, stated in the KDoc: the item names warning, danger and info, and inventing a word and a
  ground for the others would be inventing two banners nobody asked for.
- **The words.** `PlakatStrings` gains `warning = "Warning"` and `info = "Info"`; danger uses the existing `error`, as
  the snackbar's error does. An app localises all three through the theme.
- **Every number designed here (D13):**
  | What | Value | Why |
  |---|---|---|
  | height | 40 dp at the least; 48 with an action | the issue's canvas; the action's target is 48 (CLAUDE.md) |
  | sides | 16 dp (`PlakatGrid.Margin`); 8 at the end beside an action | the snackbar's |
  | word, sentence, action | 8 dp apart (`PlakatGrid.Step`); 8 dp above and below the words | a grid step |
  | word | the theme's word in `label`, one line | as the Tokens page's swatches print «ВНИМАНИЕ», «ИНФО» |
  | sentence | `bodyS`, wraps, never cut | a warning is read whole (B-65) |
  | warning | ocher fill, words in the darker of ink and paper | `plakat/semantic/warning`; ink on both papers, since the dark theme's ink is light |
  | info | slate fill, words in the lighter of the two | `plakat/semantic/info`: «текст paper», for the same reason |
  | danger | an ink plate under an 8 dp hatch of red and ink, period 6, words in paper | the snackbar's error plate (B-18); hatch and word, never red alone |
  | action | its word in `button` over a 3 dp rule, 3 dp gap, 12 dp each side, a 48 × 48 target at the least, at most half the row; pressed, the two inks trade places | the dialog's plain action; the snackbar's half (B-65) |
- **No motion of its own**: it is in the layout, and the screen it stands in moves.
- **Goldens** `Banner/Kinds_Light`, `Banner/Kinds_Dark`: warning with the issue's sentence and RETRY, danger, info, and a
  warning whose sentence wraps to five lines; looked at. The sample's kit has a «Banner» group with the three.
- **Mutants**, each caught: no hatch on danger (the ground test and `danger is told from warning without colour`), a word
  not the theme's (the ground test), the action unbounded (`a long action takes at most half…`), every kind accepted
  (`a kind that does not warn…`), warning's words in the light ink (`a banner's action shows the frame`).
- **ABI**: `PlakatBanner` and the two words of `PlakatStrings` (its constructor gains two defaulted parameters).
