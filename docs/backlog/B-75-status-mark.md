---
id: B-75
title: "PlakatStatus: a word with its sign, for the semantic kinds"
status: open
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
