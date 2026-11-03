---
id: B-33
title: "Agit graphics: the six presets, generated compositions, rapports, halftone photomontage"
status: open
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-03, B-05]
---

# B-33 — Agit graphics: the six presets, generated compositions, rapports, halftone photomontage

The Agit page delivers six hand-drawn presets with their title zones, the generator, four rapports and the photomontage rule; photos are never shown raw.

- **The decision and its reason.** Presets become `ImageVector`s generated from the SVGs (both palettes) with their zones from `presets.json`; compositions and rapports come from the port ([B-03](B-03-generator-port-and-numeric-parity.md)); `Modifier.plakatHalftone(mode)` runs the CPU dots once per source and size, and the 1-bit mask as the cheap path (D6). The kit ships the processor, not photographs; the images dropped into the design's image slots were not vendored (licence unknown).
- The skeleton loading pattern steps the rapport by one cell, 6 steps per 1200 ms; reduce motion: it stands.

- AC: each preset matches its SVG within the floor in both themes; the halftone of the design's stand-in image matches the Node output per dot centre and radius; skeleton steps at the spec's rate.
- Anchors: `reference/design-system/agit/presets.json`, `reference/design-system/agit/README.md`, `reference/design-system/plakat-gen.js`, `reference/design-system/plakat-ui Агит-графика.dc.html`
