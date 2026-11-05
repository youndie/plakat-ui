---
id: B-18
title: "Snackbar: the telegram"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-18 — Snackbar: the telegram

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `telegram`.

- **What to build.** A black plate with cream text that rides up from the bottom to the stop (200 hard-stop), then its text opens by a wipe from the left (120 linear); hides down the same way. Reduce motion: fade 100.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`

## Findings (2026-11-05)

- **API**: `PlakatSnackbar(message, modifier, visible, actionLabel, onAction, error, onHidden)`. The
  plate is the component and its show and hide are driven by `visible`; a queue and a host
  (Material's `SnackbarHostState`) are left to the app. The plate is a polite live region.
- **Parity**: the Core page's three Snackbar cells in both themes (`Snackbar_*`, 344 × 56).

  | Cell | Whole cell | Text, of its ink | Shapes |
  |---|---|---|---|
  | Default L / D | 1.76 / 1.69 % | 31.8 / 30.2 % | the plate exact |
  | Action L / D | 1.96 / 1.94 % | 27.6 / 27.3 % | the rule exact (x 257–334, rows 36–38) |
  | Error L / D | 2.18 / 2.13 % | 14.9 / 14.6 % | the 8 px hatch strip 0 px |

  No whole-pixel shift of the words lowers the difference; what is left is glyph edges, under the
  63.7 % text floor. `<b>` asks Golos for 700 and Chrome takes the 600 face without synthesising, so
  the error word is 600 here too.
- **Motion**: shown, the plate rides 200 ms hard-stop from `calc(100% + 24px)` below its stop, and its
  words — the message and the action, each its own box — open by a wipe from the left in 120 ms,
  linear, 200 ms after (two plays started together, so the delay is exact). Hidden, the words go at
  once and the plate rides down 200 ms hard-stop: the lab transitions the words' clip on the way in
  only. Reduced motion: a 100 ms linear fade both ways, the plate at its stop and the words whole.
  At rest below its stop the plate is not drawn at all.
- **Events**: `telegram` on show and on hide; `onHidden` once the plate is down (or faded out).
- **Designed here** (D13): the action pressed — inverted on a paper plate, the word and rule ink, as
  every pressed control of the kit inverts (the page draws no pressed action); an error with an
  action (the hatch over a row that also holds the action).
- **Mutants**: through the behaviour tests — the rise linear, the words without their 200 ms delay,
  the words over 200 ms, the words kept while hiding, the hide linear, the ride from 16 below, the
  fade 200, the action 88 wide, no pressed plate, the hidden plate still drawn; through the goldens —
  the rule 4 below the word, the strip 6, the hatch period 8, the message 16 sp, the plate 48. All
  fifteen killed.
