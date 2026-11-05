---
id: B-33
title: "Agit graphics: the six presets, generated compositions, rapports, halftone photomontage"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-03, B-05]
---

# B-33 — Agit graphics: the six presets, generated compositions, rapports, halftone photomontage

The Agit page delivers six hand-drawn presets with their title zones, the generator, four rapports and the photomontage rule; photos are never shown raw.

- **The decision and its reason.** Presets become `ImageVector`s generated from the SVGs (both palettes) with their zones from `presets.json`; compositions and rapports come from the port ([B-03](B-03-generator-port-and-numeric-parity.md)); `Modifier.plakatHalftone(mode)` runs the CPU dots once per source and size, and the 1-bit mask as the cheap path (D6). The kit ships the processor, not photographs; the images dropped into the design's image slots were not vendored (licence unknown).
- The skeleton loading pattern steps the rapport by one cell, 6 steps per 1200 ms; reduce motion: it stands.

- AC: each preset matches its SVG within the floor in both themes; the halftone of the design's stand-in image matches the Node output per dot centre and radius; skeleton steps at the spec's rate.
- Anchors: `reference/design-system/agit/presets.json`, `reference/design-system/agit/README.md`, `reference/design-system/plakat-gen.js`, `reference/design-system/plakat-ui Агит-графика.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatPreset` (the six, each with its `titleZone`), `PlakatPresetImage`, `PlakatGeneratedImage(seed, …, frameWidth, frameHeight)`, `PlakatRapportFill(seed)`, `PlakatSkeleton(seed)`, `Modifier.plakatHalftone(source, ink, paper, mode, pitch, angle, contrast)`. Each picture draws on paper in the theme's palette, covering the box its modifier gives it from the middle.
- **The decision changed in one point: a preset is figures with tokens, not an `ImageVector`.** A vector holds one palette, so it would have been twelve, and a dark file that drifted would be unnoticed. `scripts/presets.mjs` reads both SVGs, refuses a fill that is no token, a figure outside `rect`, `circle`, `polygon`, any other `rotate`, and a light and a dark file that are not the same figures; `--check` in `make check`. The README's rule for a new preset is so enforced where one enters.
- **Parity**: presets 0.00–0.05 % against their own SVGs, laid over the page by the references script, in both palettes; rapports 0.00 %; skeleton 0.07 %. Compositions 0.00, 0.11, 3.53, 4.37 %: the upper two have a 1 px rim on every edge, because Chrome rasters that cell at another scale and softens it (204, 52 where the fixture has 229, 27; no shift better). Geometry in that cell is held by B-03's goldens. Photomontage 4.36, 4.67, 6.23 %, the last over viddik's default: the page's canvas is 680 × 840 and the browser scales it to 340 × 420, a fixture draws at density 1. Two rasters of one screen, named, not closed.
- **Per dot** (the AC): `scripts/halftone-goldens.mjs` runs the vendored `halftone` in Node with its stubs in the script (the stand-in worked out per pixel from `standIn`'s stops, the `arc`s recorded), and `PlakatHalftoneTest` holds every dot of three small frames and a sample and the sums of three page frames, to 1e-9, and the mask bit for bit. Green on the first run.
- **A defect found by a test**: a default-size picture drew nothing. A `Spacer` takes its size only from a fixed constraint, so under `defaultMinSize` it is laid out at the minimum and drawn at zero. A `Box` here. `PlakatStretch` is a `Spacer` too, sized by its modifier by the design's word; a minimum alone would draw nothing there either.
- **Left out on purpose**: the `gray` mode, stage 2 on the page, which is a step shown in the lab and no output the rule allows («never shown raw» holds at grey too); the AGSL shader, D6 until a measurement asks; dropped photographs: none vendored, the kit ships the processor. The skeleton is the pattern alone, as the bars beside it on the page are an application's list row.
- **The page's skeleton has a jump**, and so has this: a shift of 0…20 by 4 every 200 ms, then back to 0, while the tile's period is 96 or more, so the pattern jumps back by a cell each 1200 ms. The page's own `skelPos`, kept. Under reduced motion it stands at 0.
- **Designed here** (D13), for the owner's review: covering from the middle rather than stretching, for a box of another shape; a picture with no words unless given them; `PlakatSkeleton` announcing itself as indeterminate progress; a halftone's `.35` px least radius is the design's at its own 2× canvas, so at density 1 faint dots a pixel across fall out sooner.
- **Mutants**: through the tests — radius 0.7, no root, one pixel for five, a least radius of .5, other weights, contrast from zero, the threshold, the whole source squeezed in, no cache, never worked out anew, the mask's colours crossed, four steps, a cycle of 2400, moving under reduced motion, a spacer again, no cover, no paper, no shift. Through the goldens — a wrong pivot, seven cells. Through the generator's check and the goldens — a preset edited by hand. All twenty killed. «Squeezed in» survived the first round and got its test.
