---
id: B-17
title: "Card: sheet of concrete paper, print appear, tear dismiss"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-17 — Card: sheet of concrete paper, print appear, tear dismiss

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `print / tear`.

- **What to build.** Surface is a change of paper, no shadow; the accent is a red bar on the left edge. Appear is a diagonal wipe from the top-left (200 linear, cascade 30, reduce motion fade 100); dismiss rides off along the swipe rail and the neighbours close up with a hard stop.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatCard(onClick, modifier, selected, enabled, printOrder, dismissed, onDismissed,
  interactionSource, content: RowScope.() -> Unit)`. Material's `Card` takes a column; the page draws
  a card only as a row (thumbnail, then text, `gap: 16px`, centred), so the slot is a row.
  `onClick = null` only shows. The sheet is at least 48 dp tall (`plakat/size/touch`); the page's
  cards are 88 and 104.75.
- **Parity**: the references are the Core page's four Card cells in both themes (`Card_*`), cropped to
  the floor of the sheet's fractional height; Focused grows 6 px a side for the ring
  (`outline: 3px; outline-offset: 3px`). Their thumbnails come from the page's **own** generator,
  `comp(47 · 7 + i · 131, 64, 64, …, { dot: .4 })`, not from `plakat-gen.js`; it is ported for the
  fixtures only (`parity/PageThumbnail.kt`).

  | Cell | Whole cell | Sheet, padding, bar, ring | Thumbnail | Text, of its ink |
  |---|---|---|---|---|
  | Default L / D | 7.65 / 7.70 % | 0 px | 666 / 665 px (2 px low) | 51.7 / 52.4 % |
  | Pressed L / D | 8.05 / 8.02 % | 0 px | 666 / 665 px (2 px low) | 54.9 / 54.5 % |
  | Selected L / D | 4.63 / 4.60 % | 0 px | 0 px | 37.2 / 37.1 % |
  | Focused L / D | 2.88 / 2.89 % | 0 px | 0 px | 17.5 / 17.6 % |

  Text is under the 63.7 % floor everywhere. The three-line titles (Default, Pressed) carry a cause
  of their own: Compose lays 21.25 px lines (17 px at 1.25) on whole pixels, 22 apart through
  `LineHeightStyle` (66 px for Chrome's 63.75; without a style 21 apart, but with the second baseline
  still a pixel off), so their second and third lines sit a pixel low and the thumbnail, centred
  against the taller paragraph, 2 px low (21 px still differ once shifted back, Chrome's thumbnail
  being on a fractional row there). The component's own geometry is exact in all eight cells; the
  residual belongs to the demo text's layout and is recorded as a quirk of the service.
- **Motion**: `print` is the shared diagonal wipe (`PlakatWipe.Diagonal(60.dp)`, the page's
  `polygon(0 0, x 0, x − 60px 100%, 0 100%)`), 200 ms linear, 30 ms per step of `printOrder`; under
  reduced motion a 100 ms linear fade, no cascade (the page's RM transition has no delay). `tear` is
  one 400 ms timeline: the ride (`translateX(110%)`) over its first half and the collapse of the place
  over the second, each on the hard-stop. Two `animateTo`s in a row started the collapse a frame and
  a half after 200 ms; the page's `height … 200ms` delay is exact, so one timeline. A torn card is
  clipped to its place, as the page's `overflow: hidden` wrapper does.
- **Events**: `print` when a card with a print order enters the composition; `tear` on each change of
  `dismissed` to true. `onDismissed` is called once, when the place has closed.
- **Designed here** (D13): `enabled = false` keeps the look and takes the click (the page draws no
  disabled card); `dismissed` back to false returns the card at once (the lab's ВЕРНУТЬ, with
  transitions off); `printOrder = null` for a card that should not print, for lazy lists.
- **Mutants**: through the behaviour tests — the wipe eased hard-stop, the cascade dropped, the slant
  30, the fade 200, the ride 100 %, the collapse running with the ride, the ride linear, the content
  left ink when pressed, a 40 dp minimum; through the goldens — padding 11, the selected bar not red,
  the ring 1 dp off, the pressed sheet concrete, the gap 8. All fourteen killed.
