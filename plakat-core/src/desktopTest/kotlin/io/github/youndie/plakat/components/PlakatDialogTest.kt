package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
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

/**
 * B-28: `announce`. On a 390 × 420 white stage: the screen's diagonal edge travels 390 + 120 in 320 ms, linear,
 * leaning back 120 over the height, and the sheet rides from 320 above to its top of 96, on the same clock.
 * Under reduced motion both fade, 100 ms.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatDialogTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.stage(
        visible: () -> Boolean,
        reduce: Boolean = false,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        dismissed: MutableList<Int> = mutableListOf(),
        under: () -> Unit = {},
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.size(390.dp, 420.dp).background(Color.White).testTag("s")) {
                        Box(Modifier.fillMaxSize().clickable(onClick = under))
                        PlakatDialog(
                            visible(),
                            "УДАЛИТЬ",
                            { dismissed += 1 },
                            text = "Вернуть можно.",
                        ) { PlakatDialogAction("ОТМЕНА", {}) }
                    }
                }
            }
        }
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("s").captureToImage().toPixelMap()

    /** The sheet's title plate: its top row on column 200, past the cut; null while above the stage. */
    private fun PixelMap.plateTop() = (0 until height).firstOrNull { y -> this[200, y].let { it.red - it.green > .3f } }

    /** The screen's reach on row [y], under the sheet: the last column with its ink. */
    private fun PixelMap.screenReach(y: Int) = (0 until width).lastOrNull { x -> this[x, y].red < .5f }

    @Test
    fun `it opens on one clock of 320 ms, the screen by a diagonal wipe and the sheet riding down`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            stage({ true }, heard = heard)
            val frames =
                (0 until 24).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val fits =
                (0..2).any { start ->
                    frames.withIndex().all { (i, px) ->
                        val p = ((i - start) * 16.0 / 320).coerceIn(0.0, 1.0)
                        // Row 417 is a row of the dots' four middle pixels; its edge is the wipe's less 120 × 417.5 / 420.
                        val edge = p * 510 - 120 * 417.5 / 420
                        val reach = px.screenReach(417)
                        val top = 96 - 320 * (1 - p)
                        val screenAt =
                            if (edge < 1) {
                                reach == null
                            } else {
                                reach != null && abs(reach - edge.coerceAtMost(389.0)) <= 4
                            }
                        val plate = px.plateTop()
                        val rideAt =
                            if (top + 53 <
                                0
                            ) {
                                plate == null
                            } else {
                                plate != null && abs(plate - top.coerceAtLeast(0.0)) <= 1
                            }
                        screenAt && rideAt
                    }
                }
            assertTrue(
                fits,
                "plate top per frame: ${frames.map { it.plateTop() }}; reach ${frames.map { it.screenReach(417) }}",
            )
            assertEquals(96, frames.last().plateTop())
            assertEquals(listOf(PlakatEvent.Announce), heard)
        }

    @Test
    fun `it closes the same way and is gone after`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var visible by mutableStateOf(true)
            stage({ visible }, heard = heard)
            mainClock.advanceTimeBy(500)
            assertEquals(96, frame().plateTop())
            visible = false
            val frames =
                (0 until 24).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val fits =
                (0..2).any { start ->
                    frames.withIndex().all { (i, px) ->
                        val p = 1 - ((i - start) * 16.0 / 320).coerceIn(0.0, 1.0)
                        val top = 96 - 320 * (1 - p)
                        val plate = px.plateTop()
                        if (top + 53 < 0) plate == null else plate != null && abs(plate - top.coerceAtLeast(0.0)) <= 1
                    }
                }
            assertTrue(fits, "plate top per frame: ${frames.map { it.plateTop() }}")
            // Nothing of it left: all white, and its words out of the tree.
            assertEquals(null, frames.last().screenReach(417))
            onNodeWithText("УДАЛИТЬ").assertDoesNotExist()
            assertEquals(listOf(PlakatEvent.Announce, PlakatEvent.Announce), heard)
        }

    @Test
    fun `under reduced motion it fades in and out over 100 ms at its place`() =
        runComposeUiTest {
            var visible by mutableStateOf(true)
            stage({ visible }, reduce = true)

            fun alpha(px: PixelMap) = ((1f - px[200, 100].green) / (1f - paper.red.green)).toDouble()
            val fadeIn =
                (0 until 9).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            assertTrue(
                (0..2).any { start ->
                    fadeIn.withIndex().all { (i, px) ->
                        abs(alpha(px) - ((i - start) * 16.0 / 100).coerceIn(0.0, 1.0)) <
                            .03
                    }
                },
                "in: ${fadeIn.map { alpha(it) }}",
            )
            // At its place all along, the whole screen at once rather than by a wipe.
            for (px in fadeIn.filter { alpha(it) > .7 }) {
                assertEquals(96, px.plateTop())
                assertTrue(px.screenReach(417)!! > 385)
            }
            visible = false
            val fadeOut =
                (0 until 9).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            assertTrue(
                (0..2).any { start ->
                    fadeOut.withIndex().all { (i, px) ->
                        abs(alpha(px) - (1 - ((i - start) * 16.0 / 100).coerceIn(0.0, 1.0))) <
                            .03
                    }
                },
                "out: ${fadeOut.map { alpha(it) }}",
            )
        }

    @Test
    fun `a click on the screen or Escape asks to dismiss, the sheet keeps its clicks and nothing under it is reached`() =
        runComposeUiTest {
            val dismissed = mutableListOf<Int>()
            var under = 0
            stage({ true }, reduce = true, dismissed = dismissed, under = { under++ })
            mainClock.autoAdvance = true
            waitForIdle()
            onNodeWithTag("s").performTouchInput { click(Offset(195f, 10f)) }
            waitForIdle()
            assertEquals(1, dismissed.size, "the screen")
            assertEquals(0, under, "what is under the screen")
            onNodeWithTag("s").performTouchInput { click(Offset(195f, 180f)) }
            waitForIdle()
            assertEquals(1, dismissed.size, "the sheet")
            onNodeWithTag("s").performKeyInput { pressKey(Key.Escape) }
            waitForIdle()
            assertEquals(2, dismissed.size, "Escape")
        }

    @Test
    fun `closed from the start it draws and says nothing`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            stage({ false }, heard = heard)
            mainClock.advanceTimeBy(500)
            assertEquals(null, frame().screenReach(417))
            onNodeWithText("УДАЛИТЬ").assertDoesNotExist()
            assertEquals(emptyList(), heard)
        }

    @Test
    fun `it is a dialog with a heading, its words and an action of 48`() =
        runComposeUiTest {
            var clicked = 0
            setContent {
                PlakatTheme(reduceMotion = true) {
                    PlakatDialog(true, "УДАЛИТЬ", {}, text = "Вернуть можно.") {
                        PlakatDialogAction("ОТМЕНА", { clicked++ })
                        PlakatDialogAction("СТЕРЕТЬ", {}, danger = true)
                    }
                }
            }
            onNodeWithText("УДАЛИТЬ").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            onNodeWithText("Вернуть можно.").assertIsDisplayed()
            onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.IsDialog)).assert(
                SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "УДАЛИТЬ"),
            )
            onNodeWithText("СТЕРЕТЬ").assertHeightIsEqualTo(48.dp)
            onNodeWithText("ОТМЕНА").assertHeightIsEqualTo(48.dp).performClick()
            assertEquals(1, clicked)
        }
}
