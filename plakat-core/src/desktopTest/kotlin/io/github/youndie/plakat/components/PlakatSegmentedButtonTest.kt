package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-49: the Demo page's control, 155 wide here: 77 for БУМАГА and 78 for ЧЁРНАЯ, 40 drawn from row 4 of its 48. */
@OptIn(ExperimentalTestApi::class)
class PlakatSegmentedButtonTest {
    private val paper = PlakatPalettes.Paper
    private val words = listOf("БУМАГА", "ЧЁРНАЯ")

    private fun ComposeUiTest.segmented(
        selected: () -> Int,
        onSelect: (Int) -> Unit = {},
        enabled: Boolean = true,
        reduce: Boolean = true,
        width: Dp? = null,
        density: Float = 1f,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        at: Float? = null,
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.size(400.dp, 48.dp).background(paper.paper).testTag("box")) {
                        PlakatSegmentedButtonFrame(
                            words,
                            selected(),
                            onSelect,
                            (if (width != null) Modifier.width(width) else Modifier).testTag("s"),
                            enabled,
                            forceAt = at,
                        )
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("box").captureToImage().toPixelMap()

    /** On row 8, under the frame's rule and over the words: the columns of ink inside the frame's sides. */
    private fun PixelMap.inkColumns(right: Int = 155) = (3 until right - 3).filter { this[it, 8] == paper.ink }

    private val PixelMap.fillLeft get() = inkColumns().firstOrNull() ?: 3
    private val PixelMap.fillRight get() = (inkColumns().lastOrNull() ?: 2) + 1

    @Test
    fun `one frame of 3, 40 high in a target of 48, the selected filled with ink and its word paper`() =
        runComposeUiTest {
            segmented({ 0 })
            val px = frame()
            assertEquals(paper.paper, px[120, 3], "over the frame")
            assertEquals(paper.ink, px[120, 4], "its top")
            assertEquals(paper.ink, px[120, 6])
            assertEquals(paper.paper, px[120, 7])
            assertEquals(paper.paper, px[120, 40])
            assertEquals(paper.ink, px[120, 41], "its foot")
            assertEquals(paper.ink, px[120, 43])
            assertEquals(paper.paper, px[120, 44])
            assertEquals(paper.ink, px[152, 20], "its right side")
            assertEquals(paper.ink, px[154, 20])
            assertEquals(paper.paper, px[155, 20])
            assertEquals(paper.paper, px[151, 20])
            // The fill to the end of its word and 12 past.
            assertEquals(paper.ink, px[76, 8])
            assertEquals(paper.paper, px[77, 8])
            assertTrue(
                (12 until 64).any { x ->
                    (16 until 32).any { y -> px[x, y] == paper.paper }
                },
                "its word in paper",
            )
            assertTrue(
                (88 until 142).any { x ->
                    (16 until 32).any { y -> px[x, y] == paper.ink }
                },
                "the other's in ink",
            )
        }

    @Test
    fun `given more width the segments share it`() =
        runComposeUiTest {
            segmented({ 0 }, width = 255.dp)
            val px = frame()
            assertEquals(paper.ink, px[126, 8], "77 and half of the 100 over")
            assertEquals(paper.paper, px[127, 8])
            assertEquals(paper.ink, px[254, 20], "the frame to its width")
        }

    @Test
    fun `its fill rides linearly in 200 ms, both edges, and rail is heard`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var place by mutableIntStateOf(0)
            mainClock.autoAdvance = false
            segmented({ place }, reduce = false, heard = heard)
            mainClock.advanceTimeBy(100)
            assertTrue(heard.isEmpty(), "$heard")
            place = 1
            val frames =
                (0 until 16).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val lefts = frames.map { it.fillLeft }
            val rights = frames.map { it.fillRight }
            val fits =
                (0..3).any { start ->
                    frames.indices.all { i ->
                        val p = (((i - start) * 16.0) / 200).coerceIn(0.0, 1.0)
                        // The left from the frame's inside at 3, while the fill has not left it, to 77; the right to the frame.
                        val left = (77 * p).coerceAtLeast(3.0)
                        val right = (77 + 78 * p).coerceAtMost(152.0)
                        abs(lefts[i] - left) <= 1.5 && abs(rights[i] - right) <= 1.5
                    }
                }
            assertTrue(fits, "the fill's edges per frame: $lefts, $rights")
            assertEquals(listOf(PlakatEvent.Rail), heard)
        }

    @Test
    fun `under reduced motion it is there at once`() =
        runComposeUiTest {
            var place by mutableIntStateOf(0)
            mainClock.autoAdvance = false
            segmented({ place }, reduce = true)
            mainClock.advanceTimeBy(100)
            place = 1
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(77, frame().fillLeft)
        }

    @Test
    fun `a word inverts just where the fill has reached it`() =
        runComposeUiTest {
            // At a half the fill stands from 39 to 116: half of each word under it.
            segmented({ 1 }, at = .5f)
            val px = frame()
            val rows = 16 until 32

            fun count(
                xs: IntRange,
                color: Color,
            ) = xs.sumOf { x -> rows.count { y -> px[x, y] == color } }
            assertTrue(count(13..37, paper.ink) > 20, "the first word's left part in ink, out of the fill")
            assertTrue(count(41..63, paper.paper) > 20, "and its right part in paper")
            assertTrue(count(90..114, paper.paper) > 20, "the second's left part in paper, in the fill")
            assertTrue(count(118..140, paper.ink) > 20, "and its right part in ink")
        }

    @Test
    fun `a click selects the segment under it`() =
        runComposeUiTest {
            var chosen = -1
            segmented({ 0 }, { chosen = it })
            onNodeWithContentDescription("ЧЁРНАЯ").performClick()
            waitForIdle()
            assertEquals(1, chosen)
            onNodeWithContentDescription("БУМАГА").assertIsSelected()
            onNodeWithContentDescription("ЧЁРНАЯ").assertIsNotSelected()
            onNodeWithContentDescription("ЧЁРНАЯ")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
        }

    @Test
    fun `its targets are 48 high, as wide as their segments`() =
        runComposeUiTest {
            segmented({ 0 })
            val bounds = onNodeWithContentDescription("ЧЁРНАЯ").fetchSemanticsNode().boundsInRoot
            assertEquals(48f, bounds.height)
            assertEquals(77f, bounds.left, 1f)
            assertEquals(155f, bounds.right, 1f)
        }

    @Test
    fun `pressed a segment inverts, off or on`() =
        runComposeUiTest {
            segmented({ 0 })
            onNodeWithContentDescription("ЧЁРНАЯ").performMouseInput {
                moveTo(center)
                press()
            }
            waitForIdle()
            assertEquals(paper.ink, frame()[120, 8], "off, its ground ink")
            onNodeWithContentDescription("ЧЁРНАЯ").performMouseInput { release() }
            waitForIdle()
            assertEquals(paper.paper, frame()[120, 8], "and back")
            onNodeWithContentDescription("БУМАГА").performMouseInput {
                moveTo(center)
                press()
            }
            waitForIdle()
            assertEquals(paper.paper, frame()[40, 8], "on, its ground paper")
            onNodeWithContentDescription("БУМАГА").performMouseInput { release() }
        }

    @Test
    fun `disabled its frame is a muted hair, its fill muted, and it takes nothing`() =
        runComposeUiTest {
            var chosen = -1
            segmented({ 0 }, { chosen = it }, enabled = false)
            val px = frame()
            assertEquals(paper.inkMuted, px[120, 4], "a hair")
            assertEquals(paper.paper, px[120, 5])
            assertEquals(paper.inkMuted, px[40, 8], "the fill")
            onNodeWithContentDescription("ЧЁРНАЯ").assertIsNotEnabled()
            onNodeWithContentDescription("ЧЁРНАЯ").performClick()
            waitForIdle()
            assertEquals(-1, chosen)
        }

    @Test
    fun `at a density of 2 it is drawn at the same dp`() =
        runComposeUiTest {
            segmented({ 0 }, density = 2f)
            val px = frame()
            assertEquals(96, px.height)
            assertEquals(paper.paper, px[240, 7])
            assertEquals(paper.ink, px[240, 8], "its top, 2 × 4")
            assertEquals(paper.ink, px[240, 13])
            assertEquals(paper.paper, px[240, 14], "a rule of 2 × 3")
        }
}
