package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
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
 * B-29: `rail`. A new page shifts the panels in 200 ms hard-stop and only then do the captions open, 120 ms
 * linear; at once under reduced motion. Panels 300 wide here, the pictures solid, red then blue, so the way
 * reads off the picture's row.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatWindowTest {
    private val paper = PlakatPalettes.Paper
    private val panels =
        listOf(
            PlakatWindowPanel("ЧИТАЙ", "Первая.") { Box(Modifier.fillMaxSize().background(Color.Red)) },
            PlakatWindowPanel("СОХРАНЯЙ", "Вторая.") { Box(Modifier.fillMaxSize().background(Color.Blue)) },
            PlakatWindowPanel("ДЕЛИСЬ", "Третья.") { Box(Modifier.fillMaxSize().background(Color.Green)) },
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

    private fun ComposeUiTest.window(
        reduce: Boolean,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        inView: Int = 1,
        page: () -> Int,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.background(Color.White)) {
                        PlakatWindow(panels, page(), Modifier.width(300.dp).testTag("w"), panelsInView = inView)
                    }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("w").captureToImage().toPixelMap()

    /** How many columns of the picture's row are the first panel's red, at row 120 under the number. */
    private fun PixelMap.red() = (0 until width).count { x -> this[x, 120].let { it.red > .5f && it.blue < .5f } }

    /** Ink in the captions' band, under the 160 of the picture and 8 of gap: the dark pixels. */
    private fun PixelMap.captionInk() =
        (168 until 184).sumOf { y -> (0 until width).count { x -> this[x, y].red < .5f } }

    @Test
    fun `the panels shift on the hard-stop and then the captions open from the left`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var page by mutableIntStateOf(0)
            window(reduce = false, heard = heard) { page }
            val before = frame()
            assertEquals(300, before.red())
            assertTrue(before.captionInk() > 40, "a caption stands at rest: ${before.captionInk()}")
            page = 1
            val frames =
                (0 until 24).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val fits =
                (0..2).any { start ->
                    frames.withIndex().all { (i, px) ->
                        val ms = (i - start) * 16.0
                        // A page is a panel and a gap, 308, of which the red holds its 300.
                        val left = 300 - 308 * hardStop((ms / 200).coerceIn(0.0, 1.0))
                        abs(px.red() - left.coerceAtLeast(0.0)) <= 1.5
                    }
                }
            assertTrue(fits, "the red columns per frame: ${frames.map { it.red() }}")
            // The words are gone while the panels move, from a frame or two in.
            val inks = frames.map { it.captionInk() }
            val moving = frames.indices.filter { frames[it].red() in 1..299 }
            assertTrue(moving.drop(1).all { inks[it] == 0 }, "no caption while moving: $inks")
            // After the stop they open, more with each frame, to the whole of them, by 320 ms.
            val rest = inks.drop(moving.last() + 1)
            assertEquals(rest.sorted(), rest, "only ever more: $rest")
            assertTrue(rest.last() > 40, "whole at the end: $rest")
            assertTrue(inks.subList(14, 20).toSet().size >= 3, "opening over frames: $inks")
            assertEquals(listOf(PlakatEvent.Rail), heard)
        }

    @Test
    fun `under reduced motion the page and its captions are there at once`() =
        runComposeUiTest {
            var page by mutableIntStateOf(0)
            window(reduce = true) { page }
            page = 1
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val px = frame()
            assertEquals(0, px.red())
            assertTrue(px.captionInk() > 40)
        }

    @Test
    fun `two in view share the width and gap and a page moves by one`() =
        runComposeUiTest {
            var page by mutableIntStateOf(0)
            window(reduce = true, inView = 2) { page }
            // (300 − 8) / 2 = 146, then the gap of 8 in white, then the second panel.
            val px = frame()
            assertEquals(146, px.red())
            assertEquals(Color.Blue, px[154, 120])
            assertEquals(Color.Blue, px[299, 120])
            page = 1
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val next = frame()
            assertEquals(Color.Blue, next[0, 120])
            assertEquals(Color.Green, next[154, 120])
        }

    @Test
    fun `panels are numbered and their words are text`() =
        runComposeUiTest {
            setContent { PlakatTheme { PlakatWindow(panels, 0, Modifier.width(300.dp), panelsInView = 2) } }
            onNodeWithText("01").assertIsDisplayed()
            onNodeWithText("02").assertIsDisplayed()
            onNodeWithText("ЧИТАЙ").assertIsDisplayed()
            onNodeWithText("Вторая.").assertIsDisplayed()
        }
}
