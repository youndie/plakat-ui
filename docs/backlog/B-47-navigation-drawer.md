---
id: B-47
title: "NavigationDrawer: black panel with cream capital items"
status: wip
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-26]
---

# B-47 — NavigationDrawer: black panel with cream capital items

Brief v1: "панель с чёрным фоном и кремовыми прописными пунктами".

- **What to build.** An ink panel riding in from the left on a rail (normal 200 hard-stop), items in capitals (label style), the active one on the NavigationBar's red square ([B-26](B-26-navigation-bar.md)).
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`
