---
id: B-71
title: "Wasm browser tests go red on a runner's speed: Mocha's limit of 2 s"
status: done
priority: P1
size: S
stage: stage-4-product
---

# B-71 — Wasm browser tests go red on a runner's speed: Mocha's limit of 2 s

After [#8](https://github.com/youndie/plakat-ui/pull/8) was merged, `main` went red on `check` and `publish`, so 0.1.0.5 was not published: `PlakatGenTest.every seed of every frame matches the design`, on wasmJs in the browser, ended with `Timeout of 2000ms exceeded`. The merge commit's tree is the head's, byte for byte, the head had passed twice on the same runner image, and #8 did not touch the generator. On this Mac the test takes 676–861 ms, measured inside it over three runs. Why those two runs were slower was not established.

**Decision (the owner's):** `plakat-core/karma.config.d/mocha-timeout.js` sets Mocha's limit to 30 s, and Karma's two limits above it. What a test asserts is unchanged; no comparison, tolerance or golden moves. Rejected: re-running the failed jobs, which leaves the same test able to go red on any later merge; fewer seeds on wasm, which would check less.

Mocha's limit alone does not do it, as the control showed. A test holds the browser's thread, so Karma hears nothing meanwhile: at 35 s the run was dropped on `ping timeout`, then, with that raised, on `no message in 30000 ms`, each time naming no test and leaving the rest unrun.

## Findings

The control is a throwaway test holding the thread for a stated time, not committed.

| Setting | 3 s | 12 s | 35 s | never ends |
|---|---|---|---|---|
| none | fails, `Timeout of 2000ms` | | run dropped, `ping timeout` | |
| Mocha 30 s | passes | | run dropped, `ping timeout` | |
| and `pingTimeout` 60 s | | passes | run dropped, `no message in 30000 ms` | run dropped at 47 s, red |
| and `browserNoActivityTimeout` 60 s (kept) | | passes | **fails by name, `Timeout of 30000ms`; the other 28 run** | run dropped, `no message in 60000 ms`, red, 70 s |

The real suite is green under the kept setting, 27 of 27.

- **Limit:** a hang costs a minute of CI, and a test that takes over 30 s now fails by name. A slow runner can still go over that; nothing here measured a runner's time for this test, as the report's durations are not usable (0.021 s for the failed test, 0.0 in the XML).
- **Scope:** `plakat-core` only, the one module whose wasm tests run. `:sample:wasmJsBrowserTest` is skipped, having no tests.

- AC: the test that went red has room on a slower runner; a test over the limit fails by name; a test that never ends turns the run red, not a hung job.
- Anchors: `plakat-core/karma.config.d/mocha-timeout.js`
