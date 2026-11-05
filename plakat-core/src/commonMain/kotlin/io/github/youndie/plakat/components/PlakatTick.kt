package io.github.youndie.plakat.components

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** One step of the tick, and the most a whole change may take: the spec row's `40 / шаг, ≤ 400`. */
private const val TICK_STEP_MS = 40.0
private const val TICK_MAX_MS = 400.0
private const val TICK_DIGITS = 3

/** Below any frame: a sum of 400 / n ms steps must not leave the last one undone at its end. */
private const val TICK_EPSILON_MS = 1e-6

/**
 * How a number changes in the kit (`tick`), the MVP page's `ticks(from, to)`: digit by digit, the
 * lowest first, each stepping straight from its old digit to its new one, 40 ms a step, faster when a
 * change has more than ten steps so the whole takes at most 400 ms. Values below zero count as zero.
 */
internal class PlakatTick(
    from: Int,
    to: Int,
) {
    private val target = to.coerceAtLeast(0)
    private val width = max(TICK_DIGITS, max(digits(from).size, digits(target).size))
    private val old = digits(from).padded()
    private val new = digits(target).padded()
    private val moves = old.indices.sumOf { abs(new[it] - old[it]) }
    private val step = if (moves == 0) TICK_STEP_MS else min(TICK_STEP_MS, TICK_MAX_MS / moves)

    val totalMs: Int = kotlin.math.ceil(moves * step - TICK_EPSILON_MS).toInt()

    /** The digits shown [ms] in: as many as the target has, as the page's columns are. */
    fun textAt(ms: Double): String = digitsAt(ms).joinToString("").takeLast(digits(target).size)

    /** The whole number under way [ms] in, the digits the target hides included. */
    fun valueAt(ms: Double): Int = digitsAt(ms).joinToString("").toInt()

    private fun digitsAt(ms: Double): IntArray {
        val shown = IntArray(width) { old[it] }
        var at = 0.0
        for (i in width - 1 downTo 0) {
            val n = abs(new[i] - old[i])
            val done = floor((ms - at + TICK_EPSILON_MS) / step).toInt().coerceIn(0, n)
            shown[i] = old[i] + (if (new[i] > old[i]) done else -done)
            at += n * step
        }
        return shown
    }

    private fun digits(v: Int) = v.coerceAtLeast(0).toString().map { it - '0' }

    private fun List<Int>.padded() = List(width - size) { 0 } + this
}
