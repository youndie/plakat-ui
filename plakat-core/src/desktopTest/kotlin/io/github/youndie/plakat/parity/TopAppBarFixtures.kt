package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatIconButton
import io.github.youndie.plakat.components.PlakatTopAppBar
import io.github.youndie.plakat.components.PlakatTopAppBarState
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.icons.PlakatStencil
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-25: the MVP page's two bars, 390 wide, its strip at the page's seed of 240; the bar half folded has
// no reference (the page draws the ends only): its golden is this library's own.

private const val RANGE = 69f

@Composable
private fun Bar(
    colors: PlakatColors,
    height: Int,
    fold: Float,
    nav: PlakatStencil,
    action: PlakatStencil,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    val state = remember { PlakatTopAppBarState().also { it.foldTo(fold * RANGE, RANGE) } }
    Box(Modifier.size(390.dp, height.dp).background(PlakatTheme.colors.paper)) {
        PlakatTopAppBar(
            "ЛИСТ",
            subtitle = "№ 47 · НОЯБРЬ",
            collapsedTitle = "ЛИСТ · № 47",
            seed = 240,
            state = state,
            navigationIcon = { PlakatIconButton(nav, null, {}) },
            actions = { PlakatIconButton(action, null, {}) },
        )
    }
}

@Composable
private fun Expanded(colors: PlakatColors) = Bar(colors, 128, 0f, PlakatIcons.Menu, PlakatIcons.Search)

@Composable
private fun Collapsed(colors: PlakatColors) = Bar(colors, 59, 1f, PlakatIcons.Back, PlakatIcons.Share)

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "TopAppBar", name = "Expanded_Light", width = 390, height = 128)
@Composable
fun TopAppBarExpandedLight() = Expanded(light)

@ViddikScreenshot(group = "TopAppBar", name = "Expanded_Dark", width = 390, height = 128)
@Composable
fun TopAppBarExpandedDark() = Expanded(dark)

@ViddikScreenshot(group = "TopAppBar", name = "Collapsed_Light", width = 390, height = 59)
@Composable
fun TopAppBarCollapsedLight() = Collapsed(light)

@ViddikScreenshot(group = "TopAppBar", name = "Collapsed_Dark", width = 390, height = 59)
@Composable
fun TopAppBarCollapsedDark() = Collapsed(dark)

@ViddikScreenshot(group = "TopAppBar", name = "Halfway_Light", width = 390, height = 128)
@Composable
fun TopAppBarHalfwayLight() = Bar(light, 128, .5f, PlakatIcons.Menu, PlakatIcons.Search)

@ViddikScreenshot(group = "TopAppBar", name = "Halfway_Dark", width = 390, height = 128)
@Composable
fun TopAppBarHalfwayDark() = Bar(dark, 128, .5f, PlakatIcons.Menu, PlakatIcons.Search)
