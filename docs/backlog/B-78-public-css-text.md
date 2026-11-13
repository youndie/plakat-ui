---
id: B-78
title: "A consumer's text in the kit's styles sits where Blink puts it"
status: wip
priority: P1
size: M
stage: stage-4-product
epic: feature-components
---

# B-78 — A consumer's text in the kit's styles sits where Blink puts it

Issue [#22](https://github.com/youndie/plakat-ui/issues/22), from pyatiletka, measured against a canvas rendered in Chrome.
A consumer setting its own words with `BasicText(…, style = type.displayM.style)` gets them lower than the same CSS:
«ПЛАН ВЫПОЛНЕН / НА 95 %» in `displayM` 6 px below the canvas, a `headline` number 4–5 px. The kit's components do not
have this, as they draw through `CssParagraph`, `cssBaseline` and `drawOnBaseline` (`PlakatCssText.kt`) with the faces'
`FaceMetrics`, which put the baseline where Blink does; all of that is `internal`. So every screen built from the kit's
typography carries a residual its own components do not, on every display line.

- **The decision.** A public composable that sets a role as a CSS line box does, over `CssParagraph`:
  `PlakatParagraph(text, role: PlakatText, modifier, color, maxLines, overflow)`, its colour defaulting to the content
  colour. `PlakatText` already names a role of `PlakatTypography`, so the composable takes another name. The line height
  is the role's, as the design's pages set it; the face metrics stay internal behind it.
- **Rejected.** Making `CssParagraph`, `FaceMetrics` and the baseline helpers public: four names to keep stable where one
  does, and a consumer would have to pair the right metrics with the right face. Fixing the roles' `TextStyle`s so that
  `BasicText` lands right: Compose's first baseline comes from the font's ascent and the line height's split, which
  `cssLine` already showed cannot reproduce Blink for every role (the dialog's title, B-28).
- **Not covered.** Editable text, which is `PlakatTextField`'s.

- AC: for each role, a fixture of `PlakatParagraph` matches a reference line rendered by the repository's Chrome within the
  floor, and the same text through `BasicText` is shown to differ (the issue's 6 px for `displayM`); `maxLines` with an
  ellipsis follows B-65; it does not throw with no width (B-58); a reader reads the text; the ABI dump changes by the one
  function; #22 is closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCssText.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/theme/PlakatTypography.kt`
