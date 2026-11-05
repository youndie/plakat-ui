---
id: B-51
title: "Frame cost on the desktop JVM: raskat roll and the ten-card cascade, as a proxy until the device"
status: wip
priority: P1
size: S
stage: stage-0-risks
blocked_by: [B-04, B-10]
---

# B-51 — Frame cost on the desktop JVM: raskat roll and the ten-card cascade, as a proxy until the device

The owner moved the Pixel 6a measurement to the end (2026-11-03, [B-06](B-06-frame-cost-measurement.md))
and asked for the desktop JVM meanwhile. H3 (research §3) still decides whether raskat may run on
the CPU per frame, so it gets a number now, on the machine the kit is built on.

- **The decision and its reason.** A benchmark screen in the sample: ten cards cascading with
  texture and registration on, and one raskat roll on the poster's circle at the densities 1, 2 and
  2.75. Frame times from a `withFrameNanos` recorder over interleaved runs, p50 and p95, with a
  positive control (an artificial 10 ms busy-wait per frame that the recorder must see).
  Thresholds written before the first number: p95 ≤ 16.7 ms for the cascade, ≤ 2 ms for one
  raskat comparison frame.
- A desktop number is not the brief's target: it can refute H3 (if the Mac cannot do it, a phone
  cannot) but not confirm it. The verdict says so, and B-06 repeats the benchmark on the device.
- Rejected: skipping the measurement until the end — raskat's CPU path would by then be under
  every poster.

- AC: a verdict per threshold (green, grey, red) with the raw frame logs committed, the machine and
  JVM named; the benchmark is the one B-06 runs unchanged on the device.
- Anchors: `reference/design-system/plakat-raskat.js`, `sample/build.gradle.kts`.
