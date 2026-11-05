---
id: B-34
title: "Screen transition: the red diagonal wipe, push and pop"
status: done
priority: P1
size: S
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-34 — Screen transition: the red diagonal wipe, push and pop

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `wipe`.

- **What to build.** A red strip passes diagonally and the new screen follows; pop runs the other way (320 linear, reduce motion fade 100). A container a navigation host can use, with no navigation library in core.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **`PlakatScreenWipe(screen, depth, modifier, content)`**, generic in the screen, with no navigation library: a host passes its top entry and the stack's size. A greater or equal depth pushes, a smaller one pops.
- **Geometry**: the page has it twice, its lab at 180 × 320 (edge 290 → −60, lean 100, strip 40) and its strip of frames at 60 × 106 (97 → −20, 33, 13). They agree as shares of the screen, so that is what is ported: lean 5/16 of the height, strip 2/9 of the width, start 1/18 of the width past the lean, end 1/3 short. The lean on a 390 × 844 phone is 264, 17.4° against the axis's 17°: the page's, kept.
- **Parity** on the strip of frames: 0.00 % at both ends, 2.58 % and 1.35 % between, on the slanted edges alone, where the page rounds its numbers (97, 33, 13). The frames' hairline is the page's cell, and an inset shadow paints under the children: 5 % until the fixture drew it first.
- **Two defects found by tests**: at the end of the change the layers were read at drawing before the composition that drops them, so the old screen came back for one frame (0.96, 0, 1.0 in the fade). A change's progress at its end now reads 1. And going back to the screen being left finished nothing and cut: now that change finishes at once and wipes back.
- **Reduced motion**: `fade 100`, the new screen on top, for a pop too, with no strip.
- **Designed here** (D13), for the owner's review: `depth` as the way to say push or pop; a change in the middle of a change finishing that one at once; a screen that has left being composed anew if it comes back, so what must outlive that is the host's; the screens' ground being paper; no input blocked during a change.
- **Touch target**: the container holds no control.
- **Mutants**: through the tests — a lean of 60, a strip of 80, a start of 40, an end of 90, a fade of 200, a change of 200, on the hard-stop, no `wipe`, a pop as a push, a strip under reduced motion, no fade, the flash at the end, not finishing a change under way, no paper. All fourteen killed, the last after a test of its own. Standing at 1 instead of 0 before the change starts survives: effects run before the first draw in the test's frames, so that branch only guards a host's other scheduling. Kept for that, named here.
