package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** B-62: long text scrolls between the title and the actions, which stay on screen. */
@OptIn(ExperimentalTestApi::class)
class PlakatDialogLongTextTest {
    private val long = (1..60).joinToString(" ") { "A long sentence number $it of the announcement." }

    private fun ComposeUiTest.dialog(
        text: String = long,
        high: Int = 500,
    ) {
        mainClock.autoAdvance = false
        setContent {
            PlakatTheme(reduceMotion = true) {
                Box(Modifier.size(390.dp, high.dp).testTag("window")) {
                    PlakatDialog(true, "TITLE", {}, Modifier.fillMaxSize(), text = text) {
                        PlakatDialogAction("LATER", {})
                        PlakatDialogAction("AGREE", {})
                    }
                }
            }
        }
        mainClock.advanceTimeBy(500)
    }

    /** How far the text has scrolled, from the semantics of the scroll around it. */
    private fun ComposeUiTest.scrolled(): Float =
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            .fetchSemanticsNodes()
            .single()
            .config[SemanticsProperties.VerticalScrollAxisRange]
            .value()

    @Test
    fun `the title and the actions stay on screen, in that order, with the text between`() =
        runComposeUiTest {
            dialog()
            val window = onNodeWithTag("window").getBoundsInRoot()
            val title = onNodeWithText("TITLE").assertIsDisplayed().getBoundsInRoot()
            val action = onNodeWithText("AGREE").assertIsDisplayed().getBoundsInRoot()
            assertTrue(
                action.bottom <= window.bottom,
                "the action's foot at ${action.bottom} in a window of ${window.bottom}",
            )
            assertTrue(title.bottom < action.top, "the title over the action")
            assertEquals(96.dp, onNodeWithText("TITLE").getBoundsInRoot().top - 14.dp, "the title's place")
        }

    @Test
    fun `the wheel scrolls the text`() =
        runComposeUiTest {
            dialog()
            assertEquals(0f, scrolled())
            onNodeWithTag("window").performMouseInput {
                moveTo(Offset(center.x, center.y))
                scroll(4f)
            }
            mainClock.advanceTimeBy(500)
            assertTrue(scrolled() > 0f, "the wheel did not scroll the text")
        }

    @Test
    fun `the arrows and page keys scroll the text once Tab reaches it`() =
        runComposeUiTest {
            dialog()
            // The screen is still the first stop: B-63.
            repeat(2) {
                onRoot().performKeyInput { pressKey(Key.Tab) }
                mainClock.advanceTimeBy(50)
            }
            onRoot().performKeyInput { pressKey(Key.DirectionDown) }
            mainClock.advanceTimeBy(100)
            val line = scrolled()
            assertTrue(line > 0f, "Down did not scroll the text")
            onRoot().performKeyInput { pressKey(Key.PageDown) }
            mainClock.advanceTimeBy(500)
            assertTrue(scrolled() > line, "PageDown did not scroll the text")
            onRoot().performKeyInput { pressKey(Key.DirectionUp) }
            mainClock.advanceTimeBy(100)
            assertNotEquals(scrolled(), line, "Up did not move it back")
        }

    @Test
    fun `the text shows the keyboard's frame when Tab reaches it`() =
        runComposeUiTest {
            dialog()
            // The screen is still the first stop: B-63.
            repeat(2) {
                onRoot().performKeyInput { pressKey(Key.Tab) }
                mainClock.advanceTimeBy(50)
            }
            val text =
                onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
                    .captureToImage()
                    .toPixelMap()
            val ink = PlakatPalettes.Paper.ink
            val mark = text[1, text.height / 2]
            assertTrue(abs(mark.red - ink.red) < .02f && abs(mark.green - ink.green) < .02f, "at its edge: $mark")
        }

    @Test
    fun `scrolled the text stays out of the title's plate`() =
        runComposeUiTest {
            dialog()
            val plate = {
                onNodeWithText("TITLE")
                    .captureToImage()
                    .toPixelMap()
                    .buffer
                    .toList()
            }
            val before = plate()
            onNodeWithTag("window").performMouseInput {
                moveTo(Offset(center.x, center.y))
                scroll(4f)
            }
            mainClock.advanceTimeBy(500)
            assertTrue(scrolled() > 0f)
            assertEquals(before, plate(), "the scrolled text was drawn over the title")
        }

    @Test
    fun `its last line can be reached`() =
        runComposeUiTest {
            dialog()
            onNodeWithTag("window").performMouseInput {
                moveTo(Offset(center.x, center.y))
                scroll(10_000f)
            }
            mainClock.advanceTimeBy(500)
            val range =
                onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
                    .fetchSemanticsNodes()
                    .single()
                    .config[SemanticsProperties.VerticalScrollAxisRange]
            assertEquals(range.maxValue(), range.value(), "stopped short of the end")
        }

    @Test
    fun `text that fits is no stop for the keyboard`() =
        runComposeUiTest {
            dialog(text = "Short.")
            // The screen is still the first stop: B-63. The next is the first action, with no stop between.
            repeat(2) {
                onRoot().performKeyInput { pressKey(Key.Tab) }
                mainClock.advanceTimeBy(50)
            }
            onNodeWithText("LATER").assertIsFocused()
        }

    @Test
    fun `text that fits leaves the sheet as high as its parts`() =
        runComposeUiTest {
            dialog(text = "Short.")
            val text = onNodeWithText("Short.").getBoundsInRoot()
            val action = onNodeWithText("AGREE").getBoundsInRoot()
            assertTrue(action.top - text.bottom < 40.dp, "a gap of ${action.top - text.bottom} under short text")
        }
}
