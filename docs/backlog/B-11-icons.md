---
id: B-11
title: "Icons: 16 PlakatIcons generated from plakat-icons.json"
status: open
priority: P1
size: S
stage: stage-1-materials
blocked_by: [B-01, B-05]
---

# B-11 — Icons: 16 PlakatIcons generated from plakat-icons.json

Sixteen stencil icons, one path each on a 24 dp grid, holes counter-clockwise under nonzero fill (the Icons page).

- **The decision and its reason.** Generate `ImageVector`s from `plakat-icons.json` by a committed script, `--check` in the gate. Colour comes from the parent (currentColor). The 16 SVGs were not vendored (research §1.1); on re-import their paths are compared with the JSON.
- Rejected: converting SVG files by hand.

- AC: each icon matches its cell at 48, 24 and 16 dp within the floor; the generator's check fails on a JSON edit not regenerated.
- Anchors: `reference/design-system/plakat-icons.json`, `reference/design-system/plakat-icons.js`, `reference/design-system/plakat-ui Иконки.dc.html`
