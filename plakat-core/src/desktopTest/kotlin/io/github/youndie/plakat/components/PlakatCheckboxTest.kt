package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsOn
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
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-15: the wedge in (120 hard-stop) and out (60 linear), its events, reduced motion, the zone. */
@OptIn(ExperimentalTestApi::class)
class PlakatCheckboxTest {
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

    private fun red(c: Color) = c.red - c.green > .2f

    /**
     * How far the wedge is in, read off the box: moved d along the diagonal, its visible part is a
     * triangle of side 18 − 2d, so the share of its full area that is red is (2p − 1)², p > ½.
     */
    private fun ComposeUiTest.progress(): Double {
        val px = onNodeWithTag("c").captureToImage().toPixelMap()
        var ink = 0.0
        for (y in 15 until 33) {
            for (x in 15 until 33) {
                val c = px[x, y]
                // Coverage of red over paper on the green channel, antialiased edges counted in part.
                ink += ((paper.paper.green - c.green) / (paper.paper.green - paper.red.green)).coerceIn(0f, 1f)
            }
        }
        val share = ink / (18 * 18 / 2.0)
        return if (share <= 0.001) 0.0 else (1 + sqrt(share.coerceAtMost(1.0))) / 2
    }

    private fun ComposeUiTest.box(
        checked: () -> Boolean,
        onChange: ((Boolean) -> Unit)?,
        reduce: Boolean,
        heard: MutableList<PlakatEvent>,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(reduceMotion = reduce, events = { heard += it }) {
                    // On paper: the box has no ground of its own, and the reading is red against paper.
                    Box(Modifier.background(paper.paper)) { PlakatCheckbox(checked(), onChange, Modifier.testTag("c")) }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    /** True when [way] is [curve] at the age of each 16 ms frame, from one of the first frames. */
    private fun follows(
        way: List<Double>,
        curve: (Double) -> Double,
    ) = (0..2).any { start -> way.withIndex().all { (i, p) -> abs(p - curve((i - start) * 16.0)) < .06 } }

    @Test
    fun `the wedge comes in on the hard-stop in 120 ms and leaves linearly in 60`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var checked by mutableStateOf(false)
            box({ checked }, { checked = it }, reduce = false, heard = heard)
            assertEquals(0.0, progress())
            onNodeWithTag("c").performClick()
            val inWay =
                (0 until 10).map {
                    mainClock.advanceTimeByFrame()
                    progress()
                }
            // Below half-way nothing of the wedge is in the box yet, so those frames read 0.
            assertTrue(
                follows(inWay) { ms -> hardStop((ms / 120).coerceIn(0.0, 1.0)).let { if (it < .5) 0.0 else it } },
                "in, per frame: $inWay",
            )
            onNodeWithTag("c").performClick()
            val outWay =
                (0 until 6).map {
                    mainClock.advanceTimeByFrame()
                    progress()
                }
            assertTrue(
                follows(outWay) { ms -> (1 - (ms / 60).coerceIn(0.0, 1.0)).let { if (it < .5) 0.0 else it } },
                "out, per frame: $outWay",
            )
            assertEquals(listOf(PlakatEvent.WedgeIn, PlakatEvent.WedgeOut), heard)
        }

    @Test
    fun `under reduced motion the wedge is in or out at once`() =
        runComposeUiTest {
            var checked by mutableStateOf(false)
            box({ checked }, { checked = it }, reduce = true, heard = mutableListOf())
            onNodeWithTag("c").performClick()
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(1.0, progress(), .02)
            onNodeWithTag("c").performClick()
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(0.0, progress())
        }

    @Test
    fun `the zone is 48 dp and a checkbox without a handler only shows`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    PlakatCheckbox(true, {}, Modifier.testTag("c"))
                    PlakatCheckbox(true, null, Modifier.testTag("shown"))
                }
            }
            onNodeWithTag("c")
                .assertWidthIsEqualTo(48.dp)
                .assertHeightIsEqualTo(48.dp)
                .assertIsOn()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
            onNodeWithTag("shown").assertHasNoClickAction()
        }

    @Test
    fun `the wedge leaves toward the top-right corner`() {
        for ((p, d) in listOf(0f to 18f, .5f to 9f, 1f to 0f)) {
            val o = wedgeOffset(18f, p)
            assertTrue(abs(o.x - d) < 1e-6f && abs(o.y + d) < 1e-6f, "at $p: $o")
        }
    }
}
