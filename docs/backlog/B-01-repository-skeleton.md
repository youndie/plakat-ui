---
id: B-01
title: "Repository skeleton: plakat-core, sample, sborka conventions, four targets, CI on macOS"
status: done
priority: P0
size: M
stage: stage-0-risks
---

# B-01 — Repository skeleton: plakat-core, sample, sborka conventions, four targets, CI on macOS

Nothing is built yet. Every other item needs a module to put code in and a gate that runs it.

- **The decision and its reason.** One library module `plakat-core` (package `io.github.youndie.plakat`, D1) and a `sample` kit, on sborka's conventions: explicit API, warnings as errors, the catalog's Kotlin, CMP and AGP (the stack is held at CMP 1.12.1). Targets desktop, Android, iOS (`iosArm64`, `iosSimulatorArm64`) and wasm (D2). `foundation` only, no Material artefact (D1). ABI pinned with `abiValidation`, dump in `plakat-core/api/`. `androidResources { enable = true }` so the AAR carries fonts.
- Two gates in CI on `macos-latest`: `make check` and `./gradlew check` (D12). `local.properties` git-ignored.
- Rejected: several modules from the start (core, icons, agit). One module until a consumer needs to leave part out.
- Not covered: publishing ([B-37](B-37-public-repository-and-publishing.md)).

- AC: `./gradlew check` is green on the Mac with a smoke test (the theme itself is [B-09](B-09-theme-and-reduce-motion.md)); `./gradlew :sample:run` opens a window; `compileKotlinIosArm64` and the wasm compile pass.
- Anchors: `Makefile`, `.github/workflows/check.yaml`

## Findings

- 2026-11-03. Built as toon-ui's skeleton cut to `plakat-core` and `sample` (sborka 0.4.0.111, the
  `wip` catalog, Gradle 9.8.0). `./gradlew check` green on the Mac: 122 tasks, ktlint, ABI check, the
  core smoke test and the sample test (1 + 1). By hand, also green: `:plakat-core:compileKotlinIosArm64`,
  `:plakat-core:compileAndroidMain`, `:plakat-core:compileKotlinWasmJs`, `:sample:compileKotlinWasmJs`.
  `./gradlew :sample:run` opens the window on the light paper. The sample test was checked by a
  mutant (the name changed): it fails.
- The acceptance first asked for "an empty `PlakatTheme`"; the theme is its own item (B-09), so B-01
  stops at the placeholder constant toon-ui used, and the sample paints the paper literal once.
