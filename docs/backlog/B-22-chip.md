---
id: B-22
title: "Chip: the stamp"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-22 — Chip: the stamp

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `stamp`.

- **What to build.** Capitals in a frame, 32 dp high with a 48 dp touch target; selected fills black with a wedge in one frame.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatChip(text, selected, onClick, modifier, enabled, interactionSource)`, Material's filter
  chip in Kotlin form. The word goes through the label role's capitals. The plate is 32 dp in a 48 dp
  target at least 48 wide, as wide as the word, or whatever `modifier` gives it.
- **Parity** (`Chip_*`, the MVP page). In all eight cells nothing differs outside the word: the frame,
  the fill, the dashed hair, the wedge and the ring are 0 px. Whole cells 2.63–3.98 %, the word 28–37 % of
  its ink against the 63.7 % floor. The Default cell is 80 wide because the page's column stretches it to
  its caption, so the fixture passes that width; the others are the word's. The disabled chip is 2 wider
  with its word 1 in, as `border: 1px dashed; box-sizing: border-box` makes it.
- **Two findings on the way**: the plate's width is the word's real width rounded, as a layout's own is
  ceiled (the Badge's cause); and a `Row` centred the wedge inside the text's 15 dp line and rounded it a
  pixel low, where the page's flex items centre by themselves on the plate, so wedge and word sit in the
  box each on its own (Selected 4.55 → 3.57 %). The renderer gained `round` for a fractional cell width.
- **Motion**: none: `stamp` is one frame, and the spec row's reduce-motion word is «то же». No press look,
  as neither the cell nor the lab's chips have one.
- **Events**: `stamp` when selected; letting go says nothing (the row's trigger is `select`).
- **Designed here** (D13): a short word's plate widens to the 48 dp target; a selected disabled chip draws
  as disabled, dropping the fill and wedge.
- **Mutants**: through the behaviour tests — stamp on letting go, no stamp, no wedge or fill, no capitals, a
  32 dp target; through the goldens — padding 10, the wedge 8, the gap 8, the ring 1 off, the disabled word
  without its hair's inset, a solid hair. All eleven killed.
