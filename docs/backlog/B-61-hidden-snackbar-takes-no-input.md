---
id: B-61
title: "A hidden Snackbar takes no input"
status: done
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

## Findings

Hidden and at rest the snackbar returns from its composition, which also drops the `modifier`, so it takes no space where a consumer's layout gave it some: it has always been placed over the content (the page's `position: absolute`), and a snackbar in a plain Column now closes up rather than leaving its height empty. A `reduce` fade settles by `fade`, a ride by `rise`. On its way out, under either, its action no longer answers, so the 120 or 200 ms of its exit cannot fire it. The KDoc's "nothing of it is drawn" is now also "nothing of it is there".

Mutants, each killed by the aimed test: staying composed fails `hidden under reduced motion a click goes through to what is under it`; settling only by the rise fails `hidden under reduced motion a click goes through to what is under it`; an action enabled while leaving fails `going away its action no longer answers`. The keyboard tests hold the two guards together: the disabled action alone already keeps Tab off it, so removing either one leaves them green, and it is the click and semantics tests that tell the two apart.
