package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-16: the carriage runs the rail in 120 ms hard-stop with the red segment behind it (`lever`). */
@OptIn(ExperimentalTestApi::class)
class PlakatSwitchTest {
    private val paper = PlakatPalettes.Paper

    /** `cubic-bezier(.2, .8, .9, .9)` at [x], by bisection, apart from the token's easing. */
    private fun hardStop(x: Double): Double {
        fun bez(
            t: Double,
            a: Double,
            b: Double,
        ) = 3 * (1 - t) * (1 - t) * t * a + 3 * (1 - t) * t * t * b + t * t * t
        var lo = 0.0
        var hi = 1.0
        repeat(60) {
            val mid = (lo + hi) / 2
            if (bez(mid, .2, .9) < x) lo = mid else hi = mid
        }
        return bez((lo + hi) / 2, .8, .9)
    }

    /**
     * Where the carriage is, 0 to 1 of its 36 dp way, read off row 10 (the carriage, above the rail)
     * with its antialiased left edge counted in part; and how far the red segment reaches, on row 21.
     */
    private fun ComposeUiTest.read(): Pair<Double, Double> {
        val px = onNodeWithTag("s").captureToImage().toPixelMap()

        fun cover(
            x: Int,
            y: Int,
            ink: Float,
        ) = ((paper.paper.green - px[x, y].green) / (paper.paper.green - ink)).coerceIn(0f, 1f)
        // The carriage is 20 wide; its left edge is 20 minus its coverage.
        val carriage = (0 until 56).sumOf { cover(it, 10, paper.ink.green).toDouble() }
        val left = (0 until 56).first { cover(it, 10, paper.ink.green) > 0f }
        val edge = left + 1 - cover(left, 10, paper.ink.green)
        val segment = (0 until 56).sumOf { x -> if (px[x, 21].red - px[x, 21].green > .3f) 1.0 else 0.0 }
        assertTrue(abs(carriage - 20) < .1, "the carriage is 20 wide: $carriage")
        return edge.toDouble() / 36 to segment
    }

    private fun ComposeUiTest.track(
        checked: () -> Boolean,
        onChange: (Boolean) -> Unit,
        reduce: Boolean,
        heard: MutableList<PlakatEvent>,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.background(paper.paper)) { PlakatSwitch(checked(), onChange, Modifier.testTag("s")) }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    @Test
    fun `the carriage runs the rail on the hard-stop and the segment follows it`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var on by mutableStateOf(false)
            track({ on }, { on = it }, reduce = false, heard = heard)
            assertEquals(0.0, read().first, 1e-3)
            onNodeWithTag("s").performClick()
            val way =
                (0 until 10).map {
                    mainClock.advanceTimeByFrame()
                    read()
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, r) ->
                        abs(r.first - hardStop(((i - start) * 16 / 120.0).coerceIn(0.0, 1.0))) <
                            .03
                    }
                }
            assertTrue(fits, "the carriage's way per frame: ${way.map { it.first }}")
            // The segment reaches the carriage's left edge, a pixel either way for the edge's antialiasing.
            for ((p, segment) in way) assertTrue(abs(segment - p * 36) <= 1.01, "segment $segment at ${p * 36}")
            onNodeWithTag("s").performClick()
            repeat(10) { mainClock.advanceTimeByFrame() }
            assertEquals(0.0, read().first, 1e-3)
            assertEquals(listOf(PlakatEvent.Lever, PlakatEvent.Lever), heard)
        }

    @Test
    fun `under reduced motion the carriage is at the other end at once`() =
        runComposeUiTest {
            var on by mutableStateOf(false)
            track({ on }, { on = it }, reduce = true, heard = mutableListOf())
            onNodeWithTag("s").performClick()
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(1.0, read().first, 1e-3)
        }

    @Test
    fun `the target is the 56 by 48 track`() =
        runComposeUiTest {
            setContent { PlakatTheme { PlakatSwitch(false, {}, Modifier.testTag("s")) } }
            onNodeWithTag("s")
                .assertWidthIsEqualTo(56.dp)
                .assertHeightIsEqualTo(48.dp)
                .assertIsOff()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
        }
}
