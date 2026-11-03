---
id: B-08
title: "Tokens: generate PlakatTokens from plakat-tokens.json, --check in the gate"
status: open
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
