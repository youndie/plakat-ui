---
id: B-04
title: "Raskat: port the JS field to the CPU, goldens from Node, the roll animation"
status: done
priority: P0
size: M
stage: stage-0-risks
blocked_by: [B-01]
---

# B-04 — Raskat: port the JS field to the CPU, goldens from Node, the roll animation

The design's raskat pixels come from `plakat-raskat.js`; the `.kt` and AGSL sketches draw a different noise (research §1.3).

- **The decision and its reason.** Port `hash`, `vnoise`, `raskatField` and the per-pixel decision `p > m ? B : A` exactly; the field is cached per (size, angle, grain, seed, mode, streak, rough, density) and `roll` animates only `t0` from 1.15 to the target over 320 ms hard-stop (D5). Three textures (grain with streak, dots, lines). Off by token → flat ink A.
- Goldens from Node: `T` and `M` for a fixed set of shapes, and the final colour per pixel with `raskatImage` run against a minimal canvas stub in the harness (the stub lives in the harness and says why).
- Density is an input: the JS renders at `dpr = 2` and scales the grain by it (research §3, risk 2).
- Rejected: AGSL — different noise, no common runtime shader, Android 33+.

- AC: the port reproduces every golden pixel for the Tokens page's circle, the Raskat page's three textures and the poster's circle; the roll's frames at 0, 60, 150, 320 ms match the page's four stills; a mutant in the hash is caught.
- Anchors: `reference/design-system/plakat-raskat.js`, `reference/design-system/plakat-raskat.kt`, `reference/design-system/plakat-ui Раскат.dc.html`

## Findings

- 2026-11-03. `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/print/PlakatRaskat.kt` ports
  `hash`, `vnoise`, `raskatField` (the three textures, streak, rough), the per-pixel decision and
  `roll`'s own bisection hard-stop; the field is kept in `FloatArray`, as the JS keeps it in a
  `Float32Array`. `RaskatImage.kt` writes the two-ink image with one batch of 1 px unantialiased
  points (common Compose has no pixel write; no platform code) and prints it into an outline
  unscaled and unsmoothed.
- Goldens from `scripts/raskat-goldens.mjs`: the vendored JS run untouched, with a canvas and a
  clock stubbed in the harness. Eleven cases — the Tokens page's circle, the poster's circle, the
  three textures, the four roll stills, the splash wedge at 73°, the onboarding rect in ochre — as
  row hashes of their ink maps; the first case's field as the bits of every float; `roll` to 0.58 at
  every millisecond 0–320 as the bits of every double. All equal on the desktop JVM, the iOS
  simulator and wasm: no `exp`, `cos` or `sin` difference between the platforms reached a pixel.
  The goldens' freshness is in `make check`.
- The design's `roll` stops 8·10⁻⁸ short of both ends (1.15 at 0 ms, the seam at 320 ms): 24
  bisections do not reach 0 or 1. The port keeps that; the stills on the Raskat page are labelled
  0 / 60 / 150 / 320 ms but drawn at t0 = 1.15, 0.9, 0.7, 0.55, so they are held as fixed-t0 cases,
  not as frames of `roll`.
- Mutants: the hash's multiplier off by one, the streak dropped, the points 2 px wide — killed.
  `jsHypot` replaced by `kotlin.math.hypot` survives: on every case the design draws the two agree,
  so V8's algorithm is kept for faithfulness and the mutant is equivalent here.
- Not covered here: frame cost of the per-pixel comparison (H3, [B-51](B-51-frame-cost-on-desktop.md));
  the poster's use of it ([B-19](B-19-poster.md)).
