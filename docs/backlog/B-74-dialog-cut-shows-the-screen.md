---
id: B-74
title: "The dialog's cut corner shows paper, not the screen"
status: done
priority: P3
size: S
stage: stage-2-components
epic: feature-components
---

# B-74 — The dialog's cut corner shows paper, not the screen

The owner's remark of 2026-11-07, in the sample: the cut at the dialog's top left does not look see-through. It was not:
the sheet was paper to its corner, and only the red title plate on it was cut, so the cut showed the sheet's paper
rather than the screen of dots behind. That is how the design draws it (the Components page puts
`clip-path: polygon(0 14px, 46px 0, …)` on the plate inside a sheet with a plain `background`), so this is a departure
from the design, recorded under research D13.

- **The decision (the owner's remark).** The sheet is cut as its plate is, `PlakatCutShape` on its paper, so the screen
  shows through the corner. The plate keeps its own cut, which now coincides with the sheet's.
- **Rejected.** Keeping the design's paper corner: the owner read it as a defect.
- **Checked.** A test reads the corner: no paper and no plate in it, the screen's dots there, the plate past the cut; it
  fails with the sheet uncut. The two goldens `Dialog_Danger_Light` and `_Dark` are re-recorded and looked at. Parity
  against the design moves by the triangle: Light 1.47 → 1.63 %, Dark 1.46 → 1.61 %, against the 5 % report line.
- **Not changed.** A press in the cut still lands on the sheet, not on the screen: the triangle is 46 × 14 and outside
  the sheet's actions.
- AC: the screen shows through the cut in both themes; a test fails without the fix.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatDialog.kt`
