---
id: B-49
title: "Segmented button: one frame, the black fill rides linearly"
status: open
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-22]
---

# B-49 — Segmented button: one frame, the black fill rides linearly

Brief v1: "общая рамка, выбранный сегмент залит чёрным, заливка переезжает линейно". Drawn once inside the Settings demo (theme switch: 40 dp, 3 dp inset frame, ink fill, label style).

- **What to build.** That control as a component: the fill rides linearly between segments (normal 200 linear), the label inverts as the fill passes. Height 40 drawn, target 48.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Демо.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`
