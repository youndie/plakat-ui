package io.github.youndie.plakat.parity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.agit.Mulberry32
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

// B-17: the Core page's own thumbnail generator, `comp(seed, 64, 64, pal, [], { dot: .4 })` in the
// page's script — not plakat-gen.js — ported for the Card cells' demo content only. The page seeds
// it with 47 · 7 + i · 131 for its three cards.

private sealed class PageShape(
    val color: Color,
) {
    class Box(
        val x: Double,
        val y: Double,
        val w: Double,
        val h: Double,
        color: Color,
        val rotate: Double = 0.0,
    ) : PageShape(color)

    class Round(
        val x: Double,
        val y: Double,
        val d: Double,
        color: Color,
    ) : PageShape(color)

    class Wedge(
        val x: Double,
        val y: Double,
        val w: Double,
        val h: Double,
        color: Color,
        val lowerRight: Boolean,
    ) : PageShape(color)
}

private fun pageComp(
    seed: Int,
    red: Color,
    ink: Color,
    gray: Color,
    size: Double = 64.0,
    dot: Double = .4,
): List<PageShape> {
    val r = Mulberry32(seed)

    fun <T> pick(xs: List<T>) = xs[floor(r.next() * xs.size).toInt()]

    fun s8(v: Double) = max(8.0, floor(v / 8 + .5) * 8)
    val w0 = size
    val h0 = size
    val t = tan(17 * PI / 180)
    val redSlot = pick(listOf("circle", "wedge", "bar"))

    fun c(key: String) = if (key == redSlot) red else ink
    val out = mutableListOf<PageShape>()
    run {
        val w = s8(w0 * (.35 + r.next() * .3))
        val h = s8(h0 * (.25 + r.next() * .35))
        val x = pick(listOf(0.0, w0 - w))
        out += PageShape.Box(x, s8(r.next() * h0 * .4), w, h, gray)
    }
    run {
        val d = s8(min(w0, h0) * (.4 + r.next() * .3))
        val x = s8(pick(listOf(w0 - d * .6, w0 - d * .9, w0 * .5 - d * .2)))
        out += PageShape.Round(x, s8(h0 * (.03 + r.next() * .25)), d, c("circle"))
    }
    run {
        val bt = s8(pick(listOf(8.0, 16.0, 24.0)))
        val bl = s8(w0 * (.5 + r.next() * .5))
        val rot = pick(listOf(-17.0, -17.0, -17.0, 0.0, 90.0))
        val x = s8(w0 * (.05 + r.next() * .5))
        val y = s8(h0 * (.3 + r.next() * .5))
        out += PageShape.Box(x, y, bl, bt, c("bar"), rot)
    }
    run {
        val w = s8(w0 * (.2 + r.next() * .3))
        val h = floor(w * t + .5)
        val x = s8(r.next() * max(8.0, w0 - w))
        val y = s8(h0 * (.05 + r.next() * .85))
        out += PageShape.Wedge(x, y, w, h, c("wedge"), pick(listOf(true, false)))
    }
    if (r.next() > .35) {
        val d = s8(pick(listOf(16.0, 24.0, 32.0, 48.0)) * dot)
        val x = s8(r.next() * (w0 - d))
        out += PageShape.Round(x, s8(r.next() * (h0 - d)), d, ink)
    }
    return out
}

/** The page's 64 × 64 thumbnail for card [index]: paper, and the page's shapes in their order. */
@Composable
internal fun PageThumbnail(index: Int) {
    val colors = PlakatTheme.colors
    val shapes = pageComp(47 * 7 + index * 131, colors.red, colors.ink, colors.concrete)
    Box(Modifier.size(64.dp).clipToBounds().background(colors.paper)) {
        Canvas(Modifier.size(64.dp)) {
            for (s in shapes) {
                when (s) {
                    is PageShape.Box -> {
                        // `transform-origin: 0 50%`: the bar turns about the middle of its left end.
                        rotate(s.rotate.toFloat(), Offset(s.x.toFloat(), (s.y + s.h / 2).toFloat())) {
                            drawRect(s.color, Offset(s.x.toFloat(), s.y.toFloat()), Size(s.w.toFloat(), s.h.toFloat()))
                        }
                    }

                    is PageShape.Round -> {
                        val rad = (s.d / 2).toFloat()
                        drawCircle(s.color, rad, Offset(s.x.toFloat() + rad, s.y.toFloat() + rad))
                    }

                    is PageShape.Wedge -> {
                        val (x, y, w, h) = listOf(s.x, s.y, s.w, s.h).map { it.toFloat() }
                        val path =
                            Path().apply {
                                if (s.lowerRight) {
                                    // polygon(0 100%, 100% 0, 100% 100%)
                                    moveTo(x, y + h)
                                    lineTo(x + w, y)
                                    lineTo(x + w, y + h)
                                } else {
                                    // polygon(0 0, 100% 0, 0 100%)
                                    moveTo(x, y)
                                    lineTo(x + w, y)
                                    lineTo(x, y + h)
                                }
                                close()
                            }
                        drawPath(path, s.color)
                    }
                }
            }
        }
    }
}
