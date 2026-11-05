---
id: B-55
title: "A masthead throws on a phone's width: the stretch's refusal at drawing"
status: done
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

## Findings

**The decision.** `PlakatStretch` no longer throws. It holds the stretch at 1.5 or 4 and comes short of its box: of the width from the left when too short a word would flatten, keeping the height; of the height with its baseline at the foot when too long a word would tear, keeping the width. Either way the ink stays in its box, and every in-range cell is untouched, so no golden moved.

**Rejected:** the masthead alone narrowing its title, as `require` would still throw in any application handing a user's word to `PlakatStretch`; a lower masthead, as its height is the page's. **This overturns B-31's refusal, and with it the page's sketch's `require`**, whose reason is kept: the rule of 1.5 to 4 still holds of what is drawn, only the means changed. For the owner's review, with where the line comes short: the left and the foot are chosen here (D13).

**At real widths.** A masthead on the phone's 379 dp comes 6 dp short of the rule at its right. At 840 dp «ЛИСТ» stands in about the left 370: a tablet's masthead with a short word leaves room, which is what the design's rule asks for.

**Other uses read.** The poster's Растяжка has a fixed 390 × 600 and its three page lines are in range. The counter's column boxes go by its digits, so its stretch does not move with the screen. Both only ever gain from this: neither can now throw on a word it is handed.

**`plakatStretchFactor`** stays and still says the true ratio, so a caller can see beforehand that a line will not fill its box.

**Not verified:** the phone was disconnected by the time of this item, so the fix has run at its width, 379 and 411, only in the test window. `bench-android`'s `edges` mode has «ЛИСТ» back for the next time it is attached.

**Tests.** `PlakatStretchTest`: the fit at each limit by its ink, the page's bad cell, the table of heights held within limits, and a sweep of the masthead at 7 widths from 320 to 840 with words of 3 to 12 letters, captured as drawing is where it threw. Mutants below.

