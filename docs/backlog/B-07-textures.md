---
id: B-07
title: "Textures: render the three truncated tiles by a script, seamless, beside the five delivered"
status: open
priority: P1
size: M
stage: stage-1-materials
blocked_by: [B-01]
---

# B-07 — Textures: render the three truncated tiles by a script, seamless, beside the five delivered

`paper-light-2x.png`, `paper-grain-2x.png` and `concrete-light-2x.png` came back truncated from DesignSync's 256 KiB cap (research §1.1); the Demo page's paper is one of them. The owner chose on 2026-11-03 to render them here (D16).

- **The decision and its reason.** A committed script under `scripts/` renders the three tiles at 512 px from the procedural recipe the Tokens page gives (`feTurbulence` fractal noise with `stitchTiles='stitch'`, then a colour matrix), in headless Chrome so the noise is the browser's own, and writes them beside the five vendored tiles in the module's resources — never into `reference/design-system/`. Tone is matched to the delivered siblings (mean and spread per channel) and to the token colour under it. `stitchTiles` makes them seamless by construction; a test holds all eight to it anyway: no row or column, the wraps included, stands out from the median neighbour difference.
- Bundled as `ImageShader(…, TileMode.Repeated)`, 512 px = 256 dp. Opacity follows `plakat-tokens.json` (0.45 paper, 0.55 non-print), not the Demo page's 0.8 (D8).
- Rejected: asking for the files again (the owner's choice); blending or rolling a tile to hide a seam, which repeats its middle at half the period.

- AC: `make textures` regenerates the three tiles byte-identically; the seamless test passes for all eight; the paper layer is on and off in the sample; the script and its recipe are in the repository.
- Anchors: `reference/design-system/textures/paper-dark-2x.png`, `reference/design-system/textures/nonprint-mask-2x.png`, `reference/design-system/plakat-ui Текстуры.dc.html`, `reference/design-system/plakat-ui Токены.dc.html`
