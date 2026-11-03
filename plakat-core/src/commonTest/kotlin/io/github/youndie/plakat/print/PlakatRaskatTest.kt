package io.github.youndie.plakat.print

import io.github.youndie.plakat.agit.fnv
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.tan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-04: the port prints what the vendored `plakat-raskat.js` prints, pixel for pixel, for every raskat
 * shape the design's pages draw, on every target this suite runs on (research D5). The goldens are
 * the JS's own output (`scripts/raskat-goldens.mjs`).
 */
class PlakatRaskatTest {
    private class Case(
        val name: String,
        val w: Double,
        val h: Double,
        val t0: Double,
        val hard: Double,
        val options: RaskatOptions,
    )

    // The same cases, in the same order, as CASES in scripts/raskat-goldens.mjs.
    private val cases =
        listOf(
            Case("tokensCircle", 116.0, 116.0, .58, 12.0, RaskatOptions(grain = 1.5, streak = .5)),
            Case("posterCircle", 120.0, 120.0, .58, 12.0, RaskatOptions(grain = 2.0, streak = .5)),
            Case("grain", 208.0, 208.0, .55, 10.0, RaskatOptions(mode = RaskatMode.Grain, streak = .5)),
            Case("dots", 208.0, 208.0, .55, 10.0, RaskatOptions(mode = RaskatMode.Dots)),
            Case("lines", 208.0, 208.0, .55, 10.0, RaskatOptions(mode = RaskatMode.Lines)),
        ) +
            listOf(1.15, .9, .7, .55).mapIndexed { i, t0 ->
                Case("rollStill$i", 104.0, 104.0, t0, 10.0, RaskatOptions(grain = 1.5, streak = .5))
            } +
            listOf(
                Case(
                    "splashWedge",
                    390.0,
                    390 * tan(17 * PI / 180),
                    .5,
                    9.0,
                    RaskatOptions(angle = 73.0, grain = 2.5, seed = 1923),
                ),
                Case("panelRect", 216.0, 232.0, .55, 10.0, RaskatOptions(seed = 1927)),
            )

    /** As drawRaskat sizes its image: the box times dpr, rounded half up, at least 1. */
    private fun px(v: Double) = max(1, kotlin.math.floor(v * 2 + .5).toInt())

    @Test
    fun `every case the design draws prints the same inks pixel for pixel`() {
        assertEquals(cases.map { it.name }, RaskatGoldens.inks.keys.toList(), "cases in the harness's order")
        for (c in cases) {
            val (w, h, rows) = RaskatGoldens.inks.getValue(c.name)
            assertEquals(w to h, px(c.w) to px(c.h), "${c.name}: size")
            val inks = raskatInkB(raskatField(w, h, c.options), c.t0, c.hard)
            val wrong =
                (0 until h).filter { y ->
                    val row = buildString { for (x in 0 until w) append(if (inks[y * w + x]) 'B' else 'A') }
                    fnv(row) != rows.substring(y * 16, y * 16 + 16)
                }
            assertTrue(wrong.isEmpty(), "${c.name}: ${wrong.size} of $h rows differ, first ${wrong.take(5)}")
        }
    }

    @Test
    fun `the field is the design's to the last bit of every float`() {
        val c = cases.first()
        val (w, h, _) = RaskatGoldens.inks.getValue(c.name)
        val field = raskatField(w, h, c.options)

        fun rows(
            values: FloatArray,
            golden: String,
        ) = (0 until h).filter { y ->
            val row =
                buildString {
                    for (x in 0 until w) {
                        append(
                            values[y * w + x]
                                .toRawBits()
                                .toUInt()
                                .toString(16)
                                .padStart(8, '0'),
                        )
                    }
                }
            fnv(row) != golden.substring(y * 16, y * 16 + 16)
        }
        assertEquals(emptyList(), rows(field.t, RaskatGoldens.fieldT), "T rows that differ")
        assertEquals(emptyList(), rows(field.m, RaskatGoldens.fieldM), "M rows that differ")
    }

    @Test
    fun `the roll moves t0 as the design's roll does at every millisecond`() {
        val golden = RaskatGoldens.roll.chunked(16)
        assertEquals(321, golden.size, "frames 0..320 ms")
        golden.forEachIndexed { ms, bits ->
            val t0 = raskatRollT0(ms.toDouble(), .58)
            assertEquals(
                bits,
                t0
                    .toRawBits()
                    .toULong()
                    .toString(16)
                    .padStart(16, '0'),
                "t0 at $ms ms = $t0",
            )
        }
    }

    @Test
    fun `the roll starts beyond the edge and stops on the seam without overshoot`() {
        // The design's 24 bisections stop 8·10⁻⁸ short of the edge at 0 ms; the port keeps that too.
        assertEquals(RASKAT_ROLL_FROM, raskatRollT0(0.0, .58), 1e-6)
        // …and as far short of the seam at 320 ms.
        assertEquals(.58, raskatRollT0(320.0, .58), 1e-6)
        var last = raskatRollT0(0.0, .58)
        for (ms in 1..400) {
            val t0 = raskatRollT0(ms.toDouble(), .58)
            assertTrue(t0 <= last && t0 >= .58 - 1e-6, "monotone to the seam at $ms ms: $t0")
            last = t0
        }
    }
}
