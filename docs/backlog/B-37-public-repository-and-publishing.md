---
id: B-37
title: "Public repository and publishing to Reposilite on every main — last"
status: wip
priority: P3
size: S
stage: stage-4-product
blocked_by: [B-06, B-35, B-36, B-38, B-39, B-44, B-45, B-46, B-47, B-48, B-49, B-50]
---

# B-37 — Public repository and publishing to Reposilite on every main — last

The owner set this last on 2026-11-03: the repository goes public and publishes once the rest is done. Like toon-ui: every push to `main` publishes `<version>.<run>` through sborka's workflow with a consumer probe.

- **The decision and its reason.** Create `youndie/plakat-ui`, push `main`, add credentials from the infrastructure workflow with one route per coordinate (`plakat-core`, `-desktop`, `-android`, `-iosarm64`, `-iossimulatorarm64`, `-wasm-js`), a renovate config with sborka's presets, and switch the loop to pull requests.

- AC: the repository is public; a snapshot resolves from a consumer project; the loop works through pull requests.
- Anchors: `.github/workflows/check.yaml`, `LICENSE`
