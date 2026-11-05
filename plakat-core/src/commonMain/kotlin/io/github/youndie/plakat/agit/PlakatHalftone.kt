package io.github.youndie.plakat.agit

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.raskatImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** How a source is printed. */
public enum class PlakatHalftoneMode {
    /** An AM screen: a dot of ink at each crossing, its radius by how dark the source is there. */
    Dots,

    /** One bit: ink where the source is darker than half. The cheapest, for a list's preview or a weak device. */
    Mask,
}

/** Below this a dot does not print: `if (r > .35)`, in pixels. */
private const val DOT_MIN_RADIUS = .35
private const val MASK_THRESHOLD = .5

/**
 * Luminance after contrast, per pixel of [argb]: the design's `L`, the Rec. 601 weights, stretched about the middle
 * by [contrast] and held in 0…1.
 */
internal fun halftoneLuminance(
    argb: IntArray,
    contrast: Double,
): DoubleArray =
    DoubleArray(argb.size) { i ->
        val c = argb[i]
        val l = (.299 * (c shr 16 and 255) + .587 * (c shr 8 and 255) + .114 * (c and 255)) / 255
        ((l - .5) * contrast + .5).coerceIn(0.0, 1.0)
    }

/**
 * The design's `halftone` in mode `dots`: a screen of [pitch] turned by [angle] degrees about the frame's middle, each
 * dot's radius `pitch × 0.71 × √(1 − L)` from the five pixels at and around it. As `x, y, r` per dot, in its order.
 */
internal fun halftoneDots(
    lum: DoubleArray,
    w: Int,
    h: Int,
    pitch: Double,
    angle: Double,
): DoubleArray {
    fun at(
        x: Double,
        y: Double,
    ) = lum[y.toInt().coerceIn(0, h - 1) * w + x.toInt().coerceIn(0, w - 1)]
    val a = angle * PI / 180
    val ca = cos(a)
    val sa = sin(a)
    val d = hypot(w.toDouble(), h.toDouble())
    val rMax = pitch * sqrt(.5)
    val out = ArrayList<Double>()
    var v = -d
    while (v < d) {
        var u = -d
        while (u < d) {
            val x = w / 2.0 + u * ca - v * sa
            val y = h / 2.0 + u * sa + v * ca
            if (x >= -pitch && y >= -pitch && x <= w + pitch && y <= h + pitch) {
                val l = (at(x, y) + at(x - 1, y) + at(x + 1, y) + at(x, y - 1) + at(x, y + 1)) / 5
                val r = rMax * sqrt(1 - l)
                if (r > DOT_MIN_RADIUS) {
                    out += x
                    out += y
                    out += r
                }
            }
            u += pitch
        }
        v += pitch
    }
    return out.toDoubleArray()
}

/** The design's `halftone` in mode `mask`: true where a pixel prints. */
internal fun halftoneMask(
    lum: DoubleArray,
    threshold: Double = MASK_THRESHOLD,
): BooleanArray = BooleanArray(lum.size) { lum[it] < threshold }

/** [source] covering [w] × [h] from its middle, as the design's `coverDraw` lays it, in ARGB per pixel. */
internal fun coverPixels(
    source: ImageBitmap,
    w: Int,
    h: Int,
): IntArray {
    val image = ImageBitmap(w, h)
    val s = max(w.toDouble() / source.width, h.toDouble() / source.height)
    val cropW = min(source.width, (w / s).roundToInt().coerceAtLeast(1))
    val cropH = min(source.height, (h / s).roundToInt().coerceAtLeast(1))
    Canvas(image).drawImageRect(
        source,
        IntOffset((source.width - cropW) / 2, (source.height - cropH) / 2),
        IntSize(cropW, cropH),
        IntOffset.Zero,
        IntSize(w, h),
        Paint().apply { filterQuality = FilterQuality.Low },
    )
    val out = IntArray(w * h)
    image.readPixels(out)
    return out
}

/**
 * Prints [source] in one [ink] on [paper] across this box, by the photomontage's rule: decoloured, contrast ×[contrast],
 * then a screen ([PlakatHalftoneMode.Dots]) or a threshold. A photograph is never shown raw. It is worked out once per
 * source and size, on the CPU, and kept; the frame (a circle, a cut, the axis) is the caller's, by a clip or a rotation.
 * The kit ships the processor and no photographs: a source is CC0, a close-up or a figure.
 */
public fun Modifier.plakatHalftone(
    source: ImageBitmap,
    ink: Color,
    paper: Color,
    mode: PlakatHalftoneMode = PlakatHalftoneMode.Dots,
    pitch: Dp = 4.dp,
    angle: Float = 45f,
    contrast: Float = 1.4f,
): Modifier = this then HalftoneElement(source, ink, paper, mode, pitch, angle, contrast)

private data class HalftoneElement(
    val source: ImageBitmap,
    val ink: Color,
    val paper: Color,
    val mode: PlakatHalftoneMode,
    val pitch: Dp,
    val angle: Float,
    val contrast: Float,
) : ModifierNodeElement<HalftoneNode>() {
    override fun create() = HalftoneNode(this)

    override fun update(node: HalftoneNode) {
        node.params = this
        node.forget()
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "plakatHalftone"
    }
}

private class HalftoneNode(
    var params: HalftoneElement,
) : Modifier.Node(),
    DrawModifierNode {
    private var workedOut = IntSize.Zero
    private var dots: Path? = null
    private var mask: ImageBitmap? = null

    fun forget() {
        workedOut = IntSize.Zero
        dots = null
        mask = null
    }

    override fun ContentDrawScope.draw() {
        val w = size.width.toInt()
        val h = size.height.toInt()
        if (w <= 0 || h <= 0) return
        val p = params
        if (workedOut != IntSize(w, h)) {
            forget()
            workedOut = IntSize(w, h)
            val lum = halftoneLuminance(coverPixels(p.source, w, h), p.contrast.toDouble())
            if (p.mode == PlakatHalftoneMode.Mask) {
                mask = raskatImage(w, h, halftoneMask(lum), p.paper, p.ink)
            } else {
                val drawn = halftoneDots(lum, w, h, p.pitch.toPx().toDouble(), p.angle.toDouble())
                dots =
                    Path().apply {
                        for (i in drawn.indices step 3) {
                            val x = drawn[i].toFloat()
                            val y = drawn[i + 1].toFloat()
                            val r = drawn[i + 2].toFloat()
                            addOval(Rect(x - r, y - r, x + r, y + r))
                        }
                    }
            }
        }
        mask?.let { drawImage(it) }
        dots?.let {
            drawRect(p.paper)
            drawPath(it, p.ink)
        }
    }
}
