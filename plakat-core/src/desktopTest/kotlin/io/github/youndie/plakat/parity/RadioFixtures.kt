package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatRadioButtonFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-38: designed here (D13), so no reference: goldens of its states, for the owner's review at the end.

@Composable
private fun Cell(
    colors: PlakatColors,
    selected: Boolean,
    enabled: Boolean = true,
    focused: Boolean = false,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(48.dp).background(PlakatTheme.colors.paper)) {
        PlakatRadioButtonFrame(selected, {}, Modifier, enabled, null, forceFocused = focused)
    }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Radio", name = "Off_Light", width = 48, height = 48)
@Composable
fun RadioOffLight() = Cell(light, false)

@ViddikScreenshot(group = "Radio", name = "Off_Dark", width = 48, height = 48)
@Composable
fun RadioOffDark() = Cell(dark, false)

@ViddikScreenshot(group = "Radio", name = "On_Light", width = 48, height = 48)
@Composable
fun RadioOnLight() = Cell(light, true)

@ViddikScreenshot(group = "Radio", name = "On_Dark", width = 48, height = 48)
@Composable
fun RadioOnDark() = Cell(dark, true)

@ViddikScreenshot(group = "Radio", name = "Focused_Light", width = 48, height = 48)
@Composable
fun RadioFocusedLight() = Cell(light, true, focused = true)

@ViddikScreenshot(group = "Radio", name = "Focused_Dark", width = 48, height = 48)
@Composable
fun RadioFocusedDark() = Cell(dark, true, focused = true)

@ViddikScreenshot(group = "Radio", name = "Disabled_Light", width = 48, height = 48)
@Composable
fun RadioDisabledLight() = Cell(light, true, enabled = false)

@ViddikScreenshot(group = "Radio", name = "Disabled_Dark", width = 48, height = 48)
@Composable
fun RadioDisabledDark() = Cell(dark, true, enabled = false)
