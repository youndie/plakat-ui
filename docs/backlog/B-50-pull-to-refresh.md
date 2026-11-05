---
id: B-50
title: "Pull-to-refresh: a red wedge grows with the finger, then a stepped sector"
status: done
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-24]
---

# B-50 — Pull-to-refresh: a red wedge grows with the finger, then a stepped sector

Brief v1: "красный клин растёт сверху за пальцем, при отпускании переходит в ступенчатый сектор".

- **What to build.** The wedge follows the pull linearly; past the threshold, release turns it into the circular progress's stepped sector ([B-24](B-24-progress.md)); nested scrolling from a list.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`

## Findings

**Designed here (D13), every number the design does not state, for the owner's review.** The brief's row is one line, «красный клин растёт сверху за пальцем, при отпускании переходит в ступенчатый сектор»; no page draws it. The owner has not looked at it.

| What | Number or choice | From |
|---|---|---|
| Wedge | a red right triangle from the box's top left, as tall as the finger has come, its hypotenuse on the −17° axis, so at the kit's phone width a triangle to the end | the brief's «клин»; the shape is here, as the kit has one axis |
| Follows | the finger 1 to 1, with no damping | the item's «linearly» |
| Most | 120 | here; nothing passes its stop |
| Counts | at 80 | here |
| Sign it counts | a rule of 3 in ink along the edge, in a frame | step 5's «без цвета»: the red alone would not say it |
| Let go | at or past 80 it calls `onRefresh`; either way the wedge goes back in 120 hard-stop | the brief says it turns into the sector; here it goes as the sector comes, the app's `refreshing` deciding when |
| Sector | the circular progress's own, indeterminate, on a paper plate of 56 in the same corner | [B-24](B-24-progress.md); the plate is here, as its ring has no ground and it stands over a list |
| Event | `engage`, when `refreshing` turns on, as the button's sector does | here |
| Reduced motion | the wedge goes at once, and the sector stands as the progress's does | the item's default |
| Reader | a custom action named by the theme's `refresh`, and the sector described by its `loading` | the theme had both words waiting |

**No displacement.** The wedge and plate lie over the list, which stays where it is: a list moved down would be a second thing moving.

**A wheel makes no pull.** A wheel past the list's top has a remainder as a finger does but no letting go, so the wedge would have stuck there. A pull is only taken while a pointer is down.

**Not built:** a pull from a list that is not a nested-scroll child; a determinate wedge the caller drives.

**Not verified:** a phone, and its overscroll effect, which Android's scrollers may play beside the wedge.

**Checklist.** Primitives, radius 0, the one diagonal being the wedge's. The state reads without colour, by the wedge's size and the rule at 80. No target of its own. The ink rule on red is not text; the sector's contrast is the progress's.

**Tests.** `PlakatPullToRefreshTest`, 15, through a real scroll with a finger. Mutants, the aimed test named among the failures: the count at 80, the most, the plate, asking short, not asking past, the duration, the curve, reduced motion, the wedge going first on the way back, already refreshing, not enabled, the wheel, the rule, the event, the axis, the reader's action and description. The wheel and not enabled survived at first: the wheel test started at the list's top, where nothing is left over, and no test turned the box off.
