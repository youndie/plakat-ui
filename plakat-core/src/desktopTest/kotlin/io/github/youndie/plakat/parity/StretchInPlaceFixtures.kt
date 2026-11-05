package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import io.github.youndie.plakat.components.PlakatMasthead
import io.github.youndie.plakat.components.PlakatPoster
import io.github.youndie.plakat.components.PlakatPosterLayout
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-53: the Stretch page's two cells in place. The page frames each in `box-shadow: inset 0 0 0 1px`, which is its own and
// not the component's; it is drawn over them here, where the page's lies under what covers it (the circle's edge).

@Composable
private fun Framed(content: @Composable () -> Unit) =
    PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true, print = PlakatPrintSwitches(texture = false)) {
        val ink = PlakatTheme.colors.ink
        Box(
            Modifier.fillMaxSize().background(PlakatTheme.colors.paper).drawWithContent {
                drawContent()
                drawRect(ink, Offset(.5f, .5f), Size(size.width - 1f, size.height - 1f), style = Stroke(1f))
            },
        ) { content() }
    }

@ViddikScreenshot(group = "Poster", name = "Stretch_Light", width = 390, height = 600)
@Composable
fun PosterStretchLight() =
    Framed {
        PlakatPoster(
            listOf("ЧИТАЙ", "ВЕСЬ", "НОЯБРЬ"),
            "ПОДПИСАТЬСЯ",
            {},
            layout = PlakatPosterLayout.Stretch,
            body = "12 новых выпусков. Мы собрали их в одну ленту — читай по порядку.",
        )
    }

@ViddikScreenshot(group = "TopAppBar", name = "Masthead_Light", width = 390, height = 195)
@Composable
fun MastheadLight() =
    Framed {
        PlakatMasthead(
            "ЛИСТ",
            Modifier.wrapContentHeight(Alignment.Top, unbounded = true),
            startLabel = "№ 47",
            endLabel = "НОЯБРЬ 2026",
        )
    }
