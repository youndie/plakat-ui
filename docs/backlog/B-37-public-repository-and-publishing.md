---
id: B-37
title: "Public repository and publishing to Reposilite on every main — last"
status: done
priority: P3
size: S
stage: stage-4-product
blocked_by: [B-06, B-35, B-36, B-38, B-39, B-44, B-45, B-46, B-47, B-48, B-49, B-50]
---

# B-37 — Public repository and publishing to Reposilite on every main — last

The owner set this last on 2026-11-03: the repository goes public and publishes once the rest is done. Like toon-ui: every push to `main` publishes `<version>.<run>` through sborka's workflow with a consumer probe.

- **The decision and its reason.** Create `youndie/plakat-ui`, push `main`, add credentials from the infrastructure workflow with one route per coordinate (`plakat-core`, `-desktop`, `-android`, `-iosarm64`, `-iossimulatorarm64`, `-wasm-js`), a renovate config with sborka's presets, and switch the loop to pull requests.

- AC: the repository is public; a snapshot resolves from a consumer project; the loop works through pull requests.
- Anchors: `.github/workflows/check.yaml`, `.github/workflows/publish.yaml`, `.github/workflows/pages.yaml`, `renovate.json`, `LICENSE`

## Findings

- **The first publish**, 2026-11-06, run 42197465158 over `51d1e6d`: `0.1.0.1`, all six coordinates in `maven-metadata.xml`.
- **A consumer project** outside this build, with the plugins from their public repositories and this repository only by the README's block: the README's snippet compiled for desktop, wasm, `iosArm64`, `iosSimulatorArm64` and Android, and on desktop rendered the button in the bundled font, on this Mac. It did not run on a phone, a simulator or a browser.
- **The consumer probe**: of its six checks one came back undetermined, none a defect. `api-unreachable` could not answer because its consumer asks for Java 21 and `sborka.jvmFloor` is 25, so it resolves no JVM variant. A consumer below Java 25 gets `No matching variant` on desktop.
- **The site**: <https://youndie.github.io/plakat-ui/> serves the kit from `main`. Looked at in a browser: the Core and Agit pages draw, and all the wasm, fonts and textures answer 200.
- **macOS across hosts**: goldens recorded on macOS 27.0.1 ran on `macos-latest`, macOS 26, for the first time here. 327 of 328 held. `Projection/Box_Light` read 99 of 152880 px off (0.06 %, channel delta 27) on the curved edges of its big letters, and holds 0.07 % on that fixture alone, by the owner's decision. That the macOS version is the cause is inferred from where the pixels lie, not reproduced on macOS 26; no runner has macOS 27.
- **Numbers across hosts**: the docs gate compares `WarpGoldens.kt` to 1e-11, as the same generator differed by up to 2.84e-14 between this arm64 Mac and the x64 runner. That the CPU is the cause is inferred too: Node versions were ruled out on the Mac, and it was not reproduced on another x64 Linux.
