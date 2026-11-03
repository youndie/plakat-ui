---
id: B-03
title: "Generator: commonMain port of generate/rapport held to Node goldens on every target"
status: done
priority: P0
size: M
stage: stage-0-risks
blocked_by: [B-01]
---

# B-03 — Generator: commonMain port of generate/rapport held to Node goldens on every target

The designer's `plakat-gen.kt` matches `plakat-gen.js` on 500 seeds on the JVM, but its `snap` uses `java.lang.Math.round` (research §1.3).

- **The decision and its reason.** Port the sketch into `commonMain` keeping its structure, with `snap(v) = floor(v / 8 + 0.5) * 8` (D4). A Node script loads the vendored `plakat-gen.js` untouched and writes goldens: `generate` for seeds 1…2000 at 360 × 240, 392 × 392, 240 × 400 and the onboarding panel 358 × 360 with its fixed head zone; `rapport` for the four vendored seeds at cells 12 and 24. The test runs on JVM, Native and wasm (H2).
- The four rapports double as an external check: they must equal `agit/rapport-*.svg` (research §1.1).
- Rejected: `kotlin.math.round` (half-to-even). Not covered: halftone (needs a source image, [B-33](B-33-agit-presets-and-photomontage.md)).

- AC: goldens regenerate deterministically and byte-identically; `make check` fails when they are stale against `plakat-gen.js`; the port matches every golden on all four targets; a mutant in `snap` or in mulberry32 is caught.
- Anchors: `reference/design-system/plakat-gen.js`, `reference/design-system/plakat-gen.kt`, `reference/design-system/agit/rapport-1923.svg`

## Findings

- 2026-11-03. Port in `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/agit/PlakatGen.kt`
  (internal for now; the poster, window and agit items make what they need public). Goldens from
  `scripts/gen-goldens.mjs`, which loads the vendored JS untouched and writes a Kotlin source of
  goldens into the common tests — a source, because common tests cannot read a file on iOS or in the
  browser. Coordinates are compared as integers of thousandths of a dp so no float printing takes
  part; seeds 1–2000 of five frames (empty state 360 × 240, Круг 392 × 392, splash 240 × 400, the
  onboarding panel 358 × 360 with its fixed title corner, the 64 × 64 thumbnail without a title) are
  kept as FNV-1a 64 hashes, the first 12 of each whole; rapports 1–400 hashed and the four vendored
  ones whole at cells 12 and 24.
- Result: all 10 000 compositions and 400 rapports equal on the desktop JVM, the iOS simulator
  (Kotlin/Native) and wasm in the browser — H2 holds. `make check` fails when the goldens are stale
  against the vendored JS.
- Mutants killed: `snap` through `kotlin.math.round` (half-to-even), mulberry32's increment off by
  one, the strip's row filter `>=` → `>`.
- The rule test holds 500 seeds to "exactly one red element, no ink or red in the title zone".
- The canonical text in the goldens is the thing compared; the vendored rapport SVGs equal it
  (research §1.1), so they need no separate SVG parser.
