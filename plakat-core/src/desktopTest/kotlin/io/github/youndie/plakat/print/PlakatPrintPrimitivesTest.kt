package io.github.youndie.plakat.print

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-10: the print primitives' geometry, read off the pixels a path fills. */
class PlakatPrintPrimitivesTest {
    private fun fill(
        path: Path,
        w: Int,
        h: Int,
    ): PixelMap {
        val image = ImageBitmap(w, h)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(w.toFloat(), h.toFloat())) {
            drawPath(path, Color.White)
        }
        return image.toPixelMap()
    }

    private fun PixelMap.inside(
        x: Int,
        y: Int,
    ) = this[x, y].alpha > .99f

    private fun PixelMap.outside(
        x: Int,
        y: Int,
    ) = this[x, y].alpha < .01f

    @Test
    fun `the cut is the pages' polygon, 46 by 14 on the axis, at any density`() {
        for (d in listOf(1f, 2f, 3f)) {
            val w = (164 * d).toInt()
            val h = (48 * d).toInt()
            val o =
                PlakatCutShape().createOutline(
                    Size(w.toFloat(), h.toFloat()),
                    LayoutDirection.Ltr,
                    Density(d),
                ) as Outline.Generic
            val px = fill(o.path, w, h)
            assertTrue(px.outside(0, 0), "the cut-off corner at density $d")
            assertTrue(px.inside((47 * d).toInt(), 0), "the top edge starts at 46 dp, density $d")
            // At x = 40 dp the cut line is 1.8 dp down: the top row there is wholly cut off.
            assertTrue(px.outside((40 * d).toInt(), 0), "and not before it, density $d")
            assertTrue(px.inside(0, (15 * d).toInt()), "the left edge starts at 14 dp, density $d")
            assertTrue(px.outside(0, (12 * d).toInt()), "and not above it, density $d")
            assertTrue(
                px.inside(w - 1, h - 1) && px.inside(w - 1, 0) && px.inside(0, h - 1),
                "the other three corners, density $d",
            )
        }
    }

    @Test
    fun `every wipe shows nothing at 0 and everything at 1`() {
        for (wipe in listOf(PlakatWipe.Left, PlakatWipe.Top, PlakatWipe.Bottom, PlakatWipe.Diagonal(60.dp))) {
            val none = fill(plakatWipePath(wipe, Size(358f, 88f), 0f, Density(1f)), 358, 88)
            val all = fill(plakatWipePath(wipe, Size(358f, 88f), 1f, Density(1f)), 358, 88)
            var shown = 0
            var full = 0
            for (y in 0 until 88) {
                for (x in 0 until 358) {
                    if (!none.outside(x, y)) shown++
                    if (all.inside(x, y)) full++
                }
            }
            assertEquals(0, shown, "$wipe at 0")
            assertEquals(358 * 88, full, "$wipe at 1")
        }
    }

    @Test
    fun `the diagonal wipe leans back by its slant and grows without a jump`() {
        val wipe = PlakatWipe.Diagonal(60.dp)
        val half = fill(plakatWipePath(wipe, Size(358f, 88f), .5f, Density(1f)), 358, 88)
        val top = ((358 + 60) / 2)
        assertTrue(half.inside(top - 2, 0) && half.outside(top + 1, 0), "top edge at $top")
        assertTrue(half.inside(top - 62, 87) && half.outside(top - 58, 87), "foot a slant behind")
        // Every millisecond of a 200 ms wipe shows more than the last, and never more than one step of
        // (w + slant) / 200 columns' worth more.
        var last = 0
        for (ms in 0..200) {
            val px = fill(plakatWipePath(wipe, Size(358f, 88f), ms / 200f, Density(1f)), 358, 88)
            var n = 0
            for (y in 0 until 88) for (x in 0 until 358) if (!px.outside(x, y)) n++
            assertTrue(n >= last, "monotone at $ms ms")
            assertTrue(n - last <= 88 * 3, "no jump at $ms ms: ${n - last} pixels")
            last = n
        }
    }
}
