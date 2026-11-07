---
id: B-68
title: "On release a word was ink on ink for its first frame"
status: done
priority: P3
size: XS
stage: stage-2-components
epic: feature-components
---

# B-68 — On release a word was ink on ink for its first frame

On every release of a press, the word is ink on ink, or cream on cream, for the first frames: Outlined, Text and Danger buttons and ListItem on both papers, Filled on black. This is deliberate, copied from the CSS, where only the ground transitions. It reads as a blink on each press.

The question for the owner was: keep the design's transition, or let the ink follow the ground, as the card's words were made to in [B-56](B-56-card-press-words.md). On 2026-11-07 the owner left it to the loop.

- **The decision.** A word turns over in one frame when its ground is halfway back (`inkPressed`), on a button, a list item, an icon button, a FAB and a drawer row. The spec row itself says `release` returns the paints, «Возврат красок и сдвига», and only the CSS leaves the word out, so the page and its own table disagree, and this follows the table.
- **Rejected.** Keeping the CSS: a blink on every press of the kit's commonest control, 1.01:1 on the frame after the finger lifts. A word blended along with its ground, as "follow" reads literally: on Outlined, the word goes paper to ink and the ground ink to paper, so they meet at the same grey halfway and the blink stays. Picking per frame whichever ink reads better: more code for one frame, and it needs to know what lies under a transparent ground.
- **Numbers**, the best contrast of anything in the control against its ground per 16 ms frame after the lift, on this Mac's test runtime. Paper, Outlined, ListItem and IconButton alike: before 1.01, 4.75, 8.35, 11.3, 13.7; after 13.7, 4.75, 8.35, 11.3, 13.7. Black paper, Filled and FAB alike: before 1.01, 2.26, 3.29, 4.08, 4.64; after 13.4, 2.26, 3.29, 4.08, 4.64. The worst left is 2.26:1 on one frame of 60 ms, under the 4.5:1 of text at rest, and is what a ground passing through mid-tones allows.
- **Moved.** No golden: the design draws rest and pressed, and the frames between are new. The test that held the old rule, `on release the ground fades while the slogan's colour changes at once`, says the new rule now, as the rule itself changed.
- AC: no frame of a release has a word under 2:1 on its ground, on either paper, by a real press; the old rule put back fails it.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatButton.kt`
