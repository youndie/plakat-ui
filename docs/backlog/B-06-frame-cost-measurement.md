---
id: B-06
title: "Frame cost on a Pixel 6a: raskat roll, ten cards cascading with texture and registration"
status: wip
priority: P3
size: M
stage: stage-4-product
blocked_by: [B-35, B-36, B-38, B-39, B-44, B-45, B-46, B-47, B-48, B-49, B-50, B-51]
---

# B-06 — Frame cost on a Pixel 6a: raskat roll, ten cards cascading with texture and registration

The brief's performance target is 60 fps on a Pixel 6a with ten cards cascading, textured and with registration; raskat adds a per-pixel comparison per frame during its 320 ms roll (H3).

- **The decision and its reason.** The owner moved this to the end on 2026-11-03: the device is measured once the kit is complete, and until then the same benchmark runs on the desktop JVM ([B-51](B-51-frame-cost-on-desktop.md)). Measured by the `measurement-study` protocol: a benchmark screen in the sample, frame times from the device, p50 and p95, interleaved runs, a positive control. Thresholds are written before the first number: p95 ≤ 16.7 ms for the cascade, ≤ 2 ms for one raskat frame.
- If the cascade misses, the brief's rule applies: the texture goes off, the target stays.
- From [B-51](B-51-frame-cost-on-desktop.md): on an M1 the raskat frame is 8.26 ms at 2.75, over its line, and the comparison alone 1.32. `FrameBenchmark` is ready in `commonMain` and wants an Android host. About 6 ms of that frame are unexplained: worth a look here if the roll misses.
- From [B-32](B-32-projection.md): the projection's `drawVertices` on a bitmap is compiled for Android and has never run there. Open the kit's Проекция group on the device, and note how long its first frame takes.
- Rejected: measuring on the emulator or desktop.

- AC: a verdict per question (green, grey, red, not measured) with raw logs committed; the device and build named.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`, `reference/design-system/plakat-raskat.js`

## Protocol, fixed before the first number (2026-11-05)

The item's text above is the pre-registration, with [B-51](B-51-frame-cost-on-desktop.md)'s protocol, which this runs unchanged where it can. This section makes it runnable on the device and moves no threshold. Nothing has been measured on the device when it is committed.

- **The subject.** `FrameBenchmark` as it stands, thirteen arms, 4 s an arm, six rounds rotating by one, one process, round 0 discarded. A release build, R8 on, not debuggable, signed with a debug key, in a host that does nothing else; `cmd package compile -m speed-profile -f` after installing, as an installed app with Compose's own baseline profile would be. No more rounds are added after a number is seen.
- **The stand, read and written in the log's header.** The device and build fingerprint, the commit, the display's mode and rate. The host asks for 60 Hz, the brief's target, and the interval's p50 says whether it got it. On USB power, screen on by the activity's own flag; no setting of the phone is changed. Thermal status at each round's start.
- **A second instrument, as the device has one.** B-51's `work` and `interval` see the main thread only. The host also records Android's `FrameMetrics` per frame by arm: total, GPU and whether the frame ran past its deadline. The recorder's own `work` stays the unit the thresholds were written for.
- **Verdicts.** Over counted rounds, pooled frames.
  - **Cascade** (`cascade-a`): GREEN if `work` p95 ≤ 16.7 ms and at most 5 % of intervals are ≥ 25 ms and at most 5 % of frames ran past their deadline. RED if `work` p95 > 16.7 ms or more than 20 % ran past. GREY otherwise. Stricter than B-51's by the last clause, since a phone's question includes its GPU. If it is RED, `cascade-flat` is read by the same rule, as the brief's fallback is the texture off.
  - **Raskat** at densities 1, 2 and 2.75: `roll-d`'s `work` p95 less `base-d`'s, restart frames apart. GREEN if ≤ 2 ms, RED if > 2 ms. H3's verdict is density 2.75's. The phone's own density is 2.625, so 2.75 is a little past it.
  - **The roll on time**, reported beside each: the share of `roll-d`'s frames past their deadline, as B-51 found a budget missed and a roll on time both true. GREEN at most 5 %, RED over 20 %, GREY between.
  - **H4's Android half**: GREEN if the projection's forms draw on the device, read from a screenshot of the Horn and Box cells against their desktop goldens by eye, and it does not crash; RED otherwise. Its first frame is reported: `work` of a restart of the form, 40 restarts each after 20 for warm-up. No line is set for that number, as the item sets none.
- **Control.** `busy-10` must read 10 ± 2 ms above `idle-a` at p50 of `work`; `busy-25` must miss frames, its `interval` p50 ≥ 25 ms, and at least 90 % of its frames past their deadline; `idle-a` at most 5 % past. If any fails the run is void.
- **Ruler.** `idle-a` against `idle-b`, and `cascade-a` against `cascade-b`, paired per round. A verdict within twice the cascade's ruler of its line is GREY, whichever side it falls.
- **Known order.** `cascade-flat` does less than `cascade-a`. If its `work` p95 is above `cascade-a`'s by more than the ruler, the run is void.
- **The lever.** Read from the subject: a screenshot during `cascade-a` with texture and registration showing, and during `roll-2.75` part way, each taken in a separate short run and no number kept from it.
- **Voids.** A round whose thermal status at its start is `moderate` or worse is reported and left out of no number; if any round is `severe` or worse the run is void.
- **If a line is missed.** The numbers stand as they are, and what follows is the owner's, as B-51 says: the roll off on weak devices, a GPU path, a slower cadence, the texture off. A diagnosis may follow in this item through `RaskatStepCostReport`'s steps, as the frame's unexplained 6 ms were left for here.
- **Where.** `docs/research/measurements-2026-11-05-device/`, raw frames in `raw/`, report beside them, read by `scripts/frame-report.py`, whose control is in `make check`.
- **Rejected.** A debug build, whose Compose runs many times slower and would measure nothing a user sees; Macrobenchmark, a second harness beside the one B-51 ran, whose numbers would not set beside the Mac's.

## Amendment 1, after run 1 (2026-11-05)

Late, and no threshold moves. **What forced it:** run 1 reads the raskat frame at 0.15, 0.11 and −0.06 ms at densities 1, 2 and 2.75, where the M1 reads 1.49, 5.06 and 8.26, and a roll's frame cannot cost nothing. The suspicion is that `work` is blind here: on Android a graphics layer's display list is recorded after the root's draw, and `work` ends at the last draw of the root. Run 1 is kept as it stands, in `raw/run1/`.

- **A control for the instrument, as its own control could not see this:** `blind`, two arms in turn, `plain` and `spin-5`, the second with a known 5 ms inside a layer's draw. If `spin-5` shows in `FrameMetrics`' `draw` and not in `work`, the suspicion is the mechanism.
- **If it is:** the raskat lines are read also on the main thread as the platform counts it, `animation` + `layout` + `draw`, roll less base at p95, against the same 2 ms. `FrameMetrics` is recorded with all its parts from run 2 on. The literal reading on `work` stays in the report beside it, and the owner decides which verdict stands, as the protocol named `work`.
- **If it is not:** `work` stands and the numbers are believed.
- Run 2 repeats the whole protocol. No round is added to it.

