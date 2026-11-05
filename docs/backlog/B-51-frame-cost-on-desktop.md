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

## Protocol, fixed before the first number (2026-11-05)

The item's text above is the pre-registration; this section makes it runnable and changes no threshold. Nothing has been measured when it is committed.

- **Unit.** Per frame on the main thread: `work`, from the first `withFrameNanos` callback of a frame to the last draw of that frame, and `interval`, between callbacks. The cascade's threshold is read on `work`: `interval` is paced by the display, so a 10 ms busy-wait under the budget never shows in it, and the item's control could not be seen. `interval` stands beside it. GPU submission is outside `work` and shows only as a missed frame in `interval`.
- **Verdicts.** Over counted rounds, pooled frames.
  - **Cascade** (`cascade-a`): GREEN if `work` p95 ≤ 16.7 ms and at most 5 % of intervals are ≥ 25 ms (one vsync missed at 60 Hz). RED if `work` p95 > 16.7 ms. GREY otherwise.
  - **Raskat** at each density: the frame is `roll-d`'s `work` p95 less `base-d`'s (the same poster at rest) with the frame of each restart left out and reported apart, as it is composition and not comparison. GREEN if ≤ 2 ms, RED if > 2 ms. The verdict for H3 is density 2.75's.
  - As the item says, on this machine GREEN can only fail to refute H3, and RED refutes it.
- **Control.** `busy-10`: a 10 ms busy-wait per frame, must read 10 ± 2 ms above `idle-a` at p50 of `work`; `busy-25` must miss frames, its `interval` p50 ≥ 25 ms. If either fails the run is void.
- **Ruler.** `idle-a` against `idle-b` and `cascade-a` against `cascade-b`: paired per round. A verdict within twice the cascade's ruler of its line is GREY, whichever side it falls.
- **Known order.** `cascade-flat`, texture and registration off, does less than `cascade-a`. If its `work` p95 is above `cascade-a`'s by more than the ruler, the run is void.
- **The lever.** Texture and registration are on in `cascade-a`, read from a capture of that arm in a test; the roll's progress changes per frame.
- **Arms and rounds.** Thirteen arms: `idle-a`, `idle-b`, `busy-10`, `busy-25`, `cascade-a`, `cascade-b`, `cascade-flat`, `roll-1`, `roll-2`, `roll-2.75`, `base-1`, `base-2`, `base-2.75`. 4 s an arm, six rounds, the order rotating by one each round, one JVM; round 1 discarded for warm-up. No more rounds are added after a number is seen.
- **Stand.** A real window on this Mac, as the subject sees it, in `alwaysOnTop`; machine, JVM, display and load in the log's header. Other load on the machine is not controlled: it is read by the ruler.
- **Where.** `docs/research/measurements-2026-11-05/`, raw frames in `raw/`, report beside them; `scripts/frame-report.py` turns one into the other, with a control on known input.
