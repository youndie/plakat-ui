package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-26: `lock`, the red square crossing to the active slot in 120 ms hard-stop, at once under reduced motion. */
@OptIn(ExperimentalTestApi::class)
class PlakatNavigationBarTest {
    private val paper = PlakatPalettes.Paper
    private val items =
        listOf(
            PlakatNavigationItem(PlakatIcons.Home, "ЛЕНТА"),
            PlakatNavigationItem(PlakatIcons.Search, "ПОИСК"),
            PlakatNavigationItem(PlakatIcons.Share, "ОБМЕН"),
            PlakatNavigationItem(PlakatIcons.Settings, "ОПЦИИ"),
        )

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
        onSelect: (Int) -> Unit = {},
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    PlakatNavigationBar(items, selected(), onSelect, Modifier.width(390.dp).testTag("n"))
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("n").captureToImage().toPixelMap()

    /** The square's left edge, on row 10, above the icons and in its height. */
    private fun PixelMap.squareLeft() = (0 until width).first { x -> this[x, 10].let { it.red - it.green > .3f } }

    @Test
    fun `the square crosses to a new slot on the hard-stop and is told by lock`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var selected by mutableIntStateOf(0)
            bar(reduce = false, heard = heard, selected = { selected })
            // Slots of 97.5: the first square at 28.75, snapped to 29; the third at 223.75, to 224.
            assertEquals(29, frame().squareLeft())
            selected = 2
            val way =
                (0 until 10).map {
                    mainClock.advanceTimeByFrame()
                    frame().squareLeft()
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, x) ->
                        abs(x - (28.75 + 195 * hardStop(((i - start) * 16.0 / 120).coerceIn(0.0, 1.0)))) <= 1.0
                    }
                }
            assertTrue(fits, "the square's left per frame: $way")
            assertEquals(224, way.last())
            assertEquals(listOf(PlakatEvent.Lock), heard)
        }

    @Test
    fun `under reduced motion it is there at once`() =
        runComposeUiTest {
            var selected by mutableIntStateOf(0)
            bar(reduce = true, selected = { selected })
            selected = 3
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(321, frame().squareLeft())
        }

    @Test
    fun `the strip is ink in the light theme and concrete in the dark`() =
        runComposeUiTest {
            var dark by mutableIntStateOf(0)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = if (dark == 1) PlakatPalettes.BlackPaper else paper) {
                        PlakatNavigationBar(items, 0, {}, Modifier.width(390.dp).testTag("n"))
                    }
                }
            }
            assertEquals(paper.ink, frame()[200, 2])
            dark = 1
            waitForIdle()
            assertEquals(PlakatPalettes.BlackPaper.concrete, frame()[200, 2])
        }

    @Test
    fun `each slot is a whole-height target that selects and says so`() =
        runComposeUiTest {
            var selected by mutableIntStateOf(0)
            val picks = mutableListOf<Int>()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper, reduceMotion = true) {
                        PlakatNavigationBar(
                            items,
                            selected,
                            {
                                picks += it
                                selected = it
                            },
                            Modifier.width(390.dp).testTag("n"),
                        )
                    }
                }
            }
            onNodeWithTag("n").assertHeightIsEqualTo(64.dp)
            onNodeWithContentDescription("ЛЕНТА").assertIsSelected()
            onNodeWithContentDescription("ПОИСК")
                .assertIsNotSelected()
                .assertHeightIsEqualTo(64.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            // Equal slots of 97.5 share their half pixels: 98, 97, 98, 97.
            onNodeWithContentDescription("ЛЕНТА").assertWidthIsEqualTo(98.dp)
            onNodeWithContentDescription("ПОИСК").assertWidthIsEqualTo(97.dp).performClick()
            waitForIdle()
            assertEquals(listOf(1), picks)
            onNodeWithContentDescription("ПОИСК").assertIsSelected()
        }
}
