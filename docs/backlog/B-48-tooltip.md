---
id: B-48
title: "Tooltip: a caption with a straight leader line, as on a drawing"
status: done
priority: P2
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-48 — Tooltip: a caption with a straight leader line, as on a drawing

Brief v1: "подпись-выноска с прямой линией-указателем, как на чертеже".

- **What to build.** A concrete caption plate with a 1 dp hairline leader to the anchor, at 0°, 90° or the −17° axis; appears by a wipe along the leader (quick 120 linear).
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`

## Findings

**Designed here (D13), every number the design does not state, for the owner's review.** The brief's row is one line, «подпись-выноска с прямой линией-указателем, как на чертеже»; no page draws it. The owner has not looked at it.

| What | Number or choice | From |
|---|---|---|
| Plate | concrete, padding 8, ink words in `bodyS`, at most 240 wide and wrapping | the item for concrete; the rest here. Body case, as a caption is a sentence and capitals stop at three words |
| Leader | a hair, 24 long, along 90°, 0° or −17° | the item for the hair and axes; 24 is here, on the grid |
| Where | upright: up from the anchor's middle, plate reaching right from the line. Level or tilted: from the anchor's side, plate centred on the line's end | here |
| No room | upright goes down, and its plate reaches left; level and tilted go left | here |
| Tilted to the left | on down the same line, never its mirror | the page's «диагональ в другую сторону — вторая ось» |
| Appears | a wipe along its leader, from the anchor's end, 120 linear; goes back the same way | the item |
| Event | `wipe`, on showing and going | here |
| Reduced motion | at once, both ways | the item's default |
| Pointer | shows after resting 400 ms; a press or leaving takes it away | here |
| Finger | shows after the platform's long press, while held; moving past the slop first takes that back | here |
| Keyboard | shows while the anchor has the keyboard's focus | here |
| Reader | its text as the anchor's description, without its showing | here |

**A found defect, fixed in place.** A press focuses what it lands on, and the input mode can read `Keyboard` meanwhile (it does in the test's window), so a finger's tap would have left the caption up for good. A press now marks the focus it gives as not the keyboard's. The kit's other controls read `focusShown()` for a ring, and may show a ring on a tap in that mode; not looked into here.

**A hover only by a mouse.** Reading «not a finger» would count any other pointer.

**A popup again, and how it finds its place.** Unlike the menu's it must reach the far side of a line whose start depends on where the anchor stands, so one pure function, `tooltipPlacement`, gives the box, the plate and the line, from the anchor's bounds in the window and the window's size. The window's size is `LocalWindowInfo.containerSize`, and the position provider moves the box by the difference from the bounds it is given. A popup keeps inside the window on its own, so a long plate at a window's corner could leave its leader; the flips above make it rare, and it has not been seen on a phone.

**Not built:** a tooltip the caller opens by a state; Escape to dismiss it, which WCAG asks for of hover content, as the popup takes no focus and so no keys; a rich tooltip.

**Not verified:** a phone, and a real finger. The window is the test's, 1024 × 768.

**Checklist.** Primitives, radius 0. One diagonal, the leader's, only when asked for on `Tilted`. Text is the pair ink on concrete, which `ContrastOverTextureTest` holds. Its target is the anchor's. It takes no input.

**Tests.** `PlakatTooltipTest`, 19. Mutants, the aimed test named among the failures: the leader's length and weight, the plate's width and ground, the rest's 400, the duration, the curve, both flips, the plate's reach, the axis, its line on the left, each of the wipe's four sides, the press's focus, the keyboard's focus, the event. The plate's width and the wipe's other three sides survived at first, only the upward one having been read, so each is now read at half its wipe.
