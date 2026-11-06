---
id: B-61
title: "A hidden Snackbar takes no input"
status: open
priority: P1
size: S
stage: stage-2-components
epic: feature-components
---

# B-61 — A hidden Snackbar takes no input

The KDoc says "hidden ... nothing of it is drawn", and it is not drawn, but it stays live. Under reduced motion it stays in place at alpha 0, an invisible 96 × 56 button over the content that answers a click. In every mode Tab then Enter fires the action of a hidden snackbar, and its message stays in the semantics tree (QA F04). So a keyboard or screen-reader user can trigger UNDO on something they cannot see.

- **The decision.** Hidden and at rest, the snackbar's content is not composed, as Dialog, Sheet and Menu are gone from the composition. It leaves after its exit has played, so the motion row is untouched.
- **Rejected.** Disabling the action and clearing semantics while keeping the content composed: more states for the same picture, and a stale message for a reader to reach.
- **Not covered.** The layout of a long message or action: [B-65](B-65-long-content.md).

- AC: hidden, the action answers neither a click nor the keyboard, in both motion modes, and no text of it is in the semantics tree; a snackbar shown again plays its entrance from the start; `Snackbar/*` goldens hold.
- Anchors: `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/components/PlakatSnackbar.kt`
