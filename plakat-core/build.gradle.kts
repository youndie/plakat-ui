// The library: everything a consumer of plakat-ui depends on (research D1). No Material artefact,
// no sample and no design references here; publishing arrives with B-37.
plugins {
    alias(wip.plugins.kotlinMultiplatform)
    alias(wip.plugins.composeMultiplatform)
    alias(wip.plugins.composeCompiler)
    alias(wip.plugins.androidKotlinMultiplatformLibrary)
    alias(wip.plugins.ksp)
    alias(libs.plugins.viddik)
    id("io.github.youndie.sborka.kmp")
    id("io.github.youndie.sborka.lint")
}

// Four targets (research D2). Android and iOS lead the brief; desktop is where the screenshot and
// parity gate runs, so a green `check` says nothing visual about the other three.
kotlin {
    jvm("desktop")

    // The public API is pinned: `plakat-core/api/` is the committed dump, and `check` fails on any
    // change to it that was not recorded with `updateKotlinAbi`.
    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {}

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        // Required by the Compose plugin's own check (CMP-4906): without an executable binary the
        // Skiko runtime cannot be bundled for the target's tests, and `build` fails on it.
        binaries.executable()
    }

    iosArm64()
    iosSimulatorArm64()

    android {
        // Off by default in AGP's KMP plugin, and with it off the AAR ships without the bundled
        // fonts B-02 adds, green (kvadrant-ui B-37).
        androidResources { enable = true }

        namespace = "io.github.youndie.plakat"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }

    // `foundation` and nothing above it (research D1).
    sourceSets {
        commonMain.dependencies {
            implementation(wip.compose.runtime)
            implementation(wip.compose.foundation)
            implementation(wip.compose.ui)
            // The bundled fonts (B-02). Not in `wip`, and the plugin's accessor is the one place its
            // version follows the Compose plugin's own (toon-ui does the same).
            implementation(compose.components.resources)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("desktopTest").dependencies {
            // The host's skia. Never in a published source set: the POM would pin the machine
            // that published.
            implementation(compose.desktop.currentOs)
            implementation(wip.compose.ui.test)
        }
    }
}

// B-05. Goldens are verified by `check`; design parity is a report (`viddikDesignParity`) against the
// PNGs scripts/design-references.mjs renders from the spec pages in Chrome. The tolerances are
// viddik's defaults, written here to say they were looked at; the floor they are read against is
// measured in B-05 (research D3) and is the number a component's parity is compared with.
viddik {
    verifyOnCheck.set(true)
    shards.set(1)
    designTolerancePercent.set(5.0)
    designChannelTolerance.set(16)
    designStrict.set(false)
}

compose.resources {
    packageOfResClass = "io.github.youndie.plakat.resources"
}

// A test that reads the vendored design (the font coverage test reads every page's text) declares it,
// or Gradle leaves the test UP-TO-DATE over a re-imported design and reports the last run's verdict.
tasks.withType<Test>().configureEach {
    inputs
        .dir(rootProject.layout.projectDirectory.dir("reference/design-system"))
        .withPropertyName("designSystem")
        .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
    // The same for the goldens and the design references viddik reads (oldge-ui and toon-ui both learnt this).
    inputs
        .dir(layout.projectDirectory.dir("src/desktopTest/snapshots"))
        .withPropertyName("viddikSnapshots")
        .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
}

// The shared iOS metadata compilation, and only it, without warnings-as-errors. CMP 1.12's own graph
// puts two lifecycle klibs with one `unique_name` on it, and the KLIB loader's warning about that is
// not something this module can fix (toon-ui and oldge-ui made the same cut). The per-target iOS
// compilations keep -Werror, so a warning in this library's own code still fails.
tasks
    .matching {
        it.name in
            setOf("compileIosMainKotlinMetadata", "compileAppleMainKotlinMetadata", "compileNativeMainKotlinMetadata")
    }.configureEach {
        (this as org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>).compilerOptions.allWarningsAsErrors.set(
            false,
        )
    }
