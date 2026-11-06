package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** B-60: focus from the keyboard shows on every control it lands on, as a frame of 3 inside the target. */
@OptIn(ExperimentalTestApi::class)
class PlakatKeyboardFocusTest {
    private val papers = listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)

    private fun near(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < .02f && abs(a.green - b.green) < .02f && abs(a.blue - b.blue) < .02f

    private fun SemanticsNodeInteraction.framed(color: Color): Boolean {
        val map = captureToImage().toPixelMap()
        val y = map.height / 2
        return near(map[1, y], color) && near(map[map.width - 2, y], color) && near(map[map.width / 2, 1], color)
    }

    private fun ComposeUiTest.on(
        colors: PlakatColors,
        content: @Composable () -> Unit,
    ) {
        mainClock.autoAdvance = false
        setContent {
            PlakatTheme(colors = colors, reduceMotion = true) {
                Box(Modifier.fillMaxSize().background(colors.paper)) { content() }
            }
        }
        mainClock.advanceTimeBy(300)
    }

    private fun ComposeUiTest.tab() {
        onRoot().performKeyInput { pressKey(Key.Tab) }
        mainClock.advanceTimeBy(100)
    }

    private fun check(
        name: String,
        node: ComposeUiTest.() -> SemanticsNodeInteraction,
        color: (PlakatColors) -> Color,
        content: @Composable () -> Unit,
    ) {
        for (colors in papers) {
            runComposeUiTest {
                on(colors, content)
                assertFalse(node().framed(color(colors)), "$name is framed before any focus")
                tab()
                assertTrue(node().framed(color(colors)), "$name shows no frame on focus")
            }
        }
    }

    @Test
    fun `a tab shows the frame`() =
        check("a tab", { onNodeWithContentDescription("ONE") }, { it.ink }) {
            PlakatTabs(listOf("ONE", "TWO").map { PlakatTab(it) }, 1, {}, Modifier.width(240.dp))
        }

    @Test
    fun `a navigation item shows the frame`() =
        check("a navigation item", { onNodeWithContentDescription("HOME") }, { it.onRed }) {
            PlakatNavigationBar(
                listOf(
                    PlakatNavigationItem(PlakatIcons.Home, "HOME"),
                    PlakatNavigationItem(PlakatIcons.Search, "FIND"),
                ),
                1,
                {},
                Modifier.width(240.dp),
            )
        }

    @Test
    fun `a list row shows the frame`() =
        check("a list row", { onNodeWithTag("row") }, { it.ink }) {
            PlakatListItem("Row", Modifier.fillMaxWidth().testTag("row"), onClick = {})
        }

    @Test
    fun `a snackbar's action shows the frame`() =
        check("a snackbar's action", { onNodeWithText("UNDO") }, { it.paper }) {
            PlakatSnackbar("Saved", Modifier.width(300.dp), actionLabel = "UNDO")
        }

    @Test
    fun `a row with no click shows no frame and takes no focus`() =
        runComposeUiTest {
            on(PlakatPalettes.Paper) { PlakatListItem("Row", Modifier.fillMaxWidth().testTag("row")) }
            tab()
            assertEquals(0, onAllNodes(isFocused()).fetchSemanticsNodes().size)
            assertFalse(onNodeWithTag("row").framed(PlakatPalettes.Paper.ink))
        }

    @Test
    fun `a mouse click leaves no frame`() =
        runComposeUiTest {
            on(PlakatPalettes.Paper) {
                PlakatTabs(listOf("ONE", "TWO").map { PlakatTab(it) }, 1, {}, Modifier.width(240.dp))
            }
            onNodeWithContentDescription("ONE").performMouseInput { click() }
            mainClock.advanceTimeBy(100)
            assertFalse(onNodeWithContentDescription("ONE").framed(PlakatPalettes.Paper.ink))
        }

    @Test
    fun `an item of a menu shows the frame when the arrows reach it`() {
        for (colors in papers) {
            runComposeUiTest {
                on(colors) {
                    PlakatMenu(true, {}, Modifier.testTag("menu")) {
                        PlakatMenuItem("One", {}, Modifier.testTag("one"))
                        PlakatMenuItem("Two", {}, Modifier.testTag("two"))
                    }
                }
                assertFalse(onNodeWithTag("one").framed(colors.ink), "nothing looks chosen until the arrows choose it")
                onNodeWithTag("menu").performKeyInput { pressKey(Key.DirectionDown) }
                mainClock.advanceTimeBy(100)
                assertTrue(onNodeWithTag("one").framed(colors.ink), "the first item shows no frame")
                assertFalse(onNodeWithTag("two").framed(colors.ink))
            }
        }
    }

    private fun menuPicking(keys: List<Key>): String {
        var picked = ""
        runComposeUiTest {
            on(PlakatPalettes.Paper) {
                PlakatMenu(true, {}, Modifier.testTag("menu")) {
                    PlakatMenuItem("One", { picked = "One" })
                    PlakatMenuItem("Two", { picked = "Two" })
                }
            }
            keys.forEach {
                onNodeWithTag("menu").performKeyInput { pressKey(it) }
                mainClock.advanceTimeBy(50)
            }
            onNodeWithTag("menu").performKeyInput { pressKey(Key.Enter) }
            mainClock.advanceTimeBy(50)
        }
        return picked
    }

    @Test
    fun `down past the last item comes round to the first`() =
        assertEquals("One", menuPicking(List(3) { Key.DirectionDown }))

    @Test
    fun `up from the first item comes round to the last`() =
        assertEquals("Two", menuPicking(listOf(Key.DirectionDown, Key.DirectionUp)))

    @Test
    fun `Tab goes round the items too`() = assertEquals("One", menuPicking(List(3) { Key.Tab }))
}
