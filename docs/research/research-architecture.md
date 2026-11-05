---
id: research-architecture
title: plakat-ui — architecture research
type: research
status: active
date: 2026-11-03
---

# Research: the architecture of plakat-ui

plakat-ui is a Compose Multiplatform component kit in the look of the Soviet avant-garde and
constructivism of 1919–1932: big upper-case type, red and black on paper, one diagonal axis, no
decoration, motion that clicks and rides on rails. It is the third kit of the line after oldge-ui
and toon-ui and keeps their contract: Material 3's inventory and API expectations, redrawn in its
own aesthetic, with no Material dependency. Where toon-ui springs and squashes, everything here
snaps to a hard stop (brief, «Что это и зачем»).

This document records **verified facts** (what was actually read or run in the vendored design and
in the artefacts it depends on), **decisions taken**, and **risks**. Anything unverified is marked
as a hypothesis and names the backlog item that settles it.

This is the entry point of the documentation. Nothing is built yet: every path into code below is
a path into the frozen design, `reference/design-system/`, or into an external artefact written as
an address.

---

## 1. Verified facts

### 1.1 What was imported, and what was not

Imported on 2026-11-03 from Claude Design project `d99820b8-b707-4cf2-83fc-4ba96e771c30` with
DesignSync (`get_project`, `list_files`, `get_file`). The project is `PROJECT_TYPE_PROJECT`, not a
Design System project, and holds 130 paths.

| Fact | Where verified |
|---|---|
| The chosen direction is 3a «Сборка» (2a's poster, 2b's cards, 2c's rapport strip under the header); tokens are version 0.2.0 of 2026-11-03 | `reference/design-system/plakat-tokens.json` (`$meta`), the Направления page |
| 11 spec pages, 3 generators in JS with a Kotlin sketch each, tokens, icons JSON, 6 presets and 4 rapports in light and dark SVG, 5 of 8 texture tiles | `reference/design-system/README.md` |
| `paper-light-2x.png`, `paper-grain-2x.png`, `concrete-light-2x.png` came back truncated at 262 144 bytes with no `IEND` chunk; the other five PNGs end in `IEND` | the extraction run, `reference/design-system/textures/` |
| The 16 icon SVGs were not fetched: each carries an ≈8 KB C2PA manifest, and their paths are the strings in `plakat-icons.json` | `reference/design-system/plakat-icons.json` |
| The four vendored rapports are exactly `rapport(year, 24)` of the generator for seeds 1923, 1925, 1927, 1929, element for element, in both palettes | a Node run comparing `G.rapport(seed, 24)` through `toSVG` with `reference/design-system/agit/rapport-1923.svg` and the other seven files |
| The six presets are hand-drawn, not generated: `presets.json` lists them with a fixed title zone 16, 16, 216 × 88 | `reference/design-system/agit/presets.json`, `reference/design-system/agit/README.md` |

**Consequence 1.** Three of the eight textures, among them the Demo page's paper
(`paper-grain-2x.png`), have to be made here: the owner chose on 2026-11-03 to render them by a
script in the repository rather than fetch them again (D16, [B-07](../backlog/B-07-textures.md)).
Nothing else that the code needs is missing.

**Consequence 2.** The rapports need no SVG import: generating them is the test that the generator
port is right ([B-03](../backlog/B-03-generator-port-and-numeric-parity.md)).

### 1.2 What kind of design this is

`cmp-ui-library` splits designs into CSS design systems (pixels only) and executable designs
(numbers and pixels). This one is both, by part.

| Fact | Where verified |
|---|---|
| There is no per-component file and no component engine. Every component is inline HTML with `style=""` attributes inside a spec page, driven by a `DCLogic` class in the same page | `reference/design-system/plakat-ui Ядро.dc.html`, `reference/design-system/plakat-ui Компоненты.dc.html` |
| The generator, the rapport, the raskat field and the projection meshes are pure functions: `generate`, `rapport`, `raskatField`, `horn` and the other shapes run in Node 24 with no DOM; `halftone`, `standIn`, `raskatImage` and `textSource` need `document` | a Node run importing `reference/design-system/plakat-gen.js`, `reference/design-system/plakat-raskat.js`, `reference/design-system/plakat-warp.js` |
| `generate(seed)` is deterministic: two runs give byte-identical JSON; for seeds 1…2000 every composition has exactly one red element | the same Node run |
| The pages need the network to render: `support.js` loads React 18.3.1, ReactDOM 18.3.1 and Babel standalone 7.29.0 from unpkg, and every page loads its fonts from Google Fonts | `reference/design-system/support.js`, the `<helmet>` of each page |

**Consequence 3.** Two parity regimes. Components are checked by pixels against references the
repository renders from the spec pages in headless Chrome (D3). The generator, the rapport and the
raskat field are checked by numbers against goldens the vendored JS emits in Node (D4, D5).

**Consequence 4.** The reference renderer must serve the fonts from the repository, not from Google
(the families must be the files the Compose fixtures use), and must either reach unpkg or vendor the
three runtime scripts beside itself ([B-05](../backlog/B-05-pixel-references-and-floor.md)).

### 1.3 The designer's Kotlin sketches

Each generator comes with a `.kt` "reference for the implementation". They were read and, for the
generator, run.

| Fact | Where verified |
|---|---|
| `plakat-gen.kt` compiled with `kotlinc` on the JVM gives, for seeds 1…500 and the four rapports, the same elements, red choice and shapes as the JS (to 0.01) — 504 of 504 lines identical | a JVM probe diffed against a Node probe, `reference/design-system/plakat-gen.kt` |
| It is JVM-only: `snap` calls `Math.round`, which is `java.lang.Math`. Kotlin's common `kotlin.math.round` rounds half to even, unlike JS `Math.round` (half up); `v / 8` lands on .5 for every v ≡ 4 (mod 8) | `reference/design-system/plakat-gen.kt` (`private fun snap`) |
| `plakat-raskat.kt` is **not** a port of `plakat-raskat.js`. The JS hashes integer pixel coordinates with `Math.imul`; the AGSL and the CPU sketch use `fract(sin(dot(p, …)) * 43758.5453)`. The CPU sketch has only the grain texture, no `streak`, and samples noise at `(i / grain, j / grain)` instead of along/across the transition | `reference/design-system/plakat-raskat.js` (`hash`, `raskatField`), `reference/design-system/plakat-raskat.kt` (`RASKAT_AGSL`, `raskatPixels`) |
| `plakat-warp.kt` draws through `c.nativeCanvas.drawVertices(...)` with an Android `Paint` — Android-only. Compose's own `Canvas.drawVertices(Vertices, BlendMode, Paint)` and `Vertices` exist in CMP 1.12.1 | `reference/design-system/plakat-warp.kt`, `org.jetbrains.compose.ui:ui-graphics-desktop:1.12.1!/androidx/compose/ui/graphics/Canvas.class`, `org.jetbrains.compose.ui:ui-graphics-desktop:1.12.1!/androidx/compose/ui/graphics/Vertices.class` |
| The halftone shader is AGSL for Android 33+ "and the same text through Skia RuntimeEffect" elsewhere; the CPU path is `halftoneDots`/`halftoneMask` | `reference/design-system/plakat-gen.kt` (`HALFTONE_AGSL`) |

**Consequence 5.** The generator sketch is a correct oracle on the JVM and a wrong one in
`commonMain` unless `snap` is rewritten as `floor(v / 8 + 0.5) * 8`. The pixels the design shows for
raskat come from the JS, so a port of the `.kt` sketch would draw a different grain on every screen.
Numbers come from the JS; the sketches are read for intent only.

### 1.4 Fonts

Verified against the files at `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc`, read with
fontTools.

| Fact | Where verified |
|---|---|
| Rubik Mono One 2.000: OFL 1.1 in the name table, static, 141 088 bytes (the Tokens page says ≈ 120 KB), monospace — every glyph advance 850/1000 — UPM 1000, cap height 700, x-height 520, ascender 932, descender −306 | `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc!/ofl/rubikmonoone/RubikMonoOne-Regular.ttf` |
| Golos Text 2.004: OFL 1.1, **variable**, one axis `wght` 400…900, default 400, 184 292 bytes; cap height 700, x-height 530 | `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc!/ofl/golostext/GolosText[wght].ttf` |
| Neither licence declares a Reserved Font Name | `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc!/ofl/rubikmonoone/OFL.txt`, `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc!/ofl/golostext/OFL.txt` |
| Both cover А–я, Ё, ё in full | the same files' `cmap` |
| Golos Text keeps one advance across weights: `Н` is 760/1000 at 400 and at 600, so a weight change moves no layout | `plakat-core/src/desktopTest/kotlin/io/github/youndie/plakat/type/PlakatFontMetricsTest.kt` (B-02) |
| Of the non-ASCII characters the pages use, Golos lacks `■`, `●` and `₀`, Rubik Mono One lacks `■`, `●`, `→`, `↗`, `↑`. In the kit's own components only the Счётчик legends use them: «■ = 5 статей», «● = день, красный — сегодня» | the same files' `cmap` against the text of every `*.dc.html`; `reference/design-system/plakat-ui Компоненты.dc.html` |
| The kit's pages use Golos at 400 and 600 and Rubik Mono One only; Oswald, Unbounded and weights 500/700/800/900 appear only on the Направления page (the rejected treatments) | a count of `font-family` and `font-weight` over every page |

**Consequence 6.** Cyrillic is not a problem this time, unlike toon-ui. The variable Golos is: a
`wght` setting needs Android API 26, and under it the file's default instance (400) draws, so
Title and Label (600) would fall back to Regular at `minSdk 24`. Static 400 and 600 instances are cut
from the variable file (no Reserved Font Name, so the name may stay) (D7, [B-02](../backlog/B-02-fonts.md)).

**Consequence 7.** The Счётчик legend glyphs `■` and `●` are the pictograms themselves; drawn as
text they would come from whatever face the host falls back to. The legend draws them as shapes
([B-30](../backlog/B-30-counter.md)).

### 1.5 Tokens and contrast

| Fact | Where verified |
|---|---|
| Five inks and two papers per theme; the dark theme changes `paper`, `concrete`, `ink`, `ink-muted` and keeps `red`, `ocher`, `slate`; `on-red` is paper cream in both | `reference/design-system/plakat-tokens.json` (`plakat/color`) |
| Radius 0; axis −17°; the cut corner 46 × 14 (= 17°); lines 1 / 3 / 8; grid 8, margin 16; sizes button 48, field 56, chip 32, touch 48, checkbox 24, switch 56 with a 20 × 32 carriage | `reference/design-system/plakat-tokens.json` (`plakat/shape`, `plakat/line`, `plakat/size`) |
| Durations snap 60, quick 120, normal 200, scene 320, cascade 30, screen-max 400; hard-stop = `cubic-bezier(0.2, 0.8, 0.9, 0.9)`; stepped = `steps(n)` | `reference/design-system/plakat-tokens.json` (`plakat/motion`) |
| WCAG ratios, computed: ink/paper 13.72 (the page says 15.4), dark ink/paper 13.42 (says 14.2), ink-muted/paper 5.63, dark ink-muted/paper 6.10 (says 6.6), red/paper 4.64, on-red/red 4.64, slate/paper text 5.06, ink/ocher 5.25, red text on dark paper 2.89 | `reference/design-system/plakat-ui Токены.dc.html` against a computation from the hex values |
| **ink-muted on concrete is 4.5005:1** — at the limit; red text on concrete is 3.71, ink on red 2.95 | the same computation |
| The texture opacity disagrees between sources: tokens 0.45 (paper) and 0.55 (non-print); the Tokens page's page overlay 0.35; the Demo page 0.8 multiply (light) and 0.25 screen (dark) | `reference/design-system/plakat-tokens.json` (`plakat/print`), `reference/design-system/plakat-ui Демо.dc.html` (`TH`) |
| `registration.offset` is `[1, 1]` with no unit; the brief says "1 px"; the pages draw it as CSS `1px` | `reference/design-system/plakat-tokens.json`, the brief «Токены» |

**Consequence 8.** Every ratio the design claims holds, though two are overstated. The one that does
not have room is ink-muted on concrete: any darkening of concrete — the paper texture multiplied
over a card — takes muted text below 4.5 ([B-36](../backlog/B-36-accessibility.md)).

**Consequence 9.** The tokens file is the source, the pages are one renderer's evidence. Where they
disagree (texture opacity) the item says which it follows and why (D8).

### 1.6 Coverage of the brief

| Fact | Where verified |
|---|---|
| The core page draws Button, TextField, Checkbox, Switch, Card, Snackbar and Плакат in default, pressed, focused, disabled, error, loading and selected where they apply, and a motion spec row per animation with its reduce-motion word | `reference/design-system/plakat-ui Ядро.dc.html` (`const spec`) |
| The MVP page covers IconButton, FAB, Chip, Badge, Divider, Progress, TopAppBar, NavigationBar, Tabs, List item, Dialog, Окно, Счётчик and the screen wipe, each with a spec row | `reference/design-system/plakat-ui Компоненты.dc.html` (`const spec`) |
| Плакат has three layouts: Круг 390 × 392, Баннер 390 × 160, Фон 390 × 500 | `reference/design-system/plakat-ui Ядро.dc.html` |
| The Stretch page's «Где применяем» draws a fourth, Растяжка 390 × 600, and a masthead for the top app bar, 390 × 195, which no other page has (B-53) | `reference/design-system/plakat-ui Растяжка.dc.html` |
| The core page draws its posters and card thumbnails with a generator of its own, `comp(seed, W, H, pal, safe, opt)` in its script, not with `plakat-gen.js`: a gray plate, a circle, a bar, a wedge and maybe a dot, with safe zones and a circle kept for the photo (B-19) | `reference/design-system/plakat-ui Ядро.dc.html` (`comp`, `renderVals`), `scripts/poster-goldens.mjs` |
| The MVP page's top app bar strip is its own `raport(seed, cols, rows, cell, pal)`, not `plakat-gen.js`'s `rapport`, whose red motif is chosen differently (B-25) | `reference/design-system/plakat-ui Компоненты.dc.html`, `scripts/poster-goldens.mjs` |
| The MVP page's onboarding window draws its pictures with a third generator of its own, also called `comp`, with seeds `300 + i × 17` in a frame of 171 × 160, not the core page's `comp` (B-29) | `reference/design-system/plakat-ui Компоненты.dc.html`, `plakat-core/src/desktopTest/kotlin/io/github/youndie/plakat/parity/WindowFixtures.kt` |
| The Stretch page asks for its font on mount, before its stylesheet is in, gets an answer at once and keeps its lines measured in the fallback face. The references are rendered with the faces added before the page's first script, and a Latin line typed into the sandbox measures right either way (B-31) | `reference/design-system/plakat-ui Растяжка.dc.html` (`componentDidMount`, `m`), `scripts/design-references.mjs` |
| The page's photo is a stand-in made of CSS: a radial gradient and a 45° dot grid in a `contrast(30)` group multiplied over red or concrete; its «Круг · раскат» cell is a canvas drawn by hand, with its own title position and line pitch | the same page |
| **RadioButton, an MVP component in the brief, is not drawn anywhere** | a search of every page for radio and its Russian names |
| No v1 component is designed as a component: Slider is the worked example of the Principles page; Segmented appears once inside the Settings demo; Menu, BottomSheet, Drawer, Tooltip and Pull-to-refresh do not appear | the same search, `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/plakat-ui Демо.dc.html` |
| Hover states are not drawn (only link hover on the pages); the brief left desktop and hover as an open question | the same search |

**Consequence 10.** The kit has a design for 21 of the brief's 22 MVP rows. RadioButton, the v1 set
and hover are designed in this repository from the brief and the Principles recipe (D13).

### 1.7 The parity floor (B-05)

Measured on 2026-11-03 with `scripts/design-references.mjs` (Chrome 156.0.8078.80, device scale 1,
viewport 1584 px) against fixtures in `plakat-core/src/desktopTest/kotlin/io/github/youndie/plakat/parity/FloorFixtures.kt`,
read by viddik 0.7.0's `viddikDesignParity` (±16 per channel) and by ink parity (differing pixels
over the pixels either image inks).

| Fact | Where verified |
|---|---|
| **Shape floor 0.00 %** in both themes: the MVP page's three Divider rules (1, 3, 8 px) match pixel for pixel | `plakat-core/src/desktopTest/snapshots/design/manifest.json`, the parity report |
| **Text floor: 7.05 % (light) and 6.99 % (dark) of a 224 × 15 caption cell, 63.7 % of its ink** — 12 px Golos, one line | the same |
| Baselines agree: no whole-pixel shift in x or y lowers the difference; total ink agrees within 1.6 %. The residual is each glyph's fractional horizontal position, which Chrome keeps and the fixture rounds | a shift search over the caption |
| Pinning `FontRasterizationSettings` (subpixel positioning on, hinting off — oldge-ui's `PortableTextStyle`) changes no pixel of a viddik 0.7.0 fixture | the caption rendered both ways, byte-identical |
| macOS text smoothing thickens Chrome's stems, light-on-dark most (the dark caption 10.21 % with it, 6.99 % without); Skia draws the outline on every platform Compose runs on, so references are taken with `-webkit-font-smoothing: antialiased` | the renderer, both ways |
| The pages are drawn by their runtime from unpkg (React 18.3.1, Babel 7.29.0) with Google Fonts answered by the repository's files; every cell the references use falls on whole pixels once the page is pushed down by the fraction its 14.4 px text lines leave; two renders are byte-identical | `scripts/design-references.mjs` |
| Mutants on the scale: the hair rule 1 px thicker 0.00 → 5.88 %; the caption in 600 7.05 → 11.99 %; the caption a pixel low 7.05 → 11.16 % | the parity report under each mutant |

**Consequence 11.** A component is read against two floors: its shapes against 0.00 % (any
difference in a plate, a rule or a cut is a finding), its text against the text floor, so text-
bearing components sit at several percent by nature and `designStrict` stays off. The `_DIFF` is
read every time: red on a glyph edge is the floor, red on a box edge is not.

---

## 2. Decisions

### D1. `Plakat` in every identifier, package `io.github.youndie.plakat`, module `plakat-core`, no Material

Bare `Button`, `Card` collide with foundation and Material in every consumer. Only `foundation` is a
dependency: ripple, tonal elevation and state layers would each have to be switched off, and the
design has none of them. As in oldge-ui and toon-ui.

### D2. Mobile first; desktop, Android, iOS and wasm from the start; hover and a wide poster designed here

Brief: mobile first, with the poster and demo screens laid out for desktop too; hover "cheap (an
inverted frame)" if the answer is yes. The four targets are the line's default. The owner left the
question to this repository (2026-11-03), and the brief's own proposal is taken: hover on a pointer
is the frame inverted in one frame, no animation, and the poster gets a wide layout for windows of
600 dp and more ([B-39](../backlog/B-39-desktop-and-hover.md)). Both are designed here under D13.

### D3. Components are checked by pixels against references this repository renders

The components exist only as CSS boxes in the spec pages, so the reference is a photograph of a
page element in headless Chrome at device scale 1, with the repository's font files substituted for
Google's. A component closes on the states its page shows, in both themes, within the parity floor
measured once ([B-05](../backlog/B-05-pixel-references-and-floor.md)): 0.00 % for shapes, 7 % of a
caption cell for text (§1.7). H1 held: every reference so far is reachable without editing a page.

Rejected: references taken from the designer's screenshots folder. Nobody rendered them from the source,
their scale is unknown, and they were not vendored.

### D4. The generator is ported to `commonMain` from the sketch, with `snap` rewritten, and held to Node goldens

`plakat-gen.kt` already matches the JS on 500 seeds on the JVM (§1.3); the port keeps its structure
and replaces `Math.round(v / 8.0)` with `floor(v / 8.0 + 0.5)`. A Node script loads the vendored
`plakat-gen.js` untouched and writes goldens for a seed range and the four rapports; the same test
runs on every target, so a difference in floating point or `Int` overflow on Native or wasm shows up.

Rejected: `kotlin.math.round`, which is half-to-even and gives a different snap at every
`v ≡ 4 (mod 8)`; and porting from scratch, which throws away a sketch that is already right.

### D5. Raskat is ported from the JS, computed on the CPU, with no runtime shader in core

The design's raskat pixels come from `plakat-raskat.js`; its threshold field is computed once and
cached, and the `roll` animation only moves `t0` (the file's own header says so). The port computes
the same field with the same integer hash, caches it per shape size, and per frame compares the
profile with it — one comparison per pixel. Goldens: the field's `T` and `M` arrays and the final
colours from Node, for a fixed set of shapes and seeds.

**Held, 2026-11-03** ([B-04](../backlog/B-04-raskat-port.md)): eleven cases, the field's bits and the
roll at every millisecond equal the JS's on the JVM, the iOS simulator and wasm.

Rejected: the AGSL sketch. It draws a different noise (§1.3), Compose has no common runtime shader,
and on Android it needs API 33. The price is CPU time per frame during the 320 ms roll, measured in
[B-06](../backlog/B-06-frame-cost-measurement.md) (H3).

### D6. Halftone is CPU first: dots once per source and size, the 1-bit mask as the cheap path

`halftoneDots` and `halftoneMask` run once per (source, size, pitch, mode) and are cached, as the
design itself prescribes for Android < 33. A shader is an optimisation for later, behind
`expect`/`actual`, and only if a measurement asks for it.

### D7. Fonts: Rubik Mono One as shipped, Golos Text as static 400 and 600 cut from the variable file

Brief: OFL faces, at most two families, two display weights. The two chosen faces are OFL with full
Cyrillic (§1.4). Golos is variable; static instances are cut with fontTools' instancer and named
as the family, which the OFL permits with no Reserved Font Name. Each licence file ships beside its
font, and a coverage test holds both families to every string the fixtures render.

Rejected: the variable file with a `wght` setting, which draws Regular on Android 24–25.

### D8. Tokens are generated from `plakat-tokens.json`; values that live only in page code are read there and held to their text

A generator writes one committed Kotlin file and runs with `--check` in the gate; a second test reads
the JSON independently. Values the JSON does not hold — the hatch step 6, the caret 3 × 24, the
NavigationBar height 64, the type scale's letter-spacing in em — are read from the page code that
draws them, each with the line it came from. Where the JSON and a page disagree (texture opacity,
§1.5), the JSON wins and the disagreement goes to the owner ([B-07](../backlog/B-07-textures.md)).

### D9. Reduce motion follows the spec's word per animation

The spec tables give one word per animation: `мгновенно`, `fade 100`, `то же`, `без сдвига`,
`не мигает`, `сектор стоит`, `сразу финал`. Reduce motion is a theme property and each component
implements its word, not "everything instant".

### D10. Motion is closed-form curves of elapsed time; event names are public

Hard-stop is `CubicBezierEasing(0.2f, 0.8f, 0.9f, 0.9f)`, stepped is a discrete `floor`, durations
are tokens. No `spring()`: the brief forbids springs and overshoot, and a closed form can be read at
the spec's timestamps by a test. Each animation reports its event name (`stamp`, `release`, `lever`,
`wedge-in`, …) through the theme so that sound can be attached later without touching components;
the brief fixes the names now for that reason.

### D11. Documentation, code and commits in English; the brief stays in Russian

As in oldge-ui and toon-ui. Demo strings in fixtures are copied from the design verbatim, in Russian,
because the references were rendered with them.

### D12. Built and verified on the Mac; the repository stays out of mutagen sync

Screenshot references and goldens are a claim about the rasteriser that recorded them
(`cmp-ui-library` §9). The repository is not in `mutagen sync list`.

### D13. What the design does not draw is designed here, from the brief and the Principles recipe *(deviation from the line's rule)*

This first said: no component without a design — drawn in Claude Design first, re-imported, as in
toon-ui. The owner decided otherwise on 2026-11-03 ("сам") for RadioButton, the v1 set and hover
([B-38](../backlog/B-38-radio-button.md), [B-39](../backlog/B-39-desktop-and-hover.md),
[B-42](../backlog/B-42-v1-components-designed-here.md)).

Such a component is built from what the design does say: the brief's row for it (its treatment and
motion), the Principles page's seven steps and its do/don't pairs, the tokens, and the nearest drawn
component it shares parts with (the Slider is the Principles page's worked example; Segmented is
drawn inside the Settings demo). It has no Chrome reference, so its goldens are recorded when it is built
(the owner asked on 2026-11-03 for the loop to run to the end rather than wait) and revised in the
owner's review round over the finished sample; its item lists every number it chose that the design
does not state. Everything the design does draw stays held to the design.

### D14. The registration offset is 1 dp

Brief: "1 px вправо-вниз". The tokens give `[1, 1]` without a unit, and the pages draw CSS `1px`,
which is 1 dp in every other token of this kit. At density 2.75 the two readings differ by a factor
of almost three. The owner chose dp on 2026-11-03 ([B-41](../backlog/B-41-registration-unit.md)).

### D15. MIT licence

The owner's choice on 2026-11-03, as for toon-ui (oldge-ui and kvadrant-ui are Apache 2.0). The
fonts keep their OFL and ship their own licence files ([B-40](../backlog/B-40-licence.md)).

### D16. The missing textures are rendered by a script in this repository, seamless by construction

Three tiles came back truncated (§1.1). The owner chose on 2026-11-03 not to fetch them again but to
render them here. The Tokens page gives the recipe the design used for its paper and non-print
layers — an SVG `feTurbulence` with `stitchTiles='stitch'` and a colour matrix, "own, procedural"
by `plakat/sources` — so a committed script renders the three missing tiles from that recipe at
512 px, matched in tone to the five delivered ones, and a test holds all eight to seamlessness
([B-07](../backlog/B-07-textures.md)). The vendored five are not re-rendered; texture opacity follows
`plakat-tokens.json` (D8).

**Amended in B-07 (2026-11-04): made from the delivered dark paper, not from the recipe.** Read
pixel by pixel, `paper-dark-2x.png` is not that recipe's noise: it is the token #1F1D1A with a mottle
of ±1 level (σ 1.1) and short straight fibres and specks up to +19, where the Tokens page's
`feTurbulence` overlay spreads over tens of levels. A tile rendered from the recipe would have been a
different paper beside the designer's. So `scripts/textures.py` takes the dark tile's deviation from
its token and prints it on the other papers, pixel for pixel: `paper-light` and `concrete-light` are
their token minus that deviation (the fibres print dark on light paper), opaque; `paper-grain` is the
Tokens page's grain colour (.42, .34, .20) with an alpha that, multiplied at the token's 0.45 over
flat light paper, takes off the same luminance. The source wraps without a seam (wraps 1.10 and 1.06
times the median neighbour difference), so the three do too. Rejected: the recipe (above); asking
for the files again (the owner's choice). The designer's own tiles may still differ in tone from
these; Risk 4 holds.

---

## 3. Risks and open questions

**Risk 1. A Chrome reference of an inline-styled page element is not the component.** The pages wrap
components in demo chrome (labels, cells, sticky toolbars) and some states exist only after a
click. Mitigation: each reference is an element selected by a stable selector and photographed with
its state forced by the page's own `DCLogic` state, scripted in the renderer; the selector list is
reviewed against the page per component. Open: whether every state is reachable without editing
the vendored page (H1).

**Risk 2. Raskat's grain is per pixel, so its pixels depend on density.** The JS renders at
`dpr = 2` and scales the grain by it. Mitigation: goldens are taken at the density the fixture uses,
and the Compose port takes density as an input to the field, never a constant.

**Risk 3. Muted text on textured concrete drops below 4.5:1** (§1.5). Mitigation: the accessibility
item measures the contrast of muted text over the multiplied texture and either lightens the texture
under text or swaps the token, with the owner's choice recorded ([B-36](../backlog/B-36-accessibility.md)).

Settled by B-36: `concrete-light` lifted two levels, a tile made here, so no token and nothing vendored moved (worst letter-sized
square 4.43 → 4.5 and above). Per pixel, fibres still fall to about 3.8; the owner may prefer a darker `ink-muted` instead.

**Risk 4. Three textures are made here, not delivered** (D16), so their tone may not be the
designer's. Mitigation: the script takes their grain from the delivered dark paper and their base
from the tokens (paper-light's mean is its token within 1.1 levels), the texture layer is switchable by token,
and references and goldens are taken with it off, so a texture never moves a component's parity.

**Hypothesis H1.** The spec pages render in headless Chrome from `file://` once the three runtime
scripts and the fonts are reachable, and every drawn state can be forced from outside. Settled in
[B-05](../backlog/B-05-pixel-references-and-floor.md).

**Hypothesis H2.** The commonMain generator port gives the JVM's results on Kotlin/Native and wasm
(doubles, `Int` overflow in mulberry32, `ushr`). **Held, 2026-11-03**: 10 000 compositions and 400
rapports equal the JS's on the JVM, the iOS simulator and wasm
([B-03](../backlog/B-03-generator-port-and-numeric-parity.md)).

**Hypothesis H3.** A per-pixel raskat comparison over a 120 dp circle at density 2.75 (≈ 330 × 330
px, 109 k comparisons) fits in 2 ms per frame on a Pixel 6a, and ten cards cascading with texture and
registration hold 60 fps (the brief's target). Refuted or kept on the desktop JVM first
([B-51](../backlog/B-51-frame-cost-on-desktop.md)); settled on the device once the kit is complete
([B-06](../backlog/B-06-frame-cost-measurement.md)), as the owner set on 2026-11-03.

Measured on the desktop JVM ([B-51](../backlog/B-51-frame-cost-on-desktop.md), M1): the cascade holds (0.60 ms of main-thread work, no missed frames), and the comparison alone holds its 2 ms (1.32 ms at 352 px), but the whole raskat frame does not (8.26 ms at 2.75, with about 6 ms unexplained). The device settles it.

Measured on a Pixel 6a ([B-06](../backlog/B-06-frame-cost-measurement.md), [report](measurements-2026-11-05-device/README.md)): the cascade holds (`work` p95 1.19 ms, none missed, 0.2 % past deadline). The whole raskat frame costs 1.10, 3.32 and 3.73 ms over its base on the main thread at densities 1, 2 and 2.75, so its 2 ms is refuted on the phone at 2 and 2.75, and the roll misses no frame at any. The comparison alone is not separated there. The recorder B-51 and B-06 share cannot see a layer inside a layer that is not redrawn on Android, so this reading is `FrameMetrics`'; which verdict stands is the owner's.

**Hypothesis H4.** `Canvas.drawVertices` with an `ImageShader` behaves the same on Android, iOS and
wasm as on desktop, where it was verified to exist. Settled in [B-32](../backlog/B-32-projection.md).

Settled on 2026-11-05, on a bitmap's canvas: the same picture on desktop, the iOS simulator and wasm in Chrome
(`PlakatVerticesTest`), and on a Pixel 6a by screenshot ([B-06](../backlog/B-06-frame-cost-measurement.md)), where its first frame costs 91 to 104 ms on the main thread. Two differences stood behind "the same": on Skiko `ImageShader` samples by
the nearest pixel, where Android's follows its paint, and on Android's hardware canvas `drawVertices` needs API 29
([the platform's table](https://developer.android.com/topic/performance/hardware-accel#drawing-support), which says to do what is done here). So the
projection never draws on the frame's canvas: it bakes at twice the pixels and halves.

**Answered on 2026-11-03.** Licence MIT (D15); registration in dp (D14); missing textures rendered
here (D16); RadioButton, the v1 set and hover designed here (D2, D13); the repository goes public and
publishes last ([B-37](../backlog/B-37-public-repository-and-publishing.md)).

---

## 4. What happens next

The order of work and the acceptance criteria live in [backlog.md](../../backlog.md). The earliest
steps are the ones that can change how every component is built or verified: the skeleton, the
fonts, the generator and raskat oracles, the pixel references with their floor, and the frame cost
on a real device.
