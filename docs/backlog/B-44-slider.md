---
id: B-44
title: "Slider: rail, ticks and a carriage that stops on them"
status: open
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
