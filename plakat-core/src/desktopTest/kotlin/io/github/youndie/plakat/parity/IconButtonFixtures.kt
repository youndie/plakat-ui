package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.ButtonLook
import io.github.youndie.plakat.components.PlakatBadgedBox
import io.github.youndie.plakat.components.PlakatIconButtonFrame
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-20: the MVP page's IconButton and Badge cells, 48 × 48, on the theme's paper.

@Composable
private fun Button(
    colors: PlakatColors,
    enabled: Boolean = true,
    selected: Boolean = false,
    look: ButtonLook? = null,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(48.dp).background(PlakatTheme.colors.paper)) {
        PlakatIconButtonFrame(PlakatIcons.Search, null, {}, Modifier, enabled, selected, null, look)
    }
}

@Composable
private fun Badged(
    colors: PlakatColors,
    value: Int?,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    PlakatBadgedBox(value, Modifier.background(PlakatTheme.colors.paper)) { PlakatIcon(PlakatIcons.Menu, null) }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "IconButton", name = "Default_Light", width = 48, height = 48)
@Composable
fun IconButtonDefaultLight() = Button(light)

@ViddikScreenshot(group = "IconButton", name = "Default_Dark", width = 48, height = 48)
@Composable
fun IconButtonDefaultDark() = Button(dark)

@ViddikScreenshot(group = "IconButton", name = "Pressed_Light", width = 48, height = 48)
@Composable
fun IconButtonPressedLight() = Button(light, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "IconButton", name = "Pressed_Dark", width = 48, height = 48)
@Composable
fun IconButtonPressedDark() = Button(dark, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "IconButton", name = "Selected_Light", width = 48, height = 48)
@Composable
fun IconButtonSelectedLight() = Button(light, selected = true)

@ViddikScreenshot(group = "IconButton", name = "Selected_Dark", width = 48, height = 48)
@Composable
fun IconButtonSelectedDark() = Button(dark, selected = true)

@ViddikScreenshot(group = "IconButton", name = "Focused_Light", width = 48, height = 48)
@Composable
fun IconButtonFocusedLight() = Button(light, look = ButtonLook.Focused)

@ViddikScreenshot(group = "IconButton", name = "Focused_Dark", width = 48, height = 48)
@Composable
fun IconButtonFocusedDark() = Button(dark, look = ButtonLook.Focused)

@ViddikScreenshot(group = "IconButton", name = "Disabled_Light", width = 48, height = 48)
@Composable
fun IconButtonDisabledLight() = Button(light, enabled = false)

@ViddikScreenshot(group = "IconButton", name = "Disabled_Dark", width = 48, height = 48)
@Composable
fun IconButtonDisabledDark() = Button(dark, enabled = false)

@ViddikScreenshot(group = "Badge", name = "Number_Light", width = 48, height = 48)
@Composable
fun BadgeNumberLight() = Badged(light, 12)

@ViddikScreenshot(group = "Badge", name = "Number_Dark", width = 48, height = 48)
@Composable
fun BadgeNumberDark() = Badged(dark, 12)

@ViddikScreenshot(group = "Badge", name = "Dot_Light", width = 48, height = 48)
@Composable
fun BadgeDotLight() = Badged(light, null)

@ViddikScreenshot(group = "Badge", name = "Dot_Dark", width = 48, height = 48)
@Composable
fun BadgeDotDark() = Badged(dark, null)
