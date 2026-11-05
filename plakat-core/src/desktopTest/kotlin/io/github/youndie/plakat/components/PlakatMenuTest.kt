package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-45: designed here. The frame by pixels on the panel, the rest through the real popup in the test's window of 1024 × 768, a popup being a second root over all of it. */
@OptIn(ExperimentalTestApi::class)
class PlakatMenuTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.menu(
        expanded: () -> Boolean,
        anchorX: Dp = 40.dp,
        anchorY: Dp = 40.dp,
        offset: DpOffset = DpOffset.Zero,
        reduce: Boolean = true,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        onDismiss: () -> Unit = {},
        onItem: (Int) -> Unit = {},
        count: Int = 3,
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.size(1024.dp, 768.dp).background(Color.White).testTag("window")) {
                        Box(Modifier.offset(anchorX, anchorY).size(100.dp, 48.dp).testTag("anchor")) {
                            PlakatMenu(expanded(), onDismiss, Modifier.testTag("menu"), offset) {
                                for (i in 0 until count) {
                                    PlakatMenuItem("П $i", { onItem(i) }, divider = i < count - 1)
                                }
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.panel(content: @Composable () -> Unit) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = true) {
                    Box(Modifier.size(500.dp, 300.dp).background(Color.White)) { content() }
                }
            }
        }
        waitForIdle()
    }

    @Test
    fun `its frame is a bar of 8 about the paper, closing over the last row`() =
        runComposeUiTest {
            panel {
                PlakatMenuPanel(Modifier.testTag("p"), { 1f }) {
                    PlakatMenuItem("Сохранить", {})
                    PlakatMenuItem("В архив", {}, divider = false)
                }
            }
            // 8 + 56 + a hair + 56 + 8.
            onNodeWithTag("p").assertHeightIsEqualTo(129.dp)
            val px = onNodeWithTag("p").captureToImage().toPixelMap()
            val w = px.width
            assertEquals(paper.ink, px[0, 60])
            assertEquals(paper.ink, px[7, 60])
            assertEquals(paper.paper, px[8, 60])
            assertEquals(paper.ink, px[w - 8, 60])
            assertEquals(paper.paper, px[w - 9, 60])
            assertEquals(paper.ink, px[100, 7])
            assertEquals(paper.paper, px[100, 8])
            assertEquals(paper.paper, px[100, 120], "the last row's paper to the frame, no hair")
            assertEquals(paper.ink, px[100, 121])
            assertEquals(paper.ink, px[100, 128])
            assertEquals(paper.ink, px[w / 2 + 40, 64], "the first row's hair")
            assertEquals(paper.paper, px[w / 2 + 40, 63])
        }

    @Test
    fun `it is 160 wide at least, 320 at most, and its text's width between`() =
        runComposeUiTest {
            var text by mutableStateOf("Да")
            panel {
                PlakatMenuPanel(Modifier.testTag("p"), { 1f }) { PlakatMenuItem(text, {}, divider = false) }
            }
            onNodeWithTag("p").assertWidthIsEqualTo(160.dp)
            text = "Поиск"
            waitForIdle()
            val fits = onNodeWithTag("p").getBoundsInRoot()
            assertEquals(160.dp, fits.right - fits.left, "still")
            text = "Отправить копию коллегам"
            waitForIdle()
            val grown = onNodeWithTag("p").getBoundsInRoot()
            assertTrue(grown.right - grown.left in 200.dp..319.dp, "${grown.right - grown.left}")
            text = "Отправить копию коллегам по редакции и всем остальным подписчикам издания"
            waitForIdle()
            onNodeWithTag("p").assertWidthIsEqualTo(320.dp)
        }

    @Test
    fun `it stands under its anchor at its left edge, moved by its offset`() =
        runComposeUiTest {
            menu({ true }, offset = DpOffset(8.dp, 4.dp))
            val px = onNodeWithTag("window").captureToImage().toPixelMap()
            assertEquals(Color.White, px[47, 92])
            assertEquals(paper.ink, px[48, 92], "the frame's corner at 40 + 8, 40 + 48 + 4")
            assertEquals(Color.White, px[48, 91])
        }

    @Test
    fun `it goes over its anchor where it does not fit under, and stays in the window`() =
        runComposeUiTest {
            menu({ true }, anchorX = 980.dp, anchorY = 700.dp)
            val px = onNodeWithTag("window").captureToImage().toPixelMap()
            // Height 8 + 57 × 2 + 56 + 8 = 186, so its foot at the anchor's top, 700; width 160, its right at the window's.
            assertEquals(paper.ink, px[900, 699], "its foot")
            assertEquals(Color.White, px[900, 700])
            assertEquals(paper.ink, px[900, 514], "its top")
            assertEquals(Color.White, px[900, 513])
            assertEquals(paper.ink, px[864, 600], "its left")
            assertEquals(Color.White, px[863, 600])
            assertEquals(paper.ink, px[1023, 600], "its right on the window's edge")
        }

    /** How far down the panel at 40, 88 shows: the last row of its frame's left column. */
    private fun PixelMap.reach() = (88 until height).lastOrNull { this[42, it] == paper.ink }?.let { it - 88 + 1 } ?: 0

    @Test
    fun `it opens by a wipe from the top, linear over 120 ms, and closes the same way`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var open by mutableStateOf(false)
            mainClock.autoAdvance = false
            menu({ open }, reduce = false, heard = heard)
            mainClock.advanceTimeBy(100)
            assertTrue(heard.isEmpty(), "closed from the start, it says nothing: $heard")
            onNodeWithTag("menu").assertDoesNotExist()
            open = true
            val opening =
                (0 until 12).map {
                    mainClock.advanceTimeByFrame()
                    onNodeWithTag("window").captureToImage().toPixelMap().reach()
                }

            fun fits(
                reach: List<Int>,
                edge: (Double) -> Double,
            ) = (0..3).any { start ->
                reach.withIndex().all { (i, got) ->
                    abs(got - 186 * edge((((i - start) * 16.0) / 120).coerceIn(0.0, 1.0))) <= 2
                }
            }
            assertTrue(fits(opening) { it }, "opening, the rows it reaches: $opening")
            assertEquals(186, opening.last())
            assertEquals(listOf(PlakatEvent.Wipe), heard)
            open = false
            val closing =
                (0 until 12).map {
                    mainClock.advanceTimeByFrame()
                    onNodeWithTag("window").captureToImage().toPixelMap().reach()
                }
            assertTrue(fits(closing) { 1 - it }, "closing: $closing")
            assertEquals(0, closing.last())
            assertEquals(listOf(PlakatEvent.Wipe, PlakatEvent.Wipe), heard)
            mainClock.advanceTimeByFrame()
            onNodeWithTag("menu").assertDoesNotExist()
        }

    @Test
    fun `under reduced motion it is there and gone at once`() =
        runComposeUiTest {
            var open by mutableStateOf(false)
            mainClock.autoAdvance = false
            menu({ open }, reduce = true)
            mainClock.advanceTimeBy(100)
            open = true
            repeat(2) { mainClock.advanceTimeByFrame() }
            mainClock.advanceTimeByFrame()
            assertEquals(186, onNodeWithTag("window").captureToImage().toPixelMap().reach())
            open = false
            repeat(3) { mainClock.advanceTimeByFrame() }
            onNodeWithTag("menu").assertDoesNotExist()
        }

    @Test
    fun `a click on an item calls it and the menu stays until it is closed`() =
        runComposeUiTest {
            var chosen = -1
            var dismissed = 0
            menu({ true }, onItem = { chosen = it }, onDismiss = { dismissed++ })
            onNodeWithText("П 1").performClick()
            waitForIdle()
            assertEquals(1, chosen)
            assertEquals(0, dismissed)
            onNodeWithText("П 1").assertIsDisplayed()
        }

    @Test
    fun `a click outside it dismisses it`() =
        runComposeUiTest {
            var dismissed = 0
            menu({ true }, onDismiss = { dismissed++ })
            onNodeWithTag("menu").performMouseInput {
                moveTo(Offset(600f, 400f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(1, dismissed)
        }

    @Test
    fun `Escape dismisses it`() =
        runComposeUiTest {
            var dismissed = 0
            menu({ true }, onDismiss = { dismissed++ })
            onNodeWithTag("menu").performKeyInput { pressKey(Key.Escape) }
            waitForIdle()
            assertEquals(1, dismissed)
        }

    @Test
    fun `the arrows move between its items and Enter takes one`() =
        runComposeUiTest {
            var chosen = -1
            menu({ true }, onItem = { chosen = it })
            onNodeWithTag("menu").performKeyInput {
                pressKey(Key.DirectionDown)
                pressKey(Key.DirectionDown)
            }
            waitForIdle()
            onNodeWithTag("menu").performKeyInput { pressKey(Key.Enter) }
            waitForIdle()
            assertEquals(1, chosen, "the second, after two")
            onNodeWithTag("menu").performKeyInput {
                pressKey(Key.DirectionUp)
                pressKey(Key.Enter)
            }
            waitForIdle()
            assertEquals(0, chosen, "and back up")
        }

    @Test
    fun `a list too long for the window scrolls in it`() =
        runComposeUiTest {
            menu({ true }, count = 40)
            onNodeWithTag("menu").assertIsDisplayed()
            val bounds = onNodeWithTag("menu").getBoundsInRoot()
            assertTrue(bounds.bottom <= 768.dp, "inside the window: ${bounds.bottom}")
            onNodeWithText("П 0").assertIsDisplayed()
            onNodeWithText("П 39").performScrollTo().assertIsDisplayed()
            onNodeWithText("П 0").assertIsNotDisplayed()
        }

    @Test
    fun `pressed an item inverts, as a list row does`() =
        runComposeUiTest {
            menu({ true })
            onNodeWithText("П 0").performMouseInput {
                moveTo(center)
                press()
            }
            waitForIdle()
            val px = onNodeWithTag("window").captureToImage().toPixelMap()
            assertEquals(paper.ink, px[150, 110], "the row's ground")
            onNodeWithText("П 0").performMouseInput { release() }
        }
}
