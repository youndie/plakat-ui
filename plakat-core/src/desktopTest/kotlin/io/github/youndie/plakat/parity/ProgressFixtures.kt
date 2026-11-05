package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatCircularProgress
import io.github.youndie.plakat.components.PlakatLinearProgress
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-24: the MVP page's Progress cells: the 8 px bar in its column of 218.67 px (the crop takes 219 or 218,
// as Blink snaps it), the 40 px circle and the 24 px inline one, on the theme's paper.

@Composable
private fun Linear(
    colors: PlakatColors,
    value: Float,
    width: Int,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(width.dp, 8.dp).background(PlakatTheme.colors.paper)) { PlakatLinearProgress(value) }
}

@Composable
private fun Circular(
    colors: PlakatColors,
    value: Float?,
    size: Int = 40,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(size.dp).background(PlakatTheme.colors.paper)) {
        PlakatCircularProgress(value, Modifier.size(size.dp))
    }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Progress", name = "Linear0_Light", width = 219, height = 8)
@Composable
fun ProgressLinear0Light() = Linear(light, 0f, 219)

@ViddikScreenshot(group = "Progress", name = "Linear0_Dark", width = 219, height = 8)
@Composable
fun ProgressLinear0Dark() = Linear(dark, 0f, 219)

@ViddikScreenshot(group = "Progress", name = "Linear40_Light", width = 218, height = 8)
@Composable
fun ProgressLinear40Light() = Linear(light, .4f, 218)

@ViddikScreenshot(group = "Progress", name = "Linear40_Dark", width = 218, height = 8)
@Composable
fun ProgressLinear40Dark() = Linear(dark, .4f, 218)

@ViddikScreenshot(group = "Progress", name = "Linear100_Light", width = 218, height = 8)
@Composable
fun ProgressLinear100Light() = Linear(light, 1f, 218)

@ViddikScreenshot(group = "Progress", name = "Linear100_Dark", width = 218, height = 8)
@Composable
fun ProgressLinear100Dark() = Linear(dark, 1f, 218)

@ViddikScreenshot(group = "Progress", name = "Circular25_Light", width = 40, height = 40)
@Composable
fun ProgressCircular25Light() = Circular(light, .25f)

@ViddikScreenshot(group = "Progress", name = "Circular25_Dark", width = 40, height = 40)
@Composable
fun ProgressCircular25Dark() = Circular(dark, .25f)

@ViddikScreenshot(group = "Progress", name = "Circular70_Light", width = 40, height = 40)
@Composable
fun ProgressCircular70Light() = Circular(light, .7f)

@ViddikScreenshot(group = "Progress", name = "Circular70_Dark", width = 40, height = 40)
@Composable
fun ProgressCircular70Dark() = Circular(dark, .7f)

@ViddikScreenshot(group = "Progress", name = "Spinner_Light", width = 40, height = 40)
@Composable
fun ProgressSpinnerLight() = Circular(light, null)

@ViddikScreenshot(group = "Progress", name = "Spinner_Dark", width = 40, height = 40)
@Composable
fun ProgressSpinnerDark() = Circular(dark, null)

@ViddikScreenshot(group = "Progress", name = "SpinnerInline_Light", width = 24, height = 24)
@Composable
fun ProgressSpinnerInlineLight() = Circular(light, null, 24)

@ViddikScreenshot(group = "Progress", name = "SpinnerInline_Dark", width = 24, height = 24)
@Composable
fun ProgressSpinnerInlineDark() = Circular(dark, null, 24)
