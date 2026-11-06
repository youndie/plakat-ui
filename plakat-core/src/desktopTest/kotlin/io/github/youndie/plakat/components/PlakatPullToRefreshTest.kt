package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-50: designed here. A pull through a real scroll in a box of 390 × 400; the wedge read off its left column. */
@OptIn(ExperimentalTestApi::class)
class PlakatPullToRefreshTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.pull(
        refreshing: () -> Boolean = { false },
        onRefresh: () -> Unit = {},
        enabled: Boolean = true,
        reduce: Boolean = true,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        density: Float = 1f,
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.size(390.dp, 400.dp).background(Color.White).testTag("box")) {
                        PlakatPullToRefreshBox(refreshing(), onRefresh, Modifier.fillMaxSize().testTag("p"), enabled) {
                            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("list")) {
                                Box(Modifier.fillMaxWidth().height(1200.dp).testTag("tall"))
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("box").captureToImage().toPixelMap()

    /** The wedge's height: how far its red runs down column 0. */
    private fun PixelMap.wedge(): Int =
        (0 until height).firstOrNull { this[0, it].let { c -> c != paper.red && c != paper.ink } } ?: height

    /** The finger down at the middle, and moved down by [by] past the slop. */
    private fun ComposeUiTest.drag(by: Float) {
        onNodeWithTag("list").performTouchInput {
            down(Offset(200f, 100f))
            moveBy(Offset(0f, 40f))
            moveBy(Offset(0f, by))
        }
        waitForIdle()
    }

    private fun ComposeUiTest.lift() {
        onNodeWithTag("list").performTouchInput {
            advanceEventTime(1000)
            up()
        }
        waitForIdle()
    }

    @Test
    fun `the wedge grows with the finger, its edge on the kit's one axis`() =
        runComposeUiTest {
            pull()
            assertEquals(0, frame().wedge())
            drag(30f)
            val first = frame().wedge()
            assertTrue(first in 25..71, "down by the drag, past the slop: $first")
            onNodeWithTag("list").performTouchInput { moveBy(Offset(0f, 20f)) }
            waitForIdle()
            assertEquals(first + 20, frame().wedge(), "at the finger")
            val px = frame()
            val h = first + 20
            // Along the top row it reaches h / tan 17°.
            val reach = (0 until 390).lastOrNull { px[it, 0] == paper.red } ?: -1
            val expected = h / tan(17 * PI / 180)
            assertTrue(abs(reach - expected) < 4, "the top row's reach: $reach, against $expected")
            // A column further on is shorter by the slope.
            val at = (0 until 400).count { px[100, it] == paper.red }
            assertTrue(abs(at - (h - 100 * tan(17 * PI / 180))) < 3, "at column 100: $at")
            lift()
        }

    @Test
    fun `it comes no further than 120`() =
        runComposeUiTest {
            pull()
            drag(600f)
            // Its tip runs out in antialiased rows under the rule, at a slope of 17°.
            assertTrue(frame().wedge() in 116..120, "${frame().wedge()}")
            lift()
        }

    @Test
    fun `from 80 it has a rule of ink along its edge, and short of it none`() =
        runComposeUiTest {
            pull()
            drag(20f)
            assertTrue(frame().wedge() < 80)
            val short = frame()
            assertEquals(
                0,
                (0 until 390).sumOf { x ->
                    (0 until 400).count { y -> short[x, y] == paper.ink }
                },
                "no rule yet",
            )
            onNodeWithTag("list").performTouchInput { moveBy(Offset(0f, 60f)) }
            waitForIdle()
            val px = frame()
            assertTrue(px.wedge() >= 80, "${px.wedge()}")
            val inks = (0 until 390).sumOf { x -> (0 until 400).count { y -> px[x, y] == paper.ink } }
            assertTrue(inks > 300, "the rule of 3 along its edge: $inks")
            lift()
        }

    @Test
    fun `let go short it goes back, and nothing is asked`() =
        runComposeUiTest {
            var asked = 0
            pull(onRefresh = { asked++ })
            drag(20f)
            assertTrue(frame().wedge() in 1..79)
            lift()
            assertEquals(0, asked)
            assertEquals(0, frame().wedge())
        }

    @Test
    fun `let go past 80 it asks once, and goes back`() =
        runComposeUiTest {
            var asked = 0
            pull(onRefresh = { asked++ })
            drag(100f)
            assertTrue(frame().wedge() >= 80)
            assertEquals(0, asked, "not until it is let go")
            lift()
            assertEquals(1, asked)
            assertEquals(0, frame().wedge())
        }

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

    @Test
    fun `going back it runs 120 ms on the hard stop`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            pull(reduce = false)
            mainClock.advanceTimeBy(100)
            onNodeWithTag("list").performTouchInput {
                down(Offset(200f, 100f))
                moveBy(Offset(0f, 40f))
                moveBy(Offset(0f, 60f))
            }
            mainClock.advanceTimeBy(50)
            val held = frame().wedge()
            onNodeWithTag("list").performTouchInput {
                advanceEventTime(1000)
                up()
            }
            val heights =
                (0 until 12).map {
                    mainClock.advanceTimeByFrame()
                    frame().wedge()
                }
            val fits =
                (0..3).any { start ->
                    heights.withIndex().all { (i, got) ->
                        val p = hardStop((((i - start) * 16.0) / 120).coerceIn(0.0, 1.0))
                        abs(got - held * (1 - p)) <= 1.5
                    }
                }
            assertTrue(fits, "from $held, per frame: $heights")
        }

    @Test
    fun `under reduced motion it goes back at once`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            pull(reduce = true)
            mainClock.advanceTimeBy(100)
            onNodeWithTag("list").performTouchInput {
                down(Offset(200f, 100f))
                moveBy(Offset(0f, 40f))
                moveBy(Offset(0f, 60f))
            }
            mainClock.advanceTimeBy(50)
            onNodeWithTag("list").performTouchInput {
                advanceEventTime(1000)
                up()
            }
            repeat(2) { mainClock.advanceTimeByFrame() }
            assertEquals(0, frame().wedge())
        }

    @Test
    fun `going back up, the wedge goes first and the list after`() =
        runComposeUiTest {
            pull()
            drag(40f)
            val h = frame().wedge()
            onNodeWithTag("list").performTouchInput { moveBy(Offset(0f, -30f)) }
            waitForIdle()
            assertEquals(h - 30, frame().wedge())
            onNodeWithTag("list").performTouchInput { moveBy(Offset(0f, -(h - 30f))) }
            waitForIdle()
            assertEquals(0, frame().wedge())
            onNodeWithTag("tall").assert(SemanticsMatcher("at the list's top") { it.boundsInRoot.top == 0f })
            lift()
        }

    @Test
    fun `a list not at its top scrolls and the wedge stays away`() =
        runComposeUiTest {
            pull()
            onNodeWithTag("list").performTouchInput {
                down(Offset(200f, 300f))
                moveBy(Offset(0f, -40f))
                moveBy(Offset(0f, -200f))
                advanceEventTime(1000)
                up()
            }
            waitForIdle()
            drag(50f)
            assertEquals(0, frame().wedge(), "first the list comes back")
            lift()
        }

    @Test
    fun `a wheel makes no pull`() =
        runComposeUiTest {
            pull()
            // Down a little first, so that the wheel coming back up has more than it needs at the list's top.
            onNodeWithTag("list").performMouseInput {
                moveTo(center)
                scroll(3f)
            }
            waitForIdle()
            onNodeWithTag("list").performMouseInput { scroll(-40f) }
            waitForIdle()
            assertEquals(0, frame().wedge())
        }

    @Test
    fun `already refreshing there is no pull`() =
        runComposeUiTest {
            var on by mutableStateOf(false)
            pull({ on }, enabled = true)
            on = true
            waitForIdle()
            drag(60f)
            assertEquals(paper.paper, frame()[0, 0], "the plate and no wedge")
            lift()
        }

    @Test
    fun `not enabled there is no pull`() =
        runComposeUiTest {
            pull(enabled = false)
            drag(60f)
            assertEquals(0, frame().wedge())
            lift()
        }

    @Test
    fun `while refreshing the sector stands on a plate of 56 in the corner, and engage is heard once`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var on by mutableStateOf(false)
            mainClock.autoAdvance = false
            pull({ on }, heard = heard)
            mainClock.advanceTimeBy(100)
            assertTrue(heard.isEmpty())
            on = true
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(listOf(PlakatEvent.Engage), heard)
            val px = frame()
            assertEquals(paper.paper, px[1, 1], "the plate")
            assertEquals(paper.paper, px[55, 55])
            assertEquals(Color.White, px[56, 28], "56 wide")
            assertEquals(Color.White, px[28, 56], "56 high")
            assertEquals(paper.ink, px[28, 46], "the ring's foot, 8 up and 1 in")
            onNode(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo.Indeterminate,
                ),
            ).assertExists()
            onNodeWithContentDescription("Loading").assertExists()
        }

    @Test
    fun `a reader refreshes by an action`() =
        runComposeUiTest {
            var asked = 0
            pull(onRefresh = { asked++ })
            runOnIdle { onNodeWithTag("p").fetchSemanticsNode().config[SemanticsActions.CustomActions][0].action() }
            assertEquals(1, asked)
        }

    @Test
    fun `at a density of 2 the most is 120 dp`() =
        runComposeUiTest {
            pull(density = 2f)
            onNodeWithTag("list").performTouchInput {
                down(Offset(400f, 200f))
                moveBy(Offset(0f, 80f))
                moveBy(Offset(0f, 1000f))
            }
            waitForIdle()
            assertTrue(frame().wedge() in 232..240, "${frame().wedge()}")
            lift()
        }
}
