---
id: B-29
title: "Окно: onboarding and empty-state panels in the manner of ROSTA windows"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-03, B-09, B-10, B-12, B-13]
---

# B-29 — Окно: onboarding and empty-state panels in the manner of ROSTA windows

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `rail`.

- **What to build.** `PlakatWindow`: 2–4 panels "picture + caption" paged by a horizontal shift with a hard stop; the caption opens by a wipe after the stop (200 + 120). Pictures are presets or generated compositions ([B-33](B-33-agit-presets-and-photomontage.md)).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **`PlakatWindow(panels, page, modifier, panelsInView = 1)`** with `PlakatWindowPanel(caption, body?, picture)`. `page` is the first panel in view, and it is the caller's: the page draws no control and no gesture for it. The page's onboarding is 2 of 4 panels in two columns of 358; the page's lab beside it steps through its panels, a demo of the motion and no state of its own, so the cell is the only reference (`Window_Onboarding_*`).
- **Parity**: 2.06 % and 2.09 % of the cell. Of ink, by band: number tags 15.6 %, captions 19 %, body 52.3–52.5 % against the text floor of 63.7 %; the pictures 57 and 84 px of 56000, all edges, max 47 levels. Red only on glyph edges and circles' rims.
- **Two causes found on the way, both in the layout**: a column taller than its crop was centred, so the whole picture stood 1 px up (the fixture now takes `wrapContentHeight(Alignment.Top, unbounded = true)`, as the others do); `CssParagraph` ceiled a fractional height where Blink rounds it, so the body stood 1 px low. Rounded to the nearest; the dialog stayed at 1.47 / 1.46 and no other golden moved.
- **The pictures are the caller's.** The page's `comp` here is a third generator of its own (seeds `300 + i × 17`, frame 171 × 160), neither `plakat-gen.js` nor the core page's: it is a test-only port in `WindowFixtures.kt`, because a library's window has no business generating them. The presets are [B-33](B-33-agit-presets-and-photomontage.md).
- **Split out**: «Окно / Пустое состояние», on the same page, is no panel of a window: [B-52](B-52-empty-state.md), blocked on the raskat port.
- **Motion**: `shift` 200 ms hard-stop by a page of panel + gap; the words go at once on a change (the page's `capIn: false` has no transition) and open 120 ms linear from 200 ms, left to right. The spec row's word `мгновенно` is both at once. `rail` once per change, none on first composition. The lab's `setTimeout(…, 30)` that puts `capIn` back is how a CSS transition is restarted and no timing of the design's (the spec row says 200 + 120), so the words open from 200, not 230.
- **Touch target**: the window holds no control, so nothing to size.
- **Designed here** (D13), for the owner's review: `page` and `panelsInView` as the API; no swipe, no indicator; panels of unequal height top-aligned, the window as tall as the tallest; a page held at the end of the list is the caller's to clamp.
- **Mutants**: through the behaviour tests — shift 120 ms, shift linear, words staying, words with no delay, words over 200 ms, words on the hard-stop, caption wiped from the top, body not wiped, `rail` on first composition, reduced motion ignored, no gap in the width, no gap in the step, a number unpadded, no clip at the window's edge, no clip at the picture; through the goldens — picture 152, caption line 1.3, body line 1.6, tag padding 8. All nineteen killed.
