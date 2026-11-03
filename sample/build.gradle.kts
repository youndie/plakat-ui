// The sample kit: every component grouped as the design's pages group them, and later the Demo
// page's three screens (B-35). Not published, not a dependency of anything: `./gradlew :sample:run`
// on the desktop, `:sample:wasmJsBrowserDistribution` for the browser.
plugins {
    alias(wip.plugins.kotlinMultiplatform)
    alias(wip.plugins.composeMultiplatform)
    alias(wip.plugins.composeCompiler)
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
