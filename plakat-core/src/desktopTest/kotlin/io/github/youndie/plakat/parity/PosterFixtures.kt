package io.github.youndie.plakat.parity

import androidx.compose.runtime.Composable
import io.github.youndie.plakat.components.PlakatPoster
import io.github.youndie.plakat.components.PlakatPosterLayout
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-19: the Core page's three posters at its seed 47 (Круг 47, Фон 48, Баннер 49: the page offsets
// each by one), printed, with the texture off as the references are taken; the photo is the page's
// stand-in. The raskat Круг has no reference (the page's canvas cell is laid out by hand): its golden
// is this library's own.

private val NOVEMBER = listOf("ЧИТАЙ", "ВЕСЬ", "НОЯБРЬ")

@Composable
private fun Cell(
    colors: PlakatColors,
    content: @Composable () -> Unit,
) = PlakatTheme(colors = colors, reduceMotion = true, print = PlakatPrintSwitches(texture = false), content = content)

@Composable
private fun Circle(
    colors: PlakatColors,
    raskat: Boolean = false,
) = Cell(colors) {
    PlakatPoster(
        NOVEMBER,
        "ПОДПИСАТЬСЯ",
        {},
        body = "12 новых выпусков. Мы собрали их в одну ленту — читай по порядку.",
        seed = 47,
        raskat = raskat,
        print = false,
        photo = if (raskat) null else ({ PageCirclePhoto() }),
    )
}

@Composable
private fun Background(colors: PlakatColors) =
    Cell(colors) {
        PlakatPoster(
            NOVEMBER,
            "ЧИТАТЬ",
            {},
            layout = PlakatPosterLayout.Background,
            body = "12 новых выпусков. Мы собрали их в одну ленту.",
            seed = 48,
            print = false,
            photo = { PageBackgroundPhoto() },
        )
    }

@Composable
private fun Banner(colors: PlakatColors) =
    Cell(colors) {
        PlakatPoster(
            listOf("НОВЫЙ", "ВЫПУСК"),
            "ЧИТАТЬ",
            {},
            layout = PlakatPosterLayout.Banner,
            seed = 49,
            print = false,
        )
    }

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Poster", name = "Circle_Light", width = 390, height = 392)
@Composable
fun PosterCircleLight() = Circle(light)

@ViddikScreenshot(group = "Poster", name = "Circle_Dark", width = 390, height = 392)
@Composable
fun PosterCircleDark() = Circle(dark)

@ViddikScreenshot(group = "Poster", name = "Background_Light", width = 390, height = 500)
@Composable
fun PosterBackgroundLight() = Background(light)

@ViddikScreenshot(group = "Poster", name = "Background_Dark", width = 390, height = 500)
@Composable
fun PosterBackgroundDark() = Background(dark)

@ViddikScreenshot(group = "Poster", name = "Banner_Light", width = 390, height = 160)
@Composable
fun PosterBannerLight() = Banner(light)

@ViddikScreenshot(group = "Poster", name = "Banner_Dark", width = 390, height = 160)
@Composable
fun PosterBannerDark() = Banner(dark)

@ViddikScreenshot(group = "Poster", name = "CircleRaskat_Light", width = 390, height = 392)
@Composable
fun PosterCircleRaskatLight() = Circle(light, raskat = true)

@ViddikScreenshot(group = "Poster", name = "CircleRaskat_Dark", width = 390, height = 392)
@Composable
fun PosterCircleRaskatDark() = Circle(dark, raskat = true)
