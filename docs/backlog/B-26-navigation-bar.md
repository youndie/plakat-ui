---
id: B-26
title: "NavigationBar: black strip, red square behind the active icon"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-11, B-12]
---

# B-26 — NavigationBar: black strip, red square behind the active icon

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `lock`.

- **What to build.** A 64 dp black strip (concrete in the dark theme) with stencil icons and labels always visible; the red square moves horizontally behind the active icon (120 hard-stop). Cream icon on red is 4.64:1; a red icon on dark fails contrast, which is why active is a fill (Icons page).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatNavigationBar(items, selectedIndex, onSelect, modifier)` with `PlakatNavigationItem(icon, label)`: one
  component rather than Material's bar and items, because the square is shared between the slots. Each slot is a `Tab`
  target of its whole width and the strip's 64 dp, named by its word.
- **The strip's colour**: ink in the light theme, concrete in the dark, which the page writes out as `navBg`; the rule that
  gives both is "the darker of the two". The words and stencils are on-red in both.
- **Parity** (`NavigationBar_*`, the MVP page): 2.90 / 2.80 %. The strip, the stencils and the square differ by 0 px, and no
  whole-pixel shift of any word is better. **A named gap: the words are over the floor.**

  | Word | ЛЕНТА | ПОИСК | ОБМЕН | ОПЦИИ |
  |---|---|---|---|---|
  | Light, of its ink | 75 % | 82 % | 64 % | 70 % |
  | Dark, of its ink | 73 % | 80 % | 59 % | 70 % |

  Against 63.7 %: seven of eight above it, up to 18 points. The cause is rasterisation, read pixel by pixel: Chrome draws
  the top row of an 11 px cap full where Compose draws it partly, and keeps each glyph's fractional x. Short capitals
  are all edge, the worst case the floor's caption is not (12 px mixed case). No whole-pixel shift
  helps; viddik does not see `FontRasterizationSettings` (research §1.7), so a fixture cannot test the alternative. Stated, not
  closed; **for the owner's review**. The word's box and baseline are right: each is centred by its real width, as a layout's
  ceiled width left two of them a pixel to the left (3.41 → 2.90 %).
- **Half-pixel slots**: the page's grid gives 97.5 each and `weight` hands out whole ones, so the bar lays its children out by
  hand from the true slot: the targets are 98, 97, 98, 97, and each stencil and word sits where Blink snaps it.
- **Motion**: the square's way is an `Animatable` of the slot's index, its box snapped to the pixel on each frame as Blink snaps
  a moving `left`.
- **Designed here** (D13): no press look (the page draws none, and its lab's bar has none); an out-of-range index draws
  outside the strip's slots, the caller's to keep in range.
- **Events**: `lock` when `selectedIndex` changes.
- **Mutants**: through the behaviour tests — the square linear, over 200, crossing under reduced motion, no `lock`, the strip
  always ink, whole-pixel slots; through the goldens — the square 2 up, 38 wide, a gap of 6, words of 12, words centred by
  their ceiled width, red icons. All twelve killed.
