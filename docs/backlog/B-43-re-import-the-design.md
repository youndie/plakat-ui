---
id: B-43
title: "Re-import the design project on a branch of its own"
status: dropped
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

## Dropped by the loop, the owner having left it to it (2026-11-07)

The snapshot of 2026-11-03 stays the design this kit was built from, and no re-import is planned.

- **Why.** The kit has moved on from the snapshot on purpose: English defaults ([B-57](B-57-english-kit.md)), what is designed here (D13), a word's ink on release ([B-68](B-68-release-ink-question.md)), and titles that fit their sheets ([B-70](B-70-fixed-sheets-under-a-large-font.md)). No request has come that a newer design would answer, and the gates hold the code to the frozen copy whatever the project does.
- **Not established.** Whether the project changed after 2026-11-03. The fetch would answer that, and nothing here looked: the DesignSync tool is for use only with the `/design-sync` skill, which its user starts, and the owner's leaving the decision to the loop is not that.
- **Rejected.** Fetching anyway, against the tool's own terms. Leaving the item open with no one to take it up, which only reads as work planned.
- **The way back.** The owner starts `/design-sync` and this item is reopened, with the steps above and in `CLAUDE.md`. Standalone Claude Design closes by 2026-12-14, so that may not be possible after that date.
