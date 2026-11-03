---
id: B-04
title: "Raskat: port the JS field to the CPU, goldens from Node, the roll animation"
status: open
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
