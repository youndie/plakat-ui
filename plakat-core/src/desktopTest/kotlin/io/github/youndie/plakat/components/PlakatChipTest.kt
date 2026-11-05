package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals

/** B-22: `stamp`, the fill and the wedge in one frame, under reduced motion too; a 32 dp plate in a 48 dp target. */
@OptIn(ExperimentalTestApi::class)
class PlakatChipTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.chip(
        reduce: Boolean,
        heard: MutableList<PlakatEvent>,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    var on by remember { mutableStateOf(false) }
                    Box(Modifier.background(Color.White)) {
                        PlakatChip("шрифт", on, { on = !on }, Modifier.testTag("c"))
                    }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("c").captureToImage().toPixelMap()

    /** Inside the plate (row 8 is its top, the frame 3 thick): a pixel past the frame, and one of the wedge (12, 19). */
    private fun PixelMap.fill() = this[5, 13]

    private fun PixelMap.wedge() = this[20, 21]

    private fun oneFrame(reduce: Boolean) =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            chip(reduce, heard)
            assertEquals(Color.White, frame().fill())
            assertEquals(Color.White, frame().wedge())
            // The frame at rest: ink at the plate's top row, white in the target's strip above it.
            assertEquals(paper.ink, frame()[30, 8])
            assertEquals(Color.White, frame()[30, 7])
            onNodeWithTag("c").performClick()
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val on = frame()
            assertEquals(paper.ink, on.fill())
            assertEquals(paper.red, on.wedge())
            assertEquals(listOf(PlakatEvent.Stamp), heard)
            onNodeWithTag("c").assertIsSelected()
            // Let go of, it is back in one frame too, and says nothing. This harness's `performClick` is a mouse's, and
            // leaves it over the chip, which shows its hover (B-39) until it moves away.
            onNodeWithTag("c").performClick()
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(paper.concrete, frame().fill())
            onNodeWithTag("c").performMouseInput { moveTo(Offset(-100f, -100f)) }
            mainClock.advanceTimeByFrame()
            assertEquals(Color.White, frame().fill())
            assertEquals(listOf(PlakatEvent.Stamp), heard)
            onNodeWithTag("c").assertIsNotSelected()
        }

    @Test
    fun `selected it fills and takes its wedge within a frame and says stamp once`() = oneFrame(reduce = false)

    @Test
    fun `under reduced motion it is the same`() = oneFrame(reduce = true)

    @Test
    fun `it prints its word in capitals in a 48 dp target at least 48 wide`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    PlakatChip("шрифт", false, {}, Modifier.testTag("c"))
                    PlakatChip("я", false, {}, Modifier.testTag("short"))
                    PlakatChip("нет", false, {}, Modifier.testTag("off"), enabled = false)
                }
            }
            onNodeWithText("ШРИФТ").assertIsDisplayed()
            onNodeWithTag("c")
                .assertHeightIsEqualTo(48.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            onNodeWithTag("short").assertWidthIsEqualTo(48.dp)
            onNodeWithTag("off").assertIsNotEnabled()
        }
}
