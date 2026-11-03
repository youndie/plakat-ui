---
id: B-02
title: "Fonts: Rubik Mono One and static Golos Text 400/600, licences, coverage test"
status: open
priority: P0
size: S
stage: stage-0-risks
blocked_by: [B-01]
---

# B-02 — Fonts: Rubik Mono One and static Golos Text 400/600, licences, coverage test

The two faces are OFL with full Cyrillic, verified by file (research §1.4). Golos Text ships only as a variable font, and Rubik Mono One is static.

- **The decision and its reason.** Bundle `RubikMonoOne-Regular.ttf` as fetched and cut static `GolosText-Regular` (400) and `GolosText-SemiBold` (600) from `GolosText[wght].ttf` with fontTools' instancer, by a committed script that records the source commit (D7). A `wght` axis needs Android API 26; below it Title and Label would draw at 400.
- Each `OFL.txt` ships beside its font. A coverage test holds both families to every string the fixtures render; the Счётчик legend glyphs `■` `●` are not in either face and are drawn as shapes ([B-30](B-30-counter.md)).
- Metrics pinned in a test: Rubik Mono One cap height 700/1000 and advance 850 (monospace), Golos cap height 700 and x-height 530 — the stretch text ([B-31](B-31-stretch-text.md)) depends on the cap height.
- Rejected: subsetting now. The files are 141 KB and two Golos instances; subsetting is a size decision for after a consumer measures it.

- AC: the fonts load on desktop, Android 24, iOS and wasm; the coverage test fails when a fixture string gains a missing glyph; the metrics test fails when a font file changes.
- Anchors: `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc!/ofl/rubikmonoone/RubikMonoOne-Regular.ttf`, `google/fonts@2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc!/ofl/golostext/GolosText[wght].ttf`
