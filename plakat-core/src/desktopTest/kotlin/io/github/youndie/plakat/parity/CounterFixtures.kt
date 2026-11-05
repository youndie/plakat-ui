package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatCounter
import io.github.youndie.plakat.components.PlakatCounterColumn
import io.github.youndie.plakat.components.PlakatPictogram
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-30: the MVP page's two tiles in their columns of 175, and the Stretch page's two in columns of 191.

private const val ISOSTAT_HIGH = 161
private const val COLUMN_HIGH = 159

@Composable
private fun Isostat(colors: PlakatColors) =
    PlakatTheme(colors = colors, reduceMotion = true) {
        Row(
            Modifier.fillMaxSize().background(PlakatTheme.colors.paper),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlakatCounter(
                47,
                "СТАТЕЙ ПРОЧИТАНО",
                Modifier.width(175.dp).fillMaxHeight(),
                legend = "= 5 статей",
                perPictogram = 5,
            )
            PlakatCounter(
                6,
                "ДНЕЙ ПОДРЯД",
                Modifier.width(175.dp).fillMaxHeight(),
                legend = "= день, красный — сегодня",
                pictogram = PlakatPictogram.Circle,
                accentLast = true,
            )
        }
    }

@ViddikScreenshot(group = "Counter", name = "Isostat_Light", width = 358, height = ISOSTAT_HIGH)
@Composable
fun CounterIsostatLight() = Isostat(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Counter", name = "Isostat_Dark", width = 358, height = ISOSTAT_HIGH)
@Composable
fun CounterIsostatDark() = Isostat(PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Counter", name = "Column", width = 390, height = COLUMN_HIGH)
@Composable
fun CounterColumn() =
    PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) {
        val colors = PlakatTheme.colors
        Row(
            Modifier.fillMaxSize().background(colors.paper),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlakatCounterColumn(47, "СТАТЕЙ ПРОЧИТАНО", Modifier.width(191.dp).fillMaxHeight())
            PlakatCounterColumn(6, "ДНЕЙ ПОДРЯД", Modifier.width(191.dp).fillMaxHeight(), colors.red)
        }
    }
