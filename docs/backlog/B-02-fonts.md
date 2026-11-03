---
id: B-02
title: "Fonts: Rubik Mono One and static Golos Text 400/600, licences, coverage test"
status: done
priority: P0
size: S
stage: stage-0-risks
blocked_by: [B-01]
---

# B-02 — Fonts: Rubik Mono One and static Golos Text 400/600, licences, coverage test

The two faces are OFL with full Cyrillic, verified by file (research §1.4). Golos Text ships only as a variable font, and Rubik Mono One is static.

- **The decision and its reason.** Bundle `RubikMonoOne-Regular.ttf` as fetched and cut static `GolosText-Regular` (400) and `GolosText-SemiBold` (600) from `GolosText[wght].ttf` with fontTools' instancer, by a committed script that records the source commit (D7). A `wght` axis needs Android API 26; below it Title and Label would draw at 400.
- Each `OFL.txt` ships beside its font. A coverage test holds both families to every string the fixtures render; the Счётчик legend glyphs `■` `●` are not in either face and are drawn as shapes ([B-30](B-30-counter.md)).
- Metrics pinned in a test: Rubik Mono One cap height 700/1000 and advance 850 (monospace), Golos cap height 700 and x-height 530 — the stretch text ([B-31](B-31-stretch-text.md)) depends on the cap height.
- Rejected: subsetting now. The files are 141 KB and two Golos instances; subsetting is a size decision for after a consumer measures it.

- AC: the fonts load on desktop, Android 24, iOS and wasm; the coverage test fails when a fixture string gains a missing glyph; the metrics test fails when a font file changes.
- Anchors: `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc!/ofl/rubikmonoone/RubikMonoOne-Regular.ttf`, `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc!/ofl/golostext/GolosText[wght].ttf`

## Findings

- 2026-11-03. `scripts/fonts.py` fetches both families from `google/fonts` at the commit research
  §1.4 read, checks them against `scripts/fonts.lock`, and cuts `golos_text_regular` (400) and
  `golos_text_semibold` (600) with fontTools' instancer: 70 KB each instead of the 184 KB variable
  file; a second run is byte-identical. Files in `plakat-core/src/commonMain/composeResources/font/`,
  the OFL texts in `files/licences/`; `plakatFonts()` builds the two families.
- Tests on desktop: the coverage test reads all 11 pages' text and holds both families to it (Golos
  lacks only `■ ● ₀`, Rubik Mono One only `■ ● ↑ → ↗`, each allowed with its reason); the metrics test
  pins Rubik Mono One's 700 cap height and its single 850 advance, Golos's 700 cap and 530 x-height,
  the instances' weights 400 and 600, and that both families lay out through compose resources at the
  files' advances. Mutant killed: the 600 file replaced by the 400 one fails the test.
- **Golos keeps one advance across weights** (`Н` is 760 at 400 and at 600), so a weight change
  never moves a layout; the weight shows only in the ink. Recorded in research §1.4.
- Platforms: desktop loads and lays out the fonts in the test above. Android: the AAR carries the
  three fonts and both licences under `assets/composeResources/` (`androidResources` on, B-01). iOS and
  wasm: compiled and packaged by `assemble`; drawing on them is first seen in the sample (B-35), as
  `check` runs no test there.
