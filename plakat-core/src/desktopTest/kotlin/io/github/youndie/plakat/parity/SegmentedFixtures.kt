package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatSegmentedButtonFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-49: the Demo page's settings draw it, 40 high, so that cell has a reference: the page's choice in each theme, its own: the first in the light, the second in the dark. The rest is
// designed here (D13), goldens of its states for the owner's review at the end.

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

private const val W = 200
private const val H = 48
private val words = listOf("БУМАГА", "ЧЁРНАЯ")

@Composable
private fun Cell(
    colors: PlakatColors,
    selected: Int = 0,
    enabled: Boolean = true,
    at: Float? = null,
    pressed: Int? = null,
    focused: Int? = null,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
        PlakatSegmentedButtonFrame(words, selected, {}, Modifier, enabled, at, pressed, focused)
    }
}

/** The page's 155 × 40, in which the target of 48 stands 4 over and under. */
@Composable
private fun Drawn(
    colors: PlakatColors,
    selected: Int,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
        PlakatSegmentedButtonFrame(
            words,
            selected,
            {},
            Modifier.wrapContentHeight(Alignment.Top, unbounded = true).offset(y = (-4).dp),
            true,
        )
    }
}

@ViddikScreenshot(group = "Segmented", name = "Demo_Light", width = 155, height = 40)
@Composable
fun SegmentedDemoLight() = Drawn(light, 0)

@ViddikScreenshot(group = "Segmented", name = "Demo_Dark", width = 155, height = 40)
@Composable
fun SegmentedDemoDark() = Drawn(dark, 1)

@ViddikScreenshot(group = "Segmented", name = "Second_Light", width = W, height = H)
@Composable
fun SegmentedSecondLight() = Cell(light, 1)

@ViddikScreenshot(group = "Segmented", name = "Riding_Light", width = W, height = H)
@Composable
fun SegmentedRidingLight() = Cell(light, 1, at = .5f)

@ViddikScreenshot(group = "Segmented", name = "Riding_Dark", width = W, height = H)
@Composable
fun SegmentedRidingDark() = Cell(dark, 1, at = .5f)

@ViddikScreenshot(group = "Segmented", name = "PressedOff_Light", width = W, height = H)
@Composable
fun SegmentedPressedOffLight() = Cell(light, 0, pressed = 1)

@ViddikScreenshot(group = "Segmented", name = "PressedOn_Light", width = W, height = H)
@Composable
fun SegmentedPressedOnLight() = Cell(light, 0, pressed = 0)

@ViddikScreenshot(group = "Segmented", name = "Focused_Light", width = W, height = H)
@Composable
fun SegmentedFocusedLight() = Cell(light, 0, focused = 1)

@ViddikScreenshot(group = "Segmented", name = "Disabled_Light", width = W, height = H)
@Composable
fun SegmentedDisabledLight() = Cell(light, 0, enabled = false)

@ViddikScreenshot(group = "Segmented", name = "Disabled_Dark", width = W, height = H)
@Composable
fun SegmentedDisabledDark() = Cell(dark, 0, enabled = false)
