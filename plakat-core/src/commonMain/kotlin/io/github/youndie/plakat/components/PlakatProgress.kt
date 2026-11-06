package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines

/** The wedge's front over the bar's height: the MVP page's `polygon(0 0, 100% 0, calc(100% - 26px) 100%, 0 100%)`, −17°. */
private val WedgeSlant = 26.dp

/** The circular's size: `width: 40px; height: 40px`; the page's inline one is 24, set by the modifier. */
private val CircularSize = 40.dp

/** [this] as a share of the whole, 0 to 1. A `done / total` with a total of 0 is NaN, and that is nothing done yet. */
private fun Float.progressShare(): Float = if (isNaN()) 0f else coerceIn(0f, 1f)

/**
 * The value on its way: a new value is reached linearly (the spec row's «по данным · linear» names no
 * time, so `plakat/motion/normal`), at once under reduced motion. The first value is simply there.
 */
@Composable
private fun rememberProgress(value: Float): State<Float> {
    val reduce = PlakatTheme.reduceMotion
    val shown = remember { Animatable(value.progressShare()) }
    LaunchedEffect(value, reduce) {
        val target = value.progressShare()
        if (reduce) {
            shown.snapTo(target)
        } else {
            shown.animateTo(target, tween(PlakatDurations.NORMAL, easing = PlakatEasings.Linear))
        }
    }
    return shown.asState()
}

/** The ring: `box-shadow: inset 0 0 0 3px` on a circle, the difference of two circles rather than a stroke. */
private fun DrawScope.drawRing(color: Color) {
    val w = PlakatLines.Rule.toPx()
    drawPath(
        Path().apply {
            fillType = PathFillType.EvenOdd
            addOval(Rect(Offset.Zero, size))
            addOval(Rect(Offset(w, w), Size(size.width - 2 * w, size.height - 2 * w)))
        },
        color,
    )
}

/**
 * A linear progress: a red wedge growing to [value] (0 to 1) in an 8 dp concrete bar, its front at
 * −17°, square only when full.
 */
@Composable
public fun PlakatLinearProgress(
    value: Float,
    modifier: Modifier = Modifier,
) {
    val colors = PlakatTheme.colors
    val shown by rememberProgress(value)
    Canvas(
        modifier
            .fillMaxWidth()
            .height(PlakatLines.Bar)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value.progressShare(), 0f..1f) },
    ) {
        drawRect(colors.concrete)
        val x = size.width * shown
        when {
            shown >= 1f -> {
                drawRect(colors.red)
            }

            shown > 0f -> {
                // A small value's front runs out of the bar on the left: the wedge is the part inside it.
                clipRect(right = x) {
                    drawPath(
                        Path().apply {
                            moveTo(0f, 0f)
                            lineTo(x, 0f)
                            lineTo(x - WedgeSlant.toPx(), size.height)
                            lineTo(0f, size.height)
                            close()
                        },
                        colors.red,
                    )
                }
            }
        }
    }
}

/**
 * A circular progress, 40 dp unless [modifier] sizes it (the page's inline one is 24): a 3 dp ink ring
 * filled from the top clockwise to [value] by a red sector. With a null [value] it is indeterminate: a
 * 90° sector turning in steps of 30° (a cycle of 1200 ms), which under reduced motion stands.
 */
@Composable
public fun PlakatCircularProgress(
    value: Float?,
    modifier: Modifier = Modifier,
) {
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val semantics =
        Modifier.semantics {
            progressBarRangeInfo =
                if (value ==
                    null
                ) {
                    ProgressBarRangeInfo.Indeterminate
                } else {
                    ProgressBarRangeInfo(value.progressShare(), 0f..1f)
                }
        }
    // A Box and not a Canvas: a Canvas is a Spacer, which under a minimum alone draws in nothing at the minimum's centre.
    val base = modifier.defaultMinSize(CircularSize, CircularSize).then(semantics)
    if (value != null) {
        val shown by rememberProgress(value)
        Box(
            base.drawBehind {
                drawRing(colors.ink)
                // A hard-stopped sweep, as the page's `conic-gradient`: its edges are not antialiased, an arc's would be.
                val p = shown
                if (p > 0f) {
                    rotate(-90f) {
                        drawCircle(
                            Brush.sweepGradient(
                                0f to colors.red,
                                p to colors.red,
                                p to Color.Transparent,
                                1f to Color.Transparent,
                                center = Offset(size.width / 2, size.height / 2),
                            ),
                        )
                    }
                }
            },
        )
    } else {
        val turn = remember { Animatable(0f) }
        LaunchedEffect(reduce) {
            if (reduce) {
                turn.snapTo(0f)
            } else {
                turn.animateTo(
                    SPINNER_TURN_MS.toFloat(),
                    infiniteRepeatable(tween(SPINNER_TURN_MS, easing = LinearEasing), RepeatMode.Restart),
                )
            }
        }
        Box(
            base.drawBehind {
                drawRing(colors.ink)
                rotate(spinnerDegrees(turn.value.toLong())) {
                    drawArc(
                        colors.red,
                        -90f,
                        90f,
                        useCenter = true,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                    )
                }
            },
        )
    }
}
