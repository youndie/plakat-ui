---
id: B-45
title: "Menu / Dropdown: a list on paper in a bar frame, opened by a wipe"
status: done
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-23]
---

# B-45 — Menu / Dropdown: a list on paper in a bar frame, opened by a wipe

Brief v1: "список на бумаге с брус-рамкой, раскрывается wipe сверху вниз".

- **What to build.** The list in an 8 dp bar frame, items as list rows ([B-23](B-23-list-item-and-divider.md)) with pressed inversion, opened by a top-down wipe (quick 120 linear), closed the same way.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`

## Findings

**Designed here (D13), every number the design does not state, for the owner's review.** The brief's row is one line, «список на бумаге с брус-рамкой, раскрывается wipe сверху вниз»; no page draws it. The owner has not looked at it.

| What | Number or choice | From |
|---|---|---|
| Frame | a bar of 8 | the brief's «брус», `plakat/line` |
| Rows | `PlakatListItem`'s, 56 and a hair, with their marker or number | the item |
| Width | 160 to 320, by its text | here, on the grid of 8; 320 stands in a phone's 358 |
| Open | wipe from the top, 120 linear | the item |
| Close | the same wipe back, 120 linear, gone after | the item's «the same way» |
| Event | `wipe`, on opening and on closing | here; the enum's row names the screen's bar, and now this. The menu has no row of its own |
| Reduced motion | at once, both ways | the item's default |
| Place | under its parent at the left edge, over it where it does not fit | here |
| Keyboard | focus on the panel, the arrows between items, Enter takes one, Escape closes | here |
| No screen | no scrim, a click outside closes it | here: a dialog has one, and a menu is not a scene |

**The last row.** Its hair would thicken the bottom bar by 1, and a row cannot know it is last, so `divider` is the caller's. A menu that leaves it on has a bottom bar of 9.

**A popup, the first in the kit.** The dialog is a sibling in the window, and the menu needs to stand over anything and past its parent's clip, so it is `Popup` with `PopupProperties(focusable = true)`. It takes focus on opening, on the panel and not an item, so that nothing looks chosen. Not told: the screen reader what it is, as the theme has no string for it. The fixtures draw the panel alone, as a fixture has no window for a popup; the popup itself is read in `PlakatMenuTest`, through the test's window of 1024 × 768.

**Not verified:** a phone. On desktop the popup keeps itself inside the window across, which a surviving mutant showed, so the line clamping it was removed. Android's and iOS's own popup placement has not been seen. The system back gesture is the popup's own property, not tested here.

**Checklist.** Primitives, radius 0, no diagonal, a row 56 high, a pressed row inverting by ground as well as colour. Text is the rows', held by `ContrastOverTextureTest` for the same inks on paper; the menu has no texture of its own.

**Tests.** `PlakatMenuTest`, 12. Mutants, the aimed test named among the failures: width's least and most, the duration, the curve, the wipe's side, reduced motion, the event on no change, the offset down and across, the flip, staying composed while closing, the frame's weight, the scrolling, both arrows, focus on opening. Two survived at first: the clamp, removed as above, and the scrolling, the test having read only that the last row was out of view; it now scrolls to it.
