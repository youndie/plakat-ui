package io.github.youndie.plakat.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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

// B-36: the same screens in grayscale, where red must still lead and nothing may have been said by colour alone.

@Composable
private fun Gray(content: @Composable () -> Unit) =
    Box(
        Modifier.fillMaxSize().drawWithContent {
            val paint =
                Paint().apply {
                    colorFilter =
                        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                }
            drawIntoCanvas { canvas ->
                canvas.saveLayer(Rect(Offset.Zero, size), paint)
                this@drawWithContent.drawContent()
                canvas.restore()
            }
        },
    ) { content() }

@ViddikScreenshot(group = "Gray", name = "Onboarding_Light", width = 390, height = 844)
@Composable
fun GrayOnboardingLight() = Screen(PlakatPalettes.Paper) { Gray { DemoOnboarding() } }

@ViddikScreenshot(group = "Gray", name = "Onboarding_Dark", width = 390, height = 844)
@Composable
fun GrayOnboardingDark() = Screen(PlakatPalettes.BlackPaper) { Gray { DemoOnboarding() } }

@ViddikScreenshot(group = "Gray", name = "Feed_Light", width = 390, height = 844)
@Composable
fun GrayFeedLight() = Screen(PlakatPalettes.Paper) { Gray { DemoFeed() } }

@ViddikScreenshot(group = "Gray", name = "Feed_Dark", width = 390, height = 844)
@Composable
fun GrayFeedDark() = Screen(PlakatPalettes.BlackPaper) { Gray { DemoFeed() } }

@ViddikScreenshot(group = "Gray", name = "Settings_Light", width = 390, height = 844)
@Composable
fun GraySettingsLight() = Screen(PlakatPalettes.Paper) { Gray { DemoSettings(false, {}) } }

@ViddikScreenshot(group = "Gray", name = "Settings_Dark", width = 390, height = 844)
@Composable
fun GraySettingsDark() = Screen(PlakatPalettes.BlackPaper) { Gray { DemoSettings(true, {}) } }
