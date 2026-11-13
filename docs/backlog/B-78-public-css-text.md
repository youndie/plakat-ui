---
id: B-78
title: "A consumer's text in the kit's styles sits where Blink puts it"
status: done
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

## Findings (2026-11-13)

- **Built.** `PlakatParagraph(text, role, modifier, color, maxLines, overflow)` in `components/PlakatParagraph.kt`, over
  `CssParagraph`. Three choices the item did not state, each held by a test:
  - **The face is read from the role's family**: Rubik Mono One's metrics when the role's `fontFamily` is the theme's
    display family, Golos Text's for any other. `PlakatText` carries no face and its constructor is public, so a role a
    consumer copies from `PlakatTheme.typography` keeps its place and a family of the app's own falls back to Golos's.
  - **As wide as its widest line**, as `BasicText` is, not as wide as its box (a CSS block would be): the kit's own
    `CssParagraph` keeps filling its width (`fill = true`), the public one does not (`fill = false`), so a paragraph in a
    `Row` does not take the row.
  - **`overflow`**: `Clip` stops on the last line without the ellipsis; every other value ends it with one. A line is
    never cut in half: the block's height is whole lines.
  - A role with no line height is set on CSS `normal`: the face's ascent and descent, each rounded.
- **References.** `scripts/design-references.mjs` takes the Tokens page's type specimens (`Type_<Role>_Light`, eight
  roles; the page draws no body/l). The page leaves label and button on `normal`, so their specimens get the role's line
  height from the tokens (1.2 and 1) before the shot. Every existing reference re-rendered byte for byte in Chrome
  157.0.8103.51, so only the eight are new.
- **Parity** (`viddikDesignParity`, ±16 a channel), every role within the text floor of 7.05 % (research §1.7):
  displayL 0.58 %, displayM 1.35 %, headline 1.92 %, title 3.26 %, bodyM 6.96 %, bodyS 3.03 %, label 2.45 %,
  button 1.61 %. The `_DIFF`s are red on glyph edges only; bodyM breaks its three lines where Chrome does. Each golden
  inks exactly the first and last rows its reference inks (`PlakatParagraphTest`).
- **`BasicText` against it**, both in one `ImageComposeScene` at density 1, BasicText's first inked row below
  PlakatParagraph's: displayL 4, displayM 8, headline 2, button 2, label 1, title, bodyM and bodyS 0. The issue measured
  6 px for displayM and 4–5 for a headline number against a canvas; this scene draws a fraction of a pixel higher than
  viddik's capture of the same fixture, which is why the test holds displayM at 6 or more and the Golos roles at 0, not
  at the issue's exact numbers. Golos on a line taller than its face is where Compose's centring and Blink's floored
  half-leading agree; the display face, whose line is shorter than the face, is where they part.
- **Mutants**, each killed: the face always Golos (`Type/Display*`, `Type/Headline` goldens); the role's line height
  ignored (goldens and three tests); `maxLines` left out of the height (`past maxLines…`); `Clip` drawn as an ellipsis
  (`an ellipsis and a clip…`); the paragraph filling its box (`in a wider box…`).
- **ABI**: `PlakatParagraph` added, nothing else.
