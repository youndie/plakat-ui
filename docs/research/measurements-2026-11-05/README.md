# Frame cost on the desktop JVM (B-51)

| | |
|---|---|
| Machine | Apple M1, 8 cores, 16 GB, macOS 27.0.1 (`aarch64`); display 1440 × 900 logical at 60 Hz, the window on it at density 2 |
| JVM | OpenJDK 64-Bit Server VM 25.0.4.1 (JBR) |
| Protocol | [B-51](../../backlog/B-51-frame-cost-on-desktop.md), committed at `c5802cf`, before the first number |
| Harness | `sample/.../bench/FrameBenchmark.kt`, `./gradlew :sample:frameBench`; reader `scripts/frame-report.py`, its control in `make check` |
| Same host, same day | yes, all three runs and the step costs |

## Result

| Question | Verdict | Number | Line | Run, commit |
|---|---|---|---|---|
| Ten cards cascading, textured and registered | **GREEN** | `work` p95 0.60 ms, 0.0 % of intervals ≥ 25 ms | 16.7 ms | 3, `154cfde` |
| Raskat frame at density 1 | **GREEN** | 1.49 ms | 2 ms | 3 |
| Raskat frame at density 2 | **RED** | 5.06 ms | 2 ms | 3 |
| Raskat frame at density 2.75 (H3's) | **RED** | 8.26 ms | 2 ms | 3 |

**H3 as a frame budget is refuted on this Mac**: if an M1 needs 8.26 ms for the frame, a Pixel 6a will not fit it in 2. The cascade's GREEN does not confirm the brief's target on a phone; it only fails to refute it. B-06 settles both.

**H3's own sentence, the comparison alone, does hold here:** 1.32 ms at 352 px, 1.19 ms at 330 px, where H3 says 2 ms. So the 2 ms line was drawn around the comparison, and the frame costs more than the comparison by about 6 ms that this study did not explain.

The roll missed no frame in any run: intervals ≥ 25 ms stay at 0.0–0.2 % at every density. Over budget and on time are both true on this machine.

## Three runs, one protocol

The line did not move. The code did, between runs, for causes the numbers showed; each run is committed before the next change.

| Run, commit | What changed | Raskat at 1 | at 2 | at 2.75 | Cascade |
|---|---|---|---|---|---|
| 1, `35470ab` | as built: the field and the whole image per frame | 6.97 | 10.67 | 16.62 | 0.58 |
| 2, `f3c80a3` | the field once per size | 6.31 | 10.21 | 15.63 | 0.46 |
| 3, `154cfde` | also only the new pixels written | 1.49 | 5.06 | 8.26 | 0.60 |

All in ms: raskat is `roll-d` p95 less `base-d` p95, restart frames apart; the cascade is `work` p95. Full tables: `raw/frames-run{1,2,3}.csv` through `scripts/frame-report.py`.

## A frame's parts, apart

`raw/raskat-steps.csv`, from `RaskatStepCostReport` (a roll's 60 frames after 200 to warm; the 128 dp circle of the benchmark's poster, and H3's 330 px):

| px | field, once | compare, p50 / p95 | write all, p50 / p95 | write new, p50 / p95 |
|---|---|---|---|---|
| 128 | 5.08 | 0.17 / 0.19 | 1.04 / 1.95 | 0.14 / 0.34 |
| 256 | 6.33 | 0.70 / 0.72 | 4.32 / 7.75 | 0.25 / 0.37 |
| 330 | 0.82 | 1.19 / 1.23 | 6.91 / 12.81 | 0.43 / 0.50 |
| 352 | 1.24 | 1.32 / 1.37 | 7.75 / 14.57 | 0.49 / 0.56 |

The first field of each size is taken cold, so that column is not a comparison between sizes.

Compare and write-new come to about 1.8 ms at 352 px, against 8.26 in the live frame. **The other 6 ms or so are not identified.** Candidates, none tested: the image's copy when a changed bitmap is drawn, and the wipes' redraw, as the `base` arm is at rest with no print running. A mechanism is not named here because no variable has been moved to show it.

## Controls

In each of the three runs:

- `busy-10` reads 9.87–9.88 ms above `idle-a` at p50 of `work`, against 10 ± 2: the recorder sees a known cost.
- `busy-25` has an interval p50 of 25.12 ms and misses every frame: the interval sees a missed frame.
- Known order: `cascade-flat`, texture and registration off, is at or under `cascade-a` (0.47 against 0.60 in run 3).
- Ruler: `cascade-a` against `cascade-b`, widest per-round p95 difference 0.12–0.24 ms; `idle-a` against `idle-b` at most 0.05 ms. Every verdict is far outside twice the ruler of its line.
- Levers: `FrameBenchmarkTest` reads the cards, the texture and the poster's red from what the arms draw.

## How

Thirteen arms, 4 s each, six rounds with the order rotating by one, one JVM in a real window on top; round 1 discarded, 15 233–15 250 frames counted per run. `work` runs from the first `withFrameNanos` callback of a frame to a draw placed last in it.

## What it does not show

- **A phone.** Nothing here is the brief's target. This machine can refute and cannot confirm.
- **GPU work.** `work` is the main thread; rasterising and submitting show only as a missed frame in `interval`. The cascade's 0.60 ms is that small partly because its wipes move layers on the GPU's side.
- **A quiet machine.** Other load was not controlled: one-minute load averages of 3.4–6.9 around the runs, but 42.6 just before run 2, as a compile ended, and a system storage process near one core before run 1. Round 1 is discarded and the ruler is what bounds the rest.
- **The cascade's p95 and its restarts.** A restart is 1 frame in 30, under the percentile: over run 3's 40 restart frames `work` is 6.46 ms at p50 and 16.62 ms at most, under the line but not by much. An application prints a list far less often than this arm does.
- **Warm-up.** Round 1 is discarded, so a first roll on a cold JVM is not measured.

## Corrections

- The commit messages of `bad6a0d` and `154cfde` quote 17–38 ms for writing the image. That was a deleted scratch test sweeping `t0` over the whole of 0…1, not a roll, and left no log. The figure with a run behind it is 7.75 ms, 14.57 at p95, above. The field's cost is quoted nowhere from that test.
