---
id: B-69
title: "Check what only a real window shows"
status: wip
priority: P3
size: S
stage: stage-2-components
epic: feature-components
---

# B-69 — Check what only a real window shows

All hover and focus results of the QA pass (2026-11-06) come from the test runtime. In a real `:sample:run` window nobody has yet checked hover, the mouse wheel, Tab order or resize, whether a mouse click leaves a focus ring, or whether a tooltip's popup swallows clicks over its plate. Driving a window in the background delivers a click as an accessibility press, with no hover and no focus, so this needs control of the owner's screen or the owner's own hands.

The question for the owner: when, and by which of the two.
- AC: each named behaviour is recorded as checked, with a new item for anything wrong.
- Anchors: `sample/src/desktopMain/kotlin/io/github/youndie/plakat/sample/Main.kt`
