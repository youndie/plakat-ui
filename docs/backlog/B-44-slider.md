---
id: B-44
title: "Slider: rail, ticks and a carriage that stops on them"
status: done
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-16]
---

# B-44 — Slider: rail, ticks and a carriage that stops on them

The Principles page works the Slider through all seven steps and has a live one: 3 dp rail, red 8 dp fill, 20 × 32 carriage, 3 × 8 ticks (every third 12), stops on ticks in 120 ms hard-stop, event `notch`.

- **What to build.** That slider as a component, continuous and stepped; disabled is the hairline rail and a concrete carriage (its step 5).
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`

## Findings

**Not wholly designed here.** The item says no page draws it, but the Principles page has a live one and step 5's two specimens, so those two cells have references, rendered from that page by `scripts/design-references.mjs`.

**Parity:** `Slider - Live_Light` **0.00 %**. `Slider - Specimens_Light` 0.83 %, one row of the fill: the specimen's fill stands 3 over its rail, `B(0, 27, …)` to the rail's `B(0, 30, …)`, and the live one's 2, `top: 20px` to `top: 22px`. The page disagrees with itself by a pixel; the live one is taken.

**Designed here (D13), every number the page does not state, for the owner's review:**

| What | Number | From |
|---|---|---|
| Pressed | the carriage's paper with a frame of 3 | step 5's «pressed — инверсия», drawn nowhere |
| Focused | a ring of 3 about the carriage, 4 off it | step 5's «рамка 3 с отступом»; the 4 is the Switch's. About the carriage and not the slider, as its ends stand 10 past the box |
| Continuous | no ticks | step 2, «риски — где можно остановиться»; the brief's row has ticks without saying this |
| Continuous, dragged | at the finger, with no run | the 120 ms is for stopping on a tick |
| Continuous | no `notch` | «щелчок на каждой риске» |
| Disabled, stepped | ticks a muted hair on the same place, same heights | step 5 draws no ticks at all |
| Disabled | no fill | step 5's specimen |
| Keyboard | the arrows a stop or 1 % of the range, Home and End | step 1 and the kit's other controls |
| A finger | sets it on lifting, or past the slop | the page sets on any pointer's landing, with `touch-action: none`; here a scroll over it must change nothing. A mouse sets it as it lands |

Kept from the page: the carriage stands 10 past the box at the ends, which the slider's user must leave room for. Not built: hover, which the page does not name, and right-to-left, which the Switch has not either.

**`onValueChangeFinished`** is Material's, not in step 1's list, kept because step 1 says a developer should not have to relearn it; so is `interactionSource`.

**The acceptance's order.** The item asks for the owner's look before the goldens. CLAUDE.md's later rule, the owner's of 2026-11-03, records them when built and revises them in the review at the end, so that is what is done. The owner has not looked at it. `Kit.kt` shows it in both themes; the window itself was not opened here.

**Checklist.** Primitives only, radius 0, no diagonal (step 4 says it needs none); state readable without colour: the value by the carriage's place and the disabled and pressed states by their form, the fill's red only doubling them; 48 dp high. Contrast has no text to read here. Motion by one tween on the hard stop.

**Tests.** `PlakatSliderTest`, 18. Mutants, the aimed test named among the failures: tick top, tall tick, every third, rounding half-to-even, rounding down, the duration, the curve, the drag's no-run, reduced motion, notch for a continuous one, notch on no change, the finger's slop, the mouse's landing, the no-change guard, the cancelled gesture's finish, the keyboard's stop, the carriage's place, the inversion, the ticks' width. Half-to-even survived at first, as the test read an odd half, so an even one was added. A fill 4 short also survives, and is no defect: the carriage covers it. A fill 12 short is caught.
