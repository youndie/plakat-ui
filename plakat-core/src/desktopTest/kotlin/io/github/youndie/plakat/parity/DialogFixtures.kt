package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatDialogAction
import io.github.youndie.plakat.components.PlakatDialogFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-28: the MVP page's stage, 390 × 420: four concrete rows of 72 under the screen, and the open dialog.

@Composable
private fun Cell(colors: PlakatColors) =
    PlakatTheme(colors = colors, reduceMotion = true) {
        Box(Modifier.size(390.dp, 420.dp).background(PlakatTheme.colors.paper)) {
            Column(
                Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(4) { Spacer(Modifier.fillMaxWidth().height(72.dp).background(PlakatTheme.colors.concrete)) }
            }
            PlakatDialogFrame(
                true,
                "УДАЛИТЬ СТАТЬЮ?",
                {},
                Modifier,
                "«Город из фанеры» уйдёт из закладок и с устройства. Вернуть можно в течение суток.",
                {
                    PlakatDialogAction("ОТМЕНА", {})
                    PlakatDialogAction("УДАЛИТЬ", {}, danger = true)
                },
                playIn = false,
            )
        }
    }

@ViddikScreenshot(group = "Dialog", name = "Danger_Light", width = 390, height = 420)
@Composable
fun DialogDangerLight() = Cell(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Dialog", name = "Danger_Dark", width = 390, height = 420)
@Composable
fun DialogDangerDark() = Cell(PlakatPalettes.BlackPaper)
