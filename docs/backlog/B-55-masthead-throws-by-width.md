---
id: B-55
title: "A masthead throws on a phone's width: the stretch's refusal at drawing"
status: open
priority: P1
size: S
stage: stage-2-components
epic: feature-components
blocked_by: [B-53]
---

# B-55 — A masthead throws on a phone's width: the stretch's refusal at drawing

Found in [B-54](B-54-window-insets.md), on a Pixel 6a: `PlakatMasthead("ЛИСТ")` on the whole width crashes the app. The page's own word in its own 120 high stretches by 1.56 at 358 wide, and by 1.47 at the phone's 379, under `PlakatStretch`'s 1.5, which [B-31](B-31-stretch-text.md) has it refuse with an exception at drawing. So a design's own use of the kit throws by a screen's width, or a word's length, and no test saw it, as they all stand at 390.

- **What to decide, then build.** A masthead's height is fixed and its width is the screen's, so it must do something at any width and with any word. Not a throw in a user's hands. Candidates: the masthead's title keeps the stretch inside its limits by narrowing or lowering its box; or `PlakatStretch` itself clamps rather than throws, and `plakatStretchFactor` stays for who wants to ask. The rule that the stretch stays within 1.5 to 4 is the design's and stays.
- AC: a masthead draws at widths from 320 to 840 with a one-word title of 3 to 12 letters, none past the design's stretch; a test sweeps them; a mutant is caught; the kit's other uses of `PlakatStretch`, the poster's Растяжка among them, are read for the same.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatStretch.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTopAppBar.kt`
