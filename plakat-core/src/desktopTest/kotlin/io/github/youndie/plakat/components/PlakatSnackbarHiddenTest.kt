package io.github.youndie.plakat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** B-61: a hidden snackbar takes no input, in either motion mode. */
@OptIn(ExperimentalTestApi::class)
class PlakatSnackbarHiddenTest {
    private var visible by mutableStateOf(true)
    private var acted = 0
    private var under = 0

    private fun ComposeUiTest.bar(reduce: Boolean) {
        mainClock.autoAdvance = false
        setContent {
            PlakatTheme(reduceMotion = reduce) {
                Box(Modifier.fillMaxSize().testTag("under").clickable { under++ })
                PlakatSnackbar(
                    "Saved",
                    Modifier.width(300.dp),
                    visible = visible,
                    actionLabel = "UNDO",
                    onAction = { acted++ },
                )
            }
        }
        mainClock.advanceTimeBy(1000)
    }

    private fun ComposeUiTest.hide() {
        visible = false
        mainClock.advanceTimeBy(1000)
    }

    private fun clickWhereTheActionStood(reduce: Boolean) =
        runComposeUiTest {
            bar(reduce)
            val action = onNodeWithText("UNDO").getBoundsInRoot()
            val at = Offset((action.left.value + action.right.value) / 2, (action.top.value + action.bottom.value) / 2)
            hide()
            onNodeWithTag("under").performMouseInput { click(at) }
            mainClock.advanceTimeBy(100)
            assertEquals(0, acted, "the hidden action answered a click")
            assertEquals(1, under, "the click did not reach what is under it")
        }

    @Test
    fun `hidden under reduced motion a click goes through to what is under it`() =
        clickWhereTheActionStood(reduce = true)

    @Test
    fun `hidden a click goes through to what is under it`() = clickWhereTheActionStood(reduce = false)

    private fun theKeyboardDoesNotReachIt(reduce: Boolean) =
        runComposeUiTest {
            bar(reduce)
            hide()
            onRoot().performKeyInput { pressKey(Key.Tab) }
            mainClock.advanceTimeBy(100)
            onRoot().performKeyInput { pressKey(Key.Enter) }
            mainClock.advanceTimeBy(100)
            assertEquals(0, acted, "Tab then Enter fired a hidden action")
            assertEquals(
                0,
                onAllNodes(isFocused()).fetchSemanticsNodes().count { it.config.toString().contains("UNDO") },
            )
        }

    @Test
    fun `hidden the keyboard does not reach it`() = theKeyboardDoesNotReachIt(reduce = false)

    @Test
    fun `hidden under reduced motion the keyboard does not reach it`() = theKeyboardDoesNotReachIt(reduce = true)

    private fun nothingIsLeftOfIt(reduce: Boolean) =
        runComposeUiTest {
            bar(reduce)
            hide()
            onNodeWithText("Saved").assertDoesNotExist()
            onNodeWithText("UNDO").assertDoesNotExist()
        }

    @Test
    fun `hidden its words are gone from the semantics`() = nothingIsLeftOfIt(reduce = false)

    @Test
    fun `hidden under reduced motion its words are gone from the semantics`() = nothingIsLeftOfIt(reduce = true)

    @Test
    fun `going away its action no longer answers`() =
        runComposeUiTest {
            bar(reduce = true)
            visible = false
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            onNodeWithText("UNDO").performMouseInput { click() }
            mainClock.advanceTimeBy(1000)
            assertEquals(0, acted, "a snackbar on its way out took a click")
        }

    @Test
    fun `shown again it comes back with its words and takes clicks`() =
        runComposeUiTest {
            bar(reduce = true)
            hide()
            visible = true
            mainClock.advanceTimeBy(1000)
            onNodeWithText("Saved").assertIsDisplayed()
            onNodeWithText("UNDO").performMouseInput { click() }
            mainClock.advanceTimeBy(100)
            assertEquals(1, acted)
        }
}
