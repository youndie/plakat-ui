package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-46: designed here. In a box of 390 × 500; a sheet of 200 under a handle of 48 stands from row 252 at its stop. */
@OptIn(ExperimentalTestApi::class)
class PlakatBottomSheetTest {
    private val paper = PlakatPalettes.Paper

    /** A colour nothing else has, to read the sheet's top by: the concrete of a concrete sheet. */
    private fun ComposeUiTest.sheet(
        visible: () -> Boolean,
        ground: PlakatSheetGround = PlakatSheetGround.Concrete,
        reduce: Boolean = true,
        tall: Int = 200,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        onDismiss: () -> Unit = {},
        title: String? = null,
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(
                    colors = paper,
                    reduceMotion = reduce,
                    print =
                        PlakatPrintSwitches(
                            texture = false,
                        ),
                    events = {
                        heard += it
                    },
                ) {
                    Box(Modifier.size(390.dp, 500.dp).background(Color.White).testTag("box")) {
                        PlakatBottomSheet(visible(), onDismiss, ground = ground, paneTitle = title) {
                            Box(Modifier.fillMaxWidth().height(tall.dp).testTag("content")) { BasicText("Ч") }
                        }
                    }
                }
            }
        }
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("box").captureToImage().toPixelMap()

    /** The sheet's top along a column clear of the handle: the first row of its ground. */
    private fun PixelMap.top(): Int = (0 until height).firstOrNull { this[20, it] == paper.concrete } ?: height

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
    fun `it stands at the foot with its height, a bar of 8 by 64 in the middle of a handle of 48`() =
        runComposeUiTest {
            sheet({ true })
            waitForIdle()
            val px = frame()
            assertEquals(252, px.top())
            assertEquals(paper.concrete, px[195, 271], "over the bar")
            assertEquals(paper.ink, px[195, 272], "the bar's top, 252 + 20")
            assertEquals(paper.ink, px[195, 279])
            assertEquals(paper.concrete, px[195, 280], "8 high")
            assertEquals(paper.concrete, px[162, 276], "left of it")
            assertEquals(paper.ink, px[163, 276], "its left, (390 − 64) / 2")
            assertEquals(paper.ink, px[226, 276])
            assertEquals(paper.concrete, px[227, 276], "64 wide")
        }

    @Test
    fun `its ground is paper or concrete`() =
        runComposeUiTest {
            sheet({ true }, PlakatSheetGround.Paper)
            waitForIdle()
            assertEquals(paper.paper, frame()[20, 400])
        }

    @Test
    fun `it stops 96 short of the top and its content scrolls`() =
        runComposeUiTest {
            sheet({ true }, tall = 900)
            waitForIdle()
            assertEquals(96, frame().top())
            onNode(hasScrollAction()).assertExists()
        }

    @Test
    fun `it rides up on the hard stop in 320 ms, the screen closing with it, and back the same way`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var open by mutableStateOf(false)
            mainClock.autoAdvance = false
            sheet({ open }, reduce = false, heard = heard)
            mainClock.advanceTimeBy(100)
            assertTrue(heard.isEmpty(), "closed from the start, it says nothing: $heard")
            onNodeWithTag("content").assertDoesNotExist()
            open = true

            fun read() =
                (0 until 24).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val opening = read()
            val tops = opening.map { it.top() }
            val fits =
                (0..3).any { start ->
                    tops.withIndex().all { (i, got) ->
                        val p = hardStop((((i - start) * 16.0) / 320).coerceIn(0.0, 1.0))
                        abs(got - (500 - 248 * p)) <= 1.5
                    }
                }
            assertTrue(fits, "the sheet's top per frame: $tops")
            assertEquals(tops.sortedDescending(), tops, "never past the stop")
            assertEquals(252, tops.last())
            assertEquals(listOf(PlakatEvent.Rail), heard)
            // The screen closes the background from the left, over the same clock: more dots along a row each frame.
            val dots = opening.map { px -> (0 until 390).count { px[it, 40] != Color.White } }
            assertEquals(dots.sorted(), dots, "only ever more: $dots")
            assertTrue(dots[8] in 40..340, "partway a third of the way in: $dots")
            assertEquals(195, dots.last(), "whole, a dot of 2 in every 4 along this row")
            open = false
            val closing = read().map { it.top() }
            assertEquals(closing.sorted(), closing, "going down: $closing")
            assertEquals(500, closing.last())
            assertEquals(listOf(PlakatEvent.Rail, PlakatEvent.Rail), heard)
            mainClock.advanceTimeByFrame()
            onNodeWithTag("content").assertDoesNotExist()
        }

    @Test
    fun `under reduced motion it is there and gone at once`() =
        runComposeUiTest {
            var open by mutableStateOf(false)
            mainClock.autoAdvance = false
            sheet({ open }, reduce = true)
            mainClock.advanceTimeBy(100)
            open = true
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(252, frame().top())
            open = false
            repeat(3) { mainClock.advanceTimeByFrame() }
            onNodeWithTag("content").assertDoesNotExist()
        }

    private val handleY = 252f + 24f

    @Test
    fun `the handle follows a finger down the rail, and not above its stop`() =
        runComposeUiTest {
            sheet({ true })
            waitForIdle()
            onNodeWithTag("box").performTouchInput {
                down(Offset(195f, handleY))
                moveBy(Offset(0f, 60f))
            }
            waitForIdle()
            val slop = frame().top() - 252
            assertTrue(slop in 30..60, "down by the drag, past the slop: $slop")
            onNodeWithTag("box").performTouchInput { moveBy(Offset(0f, 20f)) }
            waitForIdle()
            assertEquals(slop + 20, frame().top() - 252, "at the finger")
            onNodeWithTag("box").performTouchInput { moveBy(Offset(0f, -400f)) }
            waitForIdle()
            assertEquals(252, frame().top(), "not above its stop")
            onNodeWithTag("box").performTouchInput { up() }
        }

    @Test
    fun `let go under half it settles back in 120 ms on the hard stop, and nothing is asked`() =
        runComposeUiTest {
            var dismissed = 0
            mainClock.autoAdvance = false
            sheet({ true }, reduce = false, onDismiss = { dismissed++ })
            mainClock.advanceTimeBy(500)
            onNodeWithTag("box").performTouchInput {
                down(Offset(195f, handleY))
                moveBy(Offset(0f, 40f))
                moveBy(Offset(0f, 40f))
            }
            mainClock.advanceTimeBy(100)
            val held = frame().top()
            assertTrue(held in 290..332, "held: $held")
            onNodeWithTag("box").performTouchInput {
                advanceEventTime(1000)
                up()
            }
            val tops =
                (0 until 12).map {
                    mainClock.advanceTimeByFrame()
                    frame().top()
                }
            val fits =
                (0..3).any { start ->
                    tops.withIndex().all { (i, got) ->
                        val p = hardStop((((i - start) * 16.0) / 120).coerceIn(0.0, 1.0))
                        abs(got - (held + (252 - held) * p)) <= 1.5
                    }
                }
            assertTrue(fits, "settling, its top per frame from $held: $tops")
            assertEquals(0, dismissed)
        }

    @Test
    fun `let go over half it asks to go, and goes when told`() =
        runComposeUiTest {
            var open by mutableStateOf(true)
            var dismissed = 0
            sheet({ open }, onDismiss = {
                dismissed++
                open = false
            })
            waitForIdle()
            onNodeWithTag("box").performTouchInput {
                down(Offset(195f, handleY))
                moveBy(Offset(0f, 200f))
                advanceEventTime(1000)
                up()
            }
            waitForIdle()
            assertEquals(1, dismissed)
            onNodeWithTag("content").assertDoesNotExist()
        }

    @Test
    fun `let go over half and refused, it settles back`() =
        runComposeUiTest {
            var dismissed = 0
            sheet({ true }, onDismiss = { dismissed++ })
            waitForIdle()
            onNodeWithTag("box").performTouchInput {
                down(Offset(195f, handleY))
                moveBy(Offset(0f, 200f))
                advanceEventTime(1000)
                up()
            }
            waitForIdle()
            assertEquals(1, dismissed)
            assertEquals(252, frame().top())
        }

    @Test
    fun `flicked it asks to go, from however little`() =
        runComposeUiTest {
            var dismissed = 0
            sheet({ true }, onDismiss = { dismissed++ })
            waitForIdle()
            onNodeWithTag("box").performTouchInput {
                swipeDown(startY = handleY, endY = handleY + 60f, durationMillis = 50)
            }
            waitForIdle()
            assertEquals(1, dismissed)
        }

    @Test
    fun `a click on the screen, Escape and the reader's dismiss each ask it to go`() =
        runComposeUiTest {
            var dismissed = 0
            sheet({ true }, onDismiss = { dismissed++ })
            waitForIdle()
            onNodeWithTag("box").performMouseInput {
                moveTo(Offset(195f, 100f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(1, dismissed)
            onNodeWithTag("box").performKeyInput { pressKey(Key.Escape) }
            waitForIdle()
            assertEquals(2, dismissed)
            onNode(
                SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss),
            ).performSemanticsAction(SemanticsActions.Dismiss)
            waitForIdle()
            assertEquals(3, dismissed)
        }

    @Test
    fun `a click on the sheet stays on it`() =
        runComposeUiTest {
            var dismissed = 0
            sheet({ true }, onDismiss = { dismissed++ })
            waitForIdle()
            onNodeWithTag("box").performMouseInput {
                moveTo(Offset(195f, 400f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(0, dismissed)
        }

    @Test
    fun `it is read as a dialog, under its title`() =
        runComposeUiTest {
            sheet({ true }, title = "Действия")
            waitForIdle()
            onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.IsDialog))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Действия"))
            onNodeWithText("Ч").assertIsDisplayed()
        }

    @Test
    fun `a click on the handle does nothing`() =
        runComposeUiTest {
            var dismissed = 0
            sheet({ true }, onDismiss = { dismissed++ })
            waitForIdle()
            onNodeWithTag("box").performMouseInput {
                moveTo(Offset(195f, handleY))
                press()
                release()
            }
            waitForIdle()
            assertEquals(0, dismissed)
            assertEquals(252, frame().top())
        }
}
