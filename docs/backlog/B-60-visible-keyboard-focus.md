---
id: B-60
title: "Keyboard focus shows on every control it lands on"
status: open
priority: P1
size: M
stage: stage-2-components
epic: feature-components
---

# B-60 — Keyboard focus shows on every control it lands on

Tab reaches Menu items, ListItem, Tabs, NavigationBar and the Snackbar's action, and nothing changes on screen: 0 pixels differ with a focused node present (QA F03). On a menu, whose KDoc promises "the arrows moving between its items", the arrows move blindly. Button, Chip, Card, Slider and the Drawer's rows do show it, following the Principles page's step 05, a frame of 3 with an offset. Down past a menu's last item lands on the panel, a dead stop where Enter does nothing (F35).

- **The decision.** The same keyboard ring as the kit's other controls, drawn only under `InputMode.Keyboard`, on every clickable surface that lacks it; the arrows in a menu wrap within its items, so focus is always on something Enter can use. D13 applies: these looks are not drawn by the design's pages and are designed here from its rule.
- **Rejected.** A filled highlight as on a desktop menu: it would read as selection, which the menu's own comment rules out, and is a second focus language.
- **Not covered.** Arrow keys inside Tabs, NavigationBar and Segmented, which were read in the source and not exercised; where focus goes when an overlay opens and closes: [B-63](B-63-overlay-focus-and-input.md).

- AC: tabbing through each of the five draws the ring, a click leaves none; arrows in a menu never rest on the panel; goldens at rest do not move; the focused look is held by a test reading its pixels on both papers, which fails with it removed, not by new goldens, which would add edges the runner's macOS can differ on (B-37).
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatListItem.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatMenu.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTabs.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatNavigationBar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSnackbar.kt`
