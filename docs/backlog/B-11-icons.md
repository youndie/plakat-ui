---
id: B-11
title: "Icons: 16 PlakatIcons generated from plakat-icons.json"
status: done
priority: P1
size: S
stage: stage-1-materials
blocked_by: [B-01, B-05]
---

# B-11 — Icons: 16 PlakatIcons generated from plakat-icons.json

Sixteen stencil icons, one path each on a 24 dp grid, holes counter-clockwise under nonzero fill (the Icons page).

- **The decision and its reason.** Generate `ImageVector`s from `plakat-icons.json` by a committed script, `--check` in the gate. Colour comes from the parent (currentColor). The 16 SVGs were not vendored (research §1.1); on re-import their paths are compared with the JSON.
- Rejected: converting SVG files by hand.

- AC: each icon matches its cell at 48, 24 and 16 dp within the floor; the generator's check fails on a JSON edit not regenerated.
- Anchors: `reference/design-system/plakat-icons.json`, `reference/design-system/plakat-icons.js`, `reference/design-system/plakat-ui Иконки.dc.html`

## Findings (2026-11-04)

- **Stencils, not `ImageVector`s** (the decision above said `ImageVector`). Generated as vectors, the
  eleven icons of lines matched their cells at 0.00 %, the five with arcs did not: Search 24 5.73 %,
  Settings 16 7.03 %, up to 81 levels on the circle's edge. Compose's `PathParser` converts an SVG arc
  into cubics, Chrome draws it as conics, and Skia flattens the two differently. A throwaway test drew
  the same circles three ways against the references: parsed vector 3–36 pixels over ±16, `addOval`
  and `drawCircle` 0–7, an SVG-arc-to-`Path.arcTo` conversion 0 in 13 of 15 cells. So `PlakatIcons`
  holds `PlakatStencil`s (token name and path string), and `PlakatIcon` draws the stencil's path, built
  by its own walker with arcs as `arcTo`. Rejected: `ImageVector` with the parser's arcs (above);
  `drawCircle` per primitive (the JSON has paths, not primitives; the refresh arc is not a circle).
  Interop with an `ImageVector` API is not offered; nothing here needs it.
- **Parity**: 46 of 48 cells at 0.00 % (shape floor 0.00 %); Settings 48 at 0.17 % (4 pixels) and Error
  48 at 0.04 % (1 pixel), single-pixel antialiasing on the circle where Chrome's SVG `arcTo` builds its
  conics apart from `arcTo(oval)`. 42 of 48 goldens are byte-identical to their Chrome reference.
- References: `scripts/design-references.mjs` photographs each icon at 48, 24 and 16 from the Icons
  page's grid (`part: 'icon'`). Its readiness test never passed before (it asked for an `<x-dc>`,
  which the runtime replaces once rendered), so every page waited the 20 s timeout; it now waits for
  the template, fails when it never renders, and the 34 earlier references re-render byte-identical.
- `scripts/icons.mjs` writes `PlakatIcons.kt` and the 48 fixtures; `--check` is in `make check`. It
  also fails when `plakat-icons.js`, which the page draws, disagrees with the JSON path for path, and
  when an icon uses a command the stencil does not draw.
- Colour from the parent: `LocalPlakatContentColor` (the CSS `currentColor`), unspecified outside any
  component, then the theme's ink.
