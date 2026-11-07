package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-67: the small remainders of the QA pass. */
@OptIn(ExperimentalTestApi::class)
class PlakatQaRemaindersTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.kit(content: @Composable () -> Unit) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(reduceMotion = true, content = content)
            }
        }
    }

    @Test
    fun `a dialog action of a short word has a target of 48`() =
        runComposeUiTest {
            kit { PlakatDialogAction("OK", {}, Modifier.testTag("a")) }
            onNodeWithTag("a").assertWidthIsEqualTo(48.dp)
        }

    @Test
    fun `a reader hears one title at any fold of the bar`() =
        runComposeUiTest {
            lateinit var state: PlakatTopAppBarState
            kit {
                state = rememberPlakatTopAppBarState()
                Box(Modifier.size(390.dp, 300.dp)) { PlakatTopAppBar("NOVEMBER", state = state) }
            }
            var fold = 0f
            for (to in listOf(0f, 10f, 30f, 40f, 69f)) {
                state.nestedScrollConnection.onPreScroll(Offset(0f, fold - to), NestedScrollSource.UserInput)
                fold = to
                waitForIdle()
                assertEquals(to / 69f, state.fraction, 1e-4f)
                val heard = onAllNodesWithText("NOVEMBER", useUnmergedTree = true).fetchSemanticsNodes().size
                assertEquals(1, heard, "at a fold of $to")
            }
        }

    @Test
    fun `a torn card leaves the Tab order and the reader at once and comes back when unset`() =
        runComposeUiTest {
            var gone by mutableStateOf(false)
            kit {
                Column {
                    PlakatCard({}, Modifier.width(200.dp), printOrder = null, dismissed = gone) { BasicText("Card") }
                    PlakatButton("NEXT", {}, Modifier.testTag("n"))
                }
            }
            onNodeWithText("Card").assertExists()
            gone = true
            waitForIdle()
            onNodeWithText("Card").assertDoesNotExist()
            onRoot().performKeyInput { pressKey(Key.Tab) }
            waitForIdle()
            onNodeWithTag("n").assertIsFocused()
            gone = false
            waitForIdle()
            onNodeWithText("Card").assertExists()
        }

    /** How many pixels of the middle of a button are [color], the frame and the word aside. */
    private fun ComposeUiTest.inMiddle(color: Color): Int {
        val px = onNodeWithTag("b").captureToImage().toPixelMap()
        var n = 0
        for (y in px.height / 4 until px.height * 3 / 4) {
            for (x in px.width / 4 until px.width * 3 / 4) {
                val c = px[x, y]
                if (abs(c.red - color.red) + abs(c.green - color.green) + abs(c.blue - color.blue) < .06f) n++
            }
        }
        return n
    }

    @Test
    fun `a button loading and disabled draws its sector in the muted ink`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            kit {
                Box(Modifier.background(PlakatTheme.colors.paper)) {
                    PlakatButton("READ", {}, Modifier.width(164.dp).testTag("b"), enabled = false, loading = true)
                }
            }
            mainClock.advanceTimeBy(300)
            assertTrue(inMiddle(paper.inkMuted) > 40, "muted on concrete: ${inMiddle(paper.inkMuted)}")
        }
}
