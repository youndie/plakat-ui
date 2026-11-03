---
id: B-05
title: "Pixel references rendered from the spec pages in headless Chrome; the parity floor"
status: done
priority: P0
size: L
stage: stage-0-risks
blocked_by: [B-01, B-02]
---

# B-05 — Pixel references rendered from the spec pages in headless Chrome; the parity floor

The components exist only as inline-styled elements of the spec pages (research §1.2), and the pages pull React, Babel and the fonts from the network.

- **The decision and its reason.** A Node script opens each page from `file://` in headless Chrome at device scale 1, serves the three runtime scripts and the repository's font files in place of unpkg and Google Fonts, forces each state through the page's own `DCLogic` state, and photographs the element by a selector listed per component (D3). PNGs go where viddik's goldens would be named. `--check` re-renders and fails on any difference.
- The floor is measured once on the simplest component (the Divider, then a filled Button without text) and every later number is read against it.
- Rejected: the designer's `screenshots/` (not rendered from the source, not vendored). Settles H1.

- AC: `make references` writes the Divider's and the Button's cells; a second run is byte-identical; the floor is a number in this item's commit with how it was measured.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`, `reference/design-system/plakat-ui Компоненты.dc.html`, `reference/design-system/support.js`

## Findings

- 2026-11-03. `scripts/design-references.mjs` opens each spec page as it is and photographs cells by
  their caption and theme card; 30 references (6 Divider rules, 2 text-floor captions, 22 Button
  cells for B-13) in `plakat-core/src/desktopTest/snapshots/design/` with `manifest.json` (Chrome,
  viewport, every rectangle, the sha256 of the pages and fonts). Two re-renders are byte-identical
  (`--check`). `make references` re-renders; `make check` runs `--manifest`, which needs no Chrome and
  fails when a page or a font changed under the references.
- What the renderer does to a page, each with its reason in the script: Google Fonts answered with
  the repository's fonts as data URLs over the DevTools Fetch domain; texture overlays hidden; reduced
  motion; macOS stem thickening off (`-webkit-font-smoothing: antialiased`); the page pushed down by
  the fraction of a pixel its 14.4 px lines leave, so each cell sits on whole pixels; a 1584 px
  viewport, so every column is whole.
- viddik 0.7.0 with KSP on `plakat-core`; `check` verifies 8 goldens (the floor's fixtures). The floor
  and its mutants are research §1.7: shapes 0.00 %, text 7.05 % / 6.99 % of the cell (63.7 % of ink).
- For B-13: the `first` part of a Button cell is the button's box, which leaves out the Focused
  state's `outline` (3 px at an offset of 3); that cell needs its rectangle grown by 6 px a side
  when the Button is built.
- H1 held for every cell so far: no page was edited and no state needed forcing (the pages draw
  their states statically). Animated states will need the page's own controls.
