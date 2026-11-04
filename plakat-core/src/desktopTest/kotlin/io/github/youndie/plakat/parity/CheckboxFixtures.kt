package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.components.PlakatCheckboxFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-15: the Core page's Checkbox cells, the 48 px zone; the error cell is the zone and the page's word
// «Нужно» 4 px after it (13 px Golos 600), in a 104 px column.

@Composable
private fun Cell(
    colors: PlakatColors,
    checked: Boolean,
    enabled: Boolean = true,
    focused: Boolean = false,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Row(Modifier.size(48.dp).background(PlakatTheme.colors.paper)) {
        PlakatCheckboxFrame(checked, {}, Modifier, enabled, false, null, forceFocused = focused)
    }
}

@Composable
private fun ErrorCell(colors: PlakatColors) =
    PlakatTheme(colors = colors, reduceMotion = true) {
        Row(
            Modifier.size(104.dp, 48.dp).background(PlakatTheme.colors.paper),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlakatCheckboxFrame(false, {}, Modifier, true, true, null)
            BasicText(
                "Нужно",
                style =
                    PlakatTheme.typography.bodyS.style.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PlakatTheme.colors.ink,
                    ),
            )
        }
    }

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Checkbox", name = "Off_Light", width = 48, height = 48)
@Composable
fun CheckboxOffLight() = Cell(light, false)

@ViddikScreenshot(group = "Checkbox", name = "Off_Dark", width = 48, height = 48)
@Composable
fun CheckboxOffDark() = Cell(dark, false)

@ViddikScreenshot(group = "Checkbox", name = "On_Light", width = 48, height = 48)
@Composable
fun CheckboxOnLight() = Cell(light, true)

@ViddikScreenshot(group = "Checkbox", name = "On_Dark", width = 48, height = 48)
@Composable
fun CheckboxOnDark() = Cell(dark, true)

@ViddikScreenshot(group = "Checkbox", name = "Focused_Light", width = 48, height = 48)
@Composable
fun CheckboxFocusedLight() = Cell(light, true, focused = true)

@ViddikScreenshot(group = "Checkbox", name = "Focused_Dark", width = 48, height = 48)
@Composable
fun CheckboxFocusedDark() = Cell(dark, true, focused = true)

@ViddikScreenshot(group = "Checkbox", name = "Disabled_Light", width = 48, height = 48)
@Composable
fun CheckboxDisabledLight() = Cell(light, true, enabled = false)

@ViddikScreenshot(group = "Checkbox", name = "Disabled_Dark", width = 48, height = 48)
@Composable
fun CheckboxDisabledDark() = Cell(dark, true, enabled = false)

@ViddikScreenshot(group = "Checkbox", name = "Error_Light", width = 104, height = 48)
@Composable
fun CheckboxErrorLight() = ErrorCell(light)

@ViddikScreenshot(group = "Checkbox", name = "Error_Dark", width = 104, height = 48)
@Composable
fun CheckboxErrorDark() = ErrorCell(dark)
