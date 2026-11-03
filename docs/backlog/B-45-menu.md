---
id: B-45
title: "Menu / Dropdown: a list on paper in a bar frame, opened by a wipe"
status: open
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-23]
---

# B-45 — Menu / Dropdown: a list on paper in a bar frame, opened by a wipe

Brief v1: "список на бумаге с брус-рамкой, раскрывается wipe сверху вниз".

- **What to build.** The list in an 8 dp bar frame, items as list rows ([B-23](B-23-list-item-and-divider.md)) with pressed inversion, opened by a top-down wipe (quick 120 linear), closed the same way.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`
