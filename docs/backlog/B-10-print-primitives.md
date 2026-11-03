---
id: B-10
title: "Print primitives: cut corner, registration, hatch, wipes, hard-stop and stepped curves, non-print mask"
status: wip
priority: P0
size: M
stage: stage-1-materials
blocked_by: [B-08, B-05]
---

# B-10 — Print primitives: cut corner, registration, hatch, wipes, hard-stop and stepped curves, non-print mask

Every component is built from the same few printed devices (brief «Как это ляжет в Compose», the Tokens page «ПЕЧАТЬ»).

- **The decision and its reason.** One shared set: the cut-corner shape (`polygon(0 14px, 46px 0, 100% 0, 100% 100%, 0 100%)`), the registration layer (red drawn under at the offset of D14 on Display, Headline and plates, never on body text and icons), the danger hatch (45°, 3 on / 3 off), the rectangular and diagonal wipes as clips, the paper and non-print layers, `HardStop = CubicBezierEasing(0.2, 0.8, 0.9, 0.9)` and `stepped(n)` as closed forms (D10).
- Rejected: per-component copies of these.

- AC: each primitive has a fixture matched to a page cell within the floor; the curves are read at fixed times by a test; a mutant in the cut's points is caught.
- Anchors: `reference/design-system/plakat-ui Токены.dc.html`, `reference/design-system/plakat-ui Ядро.dc.html`
