package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatNavigationDrawerFrame
import io.github.youndie.plakat.components.PlakatNavigationItem
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-47: designed here (D13), so no reference: goldens of its states, for the owner's review at the end.

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

private const val W = 390
private const val H = 320

private val items =
    listOf(
        PlakatNavigationItem(PlakatIcons.Home, "ЛЕНТА"),
        PlakatNavigationItem(PlakatIcons.Search, "ПОИСК"),
        PlakatNavigationItem(PlakatIcons.Share, "ОБМЕН"),
        PlakatNavigationItem(PlakatIcons.Settings, "ОПЦИИ"),
    )

@Composable
private fun Cell(
    colors: PlakatColors,
    selected: Int = 0,
    pressed: Int? = null,
    focused: Int? = null,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
        Box(
            Modifier
                .padding(16.dp)
                .background(PlakatTheme.colors.red)
                .height(120.dp)
                .fillMaxSize(),
        )
        PlakatNavigationDrawerFrame(
            true,
            items,
            selected,
            {},
            {},
            Modifier,
            null,
            playIn = false,
            forcePressed = pressed,
            forceFocused = focused,
        )
    }
}

@ViddikScreenshot(group = "NavigationDrawer", name = "Open_Light", width = W, height = H)
@Composable
fun NavigationDrawerOpenLight() = Cell(light)

@ViddikScreenshot(group = "NavigationDrawer", name = "Open_Dark", width = W, height = H)
@Composable
fun NavigationDrawerOpenDark() = Cell(dark)

@ViddikScreenshot(group = "NavigationDrawer", name = "Third_Light", width = W, height = H)
@Composable
fun NavigationDrawerThirdLight() = Cell(light, selected = 2)

@ViddikScreenshot(group = "NavigationDrawer", name = "Pressed_Light", width = W, height = H)
@Composable
fun NavigationDrawerPressedLight() = Cell(light, pressed = 1)

@ViddikScreenshot(group = "NavigationDrawer", name = "Pressed_Dark", width = W, height = H)
@Composable
fun NavigationDrawerPressedDark() = Cell(dark, pressed = 1)

@ViddikScreenshot(group = "NavigationDrawer", name = "Focused_Light", width = W, height = H)
@Composable
fun NavigationDrawerFocusedLight() = Cell(light, focused = 1)
