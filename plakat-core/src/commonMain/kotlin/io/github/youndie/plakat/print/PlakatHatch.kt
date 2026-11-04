package io.github.youndie.plakat.print

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * The danger hatch: the pages' `repeating-linear-gradient(45deg, ink 0 3px, ground 3px 6px)`, the form
 * that tells danger from primary when both are red (`plakat/semantic/danger`: "штриховка 45°, шаг 6").
 * The stripes are placed as CSS places a gradient on a box of [size]: the gradient line through the
 * box's centre at [angle] (0° up, clockwise), as long as the box's projection on it, the pattern
 * starting at the line's start. Hard stops, so every pixel is one paint or the other, as in Chrome.
 * [inkShare] is the ink's part of each period: a half for the danger hatch, 2 of 5 for the error
 * checkbox's `repeating-linear-gradient(45deg, red 0 2px, transparent 2px 5px)`.
 */
public fun plakatHatchBrush(
    size: Size,
    ink: Color,
    ground: Color,
    period: Float,
    angle: Float = 45f,
    inkShare: Float = .5f,
): Brush {
    val a = angle * PI / 180
    val dx = sin(a).toFloat()
    val dy = -cos(a).toFloat()
    val length = abs(size.width * dx) + abs(size.height * dy)
    val centre = Offset(size.width / 2, size.height / 2)
    val start = centre - Offset(dx, dy) * (length / 2)
    return Brush.linearGradient(
        0f to ink,
        inkShare to ink,
        inkShare to ground,
        1f to ground,
        start = start,
        end = start + Offset(dx, dy) * period,
        tileMode = TileMode.Repeated,
    )
}

/** Fills the element's box with the danger hatch, [period] wide (two stripes), under its content. */
public fun Modifier.plakatHatch(
    ink: Color,
    ground: Color,
    period: Dp = 6.dp,
): Modifier = drawBehind { drawRect(plakatHatchBrush(size, ink, ground, period.toPx())) }
