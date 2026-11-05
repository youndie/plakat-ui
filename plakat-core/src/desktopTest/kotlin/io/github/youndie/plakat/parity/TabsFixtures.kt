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
    width: Int = 390,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(width.dp, 49.dp).background(PlakatTheme.colors.paper)) { PlakatTabs(tabs, 0, {}) }
}

@ViddikScreenshot(group = "Tabs", name = "Default_Light", width = 390, height = 49)
@Composable
fun TabsDefaultLight() = Cell(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Tabs", name = "Default_Dark", width = 390, height = 49)
@Composable
fun TabsDefaultDark() = Cell(PlakatPalettes.BlackPaper)

// A window of 391: slots of 130.33, whose first centre, 65.17, puts «ЛЕНТА» at 43 by its real width and 42
// by its ceiled one. A word's centring only shows on such a fraction, which the page's 390 lacks: this
// library's own golden holds it.
@ViddikScreenshot(group = "Tabs", name = "OddWidth_Light", width = 391, height = 49)
@Composable
fun TabsOddWidthLight() = Cell(PlakatPalettes.Paper, 391)
