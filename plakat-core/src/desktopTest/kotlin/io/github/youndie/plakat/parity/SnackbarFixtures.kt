package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatSnackbarFrame
import io.github.youndie.plakat.theme.PlakatStrings
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-18: the Core page's Snackbar cells, 344 px wide in the 752 px theme card, shown at rest.

@Composable
private fun Cell(
    colors: PlakatColors,
    message: String,
    action: String? = null,
    error: Boolean = false,
) = PlakatTheme(colors = colors, reduceMotion = true, strings = PlakatStrings(error = "Ошибка")) {
    Box(Modifier.size(344.dp, 56.dp).background(PlakatTheme.colors.paper)) {
        PlakatSnackbarFrame(message, Modifier.fillMaxWidth(), true, action, {}, error, {}, playIn = false)
    }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Snackbar", name = "Default_Light", width = 344, height = 56)
@Composable
fun SnackbarDefaultLight() = Cell(light, "Статья сохранена в закладки")

@ViddikScreenshot(group = "Snackbar", name = "Default_Dark", width = 344, height = 56)
@Composable
fun SnackbarDefaultDark() = Cell(dark, "Статья сохранена в закладки")

@ViddikScreenshot(group = "Snackbar", name = "Action_Light", width = 344, height = 56)
@Composable
fun SnackbarActionLight() = Cell(light, "Статья удалена", "ВЕРНУТЬ")

@ViddikScreenshot(group = "Snackbar", name = "Action_Dark", width = 344, height = 56)
@Composable
fun SnackbarActionDark() = Cell(dark, "Статья удалена", "ВЕРНУТЬ")

@ViddikScreenshot(group = "Snackbar", name = "Error_Light", width = 344, height = 56)
@Composable
fun SnackbarErrorLight() = Cell(light, "Нет сети — повторим сами.", error = true)

@ViddikScreenshot(group = "Snackbar", name = "Error_Dark", width = 344, height = 56)
@Composable
fun SnackbarErrorDark() = Cell(dark, "Нет сети — повторим сами.", error = true)
