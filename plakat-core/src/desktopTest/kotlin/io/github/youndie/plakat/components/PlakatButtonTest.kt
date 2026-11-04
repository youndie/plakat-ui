package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.down
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-13: the Button's motion row (stamp, release, engage), its click, its target and its states. */
@OptIn(ExperimentalTestApi::class)
class PlakatButtonTest {
    private val paper = PlakatPalettes.Paper

    /** `cubic-bezier(.2, .8, .9, .9)` at [x], solved here by bisection, apart from the token's easing. */
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

    private fun close(
        a: Color,
        b: Color,
        tolerance: Float = 1.5f / 255,
    ) = abs(a.red - b.red) <= tolerance && abs(a.green - b.green) <= tolerance && abs(a.blue - b.blue) <= tolerance

    @Test
    fun `the release returns the inks along the hard-stop curve in 60 ms`() {
        // The spec row: release, 60 · hard-stop. The token's easing is the CSS curve at every
        // timestamp, and the plate is the CSS transition from ink to red in sRGB at that progress.
        for (ms in 0..60 step 5) {
            val f = hardStop(ms / 60.0)
            assertEquals(f, PlakatEasings.HardStop.transform(ms / 60f).toDouble(), 1e-3, "progress at $ms ms")
            val plate = buttonPaint(PlakatButtonKind.Filled, paper, true, (1 - f).toFloat()).ground

            fun mix(
                from: Float,
                to: Float,
            ) = (from + (to - from) * f).toFloat()
            val want =
                Color(
                    mix(paper.ink.red, paper.red.red),
                    mix(paper.ink.green, paper.red.green),
                    mix(paper.ink.blue, paper.red.blue),
                )
            assertTrue(close(plate, want, 1e-4f), "plate at $ms ms: $plate, want $want")
        }
        // Halfway in time the hard-stop has done three quarters of the way.
        assertEquals(0.737, hardStop(0.5), 1e-3)
    }

    @Test
    fun `on release the ground fades while the slogan's colour changes at once`() {
        // CSS transitions `background-color` and `transform` only: the dark theme's pressed slogan is
        // paper on an ink plate, and the moment the finger lifts it is the on-red ink again.
        val dark = PlakatPalettes.BlackPaper
        val midway = buttonPaint(PlakatButtonKind.Filled, dark, true, pressed = .5f, held = false)
        assertEquals(dark.onRed, midway.ink)
        assertTrue(!close(midway.ground, dark.ink) && !close(midway.ground, dark.red), "the plate is under way")
        val outlined = buttonPaint(PlakatButtonKind.Outlined, dark, true, pressed = .5f, held = false)
        assertEquals(dark.ink, outlined.ink)
        assertEquals(dark.paper, buttonPaint(PlakatButtonKind.Outlined, dark, true, pressed = 1f, held = true).ink)
    }

    @Test
    fun `the loading sector turns in twelve 30 degree steps a 1200 ms turn`() {
        val at = listOf(0L to 0f, 99L to 0f, 100L to 30f, 650L to 180f, 1199L to 330f, 1200L to 0f, 2500L to 30f)
        for ((ms, deg) in at) assertEquals(deg, spinnerDegrees(ms), "at $ms ms")
    }

    private fun ComposeUiTest.plate(): Color = onNodeWithTag("b").captureToImage().toPixelMap()[150, 40]

    @Test
    fun `a press stamps at once and the release returns over 60 ms and clicks once`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var clicks = 0
            mainClock.autoAdvance = false
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(reduceMotion = false, events = { heard += it }) {
                        PlakatButton("читать", { clicks++ }, Modifier.width(164.dp).testTag("b"))
                    }
                }
            }
            mainClock.advanceTimeBy(100)
            assertTrue(close(plate(), paper.red), "at rest: ${plate()}")
            onNodeWithTag("b").performTouchInput { down(center) }
            mainClock.advanceTimeByFrame()
            assertTrue(close(plate(), paper.ink), "the stamp is one frame: ${plate()}")
            assertEquals(0, clicks, "nothing happens on the press")
            onNodeWithTag("b").performTouchInput { up() }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val early = plate()
            assertTrue(!close(early, paper.ink) && !close(early, paper.red), "under way: $early")
            mainClock.advanceTimeBy(80)
            assertTrue(close(plate(), paper.red), "back after 60 ms: ${plate()}")
            assertEquals(1, clicks, "the action fires on the release")
            assertEquals(listOf(PlakatEvent.Stamp, PlakatEvent.Release), heard)
        }

    @Test
    fun `under reduced motion the release is instant`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(reduceMotion = true) { PlakatButton("читать", {}, Modifier.width(164.dp).testTag("b")) }
                }
            }
            mainClock.advanceTimeBy(100)
            onNodeWithTag("b").performTouchInput { down(center) }
            mainClock.advanceTimeByFrame()
            assertTrue(close(plate(), paper.ink))
            onNodeWithTag("b").performTouchInput { up() }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertTrue(close(plate(), paper.red), "instant: ${plate()}")
        }

    @Test
    fun `loading engages once, wipes the slogan off and takes no clicks`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var clicks = 0
            var loading by mutableStateOf(false)
            mainClock.autoAdvance = false
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(reduceMotion = false, events = { heard += it }) {
                        Box {
                            PlakatButton("читать", { clicks++ }, Modifier.width(164.dp).testTag("b"), loading = loading)
                        }
                    }
                }
            }
            mainClock.advanceTimeBy(100)

            fun slogan(x: Int) =
                onNodeWithTag("b").captureToImage().toPixelMap().let { px ->
                    (14..34).count { y -> close(px[x, y], paper.onRed) }
                }
            val left = (40..80).sumOf { slogan(it) }
            assertTrue(left > 0, "the slogan's left half is printed")
            loading = true
            mainClock.advanceTimeBy(96)
            assertEquals(0, (40..70).sumOf { slogan(it) }, "most of 120 ms in, the left of the slogan is wiped")
            assertTrue((100..124).sumOf { slogan(it) } > 0, "and its right end not yet")
            mainClock.advanceTimeBy(100)
            assertEquals(0, (96..124).sumOf { slogan(it) }, "and then the rest, right of the sector")
            onNodeWithTag("b").performClick()
            mainClock.advanceTimeBy(100)
            assertEquals(0, clicks, "a loading button takes no clicks")
            assertEquals(listOf(PlakatEvent.Engage), heard.filter { it == PlakatEvent.Engage })
        }

    @Test
    fun `the target is 48 dp high and at least 48 wide`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    PlakatTextButton("а", {}, Modifier.testTag("b"))
                    PlakatButton("б", {}, Modifier.testTag("off"), enabled = false)
                }
            }
            onNodeWithTag("b").assertHeightIsEqualTo(48.dp).assertWidthIsAtLeast(48.dp)
            onNodeWithTag("off").assertIsNotEnabled()
        }
}
