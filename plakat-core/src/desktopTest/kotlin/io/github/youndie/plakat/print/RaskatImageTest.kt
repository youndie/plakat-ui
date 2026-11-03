package io.github.youndie.plakat.print

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-04: the image a raskat field prints is its ink map exactly — no pixel of ink B lost or blurred
 * by the batch of points that writes it — and printed into a shape it keeps every pixel inside the
 * outline. Desktop only: it reads pixels back.
 */
class RaskatImageTest {
    private val red = Color(0xFFBE2F18)
    private val paper = Color(0xFFEEE5D1)
    private val options = RaskatOptions(grain = 2.0, streak = .5)

    @Test
    fun `the image is the ink map pixel for pixel`() {
        val inks = raskatInkB(raskatField(240, 240, options), .58, 12.0)
        val px = raskatImage(240, 240, inks, red, paper).toPixelMap()
        val wrong = inks.indices.count { k -> px[k % 240, k / 240] != (if (inks[k]) paper else red) }
        assertEquals(0, wrong, "pixels whose ink differs from the map")
        assertTrue(inks.count { it } in 1 until inks.size, "both inks present")
    }

    @Test
    fun `printed into a circle it keeps the map inside and nothing outside`() {
        val inks = raskatInkB(raskatField(240, 240, options), .58, 12.0)
        val image = raskatImage(240, 240, inks, red, paper)
        val target = ImageBitmap(260, 260)
        val outline = Path().apply { addOval(Rect(Offset(10f, 10f), Size(240f, 240f))) }
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(target), Size(260f, 260f)) {
            drawRaskat(image, outline, raskatTopLeft(Offset(10f, 10f)))
        }
        val px = target.toPixelMap()
        // Inside the circle by more than a pixel, every pixel is the map's; beyond it, nothing is drawn.
        var inside = 0
        for (y in 0 until 260) {
            for (x in 0 until 260) {
                val d = kotlin.math.hypot(x + .5 - 130, y + .5 - 130)
                if (d < 118) {
                    inside++
                    val k = (y - 10) * 240 + (x - 10)
                    assertEquals(if (inks[k]) paper else red, px[x, y], "inside at $x,$y")
                } else if (d > 122) {
                    assertEquals(0f, px[x, y].alpha, "outside at $x,$y")
                }
            }
        }
        assertTrue(inside > 40000, "pixels checked inside: $inside")
    }
}
