---
id: B-07
title: "Textures: render the three truncated tiles by a script, seamless, beside the five delivered"
status: done
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

## Findings (2026-11-04)

- **Made from the delivered dark paper, not rendered from the recipe** (research D16, amended). The
  delivered `paper-dark` is its token ±1 level with fibres up to +19; the `feTurbulence` recipe
  spreads over tens of levels, so it would have been another paper. `scripts/textures.py` prints the
  dark tile's deviation on the light paper and concrete (token minus deviation, opaque) and as
  `paper-grain`'s alpha (the Tokens page's grain colour, multiply at 0.45). No browser is needed, so
  the decision above's "in headless Chrome" fell away with the recipe.
- `make textures` remakes the three and copies the five into
  `plakat-core/src/commonMain/composeResources/drawable/`; a second run is byte-identical (sha1 of all
  eight), and `--check` fails on one changed pixel. It needs Pillow, so the gate holds the tiles
  through `PlakatTextureTilesTest` instead, which reads the derivation a second time in Kotlin from
  the vendored dark paper and the tokens, and the five delivered tiles pixel for pixel.
- **Seams**: the wraps of all eight sit at 0.00–1.25 times the median neighbour difference (paper
  1.10 / 1.06, grain 1.25 / 0.91, non-print 0.30 / 1.01; the halftones' wraps are blank, 0). The test
  holds them to 1.5; the dark paper cut to 400 px (the positive control) wraps at 4.54 / 4.20. Inner
  rows and columns are not held: a fibre puts single ones at up to 4.65 times the median, and that
  is the texture.
- **Layers** (`PlakatTextures.kt`): `plakatPaper` (opaque `paper-light` / `paper-dark` by theme),
  `plakatConcrete` (`concrete-light` under «Бумага»; flat under «Чёрная бумага», for which the design
  has no tile), `plakatGrain` (multiply at `PAPER_TEXTURE_OPACITY`), `plakatNonprint` (at
  `NONPRINT_OPACITY`); each follows its switch in `PlakatTheme.print`, and off leaves the flat form.
  Tiles are laid at 256 dp; the test checks the period at densities 1 and 2.
- The sample's paper is `plakatPaper`, with a label that switches the texture; its test clicks it off
  and on and reads the paper.
