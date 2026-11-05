// B-06: the frame benchmark on the attached phone. `./gradlew :bench-android:installRelease`, then
// `scripts/device-bench.sh`, which starts the activity and pulls its logs. Not the kit, not a sample: a harness.
plugins {
    alias(wip.plugins.androidApplication)
    // No `org.jetbrains.kotlin.android`: since AGP 9 the Android plugin brings Kotlin itself.
    alias(wip.plugins.composeCompiler)
    id("io.github.youndie.sborka.base")
    id("io.github.youndie.sborka.lint")
}

android {
    namespace = "io.github.youndie.plakat.bench.android"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "io.github.youndie.plakat.bench"
        // A harness for one phone, and what it reads off the platform takes 31: a frame's deadline and its GPU time.
        minSdk = 31
        targetSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "0.1"
    }

    buildFeatures { compose = true }

    // Release with R8, because a debuggable Compose app runs paths no user does and any frame time would be meaningless.
    // Signed with the debug key: it is installed by hand onto one phone, never shipped.
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(project(":sample"))
    implementation(project(":plakat-core"))
    implementation(wip.compose.runtime)
    implementation(wip.compose.foundation)
    implementation(wip.compose.ui)
    implementation(libs.androidx.activity.compose)
}
