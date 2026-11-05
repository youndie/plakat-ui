package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.components.PlakatCardFrame
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-17: the Core page's Card cells, 344 px wide in the 752 px theme card. Their content is the page's:
// the 64 × 64 thumbnail from the page's own generator, a rubric (11 px Golos 600, tracking .08 em,
// line `normal`) and a title 4 px below it (17 px Golos 600, line 1.25). The cells are cropped to the
// floor of the sheet's fractional height, so the sheet runs on past the fixture's bottom.

private const val RING = 6

@Composable
private fun Cell(
    colors: PlakatColors,
    thumbnail: Int,
    rubric: String,
    title: String,
    height: Int,
    selected: Boolean = false,
    pressed: Boolean = false,
    focused: Boolean = false,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    val ring = if (focused) RING else 0
    Box(Modifier.size((344 + 2 * ring).dp, (height + 2 * ring).dp).background(PlakatTheme.colors.paper)) {
        PlakatCardFrame(
            {},
            Modifier.padding(ring.dp).fillMaxWidth().wrapContentHeight(Alignment.Top, unbounded = true),
            selected,
            enabled = true,
            printOrder = null,
            dismissed = false,
            onDismissed = {},
            interactionSource = null,
            forcePressed = pressed,
            forceFocused = focused,
        ) {
            PageThumbnail(thumbnail)
            Column(Modifier.weight(1f)) {
                val body = PlakatTheme.typography.bodyS.style
                val color = LocalPlakatContentColor.current
                BasicText(
                    rubric,
                    style =
                        PlakatTheme.typography.label.style
                            .copy(fontSize = 11.sp, color = color)
                            .cssLine(PlakatFaceMetrics.Body),
                )
                Spacer(Modifier.height(4.dp))
                BasicText(
                    title,
                    style =
                        body
                            .copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = color)
                            .cssLine(PlakatFaceMetrics.Body, 1.25f),
                )
            }
        }
    }
}

private const val ARCHITECTURE = "АРХИТЕКТУРА · 8 МИН"
private const val PLYWOOD = "Город из фанеры: как строят временные павильоны"

@Composable
private fun Default(colors: PlakatColors) = Cell(colors, 0, ARCHITECTURE, PLYWOOD, 104)

@Composable
private fun Pressed(colors: PlakatColors) = Cell(colors, 0, ARCHITECTURE, PLYWOOD, 104, pressed = true)

@Composable
private fun Selected(colors: PlakatColors) =
    Cell(colors, 1, "ШРИФТ · 5 МИН · ЧИТАЕТЕ", "Буквы, которые читаются с другого конца площади", 88, selected = true)

@Composable
private fun Focused(colors: PlakatColors) =
    Cell(colors, 2, "ДИЗАЙН · 12 МИН", "Семь способов сложить один лист бумаги", 88, focused = true)

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Card", name = "Default_Light", width = 344, height = 104)
@Composable
fun CardDefaultLight() = Default(light)

@ViddikScreenshot(group = "Card", name = "Default_Dark", width = 344, height = 104)
@Composable
fun CardDefaultDark() = Default(dark)

@ViddikScreenshot(group = "Card", name = "Pressed_Light", width = 344, height = 104)
@Composable
fun CardPressedLight() = Pressed(light)

@ViddikScreenshot(group = "Card", name = "Pressed_Dark", width = 344, height = 104)
@Composable
fun CardPressedDark() = Pressed(dark)

@ViddikScreenshot(group = "Card", name = "Selected_Light", width = 344, height = 88)
@Composable
fun CardSelectedLight() = Selected(light)

@ViddikScreenshot(group = "Card", name = "Selected_Dark", width = 344, height = 88)
@Composable
fun CardSelectedDark() = Selected(dark)

@ViddikScreenshot(group = "Card", name = "Focused_Light", width = 344 + 2 * RING, height = 88 + 2 * RING)
@Composable
fun CardFocusedLight() = Focused(light)

@ViddikScreenshot(group = "Card", name = "Focused_Dark", width = 344 + 2 * RING, height = 88 + 2 * RING)
@Composable
fun CardFocusedDark() = Focused(dark)
