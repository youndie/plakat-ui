package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.github.youndie.plakat.components.drawWipeStrip
import io.github.youndie.plakat.components.screenEdge
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-34: the MVP page's strip of frames, 60 × 106 at its four timestamps: paper, concrete for the new screen, the red
// strip, over the cell's own hairline, `box-shadow: inset 0 0 0 1px`, which is the page's and not the component's and
// which an inset shadow paints under the children.

@Composable
private fun Frame(ms: Int) =
    PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) {
        val colors = PlakatTheme.colors
        val p = ms / 320f
        Box(
            Modifier.fillMaxSize().background(colors.paper).drawBehind {
                drawRect(colors.ink, Offset.Zero, Size(size.width, 1f))
                drawRect(colors.ink, Offset(0f, size.height - 1f), Size(size.width, 1f))
                drawRect(colors.ink, Offset.Zero, Size(1f, size.height))
                drawRect(colors.ink, Offset(size.width - 1f, 0f), Size(1f, size.height))
            },
        ) {
            Spacer(Modifier.fillMaxSize().screenEdge { p }.background(colors.concrete))
            Spacer(Modifier.fillMaxSize().drawBehind { drawWipeStrip(p, colors.red) })
        }
    }

@ViddikScreenshot(group = "ScreenWipe", name = "F0", width = 60, height = 106)
@Composable
fun ScreenWipeF0() = Frame(0)

@ViddikScreenshot(group = "ScreenWipe", name = "F107", width = 60, height = 106)
@Composable
fun ScreenWipeF107() = Frame(107)

@ViddikScreenshot(group = "ScreenWipe", name = "F213", width = 60, height = 106)
@Composable
fun ScreenWipeF213() = Frame(213)

@ViddikScreenshot(group = "ScreenWipe", name = "F320", width = 60, height = 106)
@Composable
fun ScreenWipeF320() = Frame(320)
