---
id: B-24
title: "Progress: the wedge filling a bar, the stepped sector"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-24 — Progress: the wedge filling a bar, the stepped sector

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `— (value) / — (indeterminate)`.

- **What to build.** Linear: a red wedge grows linearly to the value inside a bar. Circular indeterminate: a 90° sector turning by 30° steps (cycle 1200, steps 12; reduce motion: the sector stands).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatLinearProgress(value, modifier)` and `PlakatCircularProgress(value, modifier)`, a null
  value being the indeterminate sector, 40 dp unless the modifier sizes it (the page's inline is 24).
  Progress is not a target, so the 48 dp of the AC does not apply. The page draws no indeterminate linear,
  so there is none.
- **Parity** (`Progress_*`, the MVP page): Linear 0, 40 and 100 % at **0.00 %**. Circular 25 %, 70 %, indeterminate and
  inline at 0.25–0.38 %: 2 to 6 antialiased pixels at most 29 levels off, on the ring's edge. The linear
  cells sit in a column of 218.67 px, rounded as Blink snaps them (219 or 218).
- **Three findings in the circular**:
  - The ring is a difference of two circles, as `box-shadow: inset` is; as a `Stroke` it was 1.81 %, every
    outer edge pixel up to 52 levels off (catalogue, "Paint").
  - The determinate sector is a hard-stopped sweep rather than an arc, since a `conic-gradient`'s stops
    are not antialiased: that is its visible stair at 70 %.
  - **A `Canvas` under a minimum alone draws nothing.** It is a `Spacer`, which reports 0 and sits at the
    centre of the minimum: the circular at its default size was an empty 40 dp box whose semantics said 40,
    while the fixtures, which size it, were right. The tests of the default size found it; it is a `Box` now.
- **The renderer stops animations.** The page's `plspin` runs on the page's own switch, not on the media
  query, so the reference was wherever the clock had got to. Every animation is now paused at its first frame
  before a shot; the earlier references re-rendered byte-identical.
- **Motion**: the sector is `spinnerDegrees`, shared with the Button: ⌊t / 100⌋ · 30°. The linear row's «по данным ·
  linear» names no time, so **a new value is reached in 200 ms (`plakat/motion/normal`), linearly: designed here**,
  and the circular determinate takes the same; a first value is simply there. No events: the row's are «—».
- **Designed here** (D13): the 200 ms above; a value below the slant's reach draws the part of the wedge inside
  the bar; values outside 0…1 are held to it.
- **Mutants**: through the behaviour tests — growing hard-stop, over 120, under reduced motion, the slant 13, a
  full bar keeping its slant, the sector continuous, a cycle of 1800, turning under reduced motion, 120° wide, the
  sweep from 3 o'clock, counter-clockwise; through the goldens — a bar of 6, a ring of 4, the track in paper. All
  fourteen killed.
