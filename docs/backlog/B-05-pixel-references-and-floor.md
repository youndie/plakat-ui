---
id: B-05
title: "Pixel references rendered from the spec pages in headless Chrome; the parity floor"
status: open
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
