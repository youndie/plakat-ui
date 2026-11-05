package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.theme.PlakatDisplayInk
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/** The page's measuring size, `font-size: 100px`; the line is laid out once at it and scaled to the box. */
private const val MEASURE_PX = 100

/** The stretch allowed, vertical scale over horizontal: under 1.5 it is weak, over 4 the letters tear (rule 02). */
private const val STRETCH_MIN = 1.5f
private const val STRETCH_MAX = 4f

/** A line's ink, as fractions of the em: from its [left] to its [right], and how far its [top] stands over the caps. */
internal class StretchInk(
    val left: Float,
    val right: Float,
    val over: Float,
)

private val rows: IntArray by lazy {
    val cells = PlakatDisplayInk.ROWS.split(';', ',').filter { it.isNotEmpty() }
    IntArray(cells.size) { i -> cells[i].toInt(if (i % 5 == 0) 16 else 10) }
}

/** The row of [code], its index in [rows], or -1 when the font does not have it. */
private fun rowOf(code: Int): Int {
    var low = 0
    var high = rows.size / 5 - 1
    while (low <= high) {
        val mid = (low + high) / 2
        val at = rows[mid * 5]
        when {
            at < code -> low = mid + 1
            at > code -> high = mid - 1
            else -> return mid * 5
        }
    }
    return -1
}

/**
 * The ink of one line of Rubik Mono One, as the page's canvas measures it, or null if it has none. A character the
 * font does not have is taken as a full cell standing to the caps, whatever the host's face draws for it.
 */
internal fun stretchInk(text: String): StretchInk? {
    var left = Int.MAX_VALUE
    var right = Int.MIN_VALUE
    var top = PlakatDisplayInk.CAP_HEIGHT
    var pen = 0
    for (c in text) {
        val row = rowOf(c.code)
        val advances = row < 0 || rows[row + 1] == 1
        val inkLeft = if (row < 0) 0 else rows[row + 2]
        val inkRight = if (row < 0) PlakatDisplayInk.ADVANCE else rows[row + 3]
        val inkTop = if (row < 0) PlakatDisplayInk.CAP_HEIGHT else rows[row + 4]
        if (inkLeft != inkRight) {
            left = min(left, pen + inkLeft)
            right = max(right, pen + inkRight)
            top = max(top, inkTop)
        }
        if (advances) pen += PlakatDisplayInk.ADVANCE
    }
    if (left == Int.MAX_VALUE) return null
    return StretchInk(left / 1000f, right / 1000f, (top - PlakatDisplayInk.CAP_HEIGHT) / 1000f)
}

private val StretchInk.height get() = PlakatDisplayInk.CAP_HEIGHT / 1000f + over

/** The stretch, sy / sx, of [ink] in a box [width] × [height] in any one unit. */
private fun stretchOf(
    ink: StretchInk,
    width: Float,
    height: Float,
) = (height / ink.height) / (width / (ink.right - ink.left))

/**
 * The part of a box of [width] × [height] that [ink] fills, the stretch held within its limits (rule 02). Under 1.5 the
 * line keeps the height and comes short of the width; over 4 it keeps the width and comes short of the height.
 */
internal fun stretchFit(
    ink: StretchInk,
    width: Float,
    height: Float,
): Size {
    val stretch = stretchOf(ink, width, height)
    return when {
        stretch < STRETCH_MIN -> Size(width * stretch / STRETCH_MIN, height)
        stretch > STRETCH_MAX -> Size(width, height * STRETCH_MAX / stretch)
        else -> Size(width, height)
    }
}

/**
 * What [PlakatStretch] would stretch [text] by in a box of [width] × [height]: vertical scale over horizontal, which
 * the design wants within 1.5 to 4, before it is held there. For a box or a text not known in advance, to see whether it
 * will fill its box; 0 for a text with no ink.
 */
public fun plakatStretchFactor(
    text: String,
    width: Dp,
    height: Dp,
): Float = stretchInk(text.uppercase())?.let { stretchOf(it, width.value, height.value) } ?: 0f

/**
 * One line of Rubik Mono One in capitals, filling the box its [modifier] gives it: the width by fitting, the height by
 * stretching the letters, the top of the caps at the box's top and the baseline at its bottom. A Й or a Ё lowers the
 * line, its mark staying in the box; a descender hangs below it. Static typography: it appears by a wipe, and the box
 * is never animated. One to a screen.
 *
 * The design forbids a stretch outside 1.5 to 4 (a flattened line, torn letters), and a box and a text met at run time can
 * ask for one, so it is held there and the line comes short of its box: of the width from the left, or of the height with
 * its baseline at the foot. [plakatStretchFactor] says beforehand.
 */
@Composable
public fun PlakatStretch(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PlakatTheme.colors.ink,
) {
    val caps = text.uppercase()
    val measurer = rememberTextMeasurer()
    val face = PlakatFaceMetrics.Display
    val font = PlakatTheme.typography.displayL.style
    val layout =
        remember(caps, font, measurer) {
            measurer.measure(
                caps,
                font.copy(fontSize = MEASURE_PX.sp).cssLine(face),
                softWrap = false,
                maxLines = 1,
                density = Density(1f),
            )
        }
    val ink = remember(caps) { stretchInk(caps) }
    Spacer(
        modifier
            .semantics { this.text = AnnotatedString(text) }
            .drawBehind {
                if (ink == null || size.minDimension <= 0f) return@drawBehind
                val fit = stretchFit(ink, size.width, size.height)
                val sx = fit.width / ((ink.right - ink.left) * MEASURE_PX)
                val sy = fit.height / (ink.height * MEASURE_PX)
                translate(top = size.height - fit.height) {
                    scale(sx, sy, pivot = Offset.Zero) {
                        // The layout's own top is the rounded ascent over its baseline (cssLine).
                        val top = ink.height * MEASURE_PX - round(face.ascent * MEASURE_PX)
                        drawText(layout, color, Offset(-ink.left * MEASURE_PX, top))
                    }
                }
            },
    )
}
