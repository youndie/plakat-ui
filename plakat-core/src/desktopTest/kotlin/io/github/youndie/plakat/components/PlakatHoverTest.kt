package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** B-39: a pointer over a control turns its frame over in one frame, a finger never does, and a press reads over it. */
@OptIn(ExperimentalTestApi::class)
class PlakatHoverTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.show(content: @Composable () -> Unit) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper) {
                    Box(Modifier.testTag("s").background(Color.White).padding(8.dp)) { content() }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("s").captureToImage().toPixelMap()

    private fun ComposeUiTest.pointerIn(tag: String) {
        onNodeWithTag(tag).performMouseInput {
            enter(center)
            moveTo(center)
        }
        mainClock.advanceTimeByFrame()
    }

    private fun ComposeUiTest.pointerOut(tag: String) {
        onNodeWithTag(tag).performMouseInput { exit() }
        mainClock.advanceTimeByFrame()
    }

    @Test
    fun `a filled plate gets the ink frame in one frame and loses it`() =
        runComposeUiTest {
            show { PlakatButton("ЧИТАТЬ", {}, Modifier.testTag("c")) }
            // Along the foot, inside the plate: 8 of padding, the button's last rows.
            val at = { px: PixelMap -> px[60, px.height - 8 - 2] }
            assertEquals(paper.red, at(frame()))
            pointerIn("c")
            assertEquals(paper.ink, at(frame()))
            pointerOut("c")
            assertEquals(paper.red, at(frame()))
        }

    @Test
    fun `an outlined frame fills with concrete, not with the press's ink`() =
        runComposeUiTest {
            show { PlakatOutlinedButton("ЧИТАТЬ", {}, Modifier.testTag("c")) }
            assertEquals(Color.White, frame()[16, 16])
            pointerIn("c")
            assertEquals(paper.concrete, frame()[16, 16])
        }

    @Test
    fun `a text button's rule becomes the bar, with its word left where it was`() =
        runComposeUiTest {
            show { PlakatTextButton("ЧИТАТЬ", {}, Modifier.testTag("c")) }

            fun PixelMap.inkRows() = (0 until height).count { y -> this[width / 2, y] == paper.ink }
            val before = frame()
            val rest = before.inkRows()
            pointerIn("c")
            val after = frame()
            assertTrue(after.inkRows() - rest in 4..6, "from a rule of 3 to a bar of 8: $rest, ${after.inkRows()}")
            for (x in 20 until after.width - 20) {
                assertEquals(before[x, 20], after[x, 20], "the word's row at $x")
            }
        }

    @Test
    fun `a danger button gets the ink frame round its hatch`() =
        runComposeUiTest {
            show { PlakatDangerButton("СТЕРЕТЬ", {}, Modifier.testTag("c")) }
            val edge = { px: PixelMap -> px[9, 30] }
            assertNotEquals(paper.ink, edge(frame()))
            pointerIn("c")
            assertEquals(paper.ink, edge(frame()))
        }

    @Test
    fun `an icon's square fills, and a selected one gets a frame`() =
        runComposeUiTest {
            show {
                Row {
                    PlakatIconButton(PlakatIcons.Home, "a", {}, Modifier.testTag("a"))
                    PlakatIconButton(PlakatIcons.Home, "b", {}, Modifier.testTag("b"), selected = true)
                }
            }
            assertEquals(Color.White, frame()[10, 10])
            pointerIn("a")
            assertEquals(paper.concrete, frame()[10, 10])
            pointerOut("a")
            assertEquals(paper.red, frame()[57, 10])
            pointerIn("b")
            assertEquals(paper.ink, frame()[57, 10])
        }

    @Test
    fun `a chip fills under its frame, and a selected one gets one that shows on ink`() =
        runComposeUiTest {
            show {
                Row {
                    PlakatChip("ШРИФТ", false, {}, Modifier.testTag("a"))
                    PlakatChip("ШРИФТ", true, {}, Modifier.testTag("b"))
                }
            }
            // The plate of 32 in its target of 48: 8 of padding, then 8 above it.
            assertEquals(Color.White, frame()[14, 22])
            pointerIn("a")
            assertEquals(paper.concrete, frame()[14, 22])
            pointerOut("a")
            val second = onNodeWithTag("b").fetchSemanticsNode().boundsInRoot
            val x = second.left.toInt() + 1
            assertEquals(paper.ink, frame()[x, 22 - 4])
            pointerIn("b")
            assertEquals(paper.concrete, frame()[x, 22 - 4])
        }

    @Test
    fun `a row's hair becomes a rule`() =
        runComposeUiTest {
            show { PlakatListItem("Шрифт", Modifier.testTag("c"), onClick = {}) }
            val px0 = frame()
            val foot = px0.height - 8
            assertEquals(paper.ink, px0[100, foot - 1])
            assertEquals(Color.White, px0[100, foot - 2])
            pointerIn("c")
            val px1 = frame()
            assertEquals(paper.ink, px1[100, foot - 3])
        }

    @Test
    fun `a finger never hovers`() =
        runComposeUiTest {
            show { PlakatOutlinedButton("ЧИТАТЬ", {}, Modifier.testTag("c")) }
            onNodeWithTag("c").performTouchInput {
                down(center)
                up()
            }
            mainClock.advanceTimeBy(200)
            assertEquals(Color.White, frame()[16, 16])
        }

    @Test
    fun `a disabled control does not answer the pointer`() =
        runComposeUiTest {
            show { PlakatOutlinedButton("ЧИТАТЬ", {}, Modifier.testTag("c"), enabled = false) }
            val before = frame()[16, 16]
            pointerIn("c")
            assertEquals(before, frame()[16, 16])
        }

    @Test
    fun `a press reads over the hover`() =
        runComposeUiTest {
            show { PlakatOutlinedButton("ЧИТАТЬ", {}, Modifier.testTag("c")) }
            pointerIn("c")
            onNodeWithTag("c").performMouseInput { press() }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(paper.ink, frame()[16, 16])
        }
}
