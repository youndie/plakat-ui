---
id: B-13
title: "Button: filled, outlined, text, danger; stamp, release, engage"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-13 — Button: filled, outlined, text, danger; stamp, release, engage

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `stamp / release / engage`.

- **What to build.** The red plate with the cut corner and an upper-case slogan; outlined is a 3 dp frame; text is underlined by a rule; danger is the hatch with a word on a paper plate (Tokens page «СЕМАНТИКА»). Pressed inverts the inks in one frame and shifts the text 1 dp right-down; release returns in 60 ms hard-stop; loading closes the text with a wipe from the left and turns a 90° sector by 30° steps (cycle 1200, steps 12).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`

## Findings (2026-11-04)

- **API**: `PlakatButton` (filled, with `loading`), `PlakatOutlinedButton`, `PlakatTextButton`,
  `PlakatDangerButton`, each `(text, onClick, modifier, enabled, interactionSource)` as Material's
  are; one internal frame draws all four. The slogan is a string set in the Button role, upper-cased.
- **Parity** (shape floor 0.00 %, text floor 63.7 % of ink): the 22 cells sit at 0.00–3.42 % of the
  cell; outside the slogan every pixel matches but two named residuals, and the slogans are at 40–52 %
  of their ink, under the text floor. Loading 0.00 % in both themes (the sector at 100 ms = 30°).
  - *Pressed cut edge*: 2–4 pixels on the diagonal over ±16 (≤ 21 levels). The red plate's edge is
    identical, the ink one is not: Chrome's coverage on the same diagonal differs by ±0.08 between
    ink-on-paper and paper-on-ink. A renderer residual; nothing in the geometry to change.
  - *Underline end*: Compose's advance for «ЧИТАТЬ» is 74.76 px, Chrome's span 74.00, so the rule
    ends 0.76 px later (one antialiased column, 3 pixels) and the disabled dashes fall a little apart.
    The rule is drawn from the line's fractional right, not the ceiled box (which was a column more).
- **Dashed borders as Blink draws them** (`blinkDashes`): dash 2w and gap w from 3 px, 3w and 2w
  below, the gap chosen so a side starts and ends on a dash (`StrokeData::SelectBestDashGap`), each
  side its own line. A plain `dashPathEffect` was 12.5 % off the disabled outlined cell; this is 2.68 %,
  the slogan alone.
- **Release**: the ground and the 1 dp shift follow 60 ms hard-stop; the slogan's colour changes at
  once (CSS transitions `background-color` and `transform` only — the frame sheet showed the dark
  theme's slogan fading through grey before this). Colour in sRGB, not Compose's Oklab `lerp`.
- **Focused**: the ring is the page's `outline: 3px; outline-offset: 3px`, drawn outside the box,
  for keyboard focus only (a click focuses and must not ring). The reference crop grew by 6 px a side
  (`scripts/design-references.mjs`); the two Focused references were re-rendered, the rest unchanged.
- **Designed here** (D13, revised in the owner's review): the danger button's pressed look (the word's
  plate inverts, the hatch stays) and disabled look (hatch and word in muted ink); the focus ring on
  outlined, text and danger buttons (the page draws it on filled only); the horizontal padding 16 dp
  (`plakat/grid/margin`, the Tokens page's primary swatch `padding: 0 16px`) where the cells are
  column-wide; minimum width 48 dp. The danger word is 13 sp with a 10 × 6 plate and 8 dp inset, the
  Demo page's numbers; the Tokens page's swatch (12 inset, 8 × 4 plate, red word) is the other
  rendering, not followed.
- **Events**: `stamp` on press and `release` on release, from the input; `engage` once when loading
  starts. Reduced motion: release instant, the slogan's wipe instant, the sector standing at 0°.
- Not built: an icon in a button (the design draws none).
- **Mutants**, each run against the committed code: release eased linearly — survived the first tests,
  killed by `the plate follows the hard-stop curve frame by frame`, written for it; release 200 ms, no
  `stamp`, eight sector steps, a loading button still clickable — each killed by its aimed test; the
  pressed shift 2 dp and Blink's gap ratio changed — killed by the goldens (pressed and disabled
  outlined cells).
