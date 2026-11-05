package io.github.youndie.plakat.parity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.agit.Mulberry32
import io.github.youndie.plakat.agit.PosterRole
import io.github.youndie.plakat.agit.PosterShape
import io.github.youndie.plakat.agit.drawPosterShape
import io.github.youndie.plakat.components.PlakatWindow
import io.github.youndie.plakat.components.PlakatWindowPanel
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

// B-29: the MVP page's onboarding, 2 panels of the window in its two columns of 358. The pictures are demo content:
// the MVP page's `comp`, a third generator of its own, ported here for these fixtures only, with its seeds of
// 300 + i × 17 in a frame of 171 × 160.

private fun pageComposition(seed: Int): List<PosterShape> {
    val w0 = 171.0
    val h0 = 160.0
    val r = Mulberry32(seed)

    fun f() = r.next()

    fun <T> pick(xs: List<T>) = xs[floor(f() * xs.size).toInt()]

    fun s8(v: Double) = max(8.0, floor(v / 8 + .5) * 8)
    val t = tan(17 * PI / 180)
    val redSlot = pick(listOf("circle", "wedge", "bar"))

    fun role(key: String) = if (key == redSlot) PosterRole.Red else PosterRole.Ink
    val out = mutableListOf<PosterShape>()
    run {
        val w = s8(w0 * (.35 + f() * .3))
        val h = s8(h0 * (.3 + f() * .35))
        val x = pick(listOf(0.0, w0 - w))
        out += PosterShape.Box(x, s8(f() * h0 * .4), w, h, PosterRole.Gray)
    }
    run {
        val d = s8(min(w0, h0) * (.45 + f() * .3))
        val x = s8(pick(listOf(w0 - d * .7, w0 * .5 - d * .5, 8.0)))
        out += PosterShape.Round(x, s8(h0 * (.05 + f() * .3)), d, role("circle"))
    }
    run {
        val bt = s8(pick(listOf(8.0, 16.0, 24.0)))
        val bl = s8(w0 * (.5 + f() * .5))
        val rot = pick(listOf(-17.0, -17.0, 0.0, 90.0))
        val x = s8(w0 * (.05 + f() * .4))
        out += PosterShape.Box(x, s8(h0 * (.35 + f() * .5)), bl, bt, role("bar"), rot)
    }
    run {
        val w = s8(w0 * (.25 + f() * .3))
        val h = floor(w * t + .5)
        val x = s8(f() * max(8.0, w0 - w))
        val y = s8(h0 * (.1 + f() * .8))
        out += PosterShape.Wedge(x, y, w, h, role("wedge"), pick(listOf(true, false)))
    }
    if (f() > .4) {
        val d = s8(pick(listOf(16.0, 24.0)))
        val x = s8(f() * (w0 - d))
        out += PosterShape.Round(x, s8(f() * (h0 - d)), d, PosterRole.Ink)
    }
    return out
}

@Composable
private fun BoxScope.Picture(seed: Int) {
    val colors = PlakatTheme.colors
    val shapes = pageComposition(seed)
    Canvas(Modifier.fillMaxSize()) {
        for (s in shapes) {
            val c =
                when (s.role) {
                    PosterRole.Red -> colors.red
                    PosterRole.Ink -> colors.ink
                    PosterRole.Gray -> colors.paper
                }
            drawPosterShape(s, c)
        }
    }
}

private val panels =
    listOf(
        PlakatWindowPanel("ЧИТАЙ ПО ПОРЯДКУ", "Каждую неделю — выпуск из 12 статей.") { Picture(300) },
        PlakatWindowPanel("СОХРАНЯЙ В ЛИСТ", "Закладки открываются без сети.") { Picture(317) },
    )

private const val HEIGHT = 247

@Composable
private fun Cell(colors: PlakatColors) =
    PlakatTheme(colors = colors, reduceMotion = true) {
        Box(Modifier.size(358.dp, HEIGHT.dp).background(PlakatTheme.colors.paper)) {
            PlakatWindow(panels, 0, Modifier.wrapContentHeight(Alignment.Top, unbounded = true), panelsInView = 2)
        }
    }

@ViddikScreenshot(group = "Window", name = "Onboarding_Light", width = 358, height = HEIGHT)
@Composable
fun WindowOnboardingLight() = Cell(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Window", name = "Onboarding_Dark", width = 358, height = HEIGHT)
@Composable
fun WindowOnboardingDark() = Cell(PlakatPalettes.BlackPaper)
