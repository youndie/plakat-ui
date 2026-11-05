---
id: B-47
title: "NavigationDrawer: black panel with cream capital items"
status: done
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-26]
---

# B-47 — NavigationDrawer: black panel with cream capital items

Brief v1: "панель с чёрным фоном и кремовыми прописными пунктами".

- **What to build.** An ink panel riding in from the left on a rail (normal 200 hard-stop), items in capitals (label style), the active one on the NavigationBar's red square ([B-26](B-26-navigation-bar.md)).
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`

## Findings

**Designed here (D13), every number the design does not state, for the owner's review.** The brief's row is one line, «панель с чёрным фоном и кремовыми прописными пунктами»; no page draws it. The owner has not looked at it.

| What | Number or choice | From |
|---|---|---|
| Ride | in and out from the left, 200 hard-stop | the item |
| Panel | 280 wide, the box's height | here, on the grid, leaving a phone's 390 some 110 to tap |
| Ground | ink in the light theme, concrete in the dark | the navigation bar's rule, now one function; the brief's «чёрный фон» read as the darker of the two |
| Items | the bar's `PlakatNavigationItem`, a stencil and its word, in on-red, the word in the label style in capitals | the item and [B-26](B-26-navigation-bar.md) |
| Rows | 56, `plakat/size/field`, inset 16, 16 between stencil and word | a list row's |
| Active | the bar's red square of 40 × 32 behind its stencil, moved in 120 hard-stop, `lock` | the item and the bar |
| Pressed | the row inverts, cream with ink words, `stamp`, 60 back | a list row's |
| Focused | a frame of 3 inside the row, in the word's colour | step 5's «рамка 3»; inside, as the panel's edge is its row's |
| Screen | the dialog's, its diagonal wipe, 200 linear, to match the ride | the item does not name it; a panel over a screen needs an outside to tap |
| Vertical inset | 16 at top and foot | here, the kit's margin |
| Event | `rail` on opening and closing | here |
| Reduced motion | at once, all of it | the item's default |
| Reader | a dialog, `paneTitle` the caller's, rows as tabs by their words, a dismiss action | the bar's roles |

**A word is one line**, clipped if it is too long: the square's place needs rows of one height, and the brief keeps capitals to three words.

**Not built:** a swipe from the edge to open or to close it, which the item does not ask for and the kit has no pattern for yet; a permanent panel beside the content, for a wide screen; a header; the safe area, as with [B-46](B-46-bottom-sheet.md).

**Not verified:** a phone. The back gesture is the dialog's handler, not tested here.

**Checklist.** Primitives, radius 0, no diagonal but the screen's. The active row reads without colour by its square's shape. On-red on the bar's grounds and on red are the pairs the bar already rests on (the Icons page's 4.64:1). Row 56.

**A press on the panel below its rows** does not reach the screen because the column's `verticalScroll` takes it, which `PlakatNavigationDrawerTest` reads; a mutant removing the scroll fails there too.

**Tests.** `PlakatNavigationDrawerTest`, 11. Mutants, the aimed test named among the failures: the panel's width, the square's width, height and place, the ride's duration and curve, the screen's clock, the square's duration, `lock`, `rail` on no change, staying composed while closing, the screen's click, the row's ground and word when pressed, the scroll, the inset, the words for the reader. The pressed word survived at first, so its colour is now read.
