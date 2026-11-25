package io.github.youndie.plakat.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-83: the sheet is 480 dp at its widest and centred; the screen fills the window whatever its width. */
@OptIn(ExperimentalTestApi::class)
class PlakatDialogWideTest {
    private val sheet = SemanticsMatcher.keyIsDefined(SemanticsProperties.IsDialog)

    private fun ComposeUiTest.dialog(
        wide: Dp,
        high: Dp = 800.dp,
        text: String = "Choose how the board looks.",
        onDismiss: () -> Unit = {},
    ): DpRect {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(reduceMotion = true) {
                    // Required: the test's window is 1024 wide, and a window of 1280 has to be one.
                    Box(Modifier.requiredSize(wide, high).testTag("window")) {
                        PlakatDialog(true, "VIEW", onDismiss, Modifier.fillMaxSize(), text = text) {
                            PlakatDialogAction("DONE", {})
                        }
                    }
                }
            }
        }
        mainClock.advanceTimeBy(500)
        // In the window's own coordinates.
        val window = onNodeWithTag("window").getUnclippedBoundsInRoot()
        val bounds = onNode(sheet).assertIsDisplayed().getUnclippedBoundsInRoot()
        return DpRect(
            bounds.left - window.left,
            bounds.top - window.top,
            bounds.right - window.left,
            bounds.bottom - window.top,
        )
    }

    @Test
    fun `in a wide window the sheet is 480 and centred`() =
        runComposeUiTest {
            val bounds = dialog(1280.dp)
            assertEquals(480.dp, bounds.width)
            assertEquals((1280.dp - 480.dp) / 2, bounds.left)
            assertEquals(96.dp, bounds.top, "it stands where the phone's does")
        }

    @Test
    fun `on a phone the sheet keeps 24 a side`() =
        runComposeUiTest {
            val bounds = dialog(390.dp)
            assertEquals(342.dp, bounds.width)
            assertEquals(24.dp, bounds.left)
        }

    @Test
    fun `it reaches 480 in a window of 528, with no jump on the way`() {
        for ((window, sheet) in listOf(500.dp to 452.dp, 528.dp to 480.dp, 600.dp to 480.dp)) {
            runComposeUiTest {
                val bounds = dialog(window)
                assertEquals(sheet, bounds.width, "a window of $window")
                assertEquals((window - sheet) / 2, bounds.left, "a window of $window")
            }
        }
    }

    @Test
    fun `a click on the screen beside the sheet dismisses`() =
        runComposeUiTest {
            var dismissed = 0
            dialog(1000.dp, high = 700.dp, onDismiss = { dismissed++ })
            // Beside the sheet, where a sheet 24 dp off each side would have stood.
            onNodeWithTag("window").performTouchInput { click(Offset(100f, 200f)) }
            mainClock.advanceTimeBy(100)
            assertEquals(1, dismissed)
        }

    @Test
    fun `long text wraps inside the 480 and the actions stay reachable`() =
        runComposeUiTest {
            val long = (1..60).joinToString(" ") { "A long sentence number $it of the announcement." }
            val bounds = dialog(1000.dp, high = 700.dp, text = long)
            assertEquals(480.dp, bounds.width)
            val done = onNodeWithText("DONE").assertIsDisplayed().getUnclippedBoundsInRoot()
            assertTrue(done.right <= bounds.right && done.bottom <= 700.dp, "$done in $bounds")
        }

    @Test
    fun `given no width it does not throw and the sheet keeps its 480`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(reduceMotion = true) {
                        Row(Modifier.horizontalScroll(rememberScrollState())) {
                            Box(Modifier.height(600.dp)) {
                                PlakatDialog(true, "VIEW", {}, text = "Words") { PlakatDialogAction("OK", {}) }
                            }
                        }
                    }
                }
            }
            mainClock.advanceTimeBy(500)
            assertTrue(onNode(sheet).getUnclippedBoundsInRoot().width <= 480.dp)
        }
}
