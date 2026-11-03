---
id: B-38
title: "RadioButton: a ring with a red disc that locks in one frame"
status: open
priority: P1
size: S
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-15]
---

# B-38 — RadioButton: a ring with a red disc that locks in one frame

MVP in the brief ("круг-рамка, внутри красный диск; выбор — диск встаёт на место за один кадр", event `lock`, 0 ms), not drawn by the design (research §1.6).

- **What to build.** A 24 dp ring in the 3 dp rule — one of the kit's few full circles — with a red disc inside; selecting shows the disc in one frame and the previous one goes out a frame earlier. Group API as Material's. Size and gap follow the Checkbox (24 dp, 48 dp target).
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`
