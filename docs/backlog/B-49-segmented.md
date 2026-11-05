---
id: B-49
title: "Segmented button: one frame, the black fill rides linearly"
status: done
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-22]
---

# B-49 — Segmented button: one frame, the black fill rides linearly

Brief v1: "общая рамка, выбранный сегмент залит чёрным, заливка переезжает линейно". Drawn once inside the Settings demo (theme switch: 40 dp, 3 dp inset frame, ink fill, label style).

- **What to build.** That control as a component: the fill rides linearly between segments (normal 200 linear), the label inverts as the fill passes. Height 40 drawn, target 48.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Демо.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`

## Findings

**Parity.** The Demo page's settings draw it, so that cell has references, in each theme, rendered by `scripts/design-references.mjs`: `Segmented - Demo_Light` 5.27 %, `Demo_Dark` 5.26 %. Read against the text floor, as the geometry is the same to the pixel (frame, fill's end, the words' first and last columns and rows): 54 % of the words' ink against 63.7 %. The page's dark screen has its second segment chosen and its light one the first, and the fixtures do as it does.

**The demo uses it now.** `Demo.kt` drew the control by hand until the kit had one, and that code is gone. Its settings screen changes by 381 and 423 pixels, and moves closer to its reference: in that box 373 pixels off to 170 in the light theme, 616 to 379 in the dark, as a word's place now comes from its real width and not a rounded one. So `Demo - Settings` and `Gray - Settings` are re-recorded, in both themes, by this item.

**Designed here (D13), every number the design does not state, for the owner's review:**

| What | Number or choice | From |
|---|---|---|
| Motion | the fill rides, 200 linear, both its edges | the item; the page's has no transition |
| Words | invert just where the fill has reached them: drawn twice, the second under a clip of the fill | the item's «as the fill passes» |
| Event | `rail` | here, as the Tabs' fill does |
| Reduced motion | at once | the item's default |
| Given more width | segments share what is over, equally | here |
| Pressed | the segment inverts, off or on, `stamp` | step 5 of the Principles page |
| Focused | a frame of 3 about the segment's target of 48 | step 5; it stands over and under the drawn 40 so that it can be told from the control's own frame |
| Disabled | the frame a muted hair, fill and words muted, all at once | step 5; there is no disabling one segment |
| Reader | a group of radio buttons, each by its word | here |

**The page's frame** is a `box-shadow: inset`, so the fill lies over it, and here the fill is drawn after it for the same picture.

**Not built:** several chosen at once, and icons in a segment. The words are one line each.

**Not verified:** a phone.

**Checklist.** Primitives, radius 0, no diagonal; the choice reads by the fill's shape, in ink, not by a colour; target 48; words ink on paper and paper on ink, the kit's first pair.

**Tests.** `PlakatSegmentedButtonTest`, 11. Mutants, the aimed test named among the failures: the drawn height, the word's inset, the duration, the curve, the event, reduced motion, the hair, the muted ground, the clip's absence and the second drawing's, the share of width, the press's inversion and its word, the fill's far edge, the target's height, the role, the focus frame. The pressed word and the focus frame were held by goldens alone at first, which a small feature can pass, so tests read them.
