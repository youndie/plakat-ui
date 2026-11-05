package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-44: the Principles page's slider. Geometry by pixels, the run off each frame, the gestures through real input. */
@OptIn(ExperimentalTestApi::class)
class PlakatSliderTest {
    private val paper = PlakatPalettes.Paper

    /** 358 wide with 20 either side, so that the carriage has its room and the capture sees past the ends. */
    private fun ComposeUiTest.slider(
        value: () -> Float,
        onChange: (Float) -> Unit = {},
        steps: Int = 5,
        enabled: Boolean = true,
        reduce: Boolean = true,
        range: ClosedFloatingPointRange<Float> = 0f..1f,
        density: Float = 1f,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        finished: (() -> Unit)? = null,
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.size(398.dp, 48.dp).background(Color.White).testTag("box")) {
                        PlakatSlider(
                            value(),
                            onChange,
                            Modifier.padding(start = 20.dp).width(358.dp).testTag("s"),
                            enabled = enabled,
                            valueRange = range,
                            steps = steps,
                            onValueChangeFinished = finished,
                        )
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("box").captureToImage().toPixelMap()

    /** The carriage's left edge along its row 10, above the rail, 20 being the slider's 0. */
    private fun PixelMap.carriage(): Int = (0 until width).first { this[it, 10] != Color.White } - 20

    private fun PixelMap.column(
        x: Int,
        rows: IntRange = 0 until 48,
    ) = rows.filter { this[x, it] != Color.White }.map { this[x, it] }

    @Test
    fun `a rail of 3, a red fill of 8 up to the value, a carriage of 20 by 32`() =
        runComposeUiTest {
            slider({ .5f }, steps = 0)
            onNodeWithTag("s").assertHeightIsEqualTo(48.dp)
            val px = frame()
            // The rail, right of the carriage, on rows 22 to 24.
            assertEquals(List(3) { paper.ink }, px.column(20 + 300))
            assertEquals(paper.ink, px[320, 22])
            assertEquals(paper.ink, px[320, 24])
            // The fill, left of it, on rows 20 to 27.
            assertEquals(List(8) { paper.red }, px.column(20 + 100))
            assertEquals(paper.red, px[120, 20])
            assertEquals(paper.red, px[120, 27])
            // The carriage from 169 to 188, rows 8 to 39.
            assertEquals(169, px.carriage())
            assertEquals(paper.ink, px[20 + 188, 10])
            assertEquals(Color.White, px[20 + 189, 10])
            assertEquals(List(32) { paper.ink }, px.column(20 + 179))
            assertEquals(paper.ink, px[20 + 179, 8])
            assertEquals(paper.ink, px[20 + 179, 39])
        }

    @Test
    fun `stepped it has a tick at each stop, 3 by 8, every third 12, and continuous none`() =
        runComposeUiTest {
            var steps by mutableIntStateOf(5)
            setContent {
                PlakatTheme(colors = paper, reduceMotion = true) {
                    CompositionLocalProvider(LocalDensity provides Density(1f)) {
                        Box(Modifier.size(398.dp, 48.dp).background(Color.White).testTag("box")) {
                            PlakatSlider(0f, {}, Modifier.padding(start = 20.dp).width(358.dp), steps = steps)
                        }
                    }
                }
            }
            val px = frame()

            fun PixelMap.tickRows(x: Int) = (32 until 48).count { this[x, it] != Color.White }
            // Seven stops: `round(358 k / 6) − 1`, 3 wide, and past the carriage's foot.
            for (k in 1..5) {
                val at = 20 + round(358f * k / 6).toInt() - 1
                val height = if (k % 3 == 0) 12 else 8
                for (x in at..at + 2) assertEquals(height, px.tickRows(x), "tick $k, column $x")
                assertEquals(0, px.tickRows(at - 1), "left of tick $k")
                assertEquals(0, px.tickRows(at + 3), "right of tick $k")
                assertEquals(paper.ink, px[at, 32])
            }
            steps = 0
            waitForIdle()
            val free = frame()
            assertEquals(0, (31 until 398).sumOf { free.tickRows(it) }, "no stops, no ticks, past the carriage at 0")
        }

    @Test
    fun `at the ends the carriage stands 10 past them, as the page's does`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(0f)
            slider({ v })
            assertEquals(-10, frame().carriage())
            v = 1f
            waitForIdle()
            assertEquals(348, frame().carriage())
            assertEquals(paper.red, frame()[20 + 330, 21], "the fill the whole way")
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
    fun `moved it runs in 120 ms on the hard stop, the fill with it, and notch is heard once`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var v by mutableFloatStateOf(0f)
            mainClock.autoAdvance = false
            slider({ v }, reduce = false, heard = heard)
            mainClock.advanceTimeBy(100)
            assertTrue(heard.isEmpty(), "nothing on the first composition: $heard")
            v = 1f
            val frames =
                (0 until 12).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val edges = frames.map { it.carriage() }
            val fits =
                (0..2).any { start ->
                    edges.withIndex().all { (i, got) ->
                        val p = hardStop((((i - start) * 16.0) / 120).coerceIn(0.0, 1.0))
                        abs(got - (358 * p - 10)) <= 1.5
                    }
                }
            assertTrue(fits, "the carriage's edge per frame: $edges")
            assertEquals(edges.sorted(), edges, "never back: no overshoot")
            assertEquals(348, edges.last())
            // The fill ends where the carriage begins.
            val mid = frames[3]
            val edge = mid.carriage()
            assertEquals(paper.red, mid[20 + edge - 1, 21], "the fill up to it: $edges")
            assertEquals(listOf(PlakatEvent.Notch), heard)
        }

    @Test
    fun `under reduced motion it is there at once, notch and all`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var v by mutableFloatStateOf(0f)
            mainClock.autoAdvance = false
            slider({ v }, reduce = true, heard = heard)
            mainClock.advanceTimeBy(100)
            v = 1f
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(348, frame().carriage())
            assertEquals(listOf(PlakatEvent.Notch), heard)
        }

    @Test
    fun `a continuous one has no notch`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var v by mutableFloatStateOf(0f)
            slider({ v }, steps = 0, heard = heard)
            v = .5f
            waitForIdle()
            assertTrue(heard.isEmpty(), "$heard")
        }

    @Test
    fun `a mouse sets it as it lands, on the nearest stop, a half going up`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(0f)
            var calls = 0
            slider({ v }, {
                v = it
                calls++
            })
            onNodeWithTag("s").performMouseInput {
                moveTo(Offset(358f * 3.5f / 6, 24f))
                press()
            }
            waitForIdle()
            assertEquals(4f / 6, v, 1e-5f, "3.5 of 6 goes to 4")
            assertEquals(1, calls)
            onNodeWithTag("s").performMouseInput {
                moveTo(Offset(358f * 3.6f / 6, 24f))
                moveTo(Offset(358f * 4.2f / 6, 24f))
            }
            waitForIdle()
            assertEquals(1, calls, "the same stop is no change")
            onNodeWithTag("s").performMouseInput { release() }
        }

    @Test
    fun `an even half goes up too, as the page's Math round does`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(0f)
            slider({ v }, { v = it }, steps = 1)
            onNodeWithTag("s").performMouseInput {
                moveTo(Offset(89.5f, 24f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(.5f, v, "a half of the stop at 0 and the stop at a half: half-to-even would stay at 0")
        }

    @Test
    fun `a finger sets it on lifting, or past the slop as it drags`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(0f)
            var finished = 0
            slider({ v }, { v = it }, steps = 0, finished = { finished++ })
            onNodeWithTag("s").performTouchInput { down(Offset(100f, 24f)) }
            waitForIdle()
            assertEquals(0f, v, "not yet: it may be a scroll")
            onNodeWithTag("s").performTouchInput { up() }
            waitForIdle()
            assertEquals(100f / 358, v, 1e-3f, "lifted")
            assertEquals(1, finished)
            onNodeWithTag("s").performTouchInput {
                down(Offset(100f, 24f))
                moveTo(Offset(200f, 24f))
            }
            waitForIdle()
            assertEquals(200f / 358, v, 1e-3f, "dragged")
            // Past the end it stays at the end.
            onNodeWithTag("s").performTouchInput {
                moveTo(Offset(900f, 24f))
                up()
            }
            waitForIdle()
            assertEquals(1f, v)
            assertEquals(2, finished)
        }

    @Test
    fun `dragged, a continuous carriage follows the finger with no run`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(0f)
            mainClock.autoAdvance = false
            slider({ v }, { v = it }, steps = 0, reduce = false)
            mainClock.advanceTimeBy(100)
            onNodeWithTag("s").performTouchInput {
                down(Offset(0f, 24f))
                moveTo(Offset(40f, 24f))
            }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            onNodeWithTag("s").performTouchInput { moveTo(Offset(300f, 24f)) }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(290, frame().carriage(), "there, not on its way")
        }

    @Test
    fun `a scroll passing over it changes nothing`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(.25f)
            var finished = 0
            setContent {
                PlakatTheme(colors = paper, reduceMotion = true) {
                    Box(Modifier.size(398.dp, 200.dp).verticalScroll(rememberScrollState()).testTag("scroll")) {
                        Box(Modifier.size(398.dp, 800.dp)) {
                            PlakatSlider(v, { v = it }, Modifier.width(358.dp).testTag("s"), onValueChangeFinished = {
                                finished++
                            })
                        }
                    }
                }
            }
            onNodeWithTag("s").performTouchInput {
                down(Offset(300f, 24f))
                moveBy(Offset(0f, -100f))
                moveBy(Offset(0f, -60f))
                up()
            }
            waitForIdle()
            assertEquals(.25f, v)
            assertEquals(0, finished)
        }

    @Test
    fun `pressed its carriage inverts, and it comes back`() =
        runComposeUiTest {
            slider({ .5f })
            assertEquals(paper.ink, frame()[20 + 179, 16], "at rest, solid")
            onNodeWithTag("s").performMouseInput {
                moveTo(Offset(179f, 24f))
                press()
            }
            waitForIdle()
            val px = frame()
            assertEquals(paper.paper, px[20 + 179, 16], "paper inside")
            assertEquals(paper.ink, px[20 + 170, 16], "a frame of 3")
            assertEquals(paper.ink, px[20 + 171, 16])
            assertEquals(paper.paper, px[20 + 172, 16])
            onNodeWithTag("s").performMouseInput { release() }
            waitForIdle()
            assertEquals(paper.ink, frame()[20 + 179, 16])
        }

    @Test
    fun `the keys move it a stop, or a hundredth, and to the ends, within them`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(.5f)
            var finished = 0
            slider({ v }, { v = it }, finished = { finished++ })
            onNodeWithTag("s").requestFocus()
            onNodeWithTag("s").performKeyInput { pressKey(Key.DirectionRight) }
            waitForIdle()
            assertEquals(4f / 6, v, 1e-5f)
            assertEquals(1, finished)
            onNodeWithTag("s").performKeyInput { pressKey(Key.DirectionDown) }
            onNodeWithTag("s").performKeyInput { pressKey(Key.DirectionLeft) }
            waitForIdle()
            assertEquals(2f / 6, v, 1e-5f)
            onNodeWithTag("s").performKeyInput { pressKey(Key.DirectionUp) }
            waitForIdle()
            assertEquals(.5f, v, 1e-5f)
            onNodeWithTag("s").performKeyInput { pressKey(Key.MoveEnd) }
            waitForIdle()
            assertEquals(1f, v)
            onNodeWithTag("s").performKeyInput { pressKey(Key.DirectionRight) }
            waitForIdle()
            assertEquals(1f, v, "held at the end")
            onNodeWithTag("s").performKeyInput { pressKey(Key.MoveHome) }
            waitForIdle()
            assertEquals(0f, v)
        }

    @Test
    fun `a continuous one steps a hundredth of its range`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(50f)
            slider({ v }, { v = it }, steps = 0, range = 0f..200f)
            onNodeWithTag("s").requestFocus()
            onNodeWithTag("s").performKeyInput { pressKey(Key.DirectionRight) }
            waitForIdle()
            assertEquals(52f, v, 1e-3f)
        }

    @Test
    fun `its range is the page's, 14 to 26 by twos`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(20f)
            slider({ v }, { v = it }, range = 14f..26f)
            assertEquals(169, frame().carriage(), "20 is the half")
            onNodeWithTag("s").performMouseInput {
                moveTo(Offset(358f * 5 / 6, 24f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(24f, v, 1e-3f)
        }

    @Test
    fun `disabled it is a hair and a concrete carriage, with no fill, and takes nothing`() =
        runComposeUiTest {
            var calls = 0
            slider({ .5f }, { calls++ }, enabled = false)
            val px = frame()
            assertEquals(List(1) { paper.inkMuted }, px.column(20 + 300, 0 until 32))
            assertEquals(paper.inkMuted, px[320, 23])
            assertEquals(Color.White, px[120, 21], "no fill")
            assertEquals(paper.inkMuted, px[120, 23], "the hair there too")
            assertEquals(paper.inkMuted, px[20 + 169, 16], "the carriage's frame")
            assertEquals(paper.concrete, px[20 + 175, 16], "and its concrete")
            onNodeWithTag("s").performMouseInput {
                moveTo(Offset(40f, 24f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(0, calls)
            onNodeWithTag("s").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Disabled))
            onNodeWithTag("s").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetProgress))
            onNodeWithTag("s").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Focused))
        }

    @Test
    fun `it is read as a range, and set by one, on a stop`() =
        runComposeUiTest {
            var v by mutableFloatStateOf(20f)
            var finished = 0
            slider({ v }, { v = it }, range = 14f..26f, finished = { finished++ })
            onNodeWithTag("s").assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo(20f, 14f..26f, 5),
                ),
            )
            onNodeWithTag("s").performSemanticsAction(SemanticsActions.SetProgress) { it(23.3f) }
            waitForIdle()
            assertEquals(24f, v, 1e-3f, "the nearest stop")
            assertEquals(1, finished)
        }

    @Test
    fun `at a density of 2 it stands at the same dp`() =
        runComposeUiTest {
            slider({ .5f }, density = 2f)
            val px = frame()
            assertEquals(796, px.width)
            assertEquals(2 * 169, (0 until px.width).first { px[it, 20] != Color.White } - 40)
            assertEquals(paper.ink, px[40 + 2 * 179, 16], "the carriage's top, 2 × 8")
            assertEquals(Color.White, px[40 + 2 * 300, 15], "no higher")
            assertEquals(paper.ink, px[40 + 2 * 300, 45], "the rail, 2 × 22 and on")
        }
}
