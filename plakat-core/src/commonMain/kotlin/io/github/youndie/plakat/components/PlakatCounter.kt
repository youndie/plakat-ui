package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatGrid
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

/** The tile: `padding: 16px; gap: 8px` on concrete. */
private val TilePadding = PlakatGrid.Step * 2
private val TileGap = PlakatGrid.Step

/** The number: `font-family: 'Rubik Mono One'; font-size: 48px; line-height: 1`. */
private const val NUMBER_SP = 48
private const val NUMBER_LINE = 1f

/** The column's number: the Stretch page's `height: 104px` and 56 wide a place. */
private val ColumnHeight = 104.dp
private val ColumnPlace = 56.dp

/** The pictograms' row: `gap: 3px`, `max-width` per kind below; a remainder's word `11px / 12px`, weight 600. */
private val PictogramGap = 3.dp
private const val REMAINDER_SP = 11
private const val REMAINDER_LINE = 12

/** The legend: `font-size: 12px`, line `normal`. */
private const val LEGEND_SP = 12

/**
 * The legend's mark, where the page has a character neither font holds. It is drawn as the pictogram's shape, with
 * the metrics of the face Chrome falls back to in the references, at the legend's 12 px: advance, left, side, bottom.
 */
private const val MARK_ID = "mark"
private const val MARK_HIGH = 8f

private class LegendMark(
    val advance: Float,
    val left: Float,
    val side: Float,
    val bottom: Float,
)

private val SquareMark = LegendMark(6.504f, 1.371f, 4.049f, 1.664f)
private val CircleMark = LegendMark(7.248f, 1.037f, 5.156f, .826f)

/** What a counter counts in: «одна = N». */
public enum class PlakatPictogram {
    /** `width: 10px; height: 10px`, in a row of at most 180. */
    Square,

    /** `width: 12px; height: 12px; border-radius: 50%`, in a row of at most 150. */
    Circle,
    ;

    internal val size get() = if (this == Square) 10.dp else 12.dp
    internal val rowWide get() = if (this == Square) 180.dp else 150.dp
}

/**
 * The Isotype tile: [value] large, a row of [pictogram]s, each standing for [perPictogram] with what is left over as
 * a word, then [caption] and a [legend] that opens with the pictogram (`■ = 5 статей`). A new value ticks (`tick`):
 * digit by digit, the lowest first, 40 ms a step and at most 400 ms in all, the pictograms added one by one across it;
 * at once under reduced motion. [accentLast] puts the last pictogram in red (`красный — сегодня`): the word says it too.
 */
@Composable
public fun PlakatCounter(
    value: Int,
    caption: String,
    modifier: Modifier = Modifier,
    legend: String? = null,
    pictogram: PlakatPictogram = PlakatPictogram.Square,
    perPictogram: Int = 1,
    accentLast: Boolean = false,
) {
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    val per = perPictogram.coerceAtLeast(1)
    val ticker = rememberPlakatTicker(value)
    val tick = ticker.tick
    val body = type.bodyS.style.copy(color = colors.ink)
    Column(modifier.background(colors.concrete).padding(TilePadding)) {
        CssParagraph(
            tick.textAt(ticker.ms),
            type.displayL.style.copy(fontSize = NUMBER_SP.sp),
            NUMBER_LINE,
            colors.ink,
            face = PlakatFaceMetrics.Display,
        )
        Spacer(Modifier.height(TileGap))
        PictogramRow(
            tick.unitsAt(ticker.ms, per),
            tick.valueAt(ticker.ms) % per,
            pictogram,
            accentLast,
            body.copy(fontSize = REMAINDER_SP.sp, fontWeight = FontWeight.SemiBold).cssLine(PlakatFaceMetrics.Body),
        )
        Spacer(Modifier.height(TileGap))
        CounterCaption(caption)
        if (legend != null) {
            Spacer(Modifier.height(TileGap))
            Legend(legend, pictogram, body.copy(fontSize = LEGEND_SP.sp).cssLine(PlakatFaceMetrics.Body))
        }
    }
}

/**
 * The Stretch page's tile: [value] stretched to a column 104 high, 56 wide a place ([PlakatStretch]), over [caption].
 * The tick changes its digits as [PlakatCounter]'s do, by a change of sheet, never by a scale.
 */
@Composable
public fun PlakatCounterColumn(
    value: Int,
    caption: String,
    modifier: Modifier = Modifier,
    color: Color = PlakatTheme.colors.ink,
) {
    val ticker = rememberPlakatTicker(value)
    // The box goes by what shows: a new value is composed once before its tick starts, with the old digits still up.
    val shown = ticker.tick.textAt(ticker.ms)
    Column(modifier.background(PlakatTheme.colors.concrete).padding(TilePadding)) {
        PlakatStretch(shown, Modifier.size(ColumnPlace * shown.length, ColumnHeight), color)
        Spacer(Modifier.height(TileGap))
        CounterCaption(caption)
    }
}

@Composable
private fun CounterCaption(caption: String) {
    val colors = PlakatTheme.colors
    BasicText(
        caption,
        style =
            PlakatTheme.typography.label.style
                .copy(color = colors.ink)
                .cssLine(PlakatFaceMetrics.Body),
    )
}

@Composable
private fun PictogramRow(
    units: Int,
    remainder: Int,
    pictogram: PlakatPictogram,
    accentLast: Boolean,
    remainderStyle: TextStyle,
) {
    val colors = PlakatTheme.colors
    Layout(
        content = {
            repeat(units) { i ->
                val color = if (accentLast && i == units - 1) colors.red else colors.ink
                Box(
                    Modifier
                        .size(pictogram.size)
                        .then(
                            if (pictogram == PlakatPictogram.Circle) {
                                Modifier.background(color, CircleShape)
                            } else {
                                Modifier.background(color)
                            },
                        ),
                )
            }
            if (remainder > 0) Remainder(remainder, remainderStyle)
        },
    ) { measurables, constraints ->
        // `display: flex; flex-wrap: wrap; align-items: center`: each line as high as its tallest, each item centred.
        val gap = PictogramGap.roundToPx()
        val wide = min(constraints.maxWidth, pictogram.rowWide.roundToPx())
        val placeables = measurables.map { it.measure(Constraints()) }
        val lines = mutableListOf<MutableList<Int>>()
        var x = 0
        placeables.forEachIndexed { i, p ->
            if (lines.isEmpty() || (x > 0 && x + gap + p.width > wide)) {
                lines += mutableListOf<Int>()
                x = 0
            } else if (x > 0) {
                x += gap
            }
            lines.last() += i
            x += p.width
        }
        val heights = lines.map { line -> line.maxOf { placeables[it].height } }
        val height = heights.sum() + gap * (lines.size - 1).coerceAtLeast(0)
        layout(constraints.maxWidth, height) {
            var y = 0
            lines.forEachIndexed { l, line ->
                var at = 0
                for (i in line) {
                    val p = placeables[i]
                    p.place(at, y + (heights[l] - p.height) / 2)
                    at += p.width + gap
                }
                y += heights[l] + gap
            }
        }
    }
}

/**
 * What is left over, in a line 12 high: shorter than its face, so beyond [cssLine]. Laid out in the face's own line
 * and lifted to the baseline Blink gives a line of 12.
 */
@Composable
private fun Remainder(
    remainder: Int,
    style: TextStyle,
) {
    BasicText(
        "+$remainder",
        Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val px = style.fontSize.toPx()
            val line = (REMAINDER_LINE * density).roundToInt()
            val lift =
                round(PlakatFaceMetrics.Body.ascent * px) - cssBaseline(PlakatFaceMetrics.Body, px, line.toFloat())
            layout(placeable.width, line) { placeable.place(0, -lift.roundToInt()) }
        },
        style = style,
        softWrap = false,
    )
}

@Composable
private fun Legend(
    legend: String,
    pictogram: PlakatPictogram,
    style: TextStyle,
) {
    val ink = PlakatTheme.colors.ink
    val text =
        buildAnnotatedString {
            appendInlineContent(MARK_ID, if (pictogram == PlakatPictogram.Square) "■" else "●")
            append(' ')
            append(legend)
        }
    val shape = if (pictogram == PlakatPictogram.Square) SquareMark else CircleMark
    val mark =
        InlineTextContent(Placeholder(shape.advance.sp, MARK_HIGH.sp, PlaceholderVerticalAlign.AboveBaseline)) {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val side = shape.side.dp.toPx()
                        val at = Offset(shape.left.dp.toPx(), size.height - (shape.bottom.dp.toPx() + side))
                        if (pictogram == PlakatPictogram.Square) {
                            drawRect(ink, at, Size(side, side))
                        } else {
                            drawCircle(ink, side / 2, at + Offset(side / 2, side / 2))
                        }
                    },
            )
        }
    BasicText(text, style = style, inlineContent = mapOf(MARK_ID to mark))
}
