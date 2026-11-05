package io.github.youndie.plakat.parity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatNavigationBar
import io.github.youndie.plakat.components.PlakatNavigationItem
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-26: the MVP page's bar, 390 wide, its first item active.

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
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(390.dp, 64.dp)) { PlakatNavigationBar(items, selected, {}) }
}

@ViddikScreenshot(group = "NavigationBar", name = "Default_Light", width = 390, height = 64)
@Composable
fun NavigationBarDefaultLight() = Cell(PlakatPalettes.Paper)

@ViddikScreenshot(group = "NavigationBar", name = "Default_Dark", width = 390, height = 64)
@Composable
fun NavigationBarDefaultDark() = Cell(PlakatPalettes.BlackPaper)

// The other slots, which fall on half pixels: no reference, this library's own golden.
@ViddikScreenshot(group = "NavigationBar", name = "Second_Light", width = 390, height = 64)
@Composable
fun NavigationBarSecondLight() = Cell(PlakatPalettes.Paper, selected = 1)
