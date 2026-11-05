---
id: B-43
title: "Re-import the design project on a branch of its own"
status: open
priority: P3
size: S
stage: stage-4-product
blocked_by: [B-05, B-08]
---

# B-43 — Re-import the design project on a branch of its own

`reference/design-system/` is a snapshot of 2026-11-03. The design may still change; RadioButton, the v1 set, hover and the three textures are made here instead (research D13, D16), so a re-import must not overwrite them.

- **The decision and its reason.** Fetch with DesignSync on a branch (`list_files`, `get_file`; skip `screenshots/`, `.thumbnail`, `.image-slots.state.json`); diff tokens, icons, generators and each page's `const spec`; regenerate tokens, goldens and references in the same branch; update research §1.1 with the new snapshot. Never overwrite in place.

- AC: a re-import branch whose commit body lists every changed token and spec row, with the gate green.
- Anchors: `reference/design-system/README.md`

## Set aside by the owner (2026-11-05)

Asked whether the loop should read the whole design project, the owner said to skip it and go on. Nothing was fetched, and the snapshot of 2026-11-03 stands. Moved to P3, after the components. The DesignSync tool says it is used only with a `/design-sync` skill the user starts, and that standalone Claude Design closes by 2026-12-14, so taking this up needs the owner's word and may not be possible after that date.
