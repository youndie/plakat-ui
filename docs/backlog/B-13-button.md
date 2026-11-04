---
id: B-13
title: "Button: filled, outlined, text, danger; stamp, release, engage"
status: wip
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-13 — Button: filled, outlined, text, danger; stamp, release, engage

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `stamp / release / engage`.

- **What to build.** The red plate with the cut corner and an upper-case slogan; outlined is a 3 dp frame; text is underlined by a rule; danger is the hatch with a word on a paper plate (Tokens page «СЕМАНТИКА»). Pressed inverts the inks in one frame and shifts the text 1 dp right-down; release returns in 60 ms hard-stop; loading closes the text with a wipe from the left and turns a 90° sector by 30° steps (cycle 1200, steps 12).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`
