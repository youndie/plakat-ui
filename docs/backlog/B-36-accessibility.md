---
id: B-36
title: "Accessibility: contrast with textures on, grayscale check, touch targets, semantics"
status: open
priority: P1
size: S
stage: stage-3-screens
blocked_by: [B-07, B-35]
---

# B-36 — Accessibility: contrast with textures on, grayscale check, touch targets, semantics

Every ratio the design claims holds, but ink-muted on concrete is 4.5005:1 and any texture over concrete lowers it (research §1.5, risk 3). The brief requires every screen to read in grayscale and red never to be the only carrier of meaning.

- **The decision and its reason.** Measure text contrast over the multiplied paper texture on both papers; where it falls below 4.5 lighten the texture under text or change the token, with the owner's choice recorded. A grayscale fixture of each demo screen; touch targets ≥ 48 dp including cut corners; semantics roles and state descriptions on every control.
- Flashing: nothing faster than three times a second (the caret is 1.25 Hz).

- AC: a contrast test over textured surfaces passes in both themes; grayscale goldens committed; a touch-target test over the sample.
- Anchors: `reference/design-system/plakat-tokens.json`, `reference/design-system/plakat-ui Токены.dc.html`

- From [B-28](B-28-dialog.md): the dialog traps neither focus nor the system back gesture yet; that is this item's to settle.
- From B-35: `:sample` has no contrast check of its own; the demo screens are the place to read contrast over textures, and grayscale goldens would go beside theirs.
