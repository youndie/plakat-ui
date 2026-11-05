package io.github.youndie.plakat.sample

import androidx.compose.runtime.Composable
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-35: the Demo page's three screens, 390 × 844, in its two themes, with the texture off as the references are.

@Composable
private fun Screen(
    colors: PlakatColors,
    content: @Composable () -> Unit,
) = PlakatTheme(colors = colors, reduceMotion = true, print = PlakatPrintSwitches(texture = false)) { content() }

@ViddikScreenshot(group = "Demo", name = "Onboarding_Light", width = 390, height = 844)
@Composable
fun DemoOnboardingLight() = Screen(PlakatPalettes.Paper) { DemoOnboarding() }

@ViddikScreenshot(group = "Demo", name = "Onboarding_Dark", width = 390, height = 844)
@Composable
fun DemoOnboardingDark() = Screen(PlakatPalettes.BlackPaper) { DemoOnboarding() }

@ViddikScreenshot(group = "Demo", name = "Feed_Light", width = 390, height = 844)
@Composable
fun DemoFeedLight() = Screen(PlakatPalettes.Paper) { DemoFeed() }

@ViddikScreenshot(group = "Demo", name = "Feed_Dark", width = 390, height = 844)
@Composable
fun DemoFeedDark() = Screen(PlakatPalettes.BlackPaper) { DemoFeed() }

@ViddikScreenshot(group = "Demo", name = "Settings_Light", width = 390, height = 844)
@Composable
fun DemoSettingsLight() = Screen(PlakatPalettes.Paper) { DemoSettings(false, {}) }

@ViddikScreenshot(group = "Demo", name = "Settings_Dark", width = 390, height = 844)
@Composable
fun DemoSettingsDark() = Screen(PlakatPalettes.BlackPaper) { DemoSettings(true, {}) }
