---
id: B-08
title: "Tokens: generate PlakatTokens from plakat-tokens.json, --check in the gate"
status: done
priority: P0
size: S
stage: stage-1-materials
blocked_by: [B-01]
---

# B-08 — Tokens: generate PlakatTokens from plakat-tokens.json, --check in the gate

The tokens file is the machine-readable source of both themes (research §1.5).

- **The decision and its reason.** A script writes one committed Kotlin file — colours per theme, type scale (sizes in sp, line heights as multipliers, letter-spacing in em), lines, shape (axis −17°, cut 46 × 14), grid, sizes, print layers with their `enabled` flags, durations and curves (D8). The gate runs it with `--check`. A second test parses the JSON independently and compares.
- Values not in the JSON (hatch step 6, caret 3 × 24, NavigationBar 64, label tracking on chips) are read from the page that draws them and cited by line.
- Rejected: hand-typed tokens.

- AC: `make tokens --check` fails on a JSON edit not regenerated; the independent reading passes; no colour literal outside the generated file.
- Anchors: `reference/design-system/plakat-tokens.json`, `reference/design-system/plakat-ui Токены.dc.html`

## Findings

- 2026-11-03. `scripts/tokens.mjs` writes `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/tokens/PlakatTokens.kt`
  from `plakat-tokens.json`: `PlakatColors` with the semantic roles as views of the inks,
  `PlakatPalettes.Paper` and `.BlackPaper`, `PlakatTypeScale` (nine roles: size in sp, line height as
  a multiple, weight, tracking in em, capitals), `PlakatLines`, `PlakatShape` (radius, the −17° axis,
  the 46 × 14 cut), `PlakatGrid`, `PlakatSizes`, `PlakatPrint` (each layer's switch, tile, opacity;
  raskat's ranges), `PlakatDurations`, `PlakatEasings` (linear, hard-stop, `stepped(n)`). Public: the
  theme (B-09) takes them. Written in the layout ktlint keeps; `make check` runs `--check`.
- The second reading, `PlakatTokensTest`, parses the JSON with kotlinx.serialization and holds every
  generated value to it; it also checks the scale's poster jump (Display M = 3 × Body M, Headline =
  2 × Body M), that 46 × 14 lies on 17°, and that Compose's hard-stop solver and raskat's 24
  bisections agree within 10⁻³ at every thousandth with no overshoot. Mutant: one hex digit of the
  paper red changed in the generated file — the generator's check and the test both fail.
- The values the JSON does not hold (the hatch step, the caret, the NavigationBar height) are read by
  the items that draw them, each from its page with the line it came from; none is needed yet.
- The semantic roles' KDoc quotes the design's Russian description of each form verbatim.
- The sample now paints its paper from the token.
