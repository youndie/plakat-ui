---
id: B-30
title: "Счётчик: the Isotype stat tile with a mechanical counter"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-31]
---

# B-30 — Счётчик: the Isotype stat tile with a mechanical counter

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `tick`.

- **What to build.** `PlakatCounter`: a big number in Rubik Mono One stretched to its box, a row of pictograms "one = N" and a caption. Digits roll per place, lowest first, 40 ms a step, the whole tick ≤ 400 ms; pictograms are added one by one.
- The Stretch page draws one more cell of it, «Счётчик / Колонна»: the number through `PlakatStretch`, 104 high, 56 wide per place, on concrete; the tick changes the digits by a change of sheet, with no scale (found in [B-31](B-31-stretch-text.md)).
- The legend's `■` and `●` are not in either font (research §1.4): they are drawn as the pictogram shapes, inline with the text.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **Two composables**: `PlakatCounter(value, caption, modifier, legend, pictogram, perPictogram, accentLast)` for the MVP page's «Изостат», `PlakatCounterColumn(value, caption, modifier, color)` for the Stretch page's «Колонна». The item spoke of one tile with a stretched number; the pages draw these two, and only the column stretches: «Изостат»'s number is plain Display at 48.
- **The tick is the badge's**, now shared as `rememberPlakatTicker`. The digits step, they do not slide: the page's `steps(n, end)` shows a whole digit per step, so `PlakatTick.textAt` is that animation, and the 48 px clip and the column of ten digits in its lab are how CSS gets it.
- **Parity**: tiles 2.07 % and 2.12 %, column 1.43 %. Of ink, by band: number 4.4 %, row 0.6 %, caption 41–43 %, legend 65 % against the text floor of 63.7 %. The legend's excess is its first line alone (its second is well under): it starts at a fractional x after the mark, where Chrome keeps each glyph's fraction and Compose rounds, and no whole-pixel shift is better. A gap, named, not closed; the owner may set it apart with B-26's and B-27's short words.
- **The marks**: `■` and `●` are in neither font, so Chrome falls back to a host's face. Drawn as shapes in an inline placeholder, by that face's `measureText` at 12 px: ■ advance 6.504, 4.05 square, 1.664 over the baseline; ● advance 7.248, 5.156 across, 0.826 over. These numbers are a claim about the reference's host, kept because the legend's words stand after them. The screen reader gets the character, as the page's.
- **A defect found by a test**: with the box set by the target's digits, a new value composed once with the old digit in a three-place box, and `PlakatStretch` refused it (×0.63), crashing on a plain 6 → 120. The box goes by what shows.
- **Designed here** (D13), for the owner's review: the pictograms' path. The spec row says «по одной» and the lab adds them at once; here they come evenly across the tick, from the old count to the new (39 → 47: 7, 8 at 60 ms, 9 at 120 ms), because the digits pass through numbers on the way (38, 37) that would move them backwards. What is left over, by the digits (+4, +3, +2). A remainder as a word, as the tile draws it, where the lab cuts the last square by `clip-path`: two answers on one page, and the tile is the drawn state. No limit on how many pictograms a value makes; a column too narrow for its digits is refused by `PlakatStretch`. `accentLast` as the name for «красный — сегодня». The lab's own squares of 12 and row of 168 are its own.
- **Touch target**: the tile holds no control.
- **Mutants**: through the tests — a step of 60, a limit of 800, the pictograms all at the end, no `tick`, reduced motion ignored, a row of 240, a gap of 4, the accent on the first, no circle, what is left over by the target, a word of `+0`, the box by the target, a column of 96, a place of 48, the word unlifted, the mark always round. Through the goldens — a number of 44, a word of 14, a legend of 13, a mark's advance of 7.5, a padding of 8. All twenty-one killed. The last two of the first list survived the first round, held by nothing but a golden's tolerance, and got tests of their own.
