---
id: B-14
title: "TextField: underline to bar, hatch error, square caret"
status: done
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12]
---

# B-14 — TextField: underline to bar, hatch error, square caret

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Ядро.dc.html` with its motion row `underline / hatch / caret`.

- **What to build.** A rule under the field that grows into an 8 dp bar from the left on focus (120 linear); the label in capitals above; error is a red hatch opened by a wipe with a 2 dp shake (200 linear, reduce motion: no shake); the caret is a 3 × 24 bar blinking 500 on / 300 off with no fade.
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`

## Findings (2026-11-04)

- **API**: `PlakatTextField(value, onValueChange, modifier, label, placeholder, error, enabled,
  keyboardOptions, keyboardActions, visualTransformation, interactionSource)`. The String API runs over
  a `TextFieldValue` as Material's does, because the caret is the kit's own and needs the selection.
  An `error` shows the hatch and «ОШИБКА.» (the theme's `strings.error`) before the message, and sets
  the semantics' error. The field takes the width it is given, as a CSS block does.
- **References**: five cells per theme added to `scripts/design-references.mjs` (`part: 'above'`, the
  label to the caption); the error cell is 148.78 px (the message wraps to two 18.9 px lines) and is
  cropped to 149. The earlier 82 references re-render byte-identical.
- **Parity** (shape floor 0.00 %, text floor 63.7 % of ink): every pixel outside the text matches —
  the rule, the 8 dp bar, the caret, the hatch, the dashed hair; the text at 38–50 % of its ink.
  Cells 1.33–3.16 %.
- **The baseline, a pixel low**, was the field's text and the message: a shift search put both a
  pixel below Chrome (Error 5.80 %, Filled 3.48 %). Blink rounds ascent and descent each and floors
  the half-leading; `cssLine` lays the text in Blink's line box with the leading split so the
  baseline lands in Blink's pixel (Error 3.16 %, Filled 2.10 %). Tried on the button's labels too:
  no change, they already sat right; left as they were.
- **Caret**: the native cursor is transparent; a 3 × 24 red bar is drawn 2 dp after the cursor's
  position (the page's flex `gap: 2px`), blinking 500 on / 300 off with no fade and restarting on
  each edit or move; it stands under reduced motion. A long single line scrolls inside
  `BasicTextField` and the caret ignores that scroll — not drawn by the design, left.
- **Error**: when an error appears, the hatch (red on transparent, the page's
  `repeating-linear-gradient`) opens from the left over 200 ms linear while the field jolts 2 dp and
  back (100 + 100 ms linear, the live lab's numbers); `hatch` is reported. When the error clears, the
  hatch and message go at once — the page's lab keeps an empty hatch row, the static cells have none;
  the static cells are followed, so a field without an error takes no space for one (decided here).
- **Events**: `underline` on gaining focus, `hatch` on an error appearing, once each.
