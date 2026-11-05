package io.github.youndie.plakat.agit

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * Draws [shape] as the page's absolutely placed `div` draws it, in [color], 1 dp = 1 CSS px: a box
 * (turned about the middle of its left end), a circle (`border-radius: 50%` on a square), or a wedge
 * (the box's `clip-path` triangle).
 */
internal fun DrawScope.drawPosterShape(
    shape: PosterShape,
    color: Color,
    blendMode: BlendMode = DrawScope.DefaultBlendMode,
) {
    val d = density
    when (shape) {
        is PosterShape.Box -> {
            val x = (shape.x * d).toFloat()
            val y = (shape.y * d).toFloat()
            val h = (shape.h * d).toFloat()
            rotate((shape.rotate ?: 0.0).toFloat(), Offset(x, y + h / 2)) {
                drawRect(color, Offset(x, y), Size((shape.w * d).toFloat(), h), blendMode = blendMode)
            }
        }

        is PosterShape.Round -> {
            val r = (shape.d * d / 2).toFloat()
            drawCircle(
                color,
                r,
                Offset((shape.x * d).toFloat() + r, (shape.y * d).toFloat() + r),
                blendMode = blendMode,
            )
        }

        is PosterShape.Wedge -> {
            val x = (shape.x * d).toFloat()
            val y = (shape.y * d).toFloat()
            val w = (shape.w * d).toFloat()
            val h = (shape.h * d).toFloat()
            val path =
                Path().apply {
                    if (shape.lowerRight) {
                        moveTo(x, y + h)
                        lineTo(x + w, y)
                        lineTo(x + w, y + h)
                    } else {
                        moveTo(x, y)
                        lineTo(x + w, y)
                        lineTo(x, y + h)
                    }
                    close()
                }
            drawPath(path, color, blendMode = blendMode)
        }
    }
}
