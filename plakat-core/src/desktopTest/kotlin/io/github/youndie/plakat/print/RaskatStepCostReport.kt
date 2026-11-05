package io.github.youndie.plakat.print

import androidx.compose.ui.graphics.Color
import io.github.youndie.plakat.agit.PosterRecipes
import io.github.youndie.plakat.agit.posterComposition
import java.io.File
import kotlin.test.Test

/**
 * B-51: what a raskat frame's parts cost, apart, for the report's decomposition. It measures only when asked:
 * `PLAKAT_RASKAT_COST=<out file> ./gradlew :plakat-core:desktopTest --tests '*RaskatStepCostReport*'`. CI does not measure.
 */
class RaskatStepCostReport {
    private fun ms(block: () -> Unit): Double {
        val t = System.nanoTime()
        block()
        return (System.nanoTime() - t) / 1e6
    }

    private fun List<Double>.at(p: Int) = sorted()[(size * p / 100).coerceAtMost(size - 1)]

    @Test
    fun report() {
        val out = System.getenv("PLAKAT_RASKAT_COST") ?: return
        val circle = posterComposition(1919, PosterRecipes.Circle).circle!!.d
        val lines =
            mutableListOf(
                "# circle of the benchmark's poster: $circle dp; 60 frames of a roll per step, after 200 to warm",
                "px,step,p50_ms,p95_ms",
            )
        val a = Color(0xFFBE2F18)
        val b = Color(0xFFEEE5D1)
        for (px in listOf(circle.toInt(), (circle * 2).toInt(), (circle * 2.75).toInt(), 330)) {
            val options = RaskatOptions(streak = .5, dpr = 2.0)
            lines += "$px,field-once,${"%.2f".format(ms { raskatField(px, px, options) })},"
            val field = raskatField(px, px, options)
            val inks = (0 until 60).map { raskatInkB(field, raskatRollT0(it * 320.0 / 60, .58), 12.0) }
            repeat(200) { raskatImage(px, px, inks[it % 60], a, b) }
            val compare = (0 until 60).map { i -> ms { raskatInkB(field, raskatRollT0(i * 320.0 / 60, .58), 12.0) } }
            val whole = inks.map { ms { raskatImage(px, px, it, a, b) } }
            val roll = RaskatRollImage(px, px, a, b)
            val kept = inks.map { ms { roll.advance(it) } }
            for ((step, values) in listOf("compare" to compare, "write-whole" to whole, "write-new" to kept)) {
                lines += "$px,$step,${"%.2f".format(values.at(50))},${"%.2f".format(values.at(95))}"
            }
        }
        File(out).writeText(lines.joinToString("\n", postfix = "\n"))
    }
}
