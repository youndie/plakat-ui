package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** B-24: the wedge grows linearly to its value; the sector turns by 30° every 100 ms, and stands under reduced motion. */
@OptIn(ExperimentalTestApi::class)
class PlakatProgressTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.linear(
        reduce: Boolean,
        value: () -> Float,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce) {
                    PlakatLinearProgress(value(), Modifier.width(200.dp).testTag("p"))
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("p").captureToImage().toPixelMap()

    /** How far along row [y] the wedge reaches, its antialiased edge counted in part. */
    private fun PixelMap.reach(y: Int) =
        (0 until width).sumOf { x ->
            ((paper.concrete.green - this[x, y].green) / (paper.concrete.green - paper.red.green))
                .coerceIn(
                    0f,
                    1f,
                ).toDouble()
        }

    /** Where the front at [x] crosses the top row's middle, half a pixel down the slant of 26 over 8. */
    private fun top(x: Double) = x - 26 * .5 / 8

    /** And the bottom row's, 7.5 down. */
    private fun bottom(x: Double) = x - 26 * 7.5 / 8

    @Test
    fun `the wedge grows linearly to a new value with its front at the slant`() =
        runComposeUiTest {
            var value by mutableFloatStateOf(.25f)
            linear(reduce = false) { value }
            assertEquals(top(50.0), frame().reach(0), 1.0)
            value = .75f
            val way =
                (0 until 16).map {
                    mainClock.advanceTimeByFrame()
                    frame().let { it.reach(0) to it.reach(7) }
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, r) ->
                        val x = 50 + 100 * ((i - start) * 16.0 / 200).coerceIn(0.0, 1.0)
                        abs(r.first - top(x)) < 1.5 && abs(r.second - bottom(x)) < 1.5
                    }
                }
            assertTrue(fits, "top and bottom rows per frame: ${way.map { "%.0f/%.0f".format(it.first, it.second) }}")
        }

    @Test
    fun `under reduced motion it is at its value at once`() =
        runComposeUiTest {
            var value by mutableFloatStateOf(.25f)
            linear(reduce = true) { value }
            value = .75f
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(top(150.0), frame().reach(0), 1.0)
        }

    @Test
    fun `empty it has no wedge and full it is square and a small value's wedge is what fits in the bar`() =
        runComposeUiTest {
            var value by mutableFloatStateOf(0f)
            linear(reduce = true) { value }
            assertEquals(0.0, frame().reach(0), .01)
            value = 1f
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(200.0, frame().reach(7), .01)
            value = .05f
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(top(10.0), frame().reach(0), 1.0)
            assertEquals(0.0, frame().reach(7), .5)
        }

    private fun ComposeUiTest.circular(
        reduce: Boolean,
        value: Float?,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce) {
                    PlakatCircularProgress(value, Modifier.testTag("p"))
                }
            }
        }
    }

    /** Whether the pixel at [degrees] clockwise from the top, 12 from the centre of the 40 dp circle, is red. */
    private fun PixelMap.redAt(degrees: Double): Boolean {
        val a = degrees * PI / 180
        val c = this[(20 + 12 * sin(a)).toInt(), (20 - 12 * cos(a)).toInt()]
        return c.red - c.green > .3f
    }

    @Test
    fun `a determinate sector runs clockwise from the top to its value`() =
        runComposeUiTest {
            circular(reduce = true, value = .7f)
            mainClock.advanceTimeByFrame()
            val px = frame()
            for (d in listOf(5.0, 90.0, 180.0, 245.0)) assertTrue(px.redAt(d), "red at $d")
            for (d in listOf(258.0, 300.0, 355.0)) assertFalse(px.redAt(d), "not red at $d")
        }

    /** Whether the sector stands turned by [step] of 30°: red just inside its leading and trailing edges and not beyond. */
    private fun PixelMap.sectorAt(step: Int): Boolean {
        val from = step * 30.0
        return redAt(from + 8) && redAt(from + 82) && !redAt(from - 8) && !redAt(from + 98)
    }

    @Test
    fun `the indeterminate sector steps 30 degrees every 100 ms through a cycle of 1200`() =
        runComposeUiTest {
            circular(reduce = false, value = null)
            val frames =
                (0 until 90).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val fits =
                (0..3).any { start ->
                    frames.withIndex().all { (i, px) ->
                        val ms = ((i - start) * 16).coerceAtLeast(0)
                        px.sectorAt((ms / 100) % 12)
                    }
                }
            assertTrue(
                fits,
                "the steps per frame: ${frames.map { px ->
                    (0 until 12).firstOrNull { px.sectorAt(it) }
                }}",
            )
        }

    @Test
    fun `under reduced motion the sector stands`() =
        runComposeUiTest {
            circular(reduce = true, value = null)
            repeat(5) {
                mainClock.advanceTimeBy(250)
                assertTrue(frame().sectorAt(0))
            }
        }

    @Test
    fun `it says its value and is 40 dp unless sized`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    PlakatLinearProgress(.4f, Modifier.testTag("l"))
                    PlakatCircularProgress(.25f, Modifier.testTag("c"))
                    PlakatCircularProgress(null, Modifier.size(24.dp).testTag("i"))
                }
            }
            onNodeWithTag("l")
                .assertHeightIsEqualTo(8.dp)
                .assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.ProgressBarRangeInfo,
                        ProgressBarRangeInfo(
                            .4f,
                            0f..1f,
                        ),
                    ),
                )
            onNodeWithTag("c").assertWidthIsEqualTo(40.dp).assertHeightIsEqualTo(40.dp)
            onNodeWithTag("i")
                .assertWidthIsEqualTo(24.dp)
                .assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.ProgressBarRangeInfo,
                        ProgressBarRangeInfo.Indeterminate,
                    ),
                )
        }
}
