package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-30: `tick`. The page's 39 → 47 (units 9 → 8 → 7, then tens 3 → 4) at 40 ms a step, the pictograms one by one
 * across it. A tile of 175 at density 1 on white: the number 16…64, the row 72…84.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatCounterTest {
    private val paper = PlakatPalettes.Paper
    private val rowY = 77

    private fun ComposeUiTest.stage(
        reduce: Boolean = false,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        content: @Composable () -> Unit,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.testTag("s").background(Color.White)) { content() }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("s").captureToImage().toPixelMap()

    private fun ComposeUiTest.shows(text: String) = onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    /** The lengths of the dark runs along row [y]. */
    private fun PixelMap.runs(y: Int): List<Int> {
        val out = mutableListOf<Int>()
        var run = 0
        for (x in 0 until width) {
            if (this[x, y].red < .5f) {
                run++
            } else if (run > 0) {
                out += run
                run = 0
            }
        }
        if (run > 0) out += run
        return out
    }

    private fun PixelMap.squares(y: Int = rowY) = runs(y).count { it == 10 }

    @Composable
    private fun Tile(value: Int) =
        PlakatCounter(value, "СТАТЕЙ", Modifier.width(175.dp), legend = "= 5 статей", perPictogram = 5)

    /** The first frame of the change is at most [late] frames after it, as a frame test elsewhere allows. */
    private fun fits(
        got: List<Any?>,
        late: Int = 2,
        want: (Double) -> Any?,
    ) = (0..late).any { start -> got.withIndex().all { (i, v) -> v == want((i - start) * 16.0) } }

    @Test
    fun `the digits step lowest first and the pictograms come one by one`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var value by mutableIntStateOf(39)
            stage(heard = heard) { Tile(value) }
            assertTrue(shows("39"))
            assertEquals(7, frame().squares())
            assertTrue(shows("+4"), "39 is 7 fives and 4")
            value = 47
            val numbers = mutableListOf<String?>()
            val squares = mutableListOf<Int>()
            val left = mutableListOf<String?>()
            repeat(16) {
                mainClock.advanceTimeByFrame()
                numbers += listOf("39", "38", "37", "47").firstOrNull { shows(it) }
                squares += frame().squares()
                left += listOf("+4", "+3", "+2").firstOrNull { shows(it) }
            }
            assertTrue(
                fits(numbers) { ms ->
                    when {
                        ms < 40 -> "39"
                        ms < 80 -> "38"
                        ms < 120 -> "37"
                        else -> "47"
                    }
                },
                "the number per frame: $numbers",
            )
            assertTrue(
                fits(squares) { ms ->
                    when {
                        ms < 60 -> 7
                        ms < 120 -> 8
                        else -> 9
                    }
                },
                "the squares per frame: $squares",
            )
            assertTrue(
                fits(left) { ms ->
                    when {
                        ms < 40 -> "+4"
                        ms < 80 -> "+3"
                        else -> "+2"
                    }
                },
                "what is left over per frame: $left",
            )
            assertEquals(listOf(PlakatEvent.Tick), heard)
        }

    @Test
    fun `a long change takes at most 400 ms`() =
        runComposeUiTest {
            var value by mutableIntStateOf(0)
            stage { Tile(value) }
            value = 999
            // 27 steps of 400 / 27 ms.
            repeat(24) { mainClock.advanceTimeByFrame() }
            assertTrue(!shows("999"), "under way at 24 frames")
            repeat(4) { mainClock.advanceTimeByFrame() }
            assertTrue(shows("999"), "there by 448 ms at the latest")
        }

    @Test
    fun `under reduced motion it is all there at once`() =
        runComposeUiTest {
            var value by mutableIntStateOf(39)
            val heard = mutableListOf<PlakatEvent>()
            stage(reduce = true, heard = heard) { Tile(value) }
            value = 47
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertTrue(shows("47"))
            assertEquals(9, frame().squares())
            assertTrue(shows("+2"))
            assertEquals(listOf(PlakatEvent.Tick), heard)
        }

    @Test
    fun `no tick and no event on composing`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            stage(heard = heard) { Tile(47) }
            assertTrue(shows("47"))
            assertEquals(emptyList(), heard)
        }

    @Test
    fun `a whole number of pictograms leaves no word and none draws none`() =
        runComposeUiTest {
            var value by mutableIntStateOf(10)
            stage(reduce = true) { Tile(value) }
            assertEquals(2, frame().squares())
            assertTrue(!shows("+0"))
            value = 0
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertTrue(shows("0"))
            assertEquals(0, frame().squares())
            value = -3
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertTrue(shows("0"), "under zero counts as zero")
        }

    @Test
    fun `the row wraps at the tile and at its own most`() =
        runComposeUiTest {
            stage {
                // Fourteen fit in 180 at 10 + 3; the tile of 400 would hold thirty.
                PlakatCounter(20, "СТАТЕЙ", Modifier.width(400.dp))
            }
            val px = frame()
            assertEquals(14, px.squares(rowY))
            assertEquals(6, px.squares(rowY + 13))
        }

    @Test
    fun `the row wraps in a narrow tile`() =
        runComposeUiTest {
            // 143 inside: eleven at 10 + 3.
            stage { PlakatCounter(14, "СТАТЕЙ", Modifier.width(175.dp)) }
            val px = frame()
            assertEquals(11, px.squares(rowY))
            assertEquals(3, px.squares(rowY + 13))
        }

    @Test
    fun `the last circle is red and moves with the count`() =
        runComposeUiTest {
            var value by mutableIntStateOf(3)
            stage(reduce = true) {
                PlakatCounter(
                    value,
                    "ДНЕЙ",
                    Modifier.width(175.dp),
                    pictogram = PlakatPictogram.Circle,
                    accentLast = true,
                )
            }

            // Circles of 12 from 16 at 15 apart, on the row's middle.
            fun PixelMap.at(i: Int) = this[22 + 15 * i, 78]
            var px = frame()
            assertEquals(paper.ink, px.at(0))
            assertEquals(paper.ink, px.at(1))
            assertEquals(paper.red, px.at(2))
            assertEquals(paper.concrete, px.at(3), "no fourth yet")
            // Round: the box's corner stays concrete.
            assertEquals(paper.concrete, px[16, 72])
            value = 4
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            px = frame()
            assertEquals(paper.ink, px.at(2))
            assertEquals(paper.red, px.at(3))
        }

    @Test
    fun `the legend reads with its mark, drawn as the pictogram`() =
        runComposeUiTest {
            stage {
                Row {
                    Tile(47)
                    PlakatCounter(
                        6,
                        "ДНЕЙ",
                        Modifier.width(175.dp),
                        legend = "= день",
                        pictogram = PlakatPictogram.Circle,
                    )
                }
            }
            onNodeWithText("■ = 5 статей").assertIsDisplayed()
            onNodeWithText("● = день").assertIsDisplayed()
            val px = frame()

            /** The width in dark pixels of each row of the mark, under the caption, at the tile's left [x]. */
            fun mark(x: Int) =
                (108 until px.height)
                    .map { y -> (x until x + 7).count { px[it, y].red < .5f } }
                    .filter { it > 0 }
            val square = mark(16)
            val circle = mark(191)
            // Its top row falls on a fraction of a pixel, so a square's rows may differ by one; a circle's by more.
            assertTrue(square.max() - square.min() <= 1, "a square's rows are about as wide: $square")
            assertTrue(circle.max() - circle.min() >= 2, "a circle's are not: $circle")
            assertEquals(4, square.size, "as high as Chrome's: $square")
        }

    @Test
    fun `what is left over stands in the row's line of 12`() =
        runComposeUiTest {
            stage { Tile(47) }
            val px = frame()
            val rows = (66 until 90).filter { y -> (130 until 152).any { px[it, y].red < .5f } }
            assertEquals(74..81, rows.first()..rows.last(), "the reference's rows")
        }

    @Test
    fun `the column's number is a place wide and 104 high through the tick`() =
        runComposeUiTest {
            var value by mutableIntStateOf(39)
            stage { PlakatCounterColumn(value, "СТАТЕЙ", Modifier.width(191.dp)) }

            fun PixelMap.extent(): List<Int> {
                val rows = (0 until 125).filter { y -> (16 until 175).any { x -> this[x, y].red < .4f } }
                val columns = (0 until 175).filter { x -> (16 until 120).any { y -> this[x, y].red < .4f } }
                return listOf(rows.first(), rows.last(), columns.first(), columns.last())
            }
            val before = frame().extent()
            value = 47
            val seen = mutableSetOf<String>()
            repeat(14) {
                mainClock.advanceTimeByFrame()
                seen += listOf("39", "38", "37", "47").filter { shows(it) }
                val e = frame().extent()
                // The top within a pixel of the box, its foot too (a round figure overshoots by one), never scaled.
                assertTrue(e[0] in 15..18 && e[1] in 117..120, "rows at $it: $e, before $before")
                assertTrue(e[2] in 15..17 && e[3] in 125..128, "columns at $it: $e, before $before")
            }
            assertEquals(setOf("39", "38", "37", "47"), seen)
        }

    @Test
    fun `the column is as wide as the places it shows`() =
        runComposeUiTest {
            var value by mutableIntStateOf(6)

            stage(reduce = true) { PlakatCounterColumn(value, "ДНЕЙ", Modifier.width(240.dp)) }

            fun PixelMap.right() = (0 until 240).last { x -> (16 until 120).any { y -> this[x, y].red < .4f } }
            assertTrue(frame().right() in 69..72, "one place: ${frame().right()}")
            value = 120
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertTrue(frame().right() in 181..184, "three places: ${frame().right()}")
        }
}
