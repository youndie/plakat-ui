---
id: B-75
title: "PlakatStatus: a word with its sign, for the semantic kinds"
status: done
priority: P2
size: S
stage: stage-4-product
epic: feature-components
---

# B-75 — PlakatStatus: a word with its sign, for the semantic kinds

Issue [#19](https://github.com/youndie/plakat-ui/issues/19), from pyatiletka's board on `0.1.0.15`. The tokens name five
semantic kinds and the form each takes (`plakat/semantic/*` in `plakat-tokens.json`): `danger` is a 45° hatch at a step of
6 plus «ОШИБКА», `success` the wedge-check, `warning` a fill plus «ВНИМАНИЕ», `info` a slate fill plus «ИНФО», `primary`
a red fill. No component draws them as a mark beside a word, so a board showing a branch's state, a pull request's checks
and an item's status draws its own hatch, wedge and plate from the kit's colours, and every consumer's will differ.

- **The decision.** `PlakatStatus(kind: PlakatStatusKind, word: String, modifier)`: a sign, then [word] in `label`, 20 dp
  high as `PlakatBadge` is. The sign is the kind's form from its token, drawn by the kit's print primitives (the hatch of
  `PlakatHatch`, the wedge of `PlakatTick`), never a colour alone, as red is never the only carrier of meaning (CLAUDE.md).
  The word is the caller's, as a board's statuses are its own words; the token's word («ОШИБКА») is what the kind means,
  not what every status says. Designed here (D13): no page draws a status mark; the item lists every number it sets.
- **Rejected.** An overload of `PlakatBadge`: a badge is a count over an icon, and a status is a word in a line.
  A sign at 16 dp through `PlakatIcon`'s `Done`: the stencil is drawn for 24 and the wedge-check is a print form, not an
  icon.
- **Not covered.** A status that moves (a running check's progress): the caller puts a `PlakatCircularProgress` beside it.

- AC: each of the five kinds draws its sign and the caller's word on both papers, 20 dp high; danger is told from primary
  without colour (a grayscale test, as B-36 has); a reader hears the word; the sample's kit shows all five; goldens
  recorded and looked at; a mutant per kind's sign is caught; #19 is closed by the pull request.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatBadge.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/print/PlakatHatch.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTick.kt`, `sample/src/commonMain/kotlin/io/github/youndie/plakat/sample/Kit.kt`

## Findings (2026-11-13)

- **The item named the wrong primitive.** `PlakatTick` is the number's tick (B-30), not a wedge; the kit had no
  wedge-check. The Tokens page draws it: the success swatch's 20 × 20 ink square cut by
  `clip-path: polygon(0 55%, 30% 55%, 40% 75%, 100% 0, 100% 30%, 40% 100%)`, which the sign takes as it is.
- **Built.** `PlakatStatus(kind: PlakatStatusKind, word, modifier)` in `components/PlakatStatus.kt`; `PlakatStatusKind`
  has the five kinds, for B-81 to share. The word is set by `PlakatParagraph` (B-78) in `label`, upper-cased by the role.
- **Every number designed here (D13):**
  | What | Value | Why |
  |---|---|---|
  | sign | 20 × 20 dp | the Tokens page's wedge, and `PlakatBadge`'s height |
  | status height | 20 dp at the least | as tall as a badge; taller only when a large font's word needs it |
  | sign to word | 8 dp, `PlakatGrid.Step` | one step of the grid |
  | primary | red fill | `plakat/semantic/primary`: «заливка» |
  | danger | 45° hatch, red and paper, 6 dp period (3 and 3) | `plakat/semantic/danger` and the Tokens page's `repeating-linear-gradient(45deg, red 0 3px, paper 3px 6px)`, through `plakatHatchBrush` |
  | success | the ink wedge-check | `plakat/semantic/success`: «клин-галочка» |
  | warning | ocher fill | `plakat/semantic/warning` |
  | info | slate fill | `plakat/semantic/info` |
  | word | `label`, the content colour in place, else ink | a status is a word in a line |
- **What the forms do not settle.** Primary, warning and info are each a fill, as the tokens' plates are, so without
  colour they are told apart by the word, which is the caller's. Danger against primary — both red — differs by its
  hatch: in grey the primary sign is one level and danger's stripes span more than 60 of 255 (`PlakatStatusTest`).
- **A reader** hears the caller's word only (the row's semantics are cleared and set to it); the sign is drawn.
- **Goldens** `Status/Kinds_Light`, `Status/Kinds_Dark`: the five beside a board's words, looked at. The sample's kit
  has a «Status» group with all five.
- **Mutants**, one per kind's sign, each caught: primary drawn ocher, warning slate, info ocher (`a fill fills…`),
  danger a plain red fill (`danger is the hatch…`, `danger is told from primary…`), success a full ink square
  (`success is the ink wedge-check…`).
- **ABI**: `PlakatStatus` and `PlakatStatusKind` added.
