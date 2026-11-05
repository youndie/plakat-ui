package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.github.youndie.plakat.components.PlakatStretch
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-31: the Stretch page's lines, each in its box and on the ground it stands on there.

@Composable
private fun Line(
    text: String,
    concrete: Boolean = false,
    red: Boolean = false,
) = PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) {
    val colors = PlakatTheme.colors
    PlakatStretch(
        text,
        Modifier.fillMaxSize().background(if (concrete) colors.concrete else colors.paper),
        if (red) colors.red else colors.ink,
    )
}

/** The sandbox's ground: `linear-gradient(rgba(29,27,24,.12) 0 1px, transparent 1px)` both ways, every 8 px. */
@Composable
private fun Sandbox(text: String) =
    PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) {
        val colors = PlakatTheme.colors
        Box(
            Modifier.fillMaxSize().background(colors.paper).drawBehind {
                val line = Color(0xFF1D1B18).copy(alpha = .12f)
                for (y in 0 until size.height.toInt() step 8) {
                    drawRect(
                        line,
                        Offset(0f, y.toFloat()),
                        Size(size.width, 1f),
                    )
                }
                for (x in 0 until size.width.toInt() step 8) {
                    drawRect(
                        line,
                        Offset(x.toFloat(), 0f),
                        Size(1f, size.height),
                    )
                }
            },
        ) {
            PlakatStretch(text, Modifier.fillMaxSize())
        }
    }

@ViddikScreenshot(group = "Stretch", name = "Hero", width = 720, height = 120)
@Composable
fun StretchHero() = Line("РАСТЯЖКА")

@ViddikScreenshot(group = "Stretch", name = "PosterFirst", width = 358, height = 136)
@Composable
fun StretchPosterFirst() = Line("ЧИТАЙ")

@ViddikScreenshot(group = "Stretch", name = "PosterSecond", width = 358, height = 120)
@Composable
fun StretchPosterSecond() = Line("ВЕСЬ")

@ViddikScreenshot(group = "Stretch", name = "PosterThird", width = 358, height = 80)
@Composable
fun StretchPosterThird() = Line("НОЯБРЬ")

@ViddikScreenshot(group = "Stretch", name = "Masthead", width = 358, height = 120)
@Composable
fun StretchMasthead() = Line("ЛИСТ")

@ViddikScreenshot(group = "Stretch", name = "Number", width = 112, height = 104)
@Composable
fun StretchNumber() = Line("47", concrete = true)

@ViddikScreenshot(group = "Stretch", name = "NumberRed", width = 56, height = 104)
@Composable
fun StretchNumberRed() = Line("6", concrete = true, red = true)

@ViddikScreenshot(group = "Stretch", name = "Section", width = 358, height = 48)
@Composable
fun StretchSection() = Line("АРХИТЕКТУРА")

@ViddikScreenshot(group = "Stretch", name = "Sandbox", width = 358, height = 160)
@Composable
fun StretchSandbox() = Sandbox("ЛИСТ")

@ViddikScreenshot(group = "Stretch", name = "SandboxLatin", width = 358, height = 160)
@Composable
fun StretchSandboxLatin() = Sandbox("POSTER")
