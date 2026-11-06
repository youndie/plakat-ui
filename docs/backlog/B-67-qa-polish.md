---
id: B-67
title: "Small remainders of the QA pass"
status: open
priority: P3
size: S
stage: stage-2-components
epic: feature-components
---

# B-67 — Small remainders of the QA pass

What the QA pass (2026-11-06) found that is small and independent. `PlakatDialogAction("OK")` is 46 wide, under the 48 of CLAUDE.md (F25). TopAppBar exposes its title twice, expanded and collapsed, at every fold (F30). A dismissed Card stays a Tab stop at height 0 until the caller removes it (F32). A button loading and disabled draws its spinner on-red on concrete, nearly invisible on paper (F33). `PlakatStrings.back`, `close`, `menu`, `clear`, `dismiss`, `warning` and `info` are read by nothing in the kit: public API with no effect (F36). In the sample, `SwitchRow` and the Texture word use foundation's grey indication, which no kit control has, and `Phone`'s fixed 390 clips its right edge in a 390 window (F40).

- AC: each is fixed, or recorded here as left with its reason.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatButton.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTopAppBar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCard.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/theme/PlakatStrings.kt`, `sample/src/commonMain/kotlin/io/github/youndie/plakat/sample/Demo.kt`
