---
id: B-32
title: "Проекция: text meshed onto a horn, cone, column, sphere, box or stairs"
status: wip
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-02, B-31]
---

# B-32 — Проекция: text meshed onto a horn, cone, column, sphere, box or stairs

The Stretch page's «ПРОЕКЦИЯ» rasterises a line once and meshes it onto a flat-faced shape. The sketch draws through Android's `nativeCanvas` (research §1.3).

- **The decision and its reason.** Port the shape maps from `plakat-warp.js` (they run in Node, so their meshes are goldens) and draw with Compose's common `Canvas.drawVertices(Vertices, BlendMode, Paint)` and an `ImageShader` of the rasterised text. The mesh is rebuilt only when text, size or shape change; appearance is a wipe, never a scale. Settles H4.
- Not MVP in the brief; built after the MVP set.

- AC: mesh positions match the Node goldens; the page's horn and box cells match within a floor measured for meshes; it draws on all four targets.
- Anchors: `reference/design-system/plakat-warp.js`, `reference/design-system/plakat-warp.kt`
