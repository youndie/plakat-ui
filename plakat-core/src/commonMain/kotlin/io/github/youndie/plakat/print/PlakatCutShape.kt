package io.github.youndie.plakat.print

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import io.github.youndie.plakat.tokens.PlakatShape

/**
 * A plate with its top-left corner cut along the kit's axis: the pages' `clip-path: polygon(0 14px,
 * 46px 0, 100% 0, 100% 100%, 0 100%)`. 46 × 14 lies on 17° (`plakat/shape/cut`); the Chip's smaller
 * plate cuts 38 × 12. The cut never shrinks the touch target, which is the layout box around it.
 */
@Immutable
public class PlakatCutShape(
    public val cutWidth: Dp = PlakatShape.CutWidth,
    public val cutHeight: Dp = PlakatShape.CutHeight,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w = with(density) { cutWidth.toPx() }
        val h = with(density) { cutHeight.toPx() }
        return Outline.Generic(
            Path().apply {
                moveTo(0f, h)
                lineTo(w, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            },
        )
    }

    override fun equals(other: Any?): Boolean =
        other is PlakatCutShape && other.cutWidth == cutWidth && other.cutHeight == cutHeight

    override fun hashCode(): Int = 31 * cutWidth.hashCode() + cutHeight.hashCode()

    override fun toString(): String = "PlakatCutShape($cutWidth × $cutHeight)"
}
