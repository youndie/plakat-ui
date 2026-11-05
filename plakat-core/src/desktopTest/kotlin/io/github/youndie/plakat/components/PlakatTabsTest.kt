package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-27: `rail`, the bar riding linearly in 200 ms and the content shifting on the hard-stop; both at once under reduced motion. */
@OptIn(ExperimentalTestApi::class)
class PlakatTabsTest {
    private val paper = PlakatPalettes.Paper
    private val tabs = listOf(PlakatTab("ЛЕНТА"), PlakatTab("АРХИВ"), PlakatTab("ЧЕРНОВИКИ", enabled = false))

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

    private fun ComposeUiTest.bar(
        reduce: Boolean,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        selected: () -> Int,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    PlakatTabs(tabs, selected(), {}, Modifier.width(390.dp).testTag("t"))
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.barLeft(): Int {
        val px: PixelMap = onNodeWithTag("t").captureToImage().toPixelMap()
        return (0 until px.width).first { x -> px[x, 44].let { it.red - it.green > .3f } }
    }

    @Test
    fun `the bar rides linearly in 200 ms and is told by rail`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var selected by mutableIntStateOf(0)
            bar(reduce = false, heard = heard, selected = { selected })
            assertEquals(0, barLeft())
            selected = 2
            val way =
                (0 until 16).map {
                    mainClock.advanceTimeByFrame()
                    barLeft()
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, x) ->
                        abs(x - 260 * ((i - start) * 16.0 / 200).coerceIn(0.0, 1.0)) <= 1.0
                    }
                }
            assertTrue(fits, "the bar's left per frame: $way")
            assertEquals(260, way.last())
            assertEquals(listOf(PlakatEvent.Rail), heard)
        }

    @Test
    fun `under reduced motion the bar is there at once`() =
        runComposeUiTest {
            var selected by mutableIntStateOf(0)
            bar(reduce = true, selected = { selected })
            selected = 1
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(130, barLeft())
        }

    @Test
    fun `the bar sits on the hairline and each slot is a 48 dp target`() =
        runComposeUiTest {
            val picks = mutableListOf<Int>()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper, reduceMotion = true) {
                        Box(Modifier.background(Color.White)) {
                            PlakatTabs(tabs, 0, { picks += it }, Modifier.width(390.dp).testTag("t"))
                        }
                    }
                }
            }
            onNodeWithTag("t").assertHeightIsEqualTo(49.dp)
            val px = onNodeWithTag("t").captureToImage().toPixelMap()
            // The bar's 8 rows are the last 8, over the hairline; beside it the hairline alone.
            assertEquals(paper.red, px[50, 48])
            assertEquals(paper.red, px[50, 41])
            assertEquals(Color.White, px[50, 40])
            assertEquals(paper.ink, px[200, 48])
            assertEquals(Color.White, px[200, 47])
            onNodeWithContentDescription("ЛЕНТА").assertIsSelected().assertHeightIsEqualTo(48.dp)
            onNodeWithContentDescription("АРХИВ")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
                .performClick()
            assertEquals(listOf(1), picks)
            onNodeWithContentDescription("ЧЕРНОВИКИ").assertIsNotEnabled()
        }

    private fun ComposeUiTest.content(
        reduce: Boolean,
        selected: () -> Int,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce) {
                    PlakatTabContent(3, selected(), Modifier.size(200.dp, 20.dp).testTag("c")) { page ->
                        Box(Modifier.fillMaxSize().background(if (page == 0) Color.Red else Color.Blue))
                    }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    @Test
    fun `the content shifts on the hard-stop in 200 ms`() =
        runComposeUiTest {
            var selected by mutableIntStateOf(0)
            content(reduce = false, selected = { selected })

            fun leaving(): Double {
                val px = onNodeWithTag("c").captureToImage().toPixelMap()
                return (0 until 200).sumOf { x -> px[x, 10].red.toDouble() }
            }
            assertEquals(200.0, leaving(), .01)
            selected = 1
            val way =
                (0 until 16).map {
                    mainClock.advanceTimeByFrame()
                    leaving()
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, r) ->
                        abs(r - 200 * (1 - hardStop(((i - start) * 16.0 / 200).coerceIn(0.0, 1.0)))) <
                            1.5
                    }
                }
            assertTrue(fits, "the old page's width per frame: $way")
        }

    @Test
    fun `under reduced motion the content is there at once`() =
        runComposeUiTest {
            var selected by mutableIntStateOf(0)
            content(reduce = true, selected = { selected })
            selected = 1
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(Color.Blue, onNodeWithTag("c").captureToImage().toPixelMap()[10, 10])
        }

    @Test
    fun `a page out of sight is not composed`() =
        runComposeUiTest {
            val composed = mutableSetOf<Int>()
            var selected by mutableIntStateOf(0)
            setContent {
                PlakatTheme(reduceMotion = true) {
                    PlakatTabContent(3, selected, Modifier.size(100.dp)) { page -> composed += page }
                }
            }
            waitForIdle()
            assertEquals(setOf(0), composed)
            composed.clear()
            selected = 2
            waitForIdle()
            // The page between was never in sight; the old one may recompose once before the shift starts.
            assertTrue(2 in composed && 1 !in composed, "composed: $composed")
        }
}
