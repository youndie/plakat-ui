---
id: B-50
title: "Pull-to-refresh: a red wedge grows with the finger, then a stepped sector"
status: wip
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-24]
---

# B-50 — Pull-to-refresh: a red wedge grows with the finger, then a stepped sector

Brief v1: "красный клин растёт сверху за пальцем, при отпускании переходит в ступенчатый сектор".

- **What to build.** The wedge follows the pull linearly; past the threshold, release turns it into the circular progress's stepped sector ([B-24](B-24-progress.md)); nested scrolling from a list.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`
