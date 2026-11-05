package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatTab
import io.github.youndie.plakat.components.PlakatTabs
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-27: the MVP page's tabs, 390 wide: 48 and the hairline, the first selected, the third disabled.

private val tabs = listOf(PlakatTab("ЛЕНТА"), PlakatTab("АРХИВ"), PlakatTab("ЧЕРНОВИКИ", enabled = false))

@Composable
private fun Cell(
    colors: PlakatColors,
    items: List<PlakatTab> = tabs,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(390.dp, 49.dp).background(PlakatTheme.colors.paper)) { PlakatTabs(items, 0, {}) }
}

@ViddikScreenshot(group = "Tabs", name = "Default_Light", width = 390, height = 49)
@Composable
fun TabsDefaultLight() = Cell(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Tabs", name = "Default_Dark", width = 390, height = 49)
@Composable
fun TabsDefaultDark() = Cell(PlakatPalettes.BlackPaper)

// Words whose real width and ceiled width centre apart, four in half-pixel slots: this library's own golden,
// to hold the centring the page's three words happen not to.
@ViddikScreenshot(group = "Tabs", name = "FourWords_Light", width = 390, height = 49)
@Composable
fun TabsFourWordsLight() = Cell(PlakatPalettes.Paper, listOf("ЛЕНТА", "ПОИСК", "ОБМЕН", "ОПЦИИ").map { PlakatTab(it) })
