package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatBottomSheetFrame
import io.github.youndie.plakat.components.PlakatListItem
import io.github.youndie.plakat.components.PlakatSheetGround
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-46: designed here (D13), so no reference: goldens of its states, for the owner's review at the end. What stands behind it
// is a block of ink and a line, so that the screen has something to close.

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

private const val W = 390
private const val H = 500

@Composable
private fun Cell(
    colors: PlakatColors,
    ground: PlakatSheetGround = PlakatSheetGround.Paper,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
        Box(
            Modifier
                .padding(16.dp)
                .background(PlakatTheme.colors.red)
                .height(120.dp)
                .fillMaxSize(),
        )
        BasicText(
            "Павильон стоит три месяца.",
            Modifier.padding(start = 16.dp, top = 160.dp),
            style =
                PlakatTheme.typography.bodyM.style
                    .copy(color = PlakatTheme.colors.ink),
        )
        PlakatBottomSheetFrame(
            true,
            {},
            Modifier,
            ground,
            null,
            {
                PlakatListItem("Сохранить", onClick = {})
                PlakatListItem("Поделиться", onClick = {})
                PlakatListItem("В архив", onClick = {}, divider = false)
            },
            playIn = false,
        )
    }
}

@ViddikScreenshot(group = "BottomSheet", name = "Paper_Light", width = W, height = H)
@Composable
fun BottomSheetPaperLight() = Cell(light)

@ViddikScreenshot(group = "BottomSheet", name = "Paper_Dark", width = W, height = H)
@Composable
fun BottomSheetPaperDark() = Cell(dark)

@ViddikScreenshot(group = "BottomSheet", name = "Concrete_Light", width = W, height = H)
@Composable
fun BottomSheetConcreteLight() = Cell(light, PlakatSheetGround.Concrete)

@ViddikScreenshot(group = "BottomSheet", name = "Concrete_Dark", width = W, height = H)
@Composable
fun BottomSheetConcreteDark() = Cell(dark, PlakatSheetGround.Concrete)
