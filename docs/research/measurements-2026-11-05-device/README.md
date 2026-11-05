# Frame cost on a Pixel 6a (B-06)

| | |
|---|---|
| Device | Google Pixel 6a, Android 17, API 37, `google/bluejay/bluejay:17/CP2A.260705.006/15641320:user/release-keys`; 1080 × 2400 at 60 Hz, density 2.625, 8 cores |
| Build | release, R8 on, not debuggable, debug key, `cmd package compile -m speed-profile -f` |
| Protocol | [B-06](../../backlog/B-06-frame-cost-measurement.md), committed at `dcabec9` before the first number, amended twice after run 1 (below) |
| Harness | `FrameBenchmark` unchanged, in `bench-android/`; `scripts/device-bench.sh`; reader `scripts/frame-report.py`, its control in `make check` |
| Commit in the logs | `9bd740a` run 1, `5aaef41` `blind1`, `1277b84` `blind2`; `1277b84-dirty` run 2 and projection, the APK being built at `1277b84` and only logs uncommitted in the tree. After the runs the host's `minSdk` went to 31 and its branches below that came out, for lint; nothing it reads changed |
| Same host, same day | yes for everything here. **The Mac's numbers in [B-51](../measurements-2026-11-05/README.md) are another host and another instrument** |

## Result

| Question | Verdict | Number | Line |
|---|---|---|---|
| Ten cards cascading, textured and registered | **GREEN** | `work` p95 1.19 ms, main thread p95 1.34 ms, 0.0 % of intervals ≥ 25 ms, 0.2 % of frames past their deadline | 16.7 ms, 5 %, 5 % |
| Raskat frame at density 1 | **GREEN** | main thread 1.10 ms over its base | 2 ms |
| Raskat frame at density 2 | **RED** | main thread 3.32 ms over its base | 2 ms |
| Raskat frame at density 2.75 (H3's) | **RED** | main thread 3.73 ms over its base | 2 ms |
| The roll on time, at each density | **GREEN** | 0.0 % of frames past their deadline, 0.0 % of intervals ≥ 25 ms | 5 % |
| The projection on Android (H4's half) | **GREEN** | draws, `raw/h4-*.png`; first frame 91 ms for the horn, 104 ms for the box, p50 | none set |

**Which raskat verdict stands is the owner's.** The protocol as first committed names `work`, on which all three read GREEN: 0.14, 0.06 and −0.05 ms (`raw/run2/report.txt`; 0.15, 0.11 and −0.06 in run 1). That instrument is shown blind here to what it would have to see, below, so the table gives the amended reading. Nothing moved the 2 ms.

**H3's sentence as written, about the comparison alone, is not measured here**: only the whole frame is, as the comparison is not separated on the device. **The frame's 2 ms is refuted on the phone at 2 and 2.75, and the roll misses no frame**: over its budget and on time are both true, as on the Mac.

So nothing here forces a change. The cascade stands with the texture on, so the brief's fallback does not apply. The roll takes about a quarter of the 16.7 ms budget at the phone's own density and fits.

## The recorder was blind, and how that was found

Run 1 read a roll's frame at −0.06 ms over its base, where an M1 reads 8.26. Every control of the protocol passed. A frame cannot cost nothing, and the lever was on, `raw/lever-roll-2.75.png`. So the instrument itself was tested, with a known cost:

| Arm, `raw/blind2/` | `work` p50 | `draw` p50 | Over `plain` | The recorder |
|---|---|---|---|---|
| `plain` | 0.92 | 0.42 | | |
| `spin-5`, 5 ms in a layer under the recorder's own parent | 5.59 | 5.25 | +4.67, +4.82 | sees it |
| `nested-5`, the same 5 ms in a layer inside a layer nothing invalidates | 0.61 | 5.27 | −0.31, +4.85 | **does not** |

The first explanation, that any layer is drawn past the recorder's stop, was wrong (`raw/blind1/`, amendment 2): one variable, the nesting, moves the signature. A layer inside a layer that is not redrawn falls past where `work` ends, and the roll's cost behaves the same way: in `draw` and not in `work`. Android's own source was not read for why. `work`'s controls could not see this, as their busy-wait runs in the frame callback.

So on this device the protocol's raskat reading is taken on `FrameMetrics`' `animation` + `layout` + `draw`. Whether `work` is blind in the same way on the desktop was not tested, and its 8.26 ms there says it did see the roll.

## Run 2

Main thread and the rest are per frame, in ms, by `FrameMetrics`. Roll frames with one in 21 off the top, as restarts cannot be tagged in this log: with them left in, 2.46, 5.24 and 7.47 ms over base, and density 1 would read RED too.

| arm | frames | work p50 | work p95 | interval p50 | interval p95 | intervals >= 25 ms |
|---|---|---|---|---|---|---|
| base-1 | 1210 | 0.49 | 0.71 | 16.62 | 17.51 | 0.0 % |
| base-2 | 1213 | 0.50 | 0.74 | 16.61 | 17.41 | 0.1 % |
| base-2.75 | 1211 | 0.51 | 0.73 | 16.62 | 17.38 | 0.0 % |
| busy-10 | 1207 | 10.21 | 10.35 | 16.61 | 17.13 | 0.0 % |
| busy-25 | 605 | 25.21 | 25.37 | 33.23 | 33.83 | 100.0 % |
| cascade-a | 1207 | 0.66 | 1.19 | 16.61 | 17.55 | 0.0 % |
| cascade-b | 1210 | 0.67 | 1.27 | 16.61 | 17.55 | 0.0 % |
| cascade-flat | 1208 | 0.67 | 1.26 | 16.61 | 17.51 | 0.1 % |
| idle-a | 1206 | 0.70 | 1.52 | 16.61 | 17.47 | 0.0 % |
| idle-b | 1210 | 0.76 | 1.60 | 16.61 | 17.35 | 0.0 % |
| roll-1 | 1209 | 0.58 | 1.09 | 16.62 | 17.31 | 0.0 % |
| roll-2 | 1209 | 0.56 | 1.13 | 16.62 | 17.37 | 0.0 % |
| roll-2.75 | 1210 | 0.50 | 0.85 | 16.62 | 17.29 | 0.0 % |

| arm | frames | main p50 | main p95 | draw p50 | draw p95 | total p50 | total p95 | GPU p50 | GPU p95 | past % |
|---|---|---|---|---|---|---|---|---|---|---|
| base-1 | 1220 | 0.56 | 0.82 | 0.16 | 0.26 | 12.19 | 12.93 | 7.20 | 8.76 | 0.0 |
| base-2 | 1218 | 0.57 | 0.85 | 0.17 | 0.29 | 12.43 | 29.31 | 7.00 | 8.74 | 0.4 |
| base-2.75 | 1211 | 0.58 | 0.84 | 0.17 | 0.27 | 12.65 | 13.41 | 7.35 | 9.04 | 0.4 |
| busy-10 | 1215 | 10.24 | 10.41 | 0.08 | 0.15 | 12.46 | 13.87 | 0.46 | 0.75 | 0.0 |
| busy-25 | 605 | 25.25 | 25.45 | 0.09 | 0.17 | 27.44 | 28.62 | 0.52 | 0.77 | 99.2 |
| cascade-a | 1210 | 0.72 | 1.34 | 0.15 | 0.31 | 5.82 | 12.55 | 0.93 | 8.03 | 0.2 |
| cascade-b | 1214 | 0.74 | 1.45 | 0.15 | 0.33 | 11.83 | 12.74 | 6.46 | 8.33 | 0.1 |
| cascade-flat | 1212 | 0.74 | 1.44 | 0.12 | 0.29 | 11.69 | 12.64 | 6.37 | 8.32 | 0.1 |
| idle-a | 1213 | 0.81 | 1.76 | 0.27 | 0.61 | 11.90 | 12.74 | 5.45 | 8.03 | 0.0 |
| idle-b | 1220 | 0.89 | 1.83 | 0.30 | 0.69 | 12.03 | 12.94 | 4.50 | 7.22 | 0.0 |
| roll-1 | 1215 | 1.31 | 3.28 | 0.78 | 1.50 | 12.19 | 13.08 | 7.07 | 8.54 | 0.0 |
| roll-2 | 1213 | 2.88 | 6.09 | 2.34 | 3.65 | 12.34 | 13.71 | 6.24 | 8.16 | 0.0 |
| roll-2.75 | 1215 | 3.80 | 8.31 | 3.29 | 4.10 | 12.59 | 14.26 | 6.15 | 7.87 | 0.0 |

Thermal status 0 at each round and at the end. Controls: `busy-10` 9.51 ms above `idle-a` (10 ± 2); `busy-25`'s interval p50 33.23 ms, 99.2 % past its deadline; `idle-a` 0.0 % past. Ruler: `cascade-a` against `cascade-b`, widest per-round p95 difference 0.41 ms. Known order: `cascade-flat` at 1.26 against `cascade-a` at 1.19, within the ruler. Lever: `raw/lever-cascade-a.png`, `raw/lever-roll-2.75.png`. Run 1, `raw/run1/report.txt`, agrees on `work`: 1.37 ms for the cascade.

## The projection on Android

`raw/h4-horn.png` and `raw/h4-box.png`: both forms draw as their desktop goldens do, the mesh, the fold and the letters, with no crash, so H4's Android half holds. **Its first frame costs 91 ms for the horn and 104 ms for the box at p50** (95 and 110 at p95; `raw/projection/projection.csv`, 41 restarts of each after 20 warm-up, one more than the protocol's 40 by how the loop ends), on the main thread: five or six frames at 60 Hz, once, whenever a projection comes into a composition. No line was set for it.

## What it does not show

- **A cold start.** Round 0 is discarded and the app is compiled by profile: a first roll after installing is not measured.
- **A strict deadline.** Android gives these frames a deadline of 33 ms at the median, so «past it» is a generous reading; intervals ≥ 25 ms, also 0.0 %, say the same more strictly.
- **Arms by their neighbours.** The order rotates by one, so an arm always follows the same other: `cascade-a` follows `busy-25` and its GPU p50 reads 0.93 ms against `cascade-b`'s 6.46, with p95 close, at 8.03 and 8.33. The GPU's clock settling is a candidate, untested. The verdicts are not read on GPU time.
- **The other half of H3's frame on the Mac.** B-51 left about 6 ms of its 8.26 unexplained. That the phone's frame is smaller than the Mac's is consistent with the Mac rasterising on its main thread and the phone on its render thread, and this was not tested.
- **The screen's other work.** The phone was left alone and on USB power, charging from 12 to 22 % over the runs, reading 30.7 to 33.8 °C by `dumpsys battery`, which is not in the raw logs. Its background apps were not controlled.
- **Other phones.** One unit of one model, on Android 17.
