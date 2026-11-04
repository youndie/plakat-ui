package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
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

/** B-14: the TextField's motion row (underline, hatch, caret) at its timestamps, reduced motion, target. */
@OptIn(ExperimentalTestApi::class)
class PlakatTextFieldTest {
    private val paper = PlakatPalettes.Paper

    private fun close(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < 2f / 255 && abs(a.green - b.green) < 2f / 255 && abs(a.blue - b.blue) < 2f / 255

    private fun ComposeUiTest.shot(): PixelMap = onNodeWithTag("f").captureToImage().toPixelMap()

    /** The share of the row 5 px above the field's bottom (in the bar, over the rule) that is ink. */
    private fun PixelMap.bar(): Float = (0 until width).count { close(this[it, 56 - 5], paper.ink) } / width.toFloat()

    private fun ComposeUiTest.field(
        reduce: Boolean,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        error: () -> String? = { null },
        value: String = "",
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(reduceMotion = reduce, events = { heard += it }) {
                    PlakatTextField(value, {}, Modifier.width(200.dp).testTag("f"), error = error())
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    @Test
    fun `focus grows the bar from the left linearly in 120 ms and reports underline once`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            field(reduce = false, heard = heard)
            assertEquals(0f, shot().bar())
            onNodeWithTag("f").performClick()
            val way =
                (0 until 10).map {
                    mainClock.advanceTimeByFrame()
                    shot().bar()
                }
            // Linear: each 16 ms frame adds 16/120 of the width, from the frame the play starts.
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, f) -> abs(f - ((i - start) * 16 / 120f).coerceIn(0f, 1f)) < 0.03f }
                }
            assertTrue(fits, "the bar's share per frame: $way")
            assertEquals(listOf(PlakatEvent.Underline), heard)
        }

    @Test
    fun `the caret blinks 500 on and 300 off without a fade`() =
        runComposeUiTest {
            field(reduce = false)
            onNodeWithTag("f").performClick()
            mainClock.advanceTimeByFrame()
            val start = mainClock.currentTime
            val seen = mutableListOf<Pair<Long, Color>>()
            while (mainClock.currentTime - start < 1700) {
                mainClock.advanceTimeByFrame()
                seen += (mainClock.currentTime - start) to shot()[3, 28]
            }
            for ((t, c) in seen) {
                val phase = (t + 16) % 800
                // Away from the edges of each phase (a frame either side), the caret is red or not drawn at all
                // (the field has no ground of its own).
                if (phase in 32..480) assertTrue(close(c, paper.red), "on at $t ms: $c")
                if (phase in 532..780) assertEquals(0f, c.alpha, "off at $t ms: $c")
                assertTrue(close(c, paper.red) || c.alpha == 0f, "no fade at $t ms: $c")
            }
        }

    @Test
    fun `an error opens the hatch from the left in 200 ms and jolts the field 2 dp and back`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var error by mutableStateOf<String?>(null)
            field(reduce = false, heard = heard, error = { error }, value = "anna")

            fun hatchShare(px: PixelMap) =
                (0 until px.width).count { close(px[it, 56 + 8 + 1], paper.red) } / px.width.toFloat()

            fun textLeft(px: PixelMap) = (0 until px.width).first { x -> (10..46).any { close(px[x, it], paper.ink) } }
            val rest = textLeft(shot())
            error = "Допишите адрес."
            val hatch = mutableListOf<Float>()
            val shift = mutableListOf<Int>()
            repeat(16) {
                mainClock.advanceTimeByFrame()
                val px = shot()
                hatch += hatchShare(px)
                shift += textLeft(px) - rest
            }
            // The hatch is half red by its nature; it grows over 200 ms to its full share, never back.
            assertTrue(hatch.zipWithNext().all { (a, b) -> b >= a - 1e-3f }, "the hatch only grows: $hatch")
            assertTrue(hatch[3] < hatch.last() * .5f && hatch.last() > .3f, "it opens over time: $hatch")
            assertTrue(2 in shift, "the field jolts 2 dp: $shift")
            assertEquals(0, shift.last(), "and comes back: $shift")
            assertEquals(listOf(PlakatEvent.Hatch), heard)
            onNodeWithTag("f").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        }

    @Test
    fun `under reduced motion the bar and the hatch are instant and nothing jolts or blinks`() =
        runComposeUiTest {
            var error by mutableStateOf<String?>(null)
            field(reduce = true, error = { error }, value = "anna")
            onNodeWithTag("f").performClick()
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertTrue(shot().bar() > .99f, "the bar at once")
            error = "Допишите адрес."
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val px = shot()
            assertTrue(
                (0 until px.width).count { close(px[it, 56 + 8 + 1], paper.red) } > px.width / 3,
                "the hatch at once",
            )
            repeat(60) {
                mainClock.advanceTimeByFrame()
                assertTrue(shot().bar() > .99f)
            }
        }

    @Test
    fun `the field is 56 dp tall`() =
        runComposeUiTest {
            setContent { PlakatTheme { PlakatTextField("", {}, Modifier.testTag("f")) } }
            onNodeWithTag("f").assertHeightIsEqualTo(56.dp)
        }
}
