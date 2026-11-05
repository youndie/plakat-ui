package io.github.youndie.plakat.components

import kotlin.test.Test
import kotlin.test.assertEquals

/** B-20: the MVP page's `ticks(from, to)`, worked by hand from its code: lowest digit first, 40 ms a step, ≤ 400 ms. */
class PlakatTickTest {
    @Test
    fun `each digit steps straight to its new one the lowest first`() {
        // 039 to 047: the units 9 to 7 in two steps, then the tens 3 to 4 in one.
        val t = PlakatTick(39, 47)
        assertEquals(120, t.totalMs)
        assertEquals(listOf(39, 39, 38, 38, 37, 37, 47), (0..6).map { t.valueAt(it * 20.0) })
        assertEquals("47", t.textAt(120.0))
    }

    @Test
    fun `the digits shown are as many as the target has`() {
        // 120 to 1: the units up, then the tens down, then the hundreds; one column throughout.
        val t = PlakatTick(120, 1)
        assertEquals(listOf(120, 121, 111, 101, 1), (0..4).map { t.valueAt(it * 40.0) })
        assertEquals(listOf("0", "1", "1", "1", "1"), (0..4).map { t.textAt(it * 40.0) })
        // 39 to 120: three columns from the first frame.
        assertEquals("039", PlakatTick(39, 120).textAt(0.0))
    }

    @Test
    fun `a long change is held to 400 ms`() {
        // 999 to 0: 27 steps at 400 / 27 ms each.
        val t = PlakatTick(999, 0)
        assertEquals(400, t.totalMs)
        assertEquals(999, t.valueAt(14.0))
        assertEquals(998, t.valueAt(15.0))
        // The units reach 0 after nine steps (133.3 ms); the tens' first step ends at 148.1.
        assertEquals(990, t.valueAt(134.0))
        assertEquals(990, t.valueAt(148.0))
        assertEquals(980, t.valueAt(149.0))
        assertEquals(0, t.valueAt(400.0))
    }

    @Test
    fun `no change takes no time and numbers past three digits keep every digit`() {
        assertEquals(0, PlakatTick(12, 12).totalMs)
        assertEquals("12", PlakatTick(12, 12).textAt(0.0))
        val t = PlakatTick(1000, 1001)
        assertEquals(40, t.totalMs)
        assertEquals("1001", t.textAt(40.0))
    }
}
