---
id: B-43
title: "Re-import the design project on a branch of its own"
status: question
priority: P2
size: S
stage: stage-4-product
blocked_by: [B-05, B-08]
---

# B-43 — Re-import the design project on a branch of its own

`reference/design-system/` is a snapshot of 2026-11-03. The design may still change; RadioButton, the v1 set, hover and the three textures are made here instead (research D13, D16), so a re-import must not overwrite them.

- **The decision and its reason.** Fetch with DesignSync on a branch (`list_files`, `get_file`; skip `screenshots/`, `.thumbnail`, `.image-slots.state.json`); diff tokens, icons, generators and each page's `const spec`; regenerate tokens, goldens and references in the same branch; update research §1.1 with the new snapshot. Never overwrite in place.

- AC: a re-import branch whose commit body lists every changed token and spec row, with the gate green.
- Anchors: `reference/design-system/README.md`

## Question for the owner (2026-11-05)

**What was found.** Nothing was fetched, and nothing in `reference/design-system/` has changed. The DesignSync tool states that it is used only with the `/design-sync` skill, which the user starts, and that `get_file` is called only for a component the user named. This item's first step reads every file of the project, and the loop did not take that on itself. No `/design-sync` skill is listed in this environment.

The tool also states that standalone Claude Design closes by 2026-12-14, so a re-import through it may not be possible after that date.

**The choices, the owner's:**

1. Go ahead: the owner says so in words, and the loop fetches read-only, diffs against the snapshot, and either reports no change or carries out the decision above on this branch.
2. Leave it: the item goes back to `open` or to `dropped`, the snapshot of 2026-11-03 stands, and the loop goes on to [B-44](B-44-slider.md).
