package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatSwitchFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-16: the Core page's Switch cells, the 56 × 48 track; the focused one with the 7 px of its ring.

private const val RING = 7

@Composable
private fun Cell(
    colors: PlakatColors,
    checked: Boolean,
    enabled: Boolean = true,
    focused: Boolean = false,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    val ring = if (focused) RING else 0
    Box(Modifier.size((56 + 2 * ring).dp, (48 + 2 * ring).dp).background(PlakatTheme.colors.paper)) {
        PlakatSwitchFrame(checked, {}, Modifier.padding(ring.dp), enabled, null, forceFocused = focused)
    }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Switch", name = "Off_Light", width = 56, height = 48)
@Composable
fun SwitchOffLight() = Cell(light, false)

@ViddikScreenshot(group = "Switch", name = "Off_Dark", width = 56, height = 48)
@Composable
fun SwitchOffDark() = Cell(dark, false)

@ViddikScreenshot(group = "Switch", name = "On_Light", width = 56, height = 48)
@Composable
fun SwitchOnLight() = Cell(light, true)

@ViddikScreenshot(group = "Switch", name = "On_Dark", width = 56, height = 48)
@Composable
fun SwitchOnDark() = Cell(dark, true)

@ViddikScreenshot(group = "Switch", name = "Focused_Light", width = 56 + 2 * RING, height = 48 + 2 * RING)
@Composable
fun SwitchFocusedLight() = Cell(light, true, focused = true)

@ViddikScreenshot(group = "Switch", name = "Focused_Dark", width = 56 + 2 * RING, height = 48 + 2 * RING)
@Composable
fun SwitchFocusedDark() = Cell(dark, true, focused = true)

@ViddikScreenshot(group = "Switch", name = "Disabled_Light", width = 56, height = 48)
@Composable
fun SwitchDisabledLight() = Cell(light, true, enabled = false)

@ViddikScreenshot(group = "Switch", name = "Disabled_Dark", width = 56, height = 48)
@Composable
fun SwitchDisabledDark() = Cell(dark, true, enabled = false)
