package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatEasings
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
    private val origin = from.coerceAtLeast(0)
    private val target = to.coerceAtLeast(0)
    private val width = max(TICK_DIGITS, max(digits(from).size, digits(target).size))
    private val old = digits(from).padded()
    private val new = digits(target).padded()
    private val moves = old.indices.sumOf { abs(new[it] - old[it]) }
    private val step = if (moves == 0) TICK_STEP_MS else min(TICK_STEP_MS, TICK_MAX_MS / moves)

    val totalMs: Int = kotlin.math.ceil(moves * step - TICK_EPSILON_MS).toInt()

    /** The digits shown [ms] in: as many as the target has, as the page's columns are. */
    fun textAt(ms: Double): String = digitsAt(ms).joinToString("").takeLast(digits(target).size)

    /**
     * The whole number under way [ms] in, the digits the target hides included. Digit by digit it may pass over the largest
     * Int on the way from one number under it to another: 2 000 000 000 to 1 999 999 999 goes through 2 399 999 999.
     */
    fun valueAt(ms: Double): Int =
        digitsAt(ms)
            .joinToString("")
            .toLong()
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()

    /**
     * How many whole [per]s stand [ms] in: from the old number's to the new one's, one at a time and evenly across
     * the tick, for a number's digits may pass through anything on the way (39, 38, 37, 47).
     */
    fun unitsAt(
        ms: Double,
        per: Int,
    ): Int {
        val from = origin / per
        val to = target / per
        if (totalMs == 0) return to
        val done = floor(abs(to - from) * (ms / totalMs).coerceIn(0.0, 1.0) + TICK_EPSILON_MS).toInt()
        return if (to > from) from + done else from - done
    }

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

/** A number under way to its latest value: [tick] [ms] in. */
@Stable
internal class PlakatTicker(
    value: Int,
) {
    var tick by mutableStateOf(PlakatTick(value, value))
    var seen by mutableStateOf(value)
    val age = Animatable(0f)

    val ms: Double get() = age.value.toDouble()
}

/** Ticks to each new [value], with its `tick`, from what shows now, so a change in the middle does not jump; at once under reduced motion. */
@Composable
internal fun rememberPlakatTicker(value: Int): PlakatTicker {
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val ticker = remember { PlakatTicker(value) }
    LaunchedEffect(value, reduce) {
        if (value == ticker.seen) return@LaunchedEffect
        val shown = ticker.tick.valueAt(ticker.ms)
        ticker.seen = value
        ticker.tick = PlakatTick(shown, value)
        events.onEvent(PlakatEvent.Tick)
        if (reduce) {
            ticker.age.snapTo(Float.MAX_VALUE)
        } else {
            ticker.age.snapTo(0f)
            ticker.age.animateTo(
                ticker.tick.totalMs.toFloat(),
                tween(ticker.tick.totalMs, easing = PlakatEasings.Linear),
            )
        }
    }
    return ticker
}
