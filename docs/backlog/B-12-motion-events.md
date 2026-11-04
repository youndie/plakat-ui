---
id: B-12
title: "Motion events: the named events every animation reports, for sound later"
status: done
priority: P1
size: S
stage: stage-1-materials
blocked_by: [B-09]
---

# B-12 — Motion events: the named events every animation reports, for sound later

The brief fixes event names now so that sound can be attached later: `stamp`, `release`, `engage`, `wedge-in`, `wedge-out`, `lever`, `underline`, `hatch`, `print`, `tear`, `telegram`, `announce`, `lock`, `rail`, `tick`, `fold`, `wipe`, `roll`, `notch` (spec rows of the core and MVP pages).

- **The decision and its reason.** A `PlakatEvent` enum and a listener in the theme; every animation reports its event once per trigger. No sound in core (brief, «Не-цели»).
- Rejected: a callback per component.

- AC: a test collects the events of a scripted session on the sample and gets the spec's names in order.
- Anchors: `reference/design-system/plakat-ui Ядро.dc.html`, `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-04)

- `PlakatEvent` holds the nineteen names, and `PlakatEventTest` holds them to the design: the core
  page's `R(…)` rows, the MVP page's `S(…)` rows, the brief's motion table (RadioButton's `lock`)
  and the Principles page's slider (`notch`) give exactly these and no others. The core page's caret
  row is `R('caret', '—', …)`, its columns swapped (the brief gives the caret no event), so rows with
  no trigger are not read as events.
- The listener is `PlakatTheme(events = …)`, a `PlakatEventListener`, `Silent` by default. Events are
  reported whether or not motion is reduced: the trigger happened, only its picture changed.
- Components report in one of two ways: from an input handler, `PlakatTheme.events.onEvent(…)`; for
  a play started by state, `ReportOnChange(trigger, event, initial)`, once per change of `trigger`,
  and on the first composition only for an appearance.
- **The AC's scripted session moved to [B-35](B-35-demo-screens-and-sample.md).** The sample has no
  animated component until the components land, so a session on it would collect nothing; each
  component item reports its spec row's event and tests it, and B-35 runs the session over them all.
