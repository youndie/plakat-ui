---
id: B-35
title: "Demo screens and the sample kit: onboarding, feed, settings in both themes"
status: open
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
- Anchors: `reference/design-system/plakat-ui Демо.dc.html`
