package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-20: IconButton's `stamp` (inverted and the icon 1 dp over on the frame it lands) and its return,
 * 60 ms hard-stop; Badge's `tick`. The Menu stencil's top bar runs x 3…21, y 5…8 of 24, centred in 48:
 * its top row at rest is 17, its left column 15. The stage is white, so the ground reads by share.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatIconButtonTest {
    private val paper = PlakatPalettes.Paper

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

    private fun ComposeUiTest.button(
        reduce: Boolean,
        heard: MutableList<PlakatEvent>,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.background(Color.White)) {
                        PlakatIconButton(PlakatIcons.Menu, "Меню", {}, Modifier.testTag("b"))
                    }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("b").captureToImage().toPixelMap()

    /**
     * The ground's share inside the focus ring, which a press in the tests' keyboard mode brings on:
     * 0 the white stage, 1 the ink.
     */
    private fun PixelMap.ground() = ((1f - this[6, 6].green) / (1f - paper.ink.green)).toDouble()

    @Test
    fun `pressed it inverts and the icon moves 1 dp on one frame then both return on the hard-stop`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            button(reduce = false, heard = heard)
            val rest = frame()
            assertEquals(0.0, rest.ground(), .01)
            assertEquals(paper.ink, rest[20, 17])
            onNodeWithTag("b").performTouchInput { down(center) }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val down = frame()
            assertEquals(1.0, down.ground(), .01)
            // Ink ground where the bar stood, the bar in paper a pixel lower and further right.
            assertEquals(paper.ink, down[20, 17])
            assertEquals(paper.paper, down[20, 18])
            assertEquals(paper.ink, down[15, 19])
            assertEquals(paper.paper, down[16, 19])
            onNodeWithTag("b").performTouchInput { up() }
            val way =
                (0 until 7).map {
                    mainClock.advanceTimeByFrame()
                    frame().ground()
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, g) ->
                        abs(g - (1 - hardStop(((i - start) * 16.0 / 60).coerceIn(0.0, 1.0)))) < .03
                    }
                }
            assertTrue(fits, "the ground per frame: $way")
            // Back: the bar in ink at its own place.
            assertEquals(paper.ink, frame()[20, 17])
            assertEquals(listOf(PlakatEvent.Stamp, PlakatEvent.Release), heard)
        }

    @Test
    fun `under reduced motion it returns at once`() =
        runComposeUiTest {
            button(reduce = true, heard = mutableListOf())
            onNodeWithTag("b").performTouchInput { down(center) }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(1.0, frame().ground(), .01)
            onNodeWithTag("b").performTouchInput { up() }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(0.0, frame().ground(), .01)
        }

    @Test
    fun `it is a 48 dp named button that says when it is selected`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    PlakatIconButton(PlakatIcons.Search, "Найти", {}, Modifier.testTag("b"), selected = true)
                    PlakatIconButton(PlakatIcons.Menu, "Меню", {}, Modifier.testTag("off"), enabled = false)
                }
            }
            onNodeWithTag("b")
                .assertWidthIsEqualTo(48.dp)
                .assertHeightIsEqualTo(48.dp)
                .assertHasClickAction()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
            onNodeWithContentDescription("Найти").assertExists()
            onNodeWithTag("off").assertIsNotEnabled()
        }

    private fun ComposeUiTest.shownNumber() =
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .first()
            .text

    private fun ComposeUiTest.badge(
        reduce: Boolean,
        heard: MutableList<PlakatEvent>,
        value: () -> Int,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    PlakatBadgedBox(value(), Modifier.testTag("box")) { PlakatIcon(PlakatIcons.Menu, null) }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    @Test
    fun `a badge ticks to its new number digit by digit at 40 ms a step`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var value by mutableIntStateOf(39)
            badge(reduce = false, heard = heard) { value }
            assertEquals("39", shownNumber())
            value = 47
            val shown =
                (0 until 12).map {
                    mainClock.advanceTimeByFrame()
                    shownNumber()
                }
            val tick = PlakatTick(39, 47)
            val fits =
                (0..2).any { start ->
                    shown.withIndex().all { (i, text) -> text == tick.textAt(((i - start) * 16.0).coerceAtLeast(0.0)) }
                }
            assertTrue(fits, "per frame: $shown")
            assertEquals(listOf("39", "38", "37", "47"), shown.distinct())
            assertEquals(listOf(PlakatEvent.Tick), heard)
        }

    @Test
    fun `under reduced motion a badge has its new number at once`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var value by mutableIntStateOf(39)
            badge(reduce = true, heard = heard) { value }
            value = 47
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals("47", shownNumber())
            assertEquals(listOf(PlakatEvent.Tick), heard)
        }

    @Test
    fun `a change during a tick goes on from what shows`() =
        runComposeUiTest {
            var value by mutableIntStateOf(39)
            badge(reduce = false, heard = mutableListOf()) { value }
            value = 47
            val under =
                (0 until 7)
                    .map {
                        mainClock.advanceTimeByFrame()
                        shownNumber()
                    }.last()
            assertTrue(under == "38" || under == "37", under)
            value = 39
            val back =
                (0 until 10).map {
                    mainClock.advanceTimeByFrame()
                    shownNumber()
                }
            assertEquals(back.first(), under, "no jump: $under then $back")
            assertEquals("39", back.last())
        }
}
