---
id: B-25
title: "TopAppBar: newspaper header, rapport strip, folding on scroll"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-03, B-09, B-10, B-11, B-12]
---

# B-25 — TopAppBar: newspaper header, rapport strip, folding on scroll

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `fold`.

- **What to build.** Capitals over an 8 dp bar, a 24 dp rapport strip under the header seeded by the section ([B-03](B-03-generator-port-and-numeric-parity.md)); on scroll the rapport and the large title ride up and leave a 3 dp rule (linear with the scroll).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatTopAppBar(title, modifier, subtitle, collapsedTitle, seed, state, navigationIcon, actions)`
  with `rememberPlakatTopAppBarState()`, whose `nestedScrollConnection` goes on the scrolling content and
  whose `fraction` the app may read. Its behaviour is Material's exit-until-collapsed: it folds before the
  list moves on the way down, and unfolds once the list is back at its top. Touch targets are the slots'.
- **What the page draws against what the item says.** The item names "capitals over an 8 dp bar"; the page draws
  no bar there, so none is drawn. The page's caption says 140, its parts sum to 128 (8 + 48, 4 + 32, 12 + 24) with
  a spacer after: 128 is built, and the collapsed bar is 59, its 56 and the 3 dp rule.
- **The strip is the page's own**: `raport(seed, cols, rows, cell, pal)`, not `plakat-gen.js`'s `rapport`, which
  chooses its red differently. `plakatStrip` ports it; `scripts/poster-goldens.mjs` cuts it from the page and the
  port equals it on 2000 seeds on the desktop JVM, the iOS simulator and wasm. The page's seed is 240.
- **Parity** (`TopAppBar_*`): expanded 0.70 %, collapsed 0.38–0.41 %, no whole-pixel shift better. The row of targets
  and the strip differ by 0 px; the title is 9 % of its ink, the subtitle 41 %, against the 63.7 % floor. First
  3.44 %, from two causes: the 32 px title's line is shorter than its face of 40, which `cssLine`'s negative leading
  cannot do, so it is a `normal` line lifted by 4; and a strip circle is a box on `1.5 px` that Blink snaps to the
  pixel before rounding it.
- **A squeezed strip, found by the fold's test.** The bar's shrinking height was the riding part's maximum too, so
  mid-fold the strip was squeezed out; the first `Halfway` golden showed a blank band and would have held it.
  The riding part now keeps its full height (`wrapContentHeight(unbounded = true)`).
- **Designed here** (D13; the page draws only the two ends): between them the title and strip ride up under the row
  by the fold, the collapsed title opens by a wipe with the fold (no fade in the kit), the targets settle from 8 to 4
  from the top, and the rule grows to 3 at the bottom. `Halfway` is this library's own golden. No word from the page
  for what is under the row's right end while `subtitle` rides: it rides too.
- **Events**: `fold` when the bar comes to rest at either end, not per pixel of the way.
- **Mutants**: through the behaviour tests — the strip squeezed, the title riding half, no rule, a hair for a rule,
  the title never wiped, pre-scroll consuming all, folding on the way down too, no way back, `fold` per pixel;
  through the strip goldens — no flip, no forced red; through the goldens — circles unsnapped, the subtitle off the
  baseline, a title of 34, collapsed 56, a row inset of 8. All sixteen killed.
