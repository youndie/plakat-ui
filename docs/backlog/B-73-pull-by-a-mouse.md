---
id: B-73
title: "Pull-to-refresh does nothing with a mouse"
status: done
priority: P2
size: S
stage: stage-2-components
epic: feature-components
---

# B-73 — Pull-to-refresh does nothing with a mouse

The owner's remark of 2026-11-07, in the sample: pull-to-refresh does not work. [B-50](B-50-pull-to-refresh.md) fed the
wedge only from scroll that the content had no use for, while a finger was down. A mouse scrolls nothing by dragging in
Compose, on desktop and in the browser alike, so a drag on the list never reached the box, and a wheel was ruled out
by B-50. In the sample, which runs on desktop and in the browser, the component could not be pulled at all.

- **The decision.** A mouse pressed on the content and dragged down, while the content stands at its top, pulls the
  wedge as a finger does; dragged back up, it takes the wedge back; letting go of the button is letting go, and counts
  from 80 dp as before. Past the slop the drag is taken, so what it was pressed on is not clicked. Whether the content
  stands at its top is read from its scrolls as they pass through the box: one that scrolled down without a remainder
  is away from it, one that came back with a remainder is at it. A scroll that never passes through the box, by the
  keys or by code, is not seen.
- **Rejected.** A wheel or a trackpad's scroll at the top pulling, with letting go read off a pause: B-50's reason
  stands, as a wheel has no letting go, and a flick up at a list's top would refresh it by accident. Letting a mouse
  drag scroll the content too: a desktop list does not do that, and the box cannot reach into its content's scroll.
- **Checked.** In the test runtime, by mouse input: a drag of 140 pulls past 80 and counts once on release; back up by
  60 takes 60 off; a list scrolled down by the wheel gives no pull; a drag that pulls does not click the row it was
  pressed on, and a plain click still does. Each test fails with its part of the fix taken out.
- **Not exercised.** A real mouse or trackpad in the window; the browser.
- AC: a mouse drag down from the top pulls and refreshes in the sample; a test fails without the fix.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatPullToRefresh.kt`
