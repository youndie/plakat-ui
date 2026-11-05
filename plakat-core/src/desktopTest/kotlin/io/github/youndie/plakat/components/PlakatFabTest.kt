package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
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
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-21: `rail`, along the −17° axis from beyond the window's right edge, 200 ms hard-stop. The FAB rests
 * at (244, 100) on a white 400 × 200 stage, so it starts 156 further right and 156 · tan 17° higher, and
 * its left and top edges stay on the stage all the way.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatFabTest {
    private val paper = PlakatPalettes.Paper
    private val rise = tan(17 * PI / 180)

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

    private fun ComposeUiTest.stage(
        reduce: Boolean = false,
        rideIn: Boolean = true,
        heard: MutableList<PlakatEvent> = mutableListOf(),
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.size(400.dp, 200.dp).background(Color.White).testTag("s")) {
                        PlakatFab(
                            PlakatIcons.Add,
                            "Добавить",
                            {},
                            Modifier.offset(244.dp, 100.dp).testTag("f"),
                            rideIn = rideIn,
                        )
                    }
                }
            }
        }
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("s").captureToImage().toPixelMap()

    private fun red(c: Color) = c.red - c.green > .3f

    /** The circle's leftmost column and topmost row, or null while nothing of it shows. */
    private fun PixelMap.corner(): Pair<Int, Int>? {
        var left = Int.MAX_VALUE
        var top = Int.MAX_VALUE
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (red(this[x, y])) {
                    if (x < left) left = x
                    if (y < top) top = y
                }
            }
        }
        return if (left == Int.MAX_VALUE) null else left to top
    }

    @Test
    fun `it rides in along the axis from beyond the right edge on the hard-stop`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            stage(heard = heard)
            val way =
                (0 until 16).map {
                    mainClock.advanceTimeByFrame()
                    frame().corner()
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, c) ->
                        val p = hardStop(((i - start) * 16.0 / 200).coerceIn(0.0, 1.0))
                        val x = 244 + 156 * (1 - p)
                        val y = 100 - 156 * rise * (1 - p)
                        // Its left side at the edge shows nothing; from there both edges are read, within the rounding of a curve.
                        if (x >= 399) c == null else c != null && abs(c.first - x) <= 2 && abs(c.second - y) <= 2
                    }
                }
            assertTrue(fits, "left and top per frame: $way")
            assertEquals(244 to 100, way.last())
            assertEquals(listOf(PlakatEvent.Rail), heard)
        }

    @Test
    fun `under reduced motion it is in place at once`() =
        runComposeUiTest {
            stage(reduce = true)
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(244 to 100, frame().corner())
        }

    @Test
    fun `told not to ride it is in place from the first frame and says nothing`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            stage(rideIn = false, heard = heard)
            mainClock.advanceTimeByFrame()
            assertEquals(244 to 100, frame().corner())
            assertEquals(emptyList(), heard)
        }

    @Test
    fun `pressed it turns ink with the icon 1 dp over and returns to red`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            stage(rideIn = false, heard = heard)
            mainClock.advanceTimeBy(100)
            // The Add stencil's crossbar, on column 264 of the icon's 24 dp centred in the circle.
            val barRow = frame().let { px -> (116 until 140).first { y -> !red(px[264, y]) } }
            onNodeWithTag("f").performTouchInput { down(center) }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val down = frame()
            assertEquals(paper.ink, down[250, 128])
            assertEquals(paper.ink, down[264, barRow], "the bar's first row is ground now")
            // The crossbar's edge falls inside a pixel: its partial row has moved down one, the full one after it.
            assertTrue(down[264, barRow + 1].green > paper.ink.green + .2f, "the icon has moved down")
            assertEquals(paper.paper, down[264, barRow + 2])
            onNodeWithTag("f").performTouchInput { up() }
            mainClock.advanceTimeBy(100)
            assertEquals(paper.red, frame()[250, 128])
            assertEquals(listOf(PlakatEvent.Stamp, PlakatEvent.Release), heard)
        }

    @Test
    fun `it is a 56 dp button and disabled it does not answer`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    PlakatFab(PlakatIcons.Add, "Добавить", {}, Modifier.testTag("f"), rideIn = false)
                    PlakatFab(PlakatIcons.Add, "Нет", {}, Modifier.testTag("off"), enabled = false, rideIn = false)
                }
            }
            onNodeWithTag("f")
                .assertWidthIsEqualTo(56.dp)
                .assertHeightIsEqualTo(56.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            onNodeWithTag("off").assertIsNotEnabled()
        }
}
