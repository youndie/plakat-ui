package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.youndie.plakat.components.PlakatListItemFrame
import io.github.youndie.plakat.components.PlakatMenuItem
import io.github.youndie.plakat.components.PlakatMenuPanel
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-45: designed here (D13), so no reference: goldens of its states, for the owner's review at the end. The panel, as a
// fixture has no window for a popup.

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

private const val W = 240
private const val H = 186

@Composable
private fun Cell(
    colors: PlakatColors,
    progress: Float = 1f,
    content: @Composable ColumnScope.() -> Unit,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.fillMaxSize().background(PlakatTheme.colors.concrete)) {
        PlakatMenuPanel(Modifier, { progress }, content)
    }
}

@Composable
private fun ColumnScope.Items() {
    PlakatMenuItem("Сохранить", {})
    PlakatMenuItem("Поделиться", {})
    PlakatMenuItem("В архив", {}, divider = false)
}

@ViddikScreenshot(group = "Menu", name = "Open_Light", width = W, height = H)
@Composable
fun MenuOpenLight() = Cell(light) { Items() }

@ViddikScreenshot(group = "Menu", name = "Open_Dark", width = W, height = H)
@Composable
fun MenuOpenDark() = Cell(dark) { Items() }

@ViddikScreenshot(group = "Menu", name = "Half_Light", width = W, height = H)
@Composable
fun MenuHalfLight() = Cell(light, .5f) { Items() }

@ViddikScreenshot(group = "Menu", name = "Numbered_Light", width = W, height = H)
@Composable
fun MenuNumberedLight() =
    Cell(light) {
        PlakatMenuItem("Сохранить", {}, number = "01")
        PlakatMenuItem("Поделиться", {}, number = "02", enabled = false)
        PlakatMenuItem("В архив", {}, number = "03", divider = false)
    }

@ViddikScreenshot(group = "Menu", name = "Pressed_Light", width = W, height = H)
@Composable
fun MenuPressedLight() = Pressed(light)

@ViddikScreenshot(group = "Menu", name = "Pressed_Dark", width = W, height = H)
@Composable
fun MenuPressedDark() = Pressed(dark)

@Composable
private fun Pressed(colors: PlakatColors) =
    Cell(colors) {
        PlakatMenuItem("Сохранить", {})
        PlakatListItemFrame("Поделиться", Modifier, null, null, null, {}, true, true, null, forcePressed = true)
        PlakatMenuItem("В архив", {}, divider = false)
    }
