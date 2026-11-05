package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatSliderFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-44: the Principles page's live slider and its step 5's two specimens have references. What the page does not draw is
// designed here (D13), goldens of its states for the owner's review at the end: pressed, focused, the dark theme, the
// disabled ticks, a continuous one. The page's `v: 3` of 7 is a half.

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

/** Room for the carriage past the ends, which stands 10 past them there. */
private val Room = 16.dp

@Composable
private fun Slider(
    value: Float = .5f,
    width: Int = 358,
    steps: Int = 5,
    enabled: Boolean = true,
    pressed: Boolean = false,
    focused: Boolean = false,
    modifier: Modifier = Modifier,
) = PlakatSliderFrame(
    value,
    {},
    modifier.width(width.dp),
    enabled,
    0f..1f,
    steps,
    null,
    null,
    forcePressed = pressed,
    forceFocused = focused,
)

@Composable
private fun Cell(
    colors: PlakatColors,
    content: @Composable () -> Unit,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper).padding(horizontal = Room)) { content() }
}

@ViddikScreenshot(group = "Slider", name = "Live_Light", width = 358, height = 48)
@Composable
fun SliderLiveLight() =
    PlakatTheme(colors = light, reduceMotion = true) {
        Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) { Slider() }
    }

/** Step 5's strip, 352 × 48 of its panel from its 24th column and 10th row: default, disabled, on the panel's concrete. */
@ViddikScreenshot(group = "Slider", name = "Specimens_Light", width = 352, height = 48)
@Composable
fun SliderSpecimensLight() =
    PlakatTheme(colors = light, reduceMotion = true) {
        Box(Modifier.fillMaxSize().background(PlakatTheme.colors.concrete)) {
            Slider(width = 160, steps = 0, modifier = Modifier.offset(0.dp, (-2).dp))
            Slider(width = 160, steps = 0, enabled = false, modifier = Modifier.offset(190.dp, (-2).dp))
        }
    }

@ViddikScreenshot(group = "Slider", name = "Stepped_Dark", width = 390, height = 48)
@Composable
fun SliderSteppedDark() = Cell(dark) { Slider() }

@ViddikScreenshot(group = "Slider", name = "Continuous_Light", width = 390, height = 48)
@Composable
fun SliderContinuousLight() = Cell(light) { Slider(.37f, steps = 0) }

@ViddikScreenshot(group = "Slider", name = "Continuous_Dark", width = 390, height = 48)
@Composable
fun SliderContinuousDark() = Cell(dark) { Slider(.37f, steps = 0) }

@ViddikScreenshot(group = "Slider", name = "Pressed_Light", width = 390, height = 48)
@Composable
fun SliderPressedLight() = Cell(light) { Slider(pressed = true) }

@ViddikScreenshot(group = "Slider", name = "Pressed_Dark", width = 390, height = 48)
@Composable
fun SliderPressedDark() = Cell(dark) { Slider(pressed = true) }

@ViddikScreenshot(group = "Slider", name = "Focused_Light", width = 390, height = 48)
@Composable
fun SliderFocusedLight() = Cell(light) { Slider(focused = true) }

@ViddikScreenshot(group = "Slider", name = "Disabled_Light", width = 390, height = 48)
@Composable
fun SliderDisabledLight() = Cell(light) { Slider(enabled = false) }

@ViddikScreenshot(group = "Slider", name = "Disabled_Dark", width = 390, height = 48)
@Composable
fun SliderDisabledDark() = Cell(dark) { Slider(enabled = false) }

@ViddikScreenshot(group = "Slider", name = "Ends_Light", width = 390, height = 48)
@Composable
fun SliderEndsLight() = Cell(light) { Slider(1f) }
