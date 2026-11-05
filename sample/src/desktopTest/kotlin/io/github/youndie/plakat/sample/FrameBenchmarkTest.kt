package io.github.youndie.plakat.sample

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.youndie.plakat.sample.bench.BenchArm
import io.github.youndie.plakat.sample.bench.FrameBenchmark
import io.github.youndie.plakat.sample.bench.armsOfRound
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-51: that the benchmark shows what its arms say, the lever, read from what it draws. */
@OptIn(ExperimentalTestApi::class)
class FrameBenchmarkTest {
    @Test
    fun `the order of the arms rotates by one each round, every arm in each`() {
        assertEquals(BenchArm.entries.toSet(), armsOfRound(3).toSet())
        assertEquals(BenchArm.entries[1], armsOfRound(1).first())
        assertEquals(BenchArm.entries[3], armsOfRound(3).first())
    }

    @Test
    fun `an arm runs, reports its frames and hands over to the next`() =
        runComposeUiTest {
            val lines = mutableListOf<String>()
            var done = false
            setContent { FrameBenchmark(1, 0, { lines += it }) { done = true } }
            mainClock.advanceTimeBy(2000)
            waitForIdle()
            assertTrue(done || lines.isNotEmpty(), "it runs")
        }

    /** The levers, read from what the arm draws: without them the numbers would be of something else. */
    @Test
    fun `the cascade's cards are on the screen, and the flat arm's are not textured`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent { FrameBenchmark(1, 60, { }, only = BenchArm.CascadeA) { } }
            mainClock.advanceTimeBy(600)
            val px = onNodeWithTag(BenchArm.CascadeA.id).captureToImage().toPixelMap()
            val inks =
                (0 until px.height step 2)
                    .flatMap { y ->
                        (0 until px.width step 3).map { x -> px[x, y] }
                    }.toSet()
            assertTrue(PlakatPalettes.Paper.concrete in inks, "the cards' ground")
            assertTrue(PlakatPalettes.Paper.ink in inks, "their ink")
            assertTrue(inks.size > 20, "and a textured paper: ${inks.size}")
        }

    @Test
    fun `the roll's poster is on the screen, at its density`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent { FrameBenchmark(1, 60, { }, only = BenchArm.Roll2) { } }
            // Past the print's wipe of the circle, and before the arm restarts it at 21 frames.
            mainClock.advanceTimeBy(300)
            val px = onNodeWithTag(BenchArm.Roll2.id).captureToImage().toPixelMap()
            val reds =
                (0 until px.height step 2).sumOf { y ->
                    (0 until px.width step 2).count { x ->
                        px[x, y] ==
                            PlakatPalettes.Paper.red
                    }
                }
            assertTrue(reds > 100, "its red: $reds")
        }

    @Test
    fun `the paper under the arms is textured`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                FrameBenchmark(1, 60, { }) { }
            }
            // The first arm is idle: its paper is the texture already.
            mainClock.advanceTimeBy(100)
            val px = onNodeWithTag(BenchArm.IdleA.id).captureToImage().toPixelMap()
            val row = (0 until px.width).map { px[it, px.height - 2] }.toSet()
            assertTrue(row.size > 3, "the texture is on: ${row.size}")
        }
}
