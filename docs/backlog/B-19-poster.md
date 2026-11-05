---
id: B-19
title: "Плакат: Круг, Баннер, Фон — generator, raskat roll, diagonal title"
status: done
priority: P1
size: L
stage: stage-2-components
epic: feature-components
blocked_by: [B-03, B-04, B-09, B-10, B-12, B-13]
---

# B-19 — Плакат: Круг, Баннер, Фон — generator, raskat roll, diagonal title

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `print / roll`.

- **What to build.** `PlakatPoster` in its three layouts (Круг 390 × 392, Баннер 390 × 160, Фон 390 × 500): a Display title rotated to −17° as a block, a composition from the generator seeded by content ([B-03](B-03-generator-port-and-numeric-parity.md)), at most one raskat shape ([B-04](B-04-raskat-port.md)), one slogan button. Appear: shapes wipe in from the left with a 30 ms cascade, then title and button (≤ 300 linear); the raskat rolls (320 hard-stop, reduce motion: final state at once). A new seed reprints without animation.
- Desktop layout of the poster is part of this item if the design draws it ([B-39](B-39-desktop-and-hover.md)).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatPoster(title, action, onAction, modifier, layout, body, seed, raskat, print, photo)`,
  with `PlakatPosterLayout.Circle | Background | Banner` at the page's fixed sizes and
  `plakatPosterSeed(title)` as the default seed. The slogan is the kit's filled `PlakatButton` (an
  internal label size gives Круг its 16 px word). Баннер has no body, as the page draws none; `raskat`
  applies to Круг's circle.
- **The generator is the page's own.** The item names the generator of [B-03](B-03-generator-port-and-numeric-parity.md),
  but the Core page draws its posters (and the card thumbnails) with `comp(seed, W, H, pal, safe, opt)`
  in its script — a gray plate, a circle, a bar, a wedge, maybe a dot, with safe zones and a circle
  reserved for the photo. To be the page's posters at all, the poster ports `comp`
  (`agit/PlakatPosterComposition.kt`); `scripts/poster-goldens.mjs` cuts `rng` and `comp` out of the
  vendored page untouched, runs them in Node and writes goldens for 2000 seeds of each of its four
  calls. The port equals them on the desktop JVM, the iOS simulator and wasm in Chrome. `plakat-gen.js`
  stays the Agit page's ([B-33](B-33-agit-presets-and-photomontage.md)). The Card's thumbnail fixture now
  uses this port; its goldens did not move.
- **The photo** is a slot. The page has no photograph: a CSS stand-in (a radial gradient and a 45°
  4 px dot grid, `plus-lighter`, in a `contrast(30)` group multiplied over the red or the concrete).
  The fixtures evaluate that chain per pixel (`parity/PagePhoto.kt`), in sRGB — in linearRGB the
  circle's residual grew from 284 to 1302 px. The kit's halftone processor is B-33's.
- **The «Круг · раскат» cell is not a reference.** It is a canvas the page draws by hand: no
  composition, a concrete block instead, the title 10 px lower (a canvas baseline at 100 + 40 against
  the DOM's line box at 92), body lines 20 apart instead of 22.4, at 2× and downscaled. Matching it would
  fork the layout. The raskat is held instead by the roll test and by its own golden
  (`Poster_CircleRaskat_*`), and the field by B-04's numeric goldens; in the seed-47 Круг the circle
  hangs half off the poster, so its seam shows near the edge.
- **Parity** (`Poster_*`, texture off as the references are taken):

  | Cell | Whole cell | Outside the text and photo | Text, of its ink |
  |---|---|---|---|
  | Круг L / D | 2.62 / 2.62 % | 0 px | title 2000 px; body 42 %; slogan 36 % |
  | Фон L / D | 2.76 / 2.00 % | 0 px | body 34 %; slogan 41 % (the title is read with the halftone under it) |
  | Баннер L / D | 1.14 / 1.16 % | 6 px | title 8 %; slogan 40 % |

  No whole-pixel shift lowers any text region. Круг's photo circle differs by 284 px on dot edges:
  Chrome samples the turned dot tile, the fixture evaluates it exactly, and `contrast(30)` magnifies
  the difference.
- **Text on Blink's baselines.** First run 2.2–4.2 %: the body a pixel low, the turned title blurred
  across its edges. Two causes, both measured. Skia lays a paragraph's fractional lines on whole
  pixels (B-17's finding). A layout's `firstBaseline` is not where Compose draws: Golos 16 reports
  15.48 and is drawn at 16; drawn at its top plus the reported value, the glyphs landed a third of a
  pixel off. Chrome's own line-box baselines, probed with the repository's fonts (16 px at 1.4:
  17, then 39.39; 48 px Rubik at .95: 37, then 82.59), agree with `cssLine`'s formula. So each line is
  a one-line `cssLine` layout of `normal` height, drawn at the computed baseline less the rounded
  ascent: snapped to a pixel upright, kept fractional at −17°. Result: 1.1–2.8 %.
- **Motion**: one clock of elapsed ms; every wipe is `clamp((age − start) / 120)` over the whole
  poster's width (the page's `inset(0 100% 0 0)` on full-size wrappers). The start is 30 ms per element
  in the page's order, the circle last, the title and body at 150, and the slogan at 180. The roll is
  B-04's `raskatRollT0(age, .58)`. Reduced motion jumps to the end; `print = false` starts there. A new
  seed recomposes the composition at the same age, so it reprints at once.
- **Designed here** (D13): the body is wiped with the title (the lab's poster has no body); Фон's plate and
  photo print as element 0 before its shapes; Фон and Баннер take Круг's timings (the lab shows Круг only).
- **Events**: `print` on appear, and `roll` with it when a raskat is drawn.
- **Mutants**: through the behaviour tests — no cascade, the circle at 0, the slogan at 150, wipes of
  200, the roll to .5, the roll at hardness 6, the roll linear, reduced motion animating, a new seed
  reprinting; through the composition goldens — the red slot always picked, `strict` ignored. Through the
  poster goldens — the title 2 lower, the stripes 8 apart, the plate's right edge at 310, Круг's word at
  14, the banner's window at 216, the body unsnapped, no multiply in the light Фон, the stripes'
  padding 8. Twenty killed. `s8` with half-to-even rounding survived and is **equivalent**: none of
  `comp`'s calls can reach `v / 8 = k + .5` with an even `k` (its halves are 12 / 8 at the banner's dot
  size, where both roundings give 16); the floor form is kept as the page's `Math.round`.
