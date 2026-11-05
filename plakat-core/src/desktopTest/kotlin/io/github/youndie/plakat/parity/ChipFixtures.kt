package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.ChipLook
import io.github.youndie.plakat.components.PlakatChipFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-22: the MVP page's Chip cells: the 32 px plate, its 48 px target cut away. The Default is stretched
// to the 80 px of its caption by the page's column, so its width is passed; the others are their word's.
// The focused cell shows the 6 px of its ring.

private const val RING = 6

@Composable
private fun Cell(
    colors: PlakatColors,
    width: Int,
    fixed: Boolean = false,
    selected: Boolean = false,
    enabled: Boolean = true,
    look: ChipLook? = null,
    ring: Int = 0,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(
        Modifier.size(width.dp + 2.dp * ring, 32.dp + 2.dp * ring).clipToBounds().background(PlakatTheme.colors.paper),
        contentAlignment = Alignment.Center,
    ) {
        val size = if (fixed) Modifier.width(width.dp) else Modifier
        PlakatChipFrame("ШРИФТ", selected, {}, size.requiredHeight(48.dp), enabled, null, look)
    }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Chip", name = "Default_Light", width = 80, height = 32)
@Composable
fun ChipDefaultLight() = Cell(light, 80, fixed = true)

@ViddikScreenshot(group = "Chip", name = "Default_Dark", width = 80, height = 32)
@Composable
fun ChipDefaultDark() = Cell(dark, 80, fixed = true)

@ViddikScreenshot(group = "Chip", name = "Selected_Light", width = 92, height = 32)
@Composable
fun ChipSelectedLight() = Cell(light, 92, selected = true)

@ViddikScreenshot(group = "Chip", name = "Selected_Dark", width = 92, height = 32)
@Composable
fun ChipSelectedDark() = Cell(dark, 92, selected = true)

@ViddikScreenshot(group = "Chip", name = "Focused_Light", width = 76 + 2 * RING, height = 32 + 2 * RING)
@Composable
fun ChipFocusedLight() = Cell(light, 76, look = ChipLook.Focused, ring = RING)

@ViddikScreenshot(group = "Chip", name = "Focused_Dark", width = 76 + 2 * RING, height = 32 + 2 * RING)
@Composable
fun ChipFocusedDark() = Cell(dark, 76, look = ChipLook.Focused, ring = RING)

@ViddikScreenshot(group = "Chip", name = "Disabled_Light", width = 78, height = 32)
@Composable
fun ChipDisabledLight() = Cell(light, 78, enabled = false)

@ViddikScreenshot(group = "Chip", name = "Disabled_Dark", width = 78, height = 32)
@Composable
fun ChipDisabledDark() = Cell(dark, 78, enabled = false)
