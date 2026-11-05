package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-38: `lock`, 0 ms. Three in a row at density 1 on white, each a zone of 48: the ring 24 across in its middle, the disc
 * 12. A disc goes out at once and the next comes a frame later, so two never stand together.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatRadioButtonTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.group(
        reduce: Boolean = false,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        colors: PlakatColors = paper,
        selected: () -> Int,
        onSelect: (Int) -> Unit = {},
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = colors, reduceMotion = reduce, events = { heard += it }) {
                    Row(Modifier.selectableGroup().background(Color.White).testTag("g")) {
                        repeat(3) { i ->
                            PlakatRadioButton(selected() == i, { onSelect(i) }, Modifier.testTag("r$i"))
                        }
                    }
                }
            }
        }
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("g").captureToImage().toPixelMap()

    /** Which buttons have a red disc at their middle. */
    private fun PixelMap.discs() = (0 until 3).filter { this[24 + 48 * it, 24] == paper.red }

    @Test
    fun `a disc goes out at once and the next comes a frame later, with no motion`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var selected by mutableIntStateOf(0)
            group(heard = heard, selected = { selected })
            assertEquals(listOf(0), frame().discs())
            selected = 1
            val seen =
                (0 until 6).map {
                    mainClock.advanceTimeByFrame()
                    frame().discs()
                }
            assertEquals(listOf(1), seen.last())
            assertTrue(seen.all { it.size <= 1 }, "never two together: $seen")
            assertTrue(emptyList<Int>() in seen, "a frame with neither: $seen")
            val settled = seen.indexOf(listOf(1))
            assertTrue(settled in 1..3, "within a frame or so: $seen")
            assertTrue(seen.drop(settled).all { it == listOf(1) }, "and it stays: $seen")
            assertEquals(listOf(PlakatEvent.Lock), heard)
        }

    @Test
    fun `under reduced motion the two change together`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var selected by mutableIntStateOf(0)
            group(reduce = true, heard = heard, selected = { selected })
            selected = 1
            val seen =
                (0 until 4).map {
                    mainClock.advanceTimeByFrame()
                    frame().discs()
                }
            assertTrue(seen.none { it.isEmpty() }, "never neither: $seen")
            assertEquals(listOf(1), seen.last())
            assertEquals(listOf(PlakatEvent.Lock), heard)
        }

    @Test
    fun `nothing is heard on composing or on leaving`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var selected by mutableIntStateOf(0)
            group(heard = heard, selected = { selected })
            assertEquals(emptyList(), heard)
            selected = -1
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(emptyList(), frame().discs())
            assertEquals(emptyList(), heard, "only a button coming in locks")
        }

    @Test
    fun `the ring is 24 across in its rule of 3, the disc 12, in a zone of 48`() =
        runComposeUiTest {
            group(selected = { 0 })
            onNodeWithTag("r0").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
            val px = frame()
            // Along the middle row of the first zone, 12 of white, 3 of ring, 3 of white, 12 of disc.
            assertEquals(Color.White, px[11, 24])
            assertEquals(paper.ink, px[13, 24])
            assertEquals(paper.ink, px[14, 24])
            assertEquals(Color.White, px[16, 24])
            assertEquals(Color.White, px[17, 24])
            assertEquals(paper.red, px[19, 24])
            assertEquals(paper.red, px[28, 24])
            assertEquals(Color.White, px[31, 24])
            assertEquals(paper.ink, px[34, 24])
            assertEquals(Color.White, px[36, 24])
            // A round ring: the box's corner is not inked, as a checkbox's would be.
            assertEquals(Color.White, px[13, 13])
        }

    @Test
    fun `it takes the theme's palette`() =
        runComposeUiTest {
            val dark = PlakatPalettes.BlackPaper
            group(colors = dark, selected = { 0 })
            val px = frame()
            assertEquals(dark.ink, px[13, 24])
            assertEquals(dark.red, px[24, 24])
        }

    @Test
    fun `it is a radio button that selects and says so`() =
        runComposeUiTest {
            var selected by mutableIntStateOf(0)
            group(selected = { selected }, onSelect = { selected = it })
            mainClock.autoAdvance = true
            onNodeWithTag("r1")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
                .performClick()
            waitForIdle()
            onNodeWithTag("r1").assertIsSelected()
            assertEquals(1, selected)
        }

    @Test
    fun `disabled it takes no click and goes muted, thin and grey, in the disc too`() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper, reduceMotion = true) {
                        Box(Modifier.background(Color.White).testTag("g")) {
                            PlakatRadioButton(true, { clicks++ }, Modifier.testTag("r"), enabled = false)
                        }
                    }
                }
            }
            onNodeWithTag("r").performClick()
            assertEquals(0, clicks)
            val px = frame()
            assertEquals(paper.inkMuted, px[24, 24], "the disc")
            assertEquals(Color.White, px[14, 24], "a hair, not a rule")
        }
}
