---
id: B-35
title: "Demo screens and the sample kit: onboarding, feed, settings in both themes"
status: done
priority: P1
size: M
stage: stage-3-screens
blocked_by: [B-13, B-14, B-16, B-17, B-19, B-25, B-26, B-27, B-29, B-30]
---

# B-35 — Demo screens and the sample kit: onboarding, feed, settings in both themes

The Demo page builds three screens of the magazine «Лист» only from kit components, in both themes.

- **The decision and its reason.** Rebuild them in the sample from the public API only, with the theme and reduce motion passed in as parameters; the sample shows every component grouped as the pages group them, with motion visible without a click. Desktop and one more host (wasm on Pages, or Android).
- A smoke test walks every section in each theme with the clock paused.

- AC: the three screens match the Demo page within the floor in both themes; the smoke test passes; `./gradlew :sample:run` shows the kit.
- AC (moved from [B-12](B-12-motion-events.md), whose sample had no animated component yet): the sample takes a `PlakatEventListener`, and a test collects the events of a scripted session on it and gets the spec's names in order.
- Anchors: `reference/design-system/plakat-ui Демо.dc.html`

## Findings (2026-11-05)

- **The sample** (`sample/`): `PlakatUiSample(dark, reduceMotion, texture, section, autoplay, events)`, with chips in its header for the sections, the theme and reduced motion, and a label for the texture; `KitCore`, `KitComponents(beat)`, `KitAgit` group every component as the pages do; a beat of 2 s moves the numbers, the progress, the tabs, the window and the change of screen with nobody touching them. Desktop and wasm, both compiled by the gate's tasks; the public API only, as a consumer.
- **The Demo page draws components at other measures than their cells**, so the kit grew: `PlakatWindow(large)` (360 picture, 32 caption, a panel the whole width), `PlakatCounter(stretched, numberColor)`, `PlakatGeneratedImage(titleZone, ground)` with `PlakatTitleZone`, `PlakatRapportFill(cell)` (the strip is seed 1923 on a cell of 4), `PlakatRaskatDisc`. What the kit has no component for is drawn in the sample, as an application would: the page indicator, the masthead and, until [B-49](B-49-segmented.md), the segmented control. `PlakatDialogAction` carries the demo's 13 px words, plain and hatched, as it shares those metrics with the page's dialog.
- **Parity**: onboarding 1.19 %, settings 1.87–1.89 %, feed 4.65–4.66 %, in both themes alike. By band: the strip 0 %, pictures and dots 0–1 %, buttons 9–16 %, tabs and nav 27–30 %. Words 34–65 % of ink against the text floor of 63.7 %, the settings legend at 71 % (B-30's named gap, its first line at a fractional x). The feed's rest is its raskat, which the page draws on a 2× canvas of its own grain, a stochastic edge, and the turned title's. Text is set by public styles only, so three lines needed a nudge to Blink's baseline (the turned title 8 along its height, the switch rows 1, the feed's body 1), said where they stand.
- **Two drawings of one state**: the page's disabled switch here has a muted rail 3 high and a knob with no outline, the Core page's a hairline and an outlined knob, which the kit holds. The kit's stays; a question for the owner's review.
- **A defect found on the way**: `PlakatCounter`'s row had no intrinsics, so under `IntrinsicSize.Max` each pictogram took a line, and a tile beside a taller one stood 100 too tall. It now reports them from the same wrapping.
- **The events** (moved from B-12): a scripted session on the sample hears, in order, `stamp`, `release`, `rail` (next), `stamp` (a chip), `lever` (a row's switch), through `PlakatEventListener`.
- **Smoke**: every section composes in both themes and both ways of moving, the clock paused. The sections were looked at in a throwaway capture at 1000 × 2600.
- **`./gradlew :sample:run` was not opened here**: the same root is held by the smoke test and the capture, and a window wants a person. It is the owner's first look, in the review round.
- **Mutants**: through the tests — no intrinsics, no stretch, a ground ignored, a zone ignored, a zone's rect dropped, a cell ignored, a square disc. Through the goldens — a large picture of 344, no panel padding. All nine killed; the zone's rect survived the first round and got its test. A body's most of 280 instead of 320 survives: none of the page's three bodies reaches it, so the value is the page's and unheld.
