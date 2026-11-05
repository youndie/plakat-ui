package io.github.youndie.plakat.parity

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

// B-19: the Core page's photo stand-in, the posters' demo content, evaluated per pixel. The page has
// no photograph: a group on white with `filter: contrast(30)` holds a radial gradient (the "face")
// and, over it with `mix-blend-mode: plus-lighter`, a 4 px grid of radial dots turned 45°
// (`inset: -50%`, `radial-gradient(circle, #000 0, #808080 2.83px)`); the group is multiplied over
// the red circle or the concrete plate. Here each pixel centre is put through the same chain, in sRGB.

/** A radial gradient's stops: positions as fractions of its ray, grey levels 0..255. */
private class Stops(
    vararg val at: Pair<Double, Int>,
) {
    fun grey(t: Double): Double {
        if (t <= at.first().first) return at.first().second / 255.0
        for (i in 1 until at.size) {
            val (p1, g1) = at[i]
            val (p0, g0) = at[i - 1]
            if (t <= p1) return (g0 + (g1 - g0) * (t - p0) / (p1 - p0)) / 255.0
        }
        return at.last().second / 255.0
    }
}

/**
 * The stand-in for a [w] × [h] box: the gradient centred at ([cx], [cy]) of the box, a circle, or with
 * [ellipse] an ellipse, both `farthest-corner`.
 */
private fun standIn(
    w: Int,
    h: Int,
    cx: Double,
    cy: Double,
    ellipse: Boolean,
    stops: Stops,
): ImageBitmap {
    val image = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val ox = cx * w
    val oy = cy * h
    // farthest-corner: a circle through the farthest corner; an ellipse of the farthest sides' ratio, scaled by √2.
    val rx = if (ellipse) max(ox, w - ox) * sqrt(2.0) else hypot(max(ox, w - ox), max(oy, h - oy))
    val ry = if (ellipse) max(oy, h - oy) * sqrt(2.0) else rx
    val a = PI / 4
    val c = cos(a)
    val s = sin(a)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val px = x + .5
            val py = y + .5
            val g = stops.grey(hypot((px - ox) / rx, (py - oy) / ry))
            // The dot grid's own frame: turned back 45° about the box's centre, tiled from the -50% corner.
            val dx = px - w / 2.0
            val dy = py - h / 2.0
            val lx = c * dx + s * dy + w
            val ly = -s * dx + c * dy + h
            val u = lx - 4 * floor(lx / 4)
            val v = ly - 4 * floor(ly / 4)
            val dot = (hypot(u - 2, v - 2) / 2.83).coerceAtMost(1.0) * 128 / 255
            val lit = (g + dot).coerceAtMost(1.0)
            val out = ((lit - .5) * 30 + .5).coerceIn(0.0, 1.0)
            val q = (out * 255 + .5).toInt()
            image.setRGB(x, y, (0xFF shl 24) or (q shl 16) or (q shl 8) or q)
        }
    }
    return image.toComposeImageBitmap()
}

/** Fills the slot with the stand-in, made for the slot's size in pixels. */
@Composable
private fun BoxScope.StandIn(make: (Int, Int) -> ImageBitmap) =
    Spacer(
        Modifier.matchParentSize().drawWithCache {
            val image = make(size.width.toInt(), size.height.toInt())
            onDrawBehind { drawImage(image) }
        },
    )

/** Круг's face: `radial-gradient(circle at 55% 42%, #2a2a2a 0, #2a2a2a 18%, #8a8a8a 34%, #d8d8d8 52%, #f4f4f4 72%)`. */
@Composable
internal fun BoxScope.PageCirclePhoto() =
    StandIn { w, h ->
        standIn(w, h, .55, .42, ellipse = false, Stops(0.0 to 0x2a, .18 to 0x2a, .34 to 0x8a, .52 to 0xd8, .72 to 0xf4))
    }

/** Фон's face: `radial-gradient(ellipse at 62% 36%, #2a2a2a 0, #2a2a2a 14%, #8a8a8a 28%, #d8d8d8 44%, #f4f4f4 66%)`. */
@Composable
internal fun BoxScope.PageBackgroundPhoto() =
    StandIn { w, h ->
        standIn(w, h, .62, .36, ellipse = true, Stops(0.0 to 0x2a, .14 to 0x2a, .28 to 0x8a, .44 to 0xd8, .66 to 0xf4))
    }
