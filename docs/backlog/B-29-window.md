---
id: B-29
title: "Окно: onboarding and empty-state panels in the manner of ROSTA windows"
status: wip
priority: P1
size: M
stage: stage-2-components
epic: feature-components
blocked_by: [B-03, B-09, B-10, B-12, B-13]
---

# B-29 — Окно: onboarding and empty-state panels in the manner of ROSTA windows

MVP component (brief «Инвентарь компонентов»), drawn on `plakat-ui Компоненты.dc.html` with its motion row `rail`.

- **What to build.** `PlakatWindow`: 2–4 panels "picture + caption" paged by a horizontal shift with a hard stop; the caption opens by a wipe after the stop (200 + 120). Pictures are presets or generated compositions ([B-33](B-33-agit-presets-and-photomontage.md)).
- Built on the shared pieces: tokens and theme ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md)), the print primitives ([B-10](B-10-print-primitives.md)), the motion events ([B-12](B-12-motion-events.md)). No literal colour, size or duration (D8).
- Its reduce-motion word comes from the spec row and is implemented as written (D9). Props follow Material's API expectations, named in Kotlin form.

- AC: every state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); the motion row's values read at its timestamps by a test; reduce motion as specified; touch target ≥ 48 dp; a mutant in the ported curve or geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`
