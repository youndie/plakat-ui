package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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

/**
 * B-34: `wipe`. On the page's lab of 180 × 320: a screen of green under a screen of blue, the strip's edge from 290 to
 * −60 in 320 ms, linear, leaning back 100 over the height, 40 wide.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatScreenWipeTest {
    private val paper = PlakatPalettes.Paper
    private val colours = mapOf("a" to Color.Green, "b" to Color.Blue)

    private fun ComposeUiTest.stage(
        screen: () -> String,
        depth: () -> Int,
        reduce: Boolean = false,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        content: @Composable (String) -> Unit = { Box(Modifier.fillMaxSize().background(colours.getValue(it))) },
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    PlakatScreenWipe(screen(), depth(), Modifier.size(180.dp, 320.dp).testTag("s"), content = content)
                }
            }
        }
        mainClock.advanceTimeBy(32)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("s").captureToImage().toPixelMap()

    /** On row 0, the strip's left, or null with none of it on the screen. */
    private fun PixelMap.stripAt(y: Int = 0) =
        (0 until width).firstOrNull { x -> this[x, y].let { it.red > .5f && it.green < .5f } }

    @Test
    fun `a push brings the new screen in behind a strip going left`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var screen by mutableStateOf("a")
            stage({ screen }, { 1 }, heard = heard)
            assertEquals(Color.Green, frame()[90, 160])
            screen = "b"
            val frames =
                (0 until 24).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val fits =
                (0..2).any { start ->
                    frames.withIndex().all { (i, px) ->
                        val p = ((i - start) * 16.0 / 320).coerceIn(0.0, 1.0)
                        val edge = 290 - 350 * p
                        val left = px.stripAt()
                        when {
                            edge >= 180 || edge + 40 <= 0 -> left == null
                            else -> left != null && abs(left - edge.coerceAtLeast(0.0)) <= 1
                        }
                    }
                }
            assertTrue(fits, "the strip per frame: ${frames.map { it.stripAt() }}")
            // Behind the strip the new screen, ahead of it the old, on the same row, leaning back 100 over the height.
            val middle = frames.first { (it.stripAt() ?: 0) in 60..100 }
            val x = middle.stripAt()!!
            assertEquals(Color.Green, middle[x - 4, 0])
            assertEquals(Color.Blue, middle[x + 44, 0])
            assertEquals(x.toFloat() - 50, middle.stripAt(160)!!.toFloat(), 2f, "the lean, halfway down")
            assertEquals(Color.Blue, frames.last()[90, 160])
            assertEquals(null, frames.last().stripAt())
            // Once a place has gone over, it stays: no frame of the old screen at the end of the change.
            val corner = frames.map { it[175, 300] }
            val over = corner.indexOf(Color.Blue)
            assertTrue(over >= 0 && corner.drop(over).all { it == Color.Blue }, "the corner per frame: $corner")
            assertEquals(listOf(PlakatEvent.Wipe), heard)
        }

    @Test
    fun `a pop runs the other way and takes the top screen off`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var screen by mutableStateOf("b")
            var depth by mutableStateOf(2)
            stage({ screen }, { depth }, heard = heard)
            screen = "a"
            depth = 1
            val frames =
                (0 until 24).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val fits =
                (0..2).any { start ->
                    frames.withIndex().all { (i, px) ->
                        val p = ((i - start) * 16.0 / 320).coerceIn(0.0, 1.0)
                        val edge = -60 + 350 * p
                        val left = px.stripAt()
                        when {
                            edge >= 180 || edge + 40 <= 0 -> left == null
                            else -> left != null && abs(left - edge.coerceAtLeast(0.0)) <= 1
                        }
                    }
                }
            assertTrue(fits, "the strip per frame: ${frames.map { it.stripAt() }}")
            // The screen that is leaving is on the right of the strip, the one it returns to on the left.
            val middle = frames.first { (it.stripAt() ?: 0) in 60..100 }
            val x = middle.stripAt()!!
            assertEquals(Color.Green, middle[x - 4, 0])
            assertEquals(Color.Blue, middle[x + 44, 0])
            assertEquals(Color.Green, frames.last()[90, 160])
            assertEquals(listOf(PlakatEvent.Wipe), heard)
        }

    @Test
    fun `it opens at nothing, with no flash of the new screen`() =
        runComposeUiTest {
            var screen by mutableStateOf("a")
            stage({ screen }, { 1 })
            screen = "b"
            repeat(4) {
                mainClock.advanceTimeByFrame()
                assertEquals(Color.Green, frame()[20, 160], "frame $it")
            }
        }

    @Test
    fun `under reduced motion the new screen fades in over 100 ms with no strip`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var screen by mutableStateOf("a")
            stage({ screen }, { 1 }, reduce = true, heard = heard)
            screen = "b"
            val frames =
                (0 until 10).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val shares = frames.map { it[90, 160].blue }
            val fits =
                (0..2).any { start ->
                    shares.withIndex().all { (i, v) -> abs(v - ((i - start) * 16f / 100).coerceIn(0f, 1f)) < .04f }
                }
            assertTrue(fits, "the blue per frame: $shares")
            assertTrue(frames.all { it.stripAt() == null && it.stripAt(160) == null }, "no strip")
            assertEquals(listOf(PlakatEvent.Wipe), heard)
        }

    @Test
    fun `a pop under reduced motion fades too`() =
        runComposeUiTest {
            var screen by mutableStateOf("b")
            var depth by mutableStateOf(2)
            stage({ screen }, { depth }, reduce = true)
            screen = "a"
            depth = 1
            val first =
                (0 until 4).map {
                    mainClock.advanceTimeByFrame()
                    frame()[90, 160]
                }
            assertTrue(first.any { it.green in .2f..0.8f }, "under way: $first")
            mainClock.advanceTimeBy(160)
            assertEquals(Color.Green, frame()[90, 160])
        }

    @Test
    fun `nothing moves and nothing is heard on composing`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            stage({ "a" }, { 1 }, heard = heard)
            assertEquals(Color.Green, frame()[90, 160])
            assertEquals(null, frame().stripAt())
            assertEquals(emptyList(), heard)
        }

    @Test
    fun `a screen keeps its state through the change and is gone after it`() =
        runComposeUiTest {
            val made = mutableMapOf<String, Int>()
            var screen by mutableStateOf("a")
            stage({ screen }, { 1 }, content = {
                remember { made[it] = (made[it] ?: 0) + 1 }
                BasicText(it)
            })
            screen = "b"
            repeat(30) { mainClock.advanceTimeByFrame() }
            assertEquals(mapOf("a" to 1, "b" to 1), made, "composed once each, through the change of layers")
            onNodeWithText("a").assertDoesNotExist()
            onNodeWithText("b").assertExists()
        }

    @Test
    fun `a change in the middle finishes the one under way`() =
        runComposeUiTest {
            var screen by mutableStateOf("a")
            val pages = mapOf("a" to Color.Green, "b" to Color.Blue, "c" to Color.Yellow)
            stage({ screen }, { 1 }, content = { Box(Modifier.fillMaxSize().background(pages.getValue(it))) })
            screen = "b"
            repeat(8) { mainClock.advanceTimeByFrame() }
            screen = "c"
            repeat(3) { mainClock.advanceTimeByFrame() }
            val px = frame()
            // No green left: b has taken the whole screen, and c comes in over it.
            for (y in 0 until 320 step 20) {
                for (x in 0 until 180 step 20) {
                    assertTrue(
                        px[x, y] != Color.Green,
                        "at $x, $y",
                    )
                }
            }
            repeat(30) { mainClock.advanceTimeByFrame() }
            assertEquals(Color.Yellow, frame()[90, 160])
        }

    @Test
    fun `going back to the screen it is leaving wipes back to it`() =
        runComposeUiTest {
            var screen by mutableStateOf("a")
            var depth by mutableStateOf(1)
            stage({ screen }, { depth })
            screen = "b"
            depth = 2
            repeat(8) { mainClock.advanceTimeByFrame() }
            screen = "a"
            depth = 1
            repeat(3) {
                mainClock.advanceTimeByFrame()
                assertEquals(Color.Blue, frame()[175, 300], "the new screen stays on the way back, frame $it")
            }
            repeat(30) { mainClock.advanceTimeByFrame() }
            assertEquals(Color.Green, frame()[90, 160])
        }

    @Test
    fun `the geometry is the page's at its lab and its strip`() {
        val lab =
            androidx.compose.ui.geometry
                .Size(180f, 320f)
        assertEquals(290f, wipeEdge(0f, lab), .01f)
        assertEquals(-60f, wipeEdge(1f, lab), .01f)
        val strip =
            androidx.compose.ui.geometry
                .Size(60f, 106f)
        assertEquals(97f, wipeEdge(0f, strip), 1f)
        assertEquals(-20f, wipeEdge(1f, strip), .01f)
    }
}
