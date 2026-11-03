---
id: B-03
title: "Generator: commonMain port of generate/rapport held to Node goldens on every target"
status: wip
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
