---
id: B-81
title: "PlakatBanner: a warning that stays in the screen until its condition passes"
status: wip
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
