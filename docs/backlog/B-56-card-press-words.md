---
id: B-56
title: "A pressed card's words stay dark in the kit and the demo"
status: wip
priority: P1
size: XS
stage: stage-3-demo
epic: feature-components
blocked_by: [B-17, B-35]
---

# B-56 — A pressed card's words stay dark in the kit and the demo

The owner's remark from the kit's review (2026-11-05): «при клике по карточке текст надо инвертировать».

- **Cause.** `PlakatCard` turns its sheet ink and offers paper through `LocalPlakatContentColor`, and the page's cell, in its fixture, reads it. The kit's card and the demo's feed set their words in the theme's ink by hand, so pressed they are ink on ink. A consumer writing the obvious `BasicText` would meet the same.
- **The decision.** The kit and demo read `LocalPlakatContentColor`, and `PlakatCard`'s KDoc says a word with a colour of its own stays that colour. Rejected: inverting whatever the content draws by a blend, as a thumbnail in the card must not invert (the page's does not), and a kit-wide text component, which is a bigger thing than this remark.
- AC: pressed, a card's words in the kit's and the demo's content are paper; at rest no golden moves; a test presses a real demo card; a mutant is caught.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCard.kt`, `sample/src/commonMain/kotlin/io/github/youndie/plakat/sample/Demo.kt`
