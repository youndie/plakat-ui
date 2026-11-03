package io.github.youndie.plakat.print

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * A print layer over the element (`plakat/print`: the paper texture, the non-print mask): [tile] laid
 * edge to edge at [tileSize] per tile, at [alpha], blended by [blend], over the content and clipped to
 * the element's box. Always a separate layer: off, the form under it is untouched (brief, principle 4).
 * Tiles are drawn whole with no smoothing seam, starting at the element's top-left.
 */
public fun Modifier.plakatPrintLayer(
    tile: ImageBitmap,
    tileSize: Dp,
    alpha: Float,
    blend: BlendMode,
): Modifier =
    drawWithContent {
        drawContent()
        val step = tileSize.toPx().roundToInt().coerceAtLeast(1)
        val cols = ceil(size.width / step).toInt()
        val rows = ceil(size.height / step).toInt()
        clipRect {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    drawImage(
                        tile,
                        dstOffset = IntOffset(c * step, r * step),
                        dstSize = IntSize(step, step),
                        alpha = alpha,
                        blendMode = blend,
                    )
                }
            }
        }
    }
