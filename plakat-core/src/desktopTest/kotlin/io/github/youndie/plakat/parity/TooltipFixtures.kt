package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatTooltipAxis
import io.github.youndie.plakat.components.PlakatTooltipCaption
import io.github.youndie.plakat.components.TooltipHolder
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-48: designed here (D13), so no reference: goldens of its states, for the owner's review at the end. The caption placed as
// the popup would, as a fixture has no window for one; the anchor a block of ink.

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

private const val W = 390
private const val H = 160

@Composable
private fun Cell(
    colors: PlakatColors,
    axis: PlakatTooltipAxis,
    anchor: IntRect = IntRect(40, 90, 88, 138),
    progress: Float = 1f,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    val holder = remember { TooltipHolder(anchor) }
    Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
        Box(
            Modifier
                .offset(anchor.left.dp, anchor.top.dp)
                .size(anchor.width.dp, anchor.height.dp)
                .background(PlakatTheme.colors.ink),
        )
        Layout(
            content = {
                PlakatTooltipCaption("Сохранить в лист", axis, holder, IntSize(W, H)) { progress }
            },
        ) { measurables, _ ->
            val caption = measurables[0].measure(Constraints())
            layout(W, H) {
                val box = holder.placement!!.box
                caption.place(box.left, box.top)
            }
        }
    }
}

@ViddikScreenshot(group = "Tooltip", name = "Upright_Light", width = W, height = H)
@Composable
fun TooltipUprightLight() = Cell(light, PlakatTooltipAxis.Upright)

@ViddikScreenshot(group = "Tooltip", name = "Upright_Dark", width = W, height = H)
@Composable
fun TooltipUprightDark() = Cell(dark, PlakatTooltipAxis.Upright)

@ViddikScreenshot(group = "Tooltip", name = "Level_Light", width = W, height = H)
@Composable
fun TooltipLevelLight() = Cell(light, PlakatTooltipAxis.Level)

@ViddikScreenshot(group = "Tooltip", name = "Tilted_Light", width = W, height = H)
@Composable
fun TooltipTiltedLight() = Cell(light, PlakatTooltipAxis.Tilted)

@ViddikScreenshot(group = "Tooltip", name = "Tilted_Dark", width = W, height = H)
@Composable
fun TooltipTiltedDark() = Cell(dark, PlakatTooltipAxis.Tilted)

/** No room to the right: the tilted leader goes on down the same line to the left, and is no mirror of it. */
@ViddikScreenshot(group = "Tooltip", name = "TiltedLeft_Light", width = W, height = H)
@Composable
fun TooltipTiltedLeftLight() = Cell(light, PlakatTooltipAxis.Tilted, IntRect(300, 40, 348, 88))

@ViddikScreenshot(group = "Tooltip", name = "UprightDown_Light", width = W, height = H)
@Composable
fun TooltipUprightDownLight() = Cell(light, PlakatTooltipAxis.Upright, IntRect(40, 10, 88, 58))

@ViddikScreenshot(group = "Tooltip", name = "Half_Light", width = W, height = H)
@Composable
fun TooltipHalfLight() = Cell(light, PlakatTooltipAxis.Level, progress = .5f)
