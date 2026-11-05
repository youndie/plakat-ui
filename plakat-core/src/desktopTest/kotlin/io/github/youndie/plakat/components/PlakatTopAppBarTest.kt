package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-25: `fold`, linear with the scroll and the same under reduced motion. The bar is 128 expanded and 59
 * collapsed, a way of 69: the title and strip ride up by the fold under the 56 of the row, and a rule of 3
 * grows at the bottom.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatTopAppBarTest {
    private val stark =
        PlakatColors(
            paper = Color.White,
            concrete = Color.Gray,
            ink = Color.Blue,
            inkMuted = Color.Gray,
            red = Color.Red,
            onRed = Color.White,
            ocher = Color.Yellow,
            slate = Color.Cyan,
        )

    private fun ComposeUiTest.bar(
        state: PlakatTopAppBarState,
        reduce: Boolean,
        heard: MutableList<PlakatEvent> = mutableListOf(),
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = stark, reduceMotion = reduce, events = {
                    heard += it
                }, print = PlakatPrintSwitches(texture = false)) {
                    PlakatTopAppBar(
                        "ЛИСТ",
                        Modifier.width(390.dp).testTag("b"),
                        subtitle = "№ 47",
                        collapsedTitle = "ЛИСТ · № 47",
                        seed = 240,
                        state = state,
                    )
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("b").captureToImage().toPixelMap()

    private fun PlakatTopAppBarState.scroll(dy: Float) =
        nestedScrollConnection.onPreScroll(Offset(0f, dy), NestedScrollSource.UserInput).y

    private fun same(
        a: PixelMap,
        ya: Int,
        b: PixelMap,
        yb: Int,
    ) = (0 until 390).all { x ->
        // Within rounding: an antialiased edge drawn a whole pixel away can land a level off.
        val p = a[x, ya]
        val q = b[x, yb]
        abs(p.red - q.red) < .02f && abs(p.green - q.green) < .02f && abs(p.blue - q.blue) < .02f
    }

    private fun folds(reduce: Boolean) =
        runComposeUiTest {
            val state = PlakatTopAppBarState()
            val heard = mutableListOf<PlakatEvent>()
            bar(state, reduce, heard)
            val open = frame()
            onNodeWithTag("b").assertHeightIsEqualTo(128.dp)
            assertEquals(0f, state.fraction)
            for (step in listOf(10, 20, 30)) {
                assertEquals(-10f, state.scroll(-10f), "consumed at $step")
                waitForIdle()
                onNodeWithTag("b").assertHeightIsEqualTo((128 - step).dp)
                assertEquals(step / 69f, state.fraction, 1e-4f)
                val px = frame()
                // Under the row, what stands is the expanded bar moved up by the fold, whole rows of it, to the rule.
                val rule = (3 * step / 69f).toInt()
                for (y in 56 until 128 - step - rule) assertTrue(same(px, y, open, y + step), "row $y at fold $step")
                // The rule at the bottom, in ink across the width.
                for (y in 128 - step - rule until 128 - step) {
                    assertEquals(
                        Color.Blue,
                        px[200, y],
                        "rule row $y at $step",
                    )
                }
            }
            assertEquals(emptyList(), heard, "no event on the way")
            // The rest of the way, and no further: the list gets what is left over.
            assertEquals(-39f, state.scroll(-100f))
            waitForIdle()
            onNodeWithTag("b").assertHeightIsEqualTo(59.dp)
            assertEquals(1f, state.fraction)
            assertEquals(0f, state.scroll(-10f))
            val shut = frame()
            for (y in 56 until 59) assertEquals(Color.Blue, shut[200, y])
            assertEquals(listOf(PlakatEvent.Fold), heard)
            // Back: a list at its top passes the rest of the drag on, and the bar opens the same way.
            val back =
                state.nestedScrollConnection.onPostScroll(
                    Offset.Zero,
                    Offset(0f, 200f),
                    NestedScrollSource.UserInput,
                )
            assertEquals(Offset(0f, 69f), back)
            waitForIdle()
            onNodeWithTag("b").assertHeightIsEqualTo(128.dp)
            assertEquals(listOf(PlakatEvent.Fold, PlakatEvent.Fold), heard)
            val again = frame()
            for (y in 0 until 128) assertTrue(same(again, y, open, y), "row $y back open")
        }

    @Test
    fun `it folds linearly with the scroll and the strip and title ride up`() = folds(reduce = false)

    @Test
    fun `under reduced motion it folds just the same`() = folds(reduce = true)

    @Test
    fun `the collapsed title opens by a wipe from the left with the fold`() =
        runComposeUiTest {
            val state = PlakatTopAppBarState()
            bar(state, reduce = false)

            // With no icons the row's title starts at its inset of 4 and takes the row's 382.
            fun titleInk(px: PixelMap) =
                (0 until 390).map { x -> (20 until 44).count { y -> px[x, y] != Color.White } > 0 }
            assertTrue(titleInk(frame()).none { it }, "hidden while open")
            state.scroll(-69f)
            waitForIdle()
            val whole = titleInk(frame())
            val right = whole.lastIndexOf(true)
            assertTrue(right > 60, "whole when shut: $right")
            // Halfway the title's box is half wiped: no ink past the middle of the row's 382.
            state.nestedScrollConnection.onPostScroll(Offset.Zero, Offset(0f, 34.5f), NestedScrollSource.UserInput)
            waitForIdle()
            val half = titleInk(frame())
            assertTrue(
                half.lastIndexOf(true) in 40..(4 + 382 / 2),
                "half: ${half.lastIndexOf(true)} of a title to $right",
            )
        }

    @Test
    fun `a swipe on the list folds the bar first and the list after and back`() =
        runComposeUiTest {
            val state = PlakatTopAppBarState()
            lateinit var list: LazyListState
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = stark) {
                        Column(Modifier.size(390.dp, 500.dp)) {
                            PlakatTopAppBar("ЛИСТ", Modifier.testTag("b"), state = state)
                            list = rememberLazyListState()
                            LazyColumn(Modifier.nestedScroll(state.nestedScrollConnection).testTag("l"), state = list) {
                                items(50) { Spacer(Modifier.fillMaxWidth().height(40.dp)) }
                            }
                        }
                    }
                }
            }
            waitForIdle()
            onNodeWithTag("l").performTouchInput { swipeUp(startY = bottom - 10, endY = top + 10) }
            waitForIdle()
            onNodeWithTag("b").assertHeightIsEqualTo(59.dp)
            assertTrue(list.firstVisibleItemIndex > 0, "the list moved after the bar: ${list.firstVisibleItemIndex}")
            onNodeWithTag("l").performTouchInput { swipeDown(startY = top + 10, endY = bottom - 10) }
            onNodeWithTag("l").performTouchInput { swipeDown(startY = top + 10, endY = bottom - 10) }
            onNodeWithTag("l").performTouchInput { swipeDown(startY = top + 10, endY = bottom - 10) }
            waitForIdle()
            assertEquals(0, list.firstVisibleItemIndex)
            onNodeWithTag("b").assertHeightIsEqualTo(128.dp)
        }

    @Test
    fun `its title is a heading`() =
        runComposeUiTest {
            setContent { PlakatTheme { PlakatTopAppBar("ЛИСТ", collapsedTitle = "№ 47") } }
            onNodeWithText("ЛИСТ").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        }
}
