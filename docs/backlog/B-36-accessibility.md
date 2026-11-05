---
id: B-36
title: "Accessibility: contrast with textures on, grayscale check, touch targets, semantics"
status: done
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

## Findings (2026-11-05)

- **Contrast** (`ContrastOverTextureTest`, both themes): every pair the kit sets is read over the tile it stands on and under the grain, as a mean over a 12 dp square wrapped across the tile, because a letter is not read against one fibre. Per pixel the design's own numbers would fail anywhere: red on the light paper's fibres reaches 3.88. All hold but one: **ink-muted on the concrete tile, 4.43 at its worst square** (4.49 as a tile's mean, 3.70 at one pixel).
- **The choice**, the item's first option, made here and for the owner's review: `concrete-light` goes up two levels. It is a tile made here (D16), so no token and nothing vendored moved, and the flat ground's 4.5005 is untouched. Rejected: a darker `ink-muted`, which is the design's token and would move every disabled look. Per pixel a fibre still falls to about 3.8. In the kit itself ink-muted on concrete is only the disabled filled button, which WCAG exempts, so this guards what an application sets.
- **Red text on the dark paper is 2.89** (research §1.5) and is in no pair: the kit sets no red words. Not a gap of the kit's, but an application could.
- **Grayscale**: goldens of the three demo screens in both themes (`Gray_*`, `:sample`), looked at. Everything reads. Two things worth the owner's eye: the selected nav square and the selected tab's bar stand as mid-grey against black, fine; and «красный — сегодня» in the legend names a colour the grey no longer has, the last circle still being lighter and last.
- **Touch targets and names** (`SampleAppTest`, over each section but Agit, which has no control): every clickable node is at least 48 × 48 and has a name or a role. It found **a text field whose input took only its text line, 22 dp**, in a field of 56: a tap on the rest did nothing. Fixed in the kit, and no golden moved. It also found the sample's own texture label at 47 and its segments at 40, which are 40 drawn in a 48 target now.
- **Dialog** (from B-28): focus stays inside it while it is open (`focusProperties.onExit` cancels, `focusGroup`), and the system back gesture dismisses it. Compose 1.12's `BackHandler` is deprecated for `NavigationBackHandler`, so the kit takes `navigationevent-compose` 1.1.0, the version Compose 1.12.1 itself resolves, one dependency more, recorded in the catalog. Focus does not return to what had it before the dialog opened: named, for the owner.
- **Flashing**: the only thing that blinks is the caret, 500 on and 300 off, 1.25 Hz, held by `PlakatTextFieldTest`. The spinner's and the skeleton's steps move, they do not flash.
- **Not done**: no screen-reader run on a real device; semantics are read from the tree. That belongs with the device item, [B-06](B-06-frame-cost-measurement.md).
- **Mutants**: through the tests — no focus trap, no back, back while closing, the field's input to its line again, a segment's target of 44, the tile unlifted (killed by both the contrast and the tile test). Through the goldens — not grey, six cells. All seven killed; back while closing survived the first round and its test was tightened.
