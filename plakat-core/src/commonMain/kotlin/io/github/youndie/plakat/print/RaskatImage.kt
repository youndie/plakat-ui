package io.github.youndie.plakat.print

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * The two-ink image of a raskat field: the box filled with [a], then every pixel [inkB] marks set to
 * [b] in one batch of 1 px square points with no antialiasing, which lands each exactly on its pixel.
 * Common Compose has no API to write pixels, and this needs no platform code.
 */
internal fun raskatImage(
    w: Int,
    h: Int,
    inkB: BooleanArray,
    a: Color,
    b: Color,
): ImageBitmap {
    val image = ImageBitmap(w, h)
    val canvas = Canvas(image)
    canvas.drawRect(
        0f,
        0f,
        w.toFloat(),
        h.toFloat(),
        Paint().apply {
            color = a
            isAntiAlias = false
        },
    )
    val points = FloatArray(inkB.count { it } * 2)
    var n = 0
    for (k in inkB.indices) {
        if (!inkB[k]) continue
        points[n++] = k % w + .5f
        points[n++] = k / w + .5f
    }
    canvas.drawRawPoints(
        PointMode.Points,
        points,
        Paint().apply {
            color = b
            strokeWidth = 1f
            strokeCap = StrokeCap.Square
            isAntiAlias = false
        },
    )
    return image
}

/**
 * A roll's image, kept between frames (B-51). Ink B only gains ground as a roll runs, so a frame writes only the pixels
 * new to it: writing all of them was the frame's whole cost, up to 38 ms at 352 px where the comparison takes 1.3. A step
 * that takes ink back starts the image over.
 */
internal class RaskatRollImage(
    private val w: Int,
    private val h: Int,
    private val a: Color,
    private val b: Color,
) {
    var image: ImageBitmap = raskatImage(w, h, BooleanArray(w * h), a, b)
        private set
    private var drawn = BooleanArray(w * h)

    fun advance(inkB: BooleanArray) {
        var fresh = 0
        for (k in inkB.indices) {
            if (drawn[k] && !inkB[k]) {
                image = raskatImage(w, h, inkB, a, b)
                drawn = inkB.copyOf()
                return
            }
            if (inkB[k] && !drawn[k]) fresh++
        }
        if (fresh == 0) return
        val points = FloatArray(fresh * 2)
        var n = 0
        for (k in inkB.indices) {
            if (inkB[k] && !drawn[k]) {
                points[n++] = k % w + .5f
                points[n++] = k / w + .5f
                drawn[k] = true
            }
        }
        Canvas(image).drawRawPoints(
            PointMode.Points,
            points,
            Paint().apply {
                color = b
                strokeWidth = 1f
                strokeCap = StrokeCap.Square
                isAntiAlias = false
            },
        )
    }
}

/**
 * [image] printed into [outline], its box's top-left at [topLeft] in pixels, unscaled and unsmoothed
 * — the JS's `drawRaskat` with `imageSmoothingEnabled = false`. Only the outline's edge is
 * antialiased, by the clip, as the JS's canvas clip is.
 */
internal fun DrawScope.drawRaskat(
    image: ImageBitmap,
    outline: Path,
    topLeft: IntOffset,
) {
    clipPath(outline) {
        drawImage(
            image,
            dstOffset = topLeft,
            dstSize = IntSize(image.width, image.height),
            filterQuality = FilterQuality.None,
        )
    }
}

/** The point raskat's box is printed from, rounded to a pixel so the image is never resampled. */
internal fun raskatTopLeft(topLeft: Offset): IntOffset =
    IntOffset(kotlin.math.floor(topLeft.x).toInt(), kotlin.math.floor(topLeft.y).toInt())
