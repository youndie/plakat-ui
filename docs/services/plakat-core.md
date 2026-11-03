---
id: plakat-core
title: plakat-core
type: service
module: plakat-core
tech_stack: [Kotlin Multiplatform, Compose Multiplatform]
owner: unassigned
depends_on:
  - sborka (build conventions and the `wip` catalog)
publishes: []
---

# plakat-core

## 1. Responsibility

The library module: everything a consumer of plakat-ui depends on. Today it is the build and nothing
else. It compiles for Android, iOS (`iosArm64`, `iosSimulatorArm64`), desktop JVM and wasmJs, and its
desktop test suite composes off-screen (B-01). Tokens, the generator and raskat ports, print
primitives, icons and the components land here item by item (backlog stages 0–2).

It deliberately does **not** depend on any Material artefact (research D1). The sample kit is the
`sample` module beside it, a consumer of the public API only. It is not published yet (B-37).

## 2. API contracts

No public API yet; `plakat-core/api/` holds the empty ABI dump that `check` holds every change to.
Packages are `io.github.youndie.plakat.*` (research D1).

## 2a. Code anchors

| File | What is there |
|---|---|
| `plakat-core/build.gradle.kts` | targets, dependencies, ABI validation, the iOS metadata warnings cut |
| `plakat-core/api/` | the committed public API dump (`updateKotlinAbi` records it) |
| `sample/build.gradle.kts` | the sample kit on desktop and wasm, `./gradlew :sample:run` |
| `settings.gradle.kts` | plugin repositories and the sborka settings plugin, which brings the `wip` catalog |
| `gradle/libs.versions.toml` | the sborka version and the Android SDK levels; the compiler and Compose come from `wip` |
| `gradle.properties` | the coordinates and licence sborka reads (`sborka.*`) |
| `plakat-core/src/desktopTest/kotlin/io/github/youndie/plakat/ComposeSmokeTest.kt` | the smoke test the visual suite stands on |
| `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/type/PlakatFonts.kt` | the display and body families |
| `plakat-core/src/commonMain/composeResources/font/` | Rubik Mono One and the static Golos Text 400 and 600, with their OFL texts in `files/licences/` |
| `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/agit/PlakatGen.kt` | the composition and rapport generator, ported from the design's JS |
| `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/print/PlakatRaskat.kt` | raskat's field, ink map and roll, ported from the design's JS; `RaskatImage.kt` prints it |
| `scripts/raskat-goldens.mjs` | raskat's goldens, emitted by the vendored JS with a stubbed canvas and clock; `--check` in `make check` |
| `scripts/gen-goldens.mjs` | the generator's goldens, emitted by the vendored JS; `--check` runs in `make check` |
| `scripts/fonts.py` | fetches the fonts at a pinned google/fonts commit and cuts the Golos statics (`scripts/fonts.lock`) |

## 3. How it is built

The build is toon-ui's, cut down to one library module and a sample. `io.github.youndie.sborka.settings`
supplies the repositories and the `wip` version catalog, so the Kotlin compiler, AGP and Compose
Multiplatform versions are a sborka bump, never a line here. `sborka.kmp` sets the toolchain and
compiler flags, explicit API and warnings-as-errors included. `sborka.lint` runs ktlint against the
portfolio's `.editorconfig`.

The AGP KMP library plugin is declared, unapplied, in the root build. The Compose plugin can only see
AGP's extension types when both are in the same build classloader. oldge-ui, kvadrant-ui and toon-ui
each met this once.

## 4. Dependencies

| Kind | Name | What for |
|---|---|---|
| Library | Compose runtime, foundation, ui (from `wip`) | everything drawn |
| Library | compose resources (`compose.components.resources`) | the bundled fonts, package `io.github.youndie.plakat.resources` |
| Build | sborka 0.4.0.111 | conventions and the `wip` catalog, the same build as toon-ui |
| Test | Compose ui-test, `compose.desktop.currentOs` (desktopTest only) | off-screen composition on the host's Skia |

## 5. Infrastructure and deploy

Nothing is published or deployed. Publishing is B-37, the last item.

## 6. Local setup

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
make check          # needs Node 20+ for the goldens check
./gradlew check
./gradlew :sample:run
```

On the Mac, not on the Linux box (research D12): the project is not in `mutagen sync list`, and the
screenshot goldens to come are a claim about the Mac's rasteriser. `check` also runs the iOS simulator
tests, so Xcode's simulator runtime has to be installed. `local.properties` names the Android SDK; it
is not committed.

## 7. Configuration

None at runtime.

Regenerating the fonts needs Python with fontTools (`.venv/bin/pip install fonttools`); the gate does
not run it, because the output is committed and pinned by the metrics test.

## 8. Quirks

- `compileIosMainKotlinMetadata` and its two siblings compile without warnings-as-errors. CMP 1.12
  brings two lifecycle klibs with one `unique_name` into the shared iOS metadata, and the loader's
  warning about it is not this module's to fix. The per-target iOS compilations keep `-Werror`.
- `./gradlew check` does **not** compile `iosArm64` or Android main. Those are
  `:plakat-core:compileKotlinIosArm64` and `:plakat-core:compileAndroidMain`, run by hand in B-01. A
  change that breaks only a device target passes `check`.
- `plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/PlakatCore.kt` holds one internal
  placeholder constant. ktlint refuses an empty file, and without a main source the iOS, wasm and
  Android compilations are `NO-SOURCE`, which proves nothing. B-08 replaces it.
- Common test names must not contain a comma: Kotlin/Native refuses the name, JVM accepts it.
- The klib ABI dump lists the compose compiler's `$stableprop` of internal classes too; adding an
  internal class changes it and needs `updateKotlinAbi`.
- The sample's paper colour is written once as a literal until the tokens exist (B-08).
