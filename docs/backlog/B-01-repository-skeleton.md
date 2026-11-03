---
id: B-01
title: "Repository skeleton: plakat-core, sample, sborka conventions, four targets, CI on macOS"
status: open
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

- AC: `./gradlew check` is green on the Mac with an empty `PlakatTheme` and a smoke test; `./gradlew :sample:run` opens a window; `compileKotlinIosArm64` and the wasm compile pass.
- Anchors: `Makefile`, `.github/workflows/check.yaml`
