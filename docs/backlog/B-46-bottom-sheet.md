---
id: B-46
title: "BottomSheet: a sheet riding up on rails, the handle is a bar"
status: done
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-28]
---

# B-46 — BottomSheet: a sheet riding up on rails, the handle is a bar

Brief v1: "лист выезжает по рельсам, ручка — брус".

- **What to build.** A paper or concrete sheet riding up vertically to a hard stop (scene 320 hard-stop), an 8 dp handle bar, the scrim closed by the Dialog's diagonal wipe ([B-28](B-28-dialog.md)); drag follows the finger on the rail and settles with a hard stop.
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`

## Findings

**Designed here (D13), every number the design does not state, for the owner's review.** The brief's row is one line, «лист выезжает по рельсам, ручка — брус»; no page draws it. The owner has not looked at it.

| What | Number or choice | From |
|---|---|---|
| Ride | up and down, 320 hard-stop | the item |
| Screen | the dialog's, its diagonal wipe, 320 linear | the item and [B-28](B-28-dialog.md) |
| Ground | paper or concrete, flat | the item |
| Handle | a bar of 8, 64 wide, centred, in a touch target of 48 across the whole width | 8 is the brief's «брус», 64 is here, on the grid |
| Tallest | 96 short of its box, the rest scrolling | the dialog's `top: 96px` |
| Drag | by the handle only; the finger's own place, and never above its stop | the item; only the handle, as the content may scroll and the kit has no nested connection for it |
| Let go | goes over half, or flicked faster than 600 dp/s | here |
| Settle | back in 120 hard-stop | the item says «hard stop»; 120 is the `quick` token, for a short way |
| Refused | asked to go and told no, it settles back, after 2 frames for the answer | here |
| Event | `rail` on opening and on closing, none for a drag | here; the enum's row names the Window and the FAB, and now this |
| Reduced motion | at once, both ways, and the settle too | the item's default; the dialog's page says fade, this one says nothing |
| Reader | a dialog, `paneTitle` the caller's as no title is drawn, and a dismiss action | here |

**Shared with the dialog:** its screen, now `PlakatDotScreen`, unchanged, as its goldens hold. Not shared: the sheet moves by one `animate` at a time on a single float, since the finger takes it over mid-way, where the dialog's one clock has nothing to interrupt it.

**Not built:** the safe area at the foot, as nothing in the kit reads window insets yet, so on a phone with a gesture bar the last row stands under it unless the caller pads the content; a sheet that stops half way; a width limit on a wide screen.

**Not verified:** a phone, and so a real finger's flick. The back gesture is the dialog's same handler, not tested here.

**Checklist.** Primitives, radius 0, no diagonal but the screen's, which is the dialog's; the handle's target 48. The bar's ink on paper and on concrete are the same pairs `ContrastOverTextureTest` holds. Flat: the sheet's edge is the change of ground, no shadow.

**Tests.** `PlakatBottomSheetTest`, 14. Mutants, the aimed test named among the failures: handle's width, the inset, the share, the flick, the ride's duration, its curve, the settle's duration, the stop's clamp, settling when refused, the event on no change, staying composed while closing, the screen's click, the content's scroll, the ground, the flick's branch, the screen's clock. Three survived at first: staying composed and the screen's clock, whose frames the test read too loosely and now reads on their curves; and an empty `pointerInput` copied from the dialog to stop presses reaching the screen, which the content's scroll already does, so it was removed.
