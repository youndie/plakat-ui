package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import io.github.youndie.plakat.theme.FaceMetrics
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.cssLine
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

// Text placed as Blink places it, for what a Compose paragraph cannot: lines of a fractional height, which Skia
// lays on whole pixels, and lines shorter than their face. B-19 (the posters) and B-28 (the dialog).

/** Blink's baseline in a line box [line] px tall: the half-leading floored, plus the rounded ascent. */
internal fun cssBaseline(
    face: FaceMetrics,
    px: Float,
    line: Float,
): Float {
    val ascent = kotlin.math.round(face.ascent * px)
    val content = ascent + kotlin.math.round(face.descent * px)
    return floor((line - content) / 2) + ascent
}

/**
 * A line laid out as a CSS line box of `normal` height ([cssLine]), so its baseline is drawn at the
 * face's rounded ascent below its top — where Chrome draws a lone line, and where a layout's own
 * `firstBaseline` would not put it (Compose draws a baseline a fraction off the one it reports).
 */
internal fun TextMeasurer.line(
    text: String,
    style: TextStyle,
    face: FaceMetrics,
) = measure(text, style.cssLine(face))

/**
 * Draws [layout] (from [line]) so its baseline sits [baseline] below [top], at [x]. Upright, Blink
 * puts a baseline on a whole pixel ([snap]); turned, it keeps the fraction.
 */
internal fun DrawScope.drawOnBaseline(
    layout: TextLayoutResult,
    face: FaceMetrics,
    x: Float,
    top: Float,
    baseline: Float,
    color: Color,
    snap: Boolean,
) {
    val at = top + baseline
    val ascent =
        kotlin.math.round(
            face.ascent *
                layout.layoutInput.style.fontSize
                    .toPx(),
        )
    drawText(layout, color, Offset(x, (if (snap) floor(at + .5f) else at) - ascent))
}

/**
 * [text] broken to [width] as one paragraph, then each line drawn in its own CSS line box of [lineHeight] × the size. Past
 * [maxLines] it stops, with an ellipsis on the last line it draws unless [ellipsis] is false.
 */
internal fun DrawScope.drawParagraph(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    width: Float,
    origin: Offset,
    color: Color,
    lineHeight: Float,
    face: FaceMetrics = PlakatFaceMetrics.Body,
    maxLines: Int = Int.MAX_VALUE,
    ellipsis: Boolean = true,
) {
    val px = style.fontSize.toPx()
    val line = px * lineHeight
    val baseline = cssBaseline(face, px, line)
    val whole = measurer.measure(text, style, constraints = Constraints(maxWidth = max(1, width.toInt())))
    val lines = min(whole.lineCount, maxLines.coerceAtLeast(1))
    for (i in 0 until lines) {
        var part = text.substring(whole.getLineStart(i), whole.getLineEnd(i, visibleEnd = true))
        if (ellipsis && i == lines - 1 && lines < whole.lineCount) {
            while (part.isNotEmpty() && measurer.measure("$part…", style).size.width > width) part = part.dropLast(1)
            part = "${part.trimEnd()}…"
        }
        drawOnBaseline(
            measurer.line(part, style, face),
            face,
            origin.x,
            origin.y + i * line,
            baseline,
            color,
            snap = true,
        )
    }
}

/**
 * [text] as a paragraph of CSS line boxes of [lineHeight] × the size, each line on its own: its height is theirs,
 * rounded to the nearest, so what stands under it falls on the pixel Blink snaps it to. A block as wide as what it is
 * given ([fill]), or as its widest line; given no limit, as wide as its text. Past [maxLines] it stops, as
 * [drawParagraph] does.
 */
@Composable
internal fun CssParagraph(
    text: String,
    style: TextStyle,
    lineHeight: Float,
    color: Color,
    modifier: Modifier = Modifier,
    face: FaceMetrics = PlakatFaceMetrics.Body,
    maxLines: Int = Int.MAX_VALUE,
    ellipsis: Boolean = true,
    fill: Boolean = true,
) {
    val measurer = rememberTextMeasurer()
    val plain = remember(style) { style.copy(lineHeight = TextUnit.Unspecified) }
    Spacer(
        modifier
            .semantics { this.text = AnnotatedString(text) }
            .layout { measurable, constraints ->
                val whole = measurer.measure(text, plain, constraints = Constraints(maxWidth = constraints.maxWidth))
                val lines = min(whole.lineCount, maxLines.coerceAtLeast(1))
                val height = floor(lines * plain.fontSize.toPx() * lineHeight + .5f).toInt()
                val width =
                    when {
                        fill && constraints.hasBoundedWidth -> constraints.maxWidth
                        else -> whole.size.width.coerceIn(constraints.minWidth, constraints.maxWidth)
                    }
                val placeable = measurable.measure(Constraints.fixed(width, height))
                layout(width, height) { placeable.place(0, 0) }
            }.drawBehind {
                drawParagraph(
                    measurer,
                    text,
                    plain,
                    size.width,
                    Offset.Zero,
                    color,
                    lineHeight,
                    face,
                    maxLines,
                    ellipsis,
                )
            },
    )
}

/**
 * A column of blocks given no limit is as wide as its widest content, and its blocks fill that: `max-content`. Compose
 * would leave a `fillMaxWidth` child 0 wide. Given a limit, this does nothing.
 */
internal fun Modifier.maxContentWidth(): Modifier =
    layout { measurable, constraints ->
        val inner =
            if (constraints.hasBoundedWidth) {
                constraints
            } else {
                val wide = measurable.maxIntrinsicWidth(constraints.maxHeight).coerceAtLeast(constraints.minWidth)
                constraints.copy(minWidth = wide, maxWidth = wide)
            }
        val placeable = measurable.measure(inner)
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
