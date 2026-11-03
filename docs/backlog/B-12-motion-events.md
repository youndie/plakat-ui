---
id: B-12
title: "Motion events: the named events every animation reports, for sound later"
status: open
priority: P1
size: S
stage: stage-1-materials
blocked_by: [B-09]
---

# B-12 — Motion events: the named events every animation reports, for sound later

The brief fixes event names now so that sound can be attached later: `stamp`, `release`, `engage`, `wedge-in`, `wedge-out`, `lever`, `underline`, `hatch`, `print`, `tear`, `telegram`, `announce`, `lock`, `rail`, `tick`, `fold`, `wipe`, `roll`, `notch` (spec rows of the core and MVP pages).

- **The decision and its reason.** A `PlakatEvent` enum and a listener in the theme; every animation reports its event once per trigger. No sound in core (brief, «Не-цели»).
- Rejected: a callback per component.

- AC: a test collects the events of a scripted session on the sample and gets the spec's names in order.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`, `reference/design-system/plakat-ui Компоненты.dc.html`
