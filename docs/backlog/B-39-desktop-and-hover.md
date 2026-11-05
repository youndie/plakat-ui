---
id: B-39
title: "Hover on pointer devices and a wide poster layout"
status: wip
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-13, B-19, B-20, B-22, B-23]
---

# B-39 — Hover on pointer devices and a wide poster layout

The brief proposed a desktop layout for the poster and demo screens and cheap hover states ("инверсия рамки"); the owner left it to this repository on 2026-11-03 (research D2).

- **What to build.** Hover, on pointer input only, inverts the element's frame in one frame (a rule becomes the 3 dp bar, an outlined frame fills, a filled plate gets the ink frame), no animation; it never stands in for pressed. A wide `PlakatPoster` layout for widths ≥ 600 dp: title zone on the left half, the composition on the right, the slogan button under the title.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`
