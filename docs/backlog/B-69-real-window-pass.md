---
id: B-69
title: "Check what only a real window shows"
status: done
priority: P3
size: S
stage: stage-2-components
epic: feature-components
---

# B-69 — Check what only a real window shows

All hover and focus results of the QA pass (2026-11-06) came from the test runtime. In a real `:sample:run` window nobody had yet checked hover, the mouse wheel, Tab order or resize, whether a mouse click leaves a focus ring, or whether a tooltip's popup swallows clicks over its plate.

- **The decision (the owner left it to the loop, 2026-11-07).** Neither of the two choices this item first offered, the owner's hands or control of the owner's screen. A throwaway driver, not committed, opened the real window (`Window` over `PlakatUiSample`, JBR 25, macOS 27.0.1) and posted `MouseEvent`, `MouseWheelEvent` and `KeyEvent` to `SwingUtilities.getDeepestComponentAt` through the AWT event queue, with the window photographed in the background after each step. That is a real window, a real Skia layer and Compose's real AWT listeners, and it takes nobody's screen.
- **What this does not cover.** The operating system's own delivery of a mouse or trackpad, including a trackpad's scroll phases and inertia, the pointer's shape, and dragging the window's edge. One Tab was also sent as a real keystroke and did the same as a posted one.
- AC: each named behaviour is recorded as checked, with a new item for anything wrong.
- Anchors: `sample/src/desktopMain/kotlin/io/github/youndie/plakat/sample/Main.kt`

## Checked

| Behaviour | What the window did |
|---|---|
| Hover | An outlined button fills with concrete under the pointer and clears when it leaves |
| A mouse click and the ring | No ring after a click, on a first click and on a click after keyboard focus, which also takes the keyboard's ring away |
| Tab order | `CORE` through the header, the Texture word, `READ`, then past the loading and the disabled button to `BOOKMARK`, `LATER`, `DELETE`, the fields and on; Shift+Tab back the same way |
| Tab past the fold | The page scrolls the focused control into view, both ways |
| Mouse wheel | 30 px a notch, both ways, over the page. An inner scroller and the ends of the page were not tried |
| Tooltip | Shows after its 400 ms on the anchor and goes when the pointer leaves |
| Tooltip's plate and a click | **Takes the press** while it is there, the 120 ms of its going included: [B-72](B-72-tooltip-plate-takes-a-press.md). Control: the same press on the same button with no plate fills it |
| Resize | At 1280, 480 and back the kit reflows and nothing is cut |

## Left as they are

- Scrolled into view by Tab, a control's ring can stand a few dp under the header or past the window's edge, as a scroll aims at the control's bounds and the ring stands outside them (`SUBSCRIBE` at the bottom, the card at the top). The next Tab moves on, and which control has focus still shows.
- At 240 × 300 the sample's header takes the window, and its last button is hidden under the page. This is the sample's header, at a size nobody uses, not a component.
