package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.youndie.plakat.components.PlakatListItem
import io.github.youndie.plakat.components.PlakatPullToRefreshFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-50: designed here (D13), so no reference: goldens of its states, for the owner's review at the end.

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

private const val W = 390
private const val H = 240

@Composable
private fun Cell(
    colors: PlakatColors,
    pull: Float = 0f,
    refreshing: Boolean = false,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    PlakatPullToRefreshFrame(
        refreshing,
        {},
        Modifier.fillMaxSize().background(PlakatTheme.colors.paper),
        true,
        {
            Column {
                PlakatListItem("Павильон стоит три месяца")
                PlakatListItem("Потом его разбирают")
                PlakatListItem("Двенадцать выпусков", divider = false)
            }
        },
        forcePull = pull,
    )
}

@ViddikScreenshot(group = "PullToRefresh", name = "Short_Light", width = W, height = H)
@Composable
fun PullToRefreshShortLight() = Cell(light, 40f)

@ViddikScreenshot(group = "PullToRefresh", name = "Counts_Light", width = W, height = H)
@Composable
fun PullToRefreshCountsLight() = Cell(light, 80f)

@ViddikScreenshot(group = "PullToRefresh", name = "Counts_Dark", width = W, height = H)
@Composable
fun PullToRefreshCountsDark() = Cell(dark, 80f)

@ViddikScreenshot(group = "PullToRefresh", name = "Most_Light", width = W, height = H)
@Composable
fun PullToRefreshMostLight() = Cell(light, 120f)

@ViddikScreenshot(group = "PullToRefresh", name = "Refreshing_Light", width = W, height = H)
@Composable
fun PullToRefreshRefreshingLight() = Cell(light, refreshing = true)

@ViddikScreenshot(group = "PullToRefresh", name = "Refreshing_Dark", width = W, height = H)
@Composable
fun PullToRefreshRefreshingDark() = Cell(dark, refreshing = true)
