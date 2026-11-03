package io.github.youndie.plakat.print

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Where a wipe opens from (brief, motion rule 4: appearing and leaving is a wipe, never a fade).
 * [Left], [Top], [Bottom] are the pages' `inset(…)` curtains; [Diagonal] is their slanted one,
 * `polygon(0 0, x 0, x − slant 100%, 0 100%)`, its edge leaning back by [Diagonal.slant] over the
 * element's height, which travels the width plus the slant so the element ends wholly shown.
 */
@Immutable
public sealed class PlakatWipe {
    public data object Left : PlakatWipe()

    public data object Top : PlakatWipe()

    public data object Bottom : PlakatWipe()

    public data class Diagonal(
        val slant: Dp,
    ) : PlakatWipe()
}

/** The part of a [size] box a wipe from [wipe] shows at [progress] (0 hidden, 1 whole). */
public fun plakatWipePath(
    wipe: PlakatWipe,
    size: Size,
    progress: Float,
    density: Density,
): Path =
    Path().apply {
        val p = progress.coerceIn(0f, 1f)
        val w = size.width
        val h = size.height
        when (wipe) {
            PlakatWipe.Left -> {
                addRect(Rect(0f, 0f, w * p, h))
            }

            PlakatWipe.Top -> {
                addRect(Rect(0f, 0f, w, h * p))
            }

            PlakatWipe.Bottom -> {
                addRect(Rect(0f, h * (1 - p), w, h))
            }

            is PlakatWipe.Diagonal -> {
                val slant = with(density) { wipe.slant.toPx() }
                val x = p * (w + slant)
                moveTo(0f, 0f)
                lineTo(x, 0f)
                lineTo(x - slant, h)
                lineTo(0f, h)
                close()
            }
        }
    }

private class WipeShape(
    val wipe: PlakatWipe,
    val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Generic(plakatWipePath(wipe, size, progress, density))
}

/**
 * Clips the element to a wipe from [wipe] at [progress], read at drawing time so an animation moves
 * the curtain without recomposing what it covers. At 1 the element is not clipped at all.
 */
public fun Modifier.plakatWipe(
    wipe: PlakatWipe,
    progress: () -> Float,
): Modifier =
    graphicsLayer {
        val p = progress()
        clip = p < 1f
        shape = WipeShape(wipe, p)
    }
