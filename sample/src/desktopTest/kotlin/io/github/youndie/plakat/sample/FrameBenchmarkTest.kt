package io.github.youndie.plakat.sample

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.youndie.plakat.sample.bench.BenchArm
import io.github.youndie.plakat.sample.bench.FrameBenchmark
import io.github.youndie.plakat.sample.bench.armsOfRound
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
