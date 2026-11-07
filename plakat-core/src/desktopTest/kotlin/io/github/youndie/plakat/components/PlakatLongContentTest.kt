package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-65: what a component does with content that does not fit: nothing overlaps a neighbour or leaves its plate. */
@OptIn(ExperimentalTestApi::class)
class PlakatLongContentTest {
    private fun ComposeUiTest.on(content: @Composable () -> Unit) {
        mainClock.autoAdvance = false
        setContent {
            PlakatTheme(reduceMotion = true) {
                Box(Modifier.fillMaxSize()) { content() }
            }
        }
        mainClock.advanceTimeBy(500)
    }

    private val longWord = "NOTIFICATIONS AND ANNOUNCEMENTS"

    private val archive = "SUBSCRIBE TO THE WHOLE ARCHIVE"

    /**
     * That [word] is set to end in an ellipsis and is short of its end on the one line it has. `isLineEllipsized` always answers
     * false on Skiko, and a cut at a space stops short too, so the setting itself is read from the laid-out text; that the
     * dots are then drawn was looked at on both papers.
     */
    private fun endsInAnEllipsis(
        word: String,
        draw: @Composable (String, Modifier) -> Unit,
    ) = runComposeUiTest {
        on { draw(word, Modifier) }
        val results = mutableListOf<TextLayoutResult>()
        onNodeWithText(word)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)
        val layout = results.single()
        assertEquals(TextOverflow.Ellipsis, layout.layoutInput.overflow, "$word is cut with no sign")
        assertEquals(1, layout.lineCount)
        assertTrue(layout.getLineEnd(0) < word.length, "$word was not long enough to test this")
    }

    @Test
    fun `a list row's number of three digits is whole`() =
        runComposeUiTest {
            on { PlakatListItem("Row", Modifier.width(300.dp), number = "1000") }
            val number = onNodeWithText("1000").getBoundsInRoot()
            assertTrue(number.right - number.left > 40.dp, "the number's box is ${number.right - number.left}")
            assertTrue(onNodeWithText("Row").getBoundsInRoot().left >= number.right, "the headline over the number")
        }

    @Test
    fun `a list row's number of two digits keeps the design's column`() =
        runComposeUiTest {
            on { PlakatListItem("Row", Modifier.width(300.dp), number = "1") }
            assertEquals(16.dp + 24.dp + 16.dp, onNodeWithText("Row").getBoundsInRoot().left)
        }

    @Test
    fun `segments given too little stay inside the box, every target among them`() =
        runComposeUiTest {
            on {
                PlakatSegmentedButton(listOf("FIRST SEGMENT", "SECOND SEGMENT", "THIRD SEGMENT"), 1, {
                }, Modifier.width(240.dp).testTag("s"))
            }
            val box = onNodeWithTag("s").getBoundsInRoot()
            assertEquals(240.dp, box.right - box.left)
            for (word in listOf("FIRST SEGMENT", "SECOND SEGMENT", "THIRD SEGMENT")) {
                val target = onNodeWithContentDescription(word, ignoreCase = true).getBoundsInRoot()
                assertTrue(target.right <= box.right && target.left >= box.left, "$word at $target")
            }
        }

    @Test
    fun `a long action leaves its snackbar's message the other half at least`() =
        runComposeUiTest {
            on { PlakatSnackbar("A message", Modifier.width(300.dp), actionLabel = longWord) }
            val message = onNodeWithText("A message").getBoundsInRoot()
            assertTrue(message.right - message.left >= 120.dp, "a message ${message.right - message.left} wide")
            val action = onNodeWithText(longWord).getBoundsInRoot()
            assertTrue(action.right <= 300.dp, "the action ends at ${action.right}")
        }

    @Test
    fun `a short action keeps the design's width of 96`() =
        runComposeUiTest {
            on { PlakatSnackbar("A message", Modifier.width(300.dp).testTag("s"), actionLabel = "UNDO", onAction = {}) }
            val action = onNodeWithText("UNDO").getBoundsInRoot()
            assertEquals(96.dp, action.right - action.left)
        }

    @Test
    fun `a tooltip on a centred anchor where neither side has room still shows on the screen`() {
        val placed =
            tooltipPlacement(
                IntRect(150, 100, 200, 148),
                IntSize(350, 800),
                IntSize(240, 40),
                PlakatTooltipAxis.Level,
                24f,
            )
        val plate = placed.plate.x + placed.box.left
        assertTrue(plate >= 0, "the plate starts at $plate")
        assertTrue(plate + 240 <= 350, "the plate ends at ${plate + 240}")
    }

    @Test
    fun `a tooltip with room on a side still goes there and is not moved`() {
        val placed =
            tooltipPlacement(
                IntRect(10, 100, 60, 148),
                IntSize(600, 800),
                IntSize(240, 40),
                PlakatTooltipAxis.Level,
                24f,
            )
        assertEquals(60 + 24, placed.plate.x + placed.box.left)
    }

    @Test
    fun `two long dialog actions keep to the sheet, each whole on its own line`() =
        runComposeUiTest {
            on {
                Box(Modifier.size(390.dp, 600.dp)) {
                    PlakatDialog(true, "TITLE", {}, Modifier.fillMaxSize()) {
                        PlakatDialogAction("A VERY LONG FIRST ACTION", {})
                        PlakatDialogAction("A VERY LONG SECOND ACTION", {}, danger = true)
                    }
                }
            }
            val first = onNodeWithText("A VERY LONG FIRST ACTION").assertIsDisplayed().getBoundsInRoot()
            val second = onNodeWithText("A VERY LONG SECOND ACTION").assertIsDisplayed().getBoundsInRoot()
            for (action in listOf(first, second)) {
                assertTrue(action.left >= 24.dp && action.right <= 366.dp, "at ${action.left}..${action.right}")
                assertTrue(action.right - action.left > 240.dp, "squeezed to ${action.right - action.left}")
            }
            assertTrue(second.top >= first.bottom, "on one line")
        }

    @Test
    fun `a filled button's long word ends in an ellipsis`() =
        endsInAnEllipsis(archive) { w, m -> PlakatButton(w, {}, m.width(200.dp)) }

    @Test
    fun `an outlined button's long word ends in an ellipsis`() =
        endsInAnEllipsis(archive) { w, m -> PlakatOutlinedButton(w, {}, m.width(200.dp)) }

    @Test
    fun `a text button's long word ends in an ellipsis`() =
        endsInAnEllipsis(archive) { w, m -> PlakatTextButton(w, {}, m.width(200.dp)) }

    @Test
    fun `a danger button's long word ends in an ellipsis, on its plate`() =
        endsInAnEllipsis(archive) { w, m -> PlakatDangerButton(w, {}, m.width(200.dp)) }

    @Test
    fun `an empty state's slogan grows past 200 rather than losing words`() =
        runComposeUiTest {
            on { PlakatEmptyState("TITLE", "Body.", "CREATE ONE MORE LIST", {}, Modifier.width(390.dp)) }
            val button = onNodeWithText("CREATE ONE MORE LIST").getBoundsInRoot()
            assertTrue(button.right - button.left > 200.dp, "a slogan ${button.right - button.left} wide")
        }

    private fun ComposeUiTest.wordsClear(words: List<String>) {
        val rights =
            words.map { word ->
                val slot = onNodeWithContentDescription(word).getBoundsInRoot()
                slot.left to slot.right
            }
        val map = onNodeWithTag("row").captureToImage().toPixelMap()
        val y = map.height / 2
        // The slot's own edge between two words stays clear: nothing is drawn across it.
        rights.dropLast(1).forEach { (_, right) ->
            val x = right.value.toInt()
            val across = (x - 2..x + 2).count { map[it, y].alpha > 0f }
            assertEquals(0, across, "a word runs across the edge at $x")
        }
    }

    @Test
    fun `long tab words end short of the next`() =
        runComposeUiTest {
            val words = listOf("ANNOUNCEMENTS", "RECOMMENDATIONS", "NOTIFICATIONS")
            on { PlakatTabs(words.map { PlakatTab(it) }, 0, {}, Modifier.width(300.dp).testTag("row")) }
            wordsClear(words)
        }

    @Test
    fun `a long title leaves the top bar's subtitle in place`() =
        runComposeUiTest {
            on {
                PlakatTopAppBar(
                    "A TITLE MUCH TOO LONG FOR THE BAR AT ALL",
                    Modifier.width(390.dp),
                    subtitle = "No. 47",
                )
            }
            val subtitle =
                onNodeWithText(
                    "NO. 47",
                    ignoreCase = true,
                    useUnmergedTree = true,
                ).assertIsDisplayed().getBoundsInRoot()
            assertTrue(subtitle.right <= 390.dp - 16.dp + 1.dp, "the subtitle ends at ${subtitle.right}")
            assertTrue(subtitle.right - subtitle.left > 30.dp, "the subtitle is ${subtitle.right - subtitle.left} wide")
        }

    @Test
    fun `a masthead's two long labels do not run together`() =
        runComposeUiTest {
            val startWord = "A LONG LABEL AT THE START OF THE LINE"
            val endWord = "A LONG LABEL AT THE END OF THE LINE"
            on { PlakatMasthead("SHEET", Modifier.width(390.dp), startLabel = startWord, endLabel = endWord) }
            val start = onNodeWithText(startWord, ignoreCase = true).getUnclippedBoundsInRoot()
            val end = onNodeWithText(endWord, ignoreCase = true).getUnclippedBoundsInRoot()
            assertTrue(start.right <= end.left, "${start.right} runs into ${end.left}")
            assertTrue(start.right - start.left >= 120.dp, "the start has ${start.right - start.left}")
            assertTrue(end.right - end.left >= 120.dp, "the end has ${end.right - end.left}")
        }

    @Test
    fun `a long chip word ends in an ellipsis`() =
        endsInAnEllipsis("A CHIP WORD MUCH TOO LONG") { w, m -> PlakatChip(w, true, {}, m.width(120.dp)) }

    @Test
    fun `a long body stops above the slogan with an ellipsis`() =
        runComposeUiTest {
            val long = (1..30).joinToString(" ") { "Sentence $it." }
            on { PlakatPoster(listOf("READ"), "GO", {}, Modifier.testTag("p"), body = long, seed = 1, print = false) }
            val button = onNodeWithText("GO").getBoundsInRoot()
            val image = onNodeWithTag("p").captureToImage().toPixelMap()
            // Under the slogan, where twelve lines of it would run on to: nothing of its ink.
            val y = button.bottom.value.toInt() + 8
            val ink = PlakatPalettes.Paper.ink
            val inked =
                (16 until 306).count {
                    abs(image[it, y].red - ink.red) < .02f &&
                        abs(image[it, y].green - ink.green) < .02f
                }
            assertEquals(0, inked, "the body's ink under the slogan")
        }

    @Test
    fun `at font scale 2 a navigation label stays in its strip`() =
        runComposeUiTest {
            on {
                CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                    Column {
                        PlakatNavigationBar(
                            listOf(PlakatNavigationItem(PlakatIcons.Home, "HOME")),
                            0,
                            {},
                            Modifier.width(300.dp).testTag("n"),
                        )
                    }
                }
            }
            val strip = onNodeWithTag("n").getBoundsInRoot()
            assertTrue(strip.bottom - strip.top > 64.dp, "the strip is ${strip.bottom - strip.top} high")
        }

    @Test
    fun `at font scale 2 a badge's number stays on its square`() =
        runComposeUiTest {
            on {
                CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                    PlakatBadge(12, Modifier.testTag("b"))
                }
            }
            val badge = onNodeWithTag("b").getBoundsInRoot()
            assertTrue(badge.bottom - badge.top > 20.dp, "the badge is ${badge.bottom - badge.top} high")
        }
}
