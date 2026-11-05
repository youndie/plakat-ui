package io.github.youndie.plakat.print

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-51: a roll's image written a little each frame is the image written whole, at every step. */
class RaskatRollImageTest {
    private val a = Color(0xFFBE2F18)
    private val b = Color(0xFFEEE5D1)
    private val size = 64
    private val field = raskatField(size, size, RaskatOptions(streak = .5, dpr = 1.0))

    private fun assertSame(
        kept: RaskatRollImage,
        inkB: BooleanArray,
        step: String,
    ) {
        val want = raskatImage(size, size, inkB, a, b).toPixelMap()
        val got = kept.image.toPixelMap()
        for (y in 0 until size) for (x in 0 until size) assertEquals(want[x, y], got[x, y], "$step at $x, $y")
    }

    @Test
    fun `at every step of a roll it is the image written whole`() {
        val kept = RaskatRollImage(size, size, a, b)
        for (i in 0..20) {
            val t0 = raskatRollT0(i * 16.0, .58)
            val inkB = raskatInkB(field, t0, 12.0)
            kept.advance(inkB)
            assertSame(kept, inkB, "frame $i")
        }
    }

    @Test
    fun `a roll only gains ink, or it would be writing everything anyway`() {
        var before = BooleanArray(size * size)
        var taken = 0
        for (i in 0..20) {
            val inkB = raskatInkB(field, raskatRollT0(i * 16.0, .58), 12.0)
            taken += inkB.indices.count { before[it] && !inkB[it] }
            before = inkB
        }
        assertEquals(0, taken)
    }

    @Test
    fun `a step that takes ink back starts over and is still right`() {
        val kept = RaskatRollImage(size, size, a, b)
        val more = raskatInkB(field, .2, 12.0)
        val less = raskatInkB(field, .9, 12.0)
        assertTrue(less.count { it } < more.count { it })
        kept.advance(more)
        kept.advance(less)
        assertSame(kept, less, "back")
    }
}
