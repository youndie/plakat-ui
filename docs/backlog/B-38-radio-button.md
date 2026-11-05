---
id: B-38
title: "RadioButton: a ring with a red disc that locks in one frame"
status: done
priority: P1
size: S
stage: stage-2-components
epic: feature-components
blocked_by: [B-09, B-10, B-12, B-15]
---

# B-38 — RadioButton: a ring with a red disc that locks in one frame

MVP in the brief ("круг-рамка, внутри красный диск; выбор — диск встаёт на место за один кадр", event `lock`, 0 ms), not drawn by the design (research §1.6).

- **What to build.** A 24 dp ring in the 3 dp rule — one of the kit's few full circles — with a red disc inside; selecting shows the disc in one frame and the previous one goes out a frame earlier. Group API as Material's. Size and gap follow the Checkbox (24 dp, 48 dp target).
- Designed here under research D13: no page draws it. Built from the brief's row, the Principles page's seven steps and do/don't pairs, the tokens and the drawn components it shares parts with. The item's commit lists every number the design does not state and where it came from.
- Uses the shared pieces ([B-08](B-08-token-generator.md), [B-09](B-09-theme-and-reduce-motion.md), [B-10](B-10-print-primitives.md), [B-12](B-12-motion-events.md)); reduce motion: instant unless the brief says otherwise.

- AC: both themes in the sample; the owner has looked at it, and its goldens are recorded after that; the Principles checklist holds (primitives only, radius 0, one diagonal at most, state readable without colour, ≥ 48 dp, ≥ 4.5:1); a mutant in its geometry or curve is caught.
- Anchors: `reference/design-system/plakat-ui Принципы.dc.html`, `reference/design-system/uploads/plakat-ui - бриф для дизайнера.md`

## Findings (2026-11-05)

- **`PlakatRadioButton(selected, onClick, modifier, enabled, interactionSource)`**, grouped as Material's are, by foundation's `Modifier.selectableGroup()`: no group class of its own. In the sample's core section, in both themes through its switch.
- **Designed here** (D13), every number the design does not state: the disc of 12, the ring's inner 18 less a gap of one rule all round; a disabled ring of a hair with a muted disc, the Checkbox's disabled look; a square focus ring on the 48 dp zone, the Checkbox's, though the ring is round; `lock` for the button coming in only, none for the one going out.
- **The motion**: the old disc goes in the composition that deselects it, the new one in the frame after, set by the effect, so one frame stands with neither and none with both. Under reduced motion they change together, since the brief gives 0 ms and one frame is the only motion there is.
- **The AC asks for goldens after the owner has looked**, and the owner put that review last (CLAUDE.md, 2026-11-03). They are recorded now, in the review's queue, and revised then. **So this item's AC is met but for the owner's look.**
- **Principles checklist**: primitives only, a ring and a disc; radius 0 where there is a corner, and full circles are the kit's permitted curve; no diagonal; its state reads without colour, disc or no disc, in grey too; zone of 48; the ring and the disc are not text, and the ring is ink on paper at 13.7.
- **Process, for the record**: the first round of mutants ran on an uncommitted file, because the commit before it was refused by the hook for a Cyrillic word in its message and the chain went on. `git checkout` could not undo them, the mutations piled up, and the file was restored from its source and re-verified (tests and goldens green) before anything was committed. Every verdict below is from committed code.
- **Mutants**: through the tests — a disc of 16, the disc going out late, the disc coming in at once, no reduced-motion case, `lock` on leaving too, no `lock`, a thin ring, a thick ring when disabled, a square, a red disc when disabled, the wrong role, a zone of 24. Through the goldens — the focus ring's colour. All thirteen killed. An earlier frame wait was removed: mutants showed it held nothing and hid «out late».
