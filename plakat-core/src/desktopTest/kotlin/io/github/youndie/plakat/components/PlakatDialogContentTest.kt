package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/** B-79: content between the text and the actions, scrolling with the text, focus entering at its first control. */
@OptIn(ExperimentalTestApi::class)
class PlakatDialogContentTest {
    private fun ComposeUiTest.dialog(
        rows: Int = 4,
        high: Int = 600,
        text: String? = "Choose how the board looks.",
    ) {
        mainClock.autoAdvance = false
        setContent {
            PlakatTheme(reduceMotion = true) {
                Box(Modifier.size(390.dp, high.dp).testTag("window")) {
                    PlakatDialog(
                        true,
                        "VIEW",
                        {},
                        Modifier.fillMaxSize(),
                        text = text,
                        content = {
                            for (i in 0 until rows) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PlakatRadioButton(i == 0, {}, Modifier.testTag("radio$i"))
                                    PlakatParagraph("Choice $i", PlakatTheme.typography.bodyM)
                                }
                            }
                        },
                    ) {
                        PlakatDialogAction("DONE", {})
                    }
                }
            }
        }
        mainClock.advanceTimeBy(500)
    }

    private fun ComposeUiTest.tab() {
        onRoot().performKeyInput { pressKey(Key.Tab) }
        mainClock.advanceTimeBy(100)
    }

    @Test
    fun `the content stands between the text and the actions`() =
        runComposeUiTest {
            dialog()
            val text = onNodeWithText("Choose how the board looks.").getBoundsInRoot()
            val first = onNodeWithTag("radio0").getBoundsInRoot()
            val last = onNodeWithTag("radio3").getBoundsInRoot()
            val action = onNodeWithText("DONE").getBoundsInRoot()
            assertTrue(text.bottom <= first.top, "text ${text.bottom} over the first radio ${first.top}")
            assertTrue(last.bottom <= action.top, "the last radio ${last.bottom} over the action ${action.top}")
            // Padded at its sides as the text is: 24 of the sheet's inset and 20 of the text's.
            assertTrue(first.left == 24.dp + 20.dp, "the radio's left ${first.left}")
        }

    @Test
    fun `Tab starts at the first radio`() =
        runComposeUiTest {
            dialog()
            tab()
            onNodeWithTag("radio0").assertIsFocused()
        }

    @Test
    fun `tall content scrolls, the actions stay on screen, and Tab brings each radio into view`() =
        runComposeUiTest {
            dialog(rows = 12, high = 420)
            val window = onNodeWithTag("window").getBoundsInRoot()
            val action = onNodeWithText("DONE").assertIsDisplayed().getBoundsInRoot()
            assertTrue(action.bottom <= window.bottom, "the action's foot ${action.bottom} in ${window.bottom}")
            onNodeWithText("VIEW").assertIsDisplayed()
            val title = onNodeWithText("VIEW").getBoundsInRoot()
            repeat(12) { tab() }
            val last = onNodeWithTag("radio11").assertIsFocused().getBoundsInRoot()
            assertTrue(
                last.top >= title.bottom && last.bottom <= action.top,
                "the last radio $last between $title and $action",
            )
        }
}
