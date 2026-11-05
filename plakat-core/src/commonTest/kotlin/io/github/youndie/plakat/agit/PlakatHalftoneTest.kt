package io.github.youndie.plakat.agit

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-33: the screen against what the vendored `halftone` draws in Node, dot by dot, over the design's stand-in. */
class PlakatHalftoneTest {
    /** `standIn(w, h, fx, fy)`'s gradient per pixel, the same stops as in the script, as opaque grey ARGB. */
    private fun standIn(
        w: Int,
        h: Int,
        fx: Double,
        fy: Double,
    ): IntArray {
        val stops = listOf(0.0 to 0x15, .3 to 0x20, .55 to 0x8a, .8 to 0xdc, 1.0 to 0xf4)
        return IntArray(w * h) { i ->
            val x = i % w
            val y = i / w
            val t = hypot(x + .5 - w * fx, y + .5 - h * fy) / (min(w, h) * .62)
            val v =
                if (t >= 1) {
                    0xf4
                } else {
                    var k = 0
                    while (t > stops[k + 1].first) k++
                    val (a, va) = stops[k]
                    val (b, vb) = stops[k + 1]
                    floor(va + (vb - va) * (t - a) / (b - a) + .5).toInt()
                }
            (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
    }

    @Test
    fun everyDotIsTheDesignsAtItsCentreAndRadius() {
        for (g in HalftoneGoldens.frames) {
            val lum = halftoneLuminance(standIn(g.w, g.h, g.fx, g.fy), g.contrast)
            val dots = halftoneDots(lum, g.w, g.h, g.pitch, g.angle)
            assertEquals(g.count, dots.size / 3, "${g.w} × ${g.h}: how many")
            for ((i, want) in g.at) {
                for (k in 0..2) {
                    val got = dots[i * 3 + k]
                    assertTrue(abs(got - want[k]) < 1e-9, "${g.w} × ${g.h}, dot $i, $k: $got vs ${want[k]}")
                }
            }
            for (k in 0..2) {
                val sum = (dots.indices step 3).sumOf { dots[it + k] }
                assertTrue(abs(sum - g.sums[k]) < 1e-6, "${g.w} × ${g.h}, sum $k: $sum vs ${g.sums[k]}")
            }
        }
    }

    @Test
    fun theMaskIsTheDesignsBitForBit() {
        for (g in HalftoneGoldens.masks) {
            val lum = halftoneLuminance(standIn(g.w, g.h, g.fx, g.fy), g.contrast)
            val bits = halftoneMask(lum, g.threshold).joinToString("") { if (it) "1" else "0" }
            assertEquals(g.bits, bits, "${g.w} × ${g.h}")
        }
    }

    @Test
    fun colourCountsByItsLuminance() {
        // A pure green is .587, a pure red .299, a pure blue .114: the Rec. 601 weights, at no contrast.
        val lum = halftoneLuminance(intArrayOf(0xFF00FF00.toInt(), 0xFFFF0000.toInt(), 0xFF0000FF.toInt()), 1.0)
        assertEquals(.587, lum[0], 1e-9)
        assertEquals(.299, lum[1], 1e-9)
        assertEquals(.114, lum[2], 1e-9)
    }

    @Test
    fun skeletonStepsSixTimesInACycle() {
        assertEquals(
            listOf(0, 0, 1, 1, 2, 5, 0, 1),
            listOf(0L, 199L, 200L, 399L, 400L, 1199L, 1200L, 1400L).map {
                skeletonStep(it)
            },
        )
    }
}
