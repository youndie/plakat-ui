package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-47: designed here. In a box of 390 × 400, the panel 280 wide; its rows from 16, each 56. */
@OptIn(ExperimentalTestApi::class)
class PlakatNavigationDrawerTest {
    private val paper = PlakatPalettes.Paper
    private val words = listOf("ЛЕНТА", "ПОИСК", "ОБМЕН", "ОПЦИИ")
    private val items =
        listOf(PlakatIcons.Home, PlakatIcons.Search, PlakatIcons.Share, PlakatIcons.Settings)
            .zip(words)
            .map { (icon, word) -> PlakatNavigationItem(icon, word) }

    private fun ComposeUiTest.drawer(
        visible: () -> Boolean,
        selected: () -> Int = { 0 },
        reduce: Boolean = true,
        colors: PlakatColors = paper,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        onSelect: (Int) -> Unit = {},
        onDismiss: () -> Unit = {},
        list: List<PlakatNavigationItem> = items,
        title: String? = null,
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(
                    colors = colors,
                    reduceMotion = reduce,
                    print =
                        PlakatPrintSwitches(
                            texture = false,
                        ),
                    events = {
                        heard += it
                    },
                ) {
                    Box(Modifier.size(390.dp, 400.dp).background(Color.White).testTag("box")) {
                        PlakatNavigationDrawer(visible(), list, selected(), onSelect, onDismiss, paneTitle = title)
                    }
                }
            }
        }
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("box").captureToImage().toPixelMap()

    /** `cubic-bezier(.2, .8, .9, .9)` at [x], by bisection, apart from the token's easing. */
    private fun hardStop(x: Double): Double {
        fun bez(
            t: Double,
            a: Double,
            b: Double,
        ) = 3 * (1 - t) * (1 - t) * t * a + 3 * (1 - t) * t * t * b + t * t * t
        var lo = 0.0
        var hi = 1.0
        repeat(60) {
            val mid = (lo + hi) / 2
            if (bez(mid, .2, .9) < x) lo = mid else hi = mid
        }
        return bez((lo + hi) / 2, .8, .9)
    }

    /** The panel's right edge along row 392, under its rows, where the screen's dots leave gaps: the run of its ground. */
    private fun PixelMap.edge(): Int = (0 until width).firstOrNull { this[it, 392] != paper.ink } ?: width

    @Test
    fun `an ink panel of 280, its rows of 56, the active stencil on a red square of 40 by 32`() =
        runComposeUiTest {
            drawer({ true }, { 1 })
            waitForIdle()
            val px = frame()
            assertEquals(280, px.edge())
            assertEquals(paper.ink, px[279, 200])
            // Row 1 from 72 to 128, the square centred in it from 84 to 116, 16 in.
            assertEquals(paper.ink, px[15, 90], "left of the square")
            assertEquals(paper.red, px[16, 84 + 1])
            assertEquals(paper.red, px[20, 84], "its top")
            assertEquals(paper.ink, px[20, 83])
            assertEquals(paper.red, px[20, 115], "its foot")
            assertEquals(paper.ink, px[20, 116])
            assertEquals(paper.red, px[55, 90], "its right")
            assertEquals(paper.ink, px[56, 90])
            // Row 0 has none of it; its word in on-red, 16 past the square.
            assertEquals(paper.ink, px[20, 30])
            assertTrue(
                (72 until 180).any { x ->
                    (30 until 58).any { y -> px[x, y] == paper.onRed }
                },
                "a word in cream",
            )
        }

    @Test
    fun `in the dark theme its ground is concrete, as the bar's is`() =
        runComposeUiTest {
            val dark = PlakatPalettes.BlackPaper
            drawer({ true }, colors = dark)
            waitForIdle()
            assertEquals(dark.concrete, frame()[100, 390])
        }

    @Test
    fun `it rides in on the hard stop in 200 ms, the screen closing with it, and back the same way`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var open by mutableStateOf(false)
            mainClock.autoAdvance = false
            drawer({ open }, reduce = false, heard = heard)
            mainClock.advanceTimeBy(100)
            assertTrue(heard.isEmpty(), "closed from the start, it says nothing: $heard")
            onNodeWithContentDescription("ЛЕНТА").assertDoesNotExist()
            open = true

            fun read() =
                (0 until 16).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val opening = read()
            val edges = opening.map { it.edge() }
            val rides =
                (0..3).any { start ->
                    edges.withIndex().all { (i, got) ->
                        val p = hardStop((((i - start) * 16.0) / 200).coerceIn(0.0, 1.0))
                        abs(got - 280 * p) <= 1.5
                    }
                }
            assertTrue(rides, "the panel's edge per frame: $edges")
            assertEquals(280, edges.last())
            assertEquals(listOf(PlakatEvent.Rail), heard)
            // Past the panel, along a row, the screen's edge is linear over the same 200: (390 + 120) p − 120 × 100 / 400.
            val dots = opening.map { px -> (280 until 390).count { px[it, 100] != Color.White } }
            val closes =
                (0..3).any { start ->
                    dots.withIndex().all { (i, got) ->
                        val p = (((i - start) * 16.0) / 200).coerceIn(0.0, 1.0)
                        val edge = 510 * p - 30
                        abs(got - ((edge - 280) / 2).coerceIn(0.0, 55.0)) <= 3
                    }
                }
            assertTrue(closes, "the dots past the panel per frame: $dots")
            open = false
            val closing = read().map { it.edge() }
            val back =
                (0..3).any { start ->
                    closing.withIndex().all { (i, got) ->
                        val p = hardStop((((i - start) * 16.0) / 200).coerceIn(0.0, 1.0))
                        abs(got - 280 * (1 - p)) <= 1.5
                    }
                }
            assertTrue(back, "going back the same way: $closing")
            assertEquals(listOf(PlakatEvent.Rail, PlakatEvent.Rail), heard)
            mainClock.advanceTimeByFrame()
            onNodeWithContentDescription("ЛЕНТА").assertDoesNotExist()
        }

    @Test
    fun `under reduced motion it is there and gone at once`() =
        runComposeUiTest {
            var open by mutableStateOf(false)
            mainClock.autoAdvance = false
            drawer({ open }, reduce = true)
            mainClock.advanceTimeBy(100)
            open = true
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(280, frame().edge())
            open = false
            repeat(3) { mainClock.advanceTimeByFrame() }
            onNodeWithContentDescription("ЛЕНТА").assertDoesNotExist()
        }

    @Test
    fun `a new place sends the square along in 120 ms on the hard stop, and lock is heard`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var place by mutableIntStateOf(0)
            mainClock.autoAdvance = false
            drawer({ true }, { place }, reduce = false, heard = heard)
            mainClock.advanceTimeBy(500)
            heard.clear()
            place = 3
            val tops =
                (0 until 10).map {
                    mainClock.advanceTimeByFrame()
                    val px = frame()
                    (0 until 400).first { px[18, it] == paper.red }
                }
            val fits =
                (0..3).any { start ->
                    tops.withIndex().all { (i, got) ->
                        val p = hardStop((((i - start) * 16.0) / 120).coerceIn(0.0, 1.0))
                        abs(got - (28 + 168 * p)) <= 1.5
                    }
                }
            assertTrue(fits, "the square's top per frame: $tops")
            assertEquals(196, tops.last())
            assertEquals(listOf(PlakatEvent.Lock), heard)
        }

    @Test
    fun `a row selects itself, and the panel stays until it is closed`() =
        runComposeUiTest {
            var chosen = -1
            var dismissed = 0
            drawer({ true }, onSelect = { chosen = it }, onDismiss = { dismissed++ })
            waitForIdle()
            onNodeWithContentDescription("ОБМЕН").performClick()
            waitForIdle()
            assertEquals(2, chosen)
            assertEquals(0, dismissed)
            onNodeWithContentDescription("ЛЕНТА").assertIsSelected()
        }

    @Test
    fun `pressed a row inverts`() =
        runComposeUiTest {
            drawer({ true })
            waitForIdle()
            onNodeWithContentDescription("ПОИСК").performMouseInput {
                moveTo(center)
                press()
            }
            waitForIdle()
            val px = frame()
            assertEquals(paper.onRed, px[250, 100], "the row's ground, cream")
            assertEquals(paper.ink, px[250, 50], "the row above, as it was")
            onNodeWithContentDescription("ПОИСК").performMouseInput { release() }
        }

    @Test
    fun `the screen, Escape and the reader's dismiss each ask it to go, and the panel takes its own clicks`() =
        runComposeUiTest {
            var dismissed = 0
            drawer({ true }, onDismiss = { dismissed++ })
            waitForIdle()
            onNodeWithTag("box").performMouseInput {
                moveTo(Offset(340f, 200f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(1, dismissed, "the screen")
            onNodeWithTag("box").performMouseInput {
                moveTo(Offset(200f, 350f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(1, dismissed, "the panel under its rows")
            onNodeWithTag("box").performKeyInput { pressKey(Key.Escape) }
            waitForIdle()
            assertEquals(2, dismissed)
            onNode(
                SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss),
            ).performSemanticsAction(SemanticsActions.Dismiss)
            waitForIdle()
            assertEquals(3, dismissed)
        }

    @Test
    fun `it is read as a dialog under its title, its rows by their words`() =
        runComposeUiTest {
            drawer({ true }, title = "Разделы")
            waitForIdle()
            onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.IsDialog))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Разделы"))
            onNodeWithContentDescription("ОПЦИИ").assert(
                SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab),
            )
        }

    @Test
    fun `too many rows scroll in it`() =
        runComposeUiTest {
            drawer({ true }, list = List(20) { PlakatNavigationItem(PlakatIcons.Home, "П $it") })
            waitForIdle()
            onNode(hasScrollAction()).assert(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy))
        }

    @Test
    fun `a row's target is 56 high`() =
        runComposeUiTest {
            drawer({ true })
            waitForIdle()
            val bounds = onNodeWithContentDescription("ЛЕНТА").fetchSemanticsNode().boundsInRoot
            assertEquals(56f, bounds.height)
            assertEquals(280f, bounds.width)
            assertEquals(16f, bounds.top)
        }
}
