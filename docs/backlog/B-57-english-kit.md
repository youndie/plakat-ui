---
id: B-57
title: "The kit speaks English: the sample, the site and the default strings"
status: done
priority: P1
size: S
stage: stage-3-demo
epic: feature-components
blocked_by: [B-35]
---

# B-57 — The kit speaks English: the sample, the site and the default strings

The owner's remark (2026-11-06): «надо на английский перевести кит».

- **The decision.** What a person sees or a consumer gets by default is English: the sample's pages and header, which are the site, the Demo screens as the sample shows them, the README's snippet and the defaults of `PlakatStrings`. The design's Russian stays where it is evidence: in the fixtures, because the references were rendered with it (D11, amended). The Demo screens take a `DemoCopy`, English by default, and `DemoFixtures` passes the design's words; the Snackbar and TextField fixtures, the only ones that print a word of the theme, pass `PlakatStrings(error = "Ошибка")`.
- **Rejected.** Translating the fixtures and re-recording the goldens: it would have dropped the Demo screens' and the two components' parity with the design to get an English sample. Leaving the library's defaults Russian with the sample overriding them: every consumer would have met Russian words on a danger state.
- **Left in Russian on purpose.** The frame benchmark and `bench-android`'s screens, so that B-06's and B-51's numbers stay comparable; the vendored design and the brief; test data and the design's names quoted in comments.
- AC: no section of the sample shows or names a word in Cyrillic, held by a test; no golden moves; a Russian app gets its words through `PlakatStrings`, and the public API does not change.
- Anchors: `sample/src/commonMain/kotlin/io/github/youndie/plakat/sample/Demo.kt`, `sample/src/commonMain/kotlin/io/github/youndie/plakat/sample/Kit.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/theme/PlakatStrings.kt`, `sample/src/desktopTest/kotlin/io/github/youndie/plakat/sample/DemoFixtures.kt`

## Findings

The translation changes what the English screens look like, and no golden holds them: the Demo goldens are the Russian screens, by the decision above. All six sections and the onboarding's other two panels were rendered and looked at, in the paper theme only. Rubik Mono One's word space is a full cell, so two short words on one poster line open a hole; that is why the title reads READ / THIS / NOVEMBER and not ALL OF. English goldens were left out: the curved edges of the display face's big letters are what `Projection/Box_Light` differed on across macOS versions (B-37), so new goldens of big letters risk new red on the runner. The test that no section shows Cyrillic is what holds the translation.

Mutants, each caught by the aimed check: a Russian group title in the kit and a Russian default in `PlakatStrings` fail `no section shows a word in Russian`, the default also fails the theme's default test; `DemoFeed` without the design's words fails `Demo/Feed_*` and `Gray/Feed_*` (8.6 %); the Snackbar fixture without the theme's word fails `Snackbar/Error_*` (10.3 %).

`bench-android`'s `kit` mode shows `PlakatUiSample`, so that mode now shows English; its scripted screens do not.
