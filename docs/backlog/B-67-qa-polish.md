---
id: B-67
title: "Small remainders of the QA pass"
status: done
priority: P3
size: S
stage: stage-2-components
epic: feature-components
---

# B-67 — Small remainders of the QA pass

What the QA pass (2026-11-06) found that is small and independent. `PlakatDialogAction("OK")` is 46 wide, under the 48 of CLAUDE.md (F25). TopAppBar exposes its title twice, expanded and collapsed, at every fold (F30). A dismissed Card stays a Tab stop at height 0 until the caller removes it (F32). A button loading and disabled draws its spinner on-red on concrete, nearly invisible on paper (F33). `PlakatStrings.back`, `close`, `menu`, `clear`, `dismiss`, `warning` and `info` are read by nothing in the kit: public API with no effect (F36). In the sample, `SwitchRow` and the Texture word use foundation's grey indication, which no kit control has, and `Phone`'s fixed 390 clips its right edge in a 390 window (F40).

All six are fixed; the owner left how to the loop (2026-11-07).

- **The dialog action.** A fitted width gives way to the 48 of a touch target, so `OK` is 48. Rejected: a padding on the action, which would also widen every long word.
- **The bar's title.** A reader hears one heading at any fold: the expanded title under half folded, the collapsed one from half on, and on a masthead the stretch until the ordinary bar arrives. Both stay drawn, as what is drawn is what the goldens hold.
- **A torn card.** It leaves the Tab order and the reader the moment `dismissed` is set, and is back the moment it is unset. Rejected: leaving it to the caller's `onDismissed`, as the card is 400 ms on its way out with focus still able to land on it.
- **The sector.** It takes the button's own ink, so disabled it is muted on concrete and enabled the on-red it was. Only `PlakatButton` has `loading`.
- **`PlakatStrings`.** `back`, `close`, `menu`, `clear`, `dismiss`, `warning` and `info` are removed, a breaking change on the snapshot line pinned in the ABI dump. A word that nothing reads tells a consumer it has localised something it has not. Rejected: keeping them for later, as adding one back with the control that reads it costs one line. The kit's icon actions take their names from the caller, which is why nothing read them.
- **The sample.** `SwitchRow` and the Texture word have no indication, as no kit control has foundation's grey. A demo screen is at most 390 wide, so it fits a window of 390.
- AC: each is fixed, or recorded here as left with its reason.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatButton.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatTopAppBar.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatCard.kt`, `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/theme/PlakatStrings.kt`, `sample/src/commonMain/kotlin/io/github/youndie/plakat/sample/Demo.kt`
