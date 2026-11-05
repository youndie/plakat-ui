package io.github.youndie.plakat.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.VertexMode
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.toArgb
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * B-32, hypothesis H4: `drawVertices` with an image shader, on a bitmap's canvas, draws the same picture on every
 * target this suite runs on. A source of a red half and a blue half is mapped onto a mesh, flipped, so that a wrong
 * texture coordinate shows, and read back by pixel. Kept off the halves' border, where nearest and linear disagree.
 */
class PlakatVerticesTest {
    private val red = Color.Red.toArgb()
    private val blue = Color.Blue.toArgb()

    private fun source(): ImageBitmap {
        val image = ImageBitmap(40, 20)
        val canvas = Canvas(image)
        canvas.drawRect(Rect(0f, 0f, 20f, 20f), Paint().apply { color = Color.Red })
        canvas.drawRect(Rect(20f, 0f, 40f, 20f), Paint().apply { color = Color.Blue })
        return image
    }

    private fun draw(
        positions: List<Offset>,
        texture: List<Offset>,
        indices: List<Int>,
    ): IntArray {
        val out = ImageBitmap(80, 80)
        Canvas(out).drawVertices(
            Vertices(VertexMode.Triangles, positions, texture, List(positions.size) { Color.White }, indices),
            BlendMode.Modulate,
            Paint().apply { shader = ImageShader(source()) },
        )
        return IntArray(80 * 80).also { out.readPixels(it) }
    }

    @Test
    fun aMeshCarriesItsTextureOntoItsPositions() {
        // Two quads sharing an edge: x 10…70, y 20…60, the source's u running right to left.
        val px =
            draw(
                listOf(
                    Offset(10f, 20f),
                    Offset(40f, 20f),
                    Offset(70f, 20f),
                    Offset(10f, 60f),
                    Offset(40f, 60f),
                    Offset(70f, 60f),
                ),
                listOf(
                    Offset(40f, 0f),
                    Offset(20f, 0f),
                    Offset(0f, 0f),
                    Offset(40f, 20f),
                    Offset(20f, 20f),
                    Offset(0f, 20f),
                ),
                listOf(0, 1, 4, 0, 4, 3, 1, 2, 5, 1, 5, 4),
            )

        fun at(
            x: Int,
            y: Int,
        ) = px[y * 80 + x]
        assertEquals(blue, at(20, 40), "the left, flipped")
        assertEquals(red, at(60, 40), "the right, flipped")
        assertEquals(blue, at(38, 21), "up to the shared edge, on the diagonal's far side")
        assertEquals(blue, at(38, 58))
        assertEquals(red, at(42, 21))
        assertEquals(red, at(42, 58))
        assertEquals(0, at(5, 40), "nothing past the mesh")
        assertEquals(0, at(75, 40))
        assertEquals(0, at(40, 10))
        assertEquals(0, at(40, 70))
    }

    @Test
    fun aMeshHasNoSeamAlongItsSharedEdges() {
        // One colour across a fan of triangles about a point: every pixel inside is that colour, no hole on an edge.
        val c = Offset(40f, 40f)
        val ring = listOf(Offset(10f, 10f), Offset(70f, 10f), Offset(70f, 70f), Offset(10f, 70f))
        val px =
            draw(
                ring + c,
                List(5) { Offset(10f, 10f) },
                listOf(0, 1, 4, 1, 2, 4, 2, 3, 4, 3, 0, 4),
            )
        for (y in 12 until 68) for (x in 12 until 68) assertEquals(red, px[y * 80 + x], "at $x, $y")
    }
}
