---
id: B-06
title: "Frame cost on a Pixel 6a: raskat roll, ten cards cascading with texture and registration"
status: open
priority: P3
size: M
stage: stage-4-product
blocked_by: [B-35, B-36, B-38, B-39, B-44, B-45, B-46, B-47, B-48, B-49, B-50, B-51]
---

# B-06 — Frame cost on a Pixel 6a: raskat roll, ten cards cascading with texture and registration

The brief's performance target is 60 fps on a Pixel 6a with ten cards cascading, textured and with registration; raskat adds a per-pixel comparison per frame during its 320 ms roll (H3).

- **The decision and its reason.** The owner moved this to the end on 2026-11-03: the device is measured once the kit is complete, and until then the same benchmark runs on the desktop JVM ([B-51](B-51-frame-cost-on-desktop.md)). Measured by the `measurement-study` protocol: a benchmark screen in the sample, frame times from the device, p50 and p95, interleaved runs, a positive control. Thresholds are written before the first number: p95 ≤ 16.7 ms for the cascade, ≤ 2 ms for one raskat frame.
- If the cascade misses, the brief's rule applies: the texture goes off, the target stays.
- Rejected: measuring on the emulator or desktop.

- AC: a verdict per question (green, grey, red, not measured) with raw logs committed; the device and build named.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`, `reference/design-system/plakat-raskat.js`
