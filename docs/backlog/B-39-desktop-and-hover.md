---
id: B-39
title: "Hover on pointer devices and a wide poster layout"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-13, B-19, B-20, B-22, B-23]
---

# B-39 — Hover on pointer devices and a wide poster layout

The brief proposed a desktop layout for the poster and demo screens and cheap hover states ("инверсия рамки"); the owner left it to this repository on 2026-11-03 (research D2).

- **What to build.** Hover, on pointer input only, inverts the element's frame in one frame (a rule becomes the 3 dp bar, an outlined frame fills, a filled plate gets the ink frame), no animation; it never stands in for pressed. A wide `PlakatPoster` layout for widths ≥ 600 dp: title zone on the left half, the composition on the right, the slogan button under the title.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`

## Findings (2026-11-05)

- **Designed here** (D13), every number the design does not state. Hover: an outlined frame fills with concrete, because the item's «fills» in ink would be the press; a filled plate gets a 3 dp ink frame inside its cut; a text button's 3 dp rule becomes the 8 dp bar, growing down with the layout unmoved; a danger button gets the ink frame round its hatch; an icon's empty square fills with concrete and a selected one gets the frame; a chip fills under its frame, and a selected one, being ink, gets a concrete frame; a row's hair becomes the 3 dp rule. The item says «a rule becomes the 3 dp bar», and the tokens call 3 the rule and 8 the bar: read as one step up the scale, hair to rule and rule to bar. For the owner's review.
- **Wide poster**, `PlakatPosterLayout.Wide`, 392 high, at least 600, as wide as its box: a composition frame of 360 × 392 against the right edge, cut by the half; the title at the Circle's 48, upright (the −17° axis does not fit the half's width without leaving it), 32 down; the body 16 under it, as wide as the half less its margins; the slogan where the Circle's stands, 240 wide as the Stretch page's poster's. Its recipe has no safe zone, as the title is not over it. The print cascade is the others'. A box forcing it under 600 gets what it forces.
- **The AC's look**: both are in the sample, both themes; goldens are recorded now for the owner's review at the end, as for B-38. **Met but for that look.**
- **Principles checklist**: primitives only; radius 0; no new diagonal (the plate's cut is the one it had); hover reads without colour, by a frame or a fill, in grey too; targets unchanged; the words stay on the grounds `ContrastOverTextureTest` holds, concrete included.
- **Hover is never a press**: no event, no shift; a press reads over it (held by a test); a finger never hovers, as a touch emits no `HoverInteraction`.
- **Mutants**: through the tests — hover never set, never cleared, over a press, the outline filled in ink, no plate frame, no bar, no danger frame, no icon fill, no icon frame, no chip fill, no chip frame, no row rule, the composition on the left, a slogan of 200, a poster not as wide as its parent. All fifteen killed; the last after a test of its own. A hover on a disabled control survives its guard's removal, because a disabled `clickable` emits no hover at all: the guard only covers the forced fixture.
