plugins {
    alias(wip.plugins.kotlinMultiplatform) apply false
    alias(wip.plugins.composeMultiplatform) apply false
    alias(wip.plugins.composeCompiler) apply false
    alias(wip.plugins.ksp) apply false
    alias(libs.plugins.viddik) apply false
    // Declared here, unapplied, and that is load-bearing: the Compose plugin reads AGP's extension
    // types to wire resources and can only see them when both land in the same build classloader
    // (oldge-ui, kvadrant-ui and toon-ui met this).
    alias(wip.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(wip.plugins.androidApplication) apply false
    alias(libs.plugins.sborkaKmp) apply false
    alias(libs.plugins.sborkaLint) apply false
}
