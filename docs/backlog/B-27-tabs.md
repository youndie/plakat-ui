---
id: B-27
title: "Tabs: rubrics with a red bar riding linearly"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-27 — Tabs: rubrics with a red bar riding linearly

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `rail`.

- **What to build.** Rubric tabs with an 8 dp red indicator that rides linearly (200), the content shifting with a hard stop.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatTabs(tabs, selectedIndex, onSelect, modifier)` with `PlakatTab(label, enabled)`, and `PlakatTabContent(pageCount,
  selectedIndex, modifier, page)` for what they choose, composing only the pages in sight. Each slot is a 48 dp `Tab` target
  named by its word.
- **Parity** (`Tabs_*`, the MVP page): 3.69 / 3.68 %. The bar, the hairline and the slots differ by 0 px and no whole-pixel
  shift is better. **The same named gap as [B-26](B-26-navigation-bar.md)**: the words are short capitals, of their ink 76, 69, 62 % in
  the light theme and 77, 70, 61 % in the dark, four of six above the 63.7 % floor, for the same cause. Stated, not closed.
- **Motion**: the bar `200 · linear`, its box snapped to the pixel on each frame; the content `200 · hard-stop`, which the
  page's row names and its lab does not draw: **`PlakatTabContent` is designed here (D13)**, the pages side by side, moving by a
  page's width.
- **Events**: `rail` when `selectedIndex` changes; the content says nothing of its own.
- **Designed here** (D13): the content host; selecting a disabled tab from code still moves the bar, the caller's to prevent.
- **Mutants**: through the behaviour tests — the bar hard-stop, over 120, crossing under reduced motion, no `rail`, the content
  linear, crossing under reduced motion, all pages composed, the bar off the hairline, a disabled tab answering; through the
  goldens — the disabled word in ink, a row of 46, and words centred by their ceiled width. That last one survived twice: on a
  whole-pixel centre the real and the ceiled width always round alike, so neither the page's cell nor a first added golden of
  four words could see it. A window of 391 can (`Tabs_OddWidth_Light`: slots of 130.33, «ЛЕНТА» at 43 against 42). All twelve killed.
