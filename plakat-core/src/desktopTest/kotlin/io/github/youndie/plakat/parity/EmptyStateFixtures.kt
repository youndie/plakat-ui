package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.youndie.plakat.components.PlakatEmptyState
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-52: the MVP page's «Окно / Пустое состояние», in its 358.

@Composable
private fun Cell(colors: PlakatColors) =
    PlakatTheme(colors = colors, reduceMotion = true) {
        Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
            PlakatEmptyState(
                "СОХРАНИТЕ ПЕРВУЮ СТАТЬЮ",
                "Закладки появятся здесь и откроются без сети.",
                "К ЛЕНТЕ",
                {},
                Modifier.fillMaxWidth().wrapContentHeight(Alignment.Top, unbounded = true),
            )
        }
    }

@ViddikScreenshot(group = "EmptyState", name = "Window_Light", width = 358, height = 285)
@Composable
fun EmptyStateLight() = Cell(PlakatPalettes.Paper)

@ViddikScreenshot(group = "EmptyState", name = "Window_Dark", width = 358, height = 285)
@Composable
fun EmptyStateDark() = Cell(PlakatPalettes.BlackPaper)
