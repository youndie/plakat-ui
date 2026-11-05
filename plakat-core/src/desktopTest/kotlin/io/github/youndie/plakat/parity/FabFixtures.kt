package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.ButtonLook
import io.github.youndie.plakat.components.PlakatFabFrame
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-21: the MVP page's FAB cells, the 56 × 56 circle on the theme's paper. A focused one has no
// reference (the page draws none): its golden is this library's own, 6 px wider each side for its ring.

private const val RING = 6

@Composable
private fun Cell(
    colors: PlakatColors,
    enabled: Boolean = true,
    look: ButtonLook? = null,
    ring: Int = 0,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size((56 + 2 * ring).dp).background(PlakatTheme.colors.paper), contentAlignment = Alignment.Center) {
        PlakatFabFrame(
            PlakatIcons.Add,
            null,
            {},
            Modifier,
            enabled,
            rideIn = false,
            interactionSource = null,
            look = look,
        )
    }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Fab", name = "Default_Light", width = 56, height = 56)
@Composable
fun FabDefaultLight() = Cell(light)

@ViddikScreenshot(group = "Fab", name = "Default_Dark", width = 56, height = 56)
@Composable
fun FabDefaultDark() = Cell(dark)

@ViddikScreenshot(group = "Fab", name = "Pressed_Light", width = 56, height = 56)
@Composable
fun FabPressedLight() = Cell(light, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Fab", name = "Pressed_Dark", width = 56, height = 56)
@Composable
fun FabPressedDark() = Cell(dark, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Fab", name = "Disabled_Light", width = 56, height = 56)
@Composable
fun FabDisabledLight() = Cell(light, enabled = false)

@ViddikScreenshot(group = "Fab", name = "Disabled_Dark", width = 56, height = 56)
@Composable
fun FabDisabledDark() = Cell(dark, enabled = false)

@ViddikScreenshot(group = "Fab", name = "Focused_Light", width = 56 + 2 * RING, height = 56 + 2 * RING)
@Composable
fun FabFocusedLight() = Cell(light, look = ButtonLook.Focused, ring = RING)

@ViddikScreenshot(group = "Fab", name = "Focused_Dark", width = 56 + 2 * RING, height = 56 + 2 * RING)
@Composable
fun FabFocusedDark() = Cell(dark, look = ButtonLook.Focused, ring = RING)
