// The sample kit: every component grouped as the design's pages group them, and later the Demo
// page's three screens (B-35). Not published, not a dependency of anything: `./gradlew :sample:run`
// on the desktop, `:sample:wasmJsBrowserDistribution` for the browser.
plugins {
    alias(wip.plugins.kotlinMultiplatform)
    alias(wip.plugins.composeMultiplatform)
    alias(wip.plugins.composeCompiler)
    alias(wip.plugins.ksp)
    alias(libs.plugins.viddik)
    id("io.github.youndie.sborka.kmp")
    id("io.github.youndie.sborka.lint")
}

kotlin {
    // A sample, not a library: sborka's explicit API mode guards a published surface it does not have.
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Disabled

    jvm("desktop")

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser { commonWebpackConfig { outputFileName = "sample.js" } }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":plakat-core"))
            implementation(wip.compose.runtime)
            implementation(wip.compose.foundation)
            implementation(wip.compose.ui)
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
        }
        getByName("desktopTest").dependencies {
            implementation(kotlin("test"))
            implementation(wip.compose.ui.test)
        }
    }
}

compose.desktop.application {
    mainClass = "io.github.youndie.plakat.sample.MainKt"
    nativeDistributions { packageName = "plakat-ui" }
}

// B-35: the Demo page's screens, held as plakat-core's components are (its `viddik` block says why these numbers).
viddik {
    verifyOnCheck.set(true)
    shards.set(1)
    designTolerancePercent.set(5.0)
    designChannelTolerance.set(16)
    designStrict.set(false)
}

tasks.withType<Test>().configureEach {
    inputs
        .dir(layout.projectDirectory.dir("src/desktopTest/snapshots"))
        .withPropertyName("viddikSnapshots")
        .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
}
