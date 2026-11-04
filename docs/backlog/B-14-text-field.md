---
id: B-14
title: "TextField: underline to bar, hatch error, square caret"
status: wip
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-14 — TextField: underline to bar, hatch error, square caret

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `underline / hatch / caret`.

- **What to build.** A rule under the field that grows into an 8 dp bar from the left on focus (120 linear); the label in capitals above; error is a red hatch opened by a wipe with a 2 dp shake (200 linear, reduce motion: no shake); the caret is a 3 × 24 bar blinking 500 on / 300 off with no fade.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`
