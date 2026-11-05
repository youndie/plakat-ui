package io.github.youndie.plakat.components

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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.RaskatOptions
import io.github.youndie.plakat.print.raskatField
import io.github.youndie.plakat.print.raskatInkB
import io.github.youndie.plakat.print.raskatRollT0
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-19: print — each element wiped in from the left over the whole poster in 120 ms, linear, the
 * composition's 30 ms apart, the title at 150 and the slogan at 180 — and the raskat's roll, 320 ms
 * hard-stop. The Круг of seed 47 (the page's) has a gray plate (0, 64, 208 × 88), an ink dot
 * (296, 152, ⌀ 32) and the red circle (312, 40, ⌀ 136), elements 0, 1 and 2; its slogan runs from 16
 * to 374 at rows 320…368. The palette is pure colours so every ink reads apart.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatPosterTest {
    private val stark =
        PlakatColors(
            paper = Color.White,
            concrete = Color.Black,
            ink = Color.Blue,
            inkMuted = Color.Gray,
            red = Color.Red,
            onRed = Color.White,
            ocher = Color.Yellow,
            slate = Color.Cyan,
        )

    private fun ComposeUiTest.poster(
        reduce: Boolean = false,
        raskat: Boolean = false,
        print: Boolean = true,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        seed: () -> Int = { 47 },
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = stark, reduceMotion = reduce, events = {
                    heard += it
                }, print = PlakatPrintSwitches(texture = false)) {
                    // No title and no body: the composition and the slogan alone, so a row reads one element.
                    PlakatPoster(
                        emptyList(),
                        "ПОДПИСАТЬСЯ",
                        {},
                        Modifier.testTag("p"),
                        seed = seed(),
                        raskat = raskat,
                        print = print,
                    )
                }
            }
        }
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("p").captureToImage().toPixelMap()

    /** How far along row [y], from [from] for [span] columns, [ink] is printed, its edge counted in part. */
    private fun PixelMap.printed(
        y: Int,
        from: Int,
        span: Int,
        ink: (Color) -> Float,
    ) = (from until (from + span).coerceAtMost(width)).sumOf { ink(this[it, y]).coerceIn(0f, 1f).toDouble() }

    private val black = { c: Color -> 1 - c.green }
    private val blue = { c: Color -> c.blue - c.red }
    private val red = { c: Color -> c.red - c.green }

    /** The wipe's edge [startMs] into the print at [ms]: the poster's 390 width times the linear progress over 120 ms. */
    private fun edge(
        ms: Double,
        startMs: Int,
    ) = 390 * ((ms - startMs) / 120).coerceIn(0.0, 1.0)

    @Test
    fun `the elements are wiped in from the left one after another, then the slogan`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            poster(heard = heard)
            val frames =
                (0 until 22).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val read =
                frames.map {
                    listOf(
                        it.printed(100, 0, 208, black),
                        it.printed(168, 296, 32, blue),
                        it.printed(108, 312, 78, red),
                        it.printed(360, 16, 358, red),
                    )
                }
            val fits =
                (0..2).any { start ->
                    read.withIndex().all { (i, r) ->
                        val ms = (i - start) * 16.0
                        abs(r[0] - edge(ms, 0).coerceIn(0.0, 208.0)) < 1.5 &&
                            abs(r[1] - (edge(ms, 30) - 296).coerceIn(0.0, 32.0)) < 1.5 &&
                            abs(r[2] - (edge(ms, 60) - 312).coerceIn(0.0, 78.0)) < 1.5 &&
                            abs(r[3] - (edge(ms, 180) - 16).coerceIn(0.0, 358.0)) < 1.5
                    }
                }
            assertTrue(fits, "plate, dot, circle, slogan per frame: ${read.map { r -> r.map { it.toInt() } }}")
            assertEquals(listOf(PlakatEvent.Print), heard)
        }

    @Test
    fun `the raskat rolls in on the design's roll while the circle is printed`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            poster(raskat = true, heard = heard)
            val field = raskatField(136, 136, RaskatOptions(streak = .5, dpr = 1.0))

            // Paper pixels inside the circle, left of the wipe's edge by two columns, against the field's at t0.
            fun count(
                px: PixelMap?,
                ms: Double,
            ): Int {
                val ink = if (px == null) raskatInkB(field, raskatRollT0(ms.coerceAtLeast(0.0), .58), 12.0) else null
                val limit = edge(ms, 60) - 2
                var n = 0
                for (j in 0 until 136) {
                    for (i in 0 until 136) {
                        val x = 312 + i
                        if (x >= 390 || x + 1 > limit || hypot(i + .5 - 68, j + .5 - 68) > 66) continue
                        val paper = if (px != null) px[x, 40 + j].green > .5f else ink!![j * 136 + i]
                        if (paper) n++
                    }
                }
                return n
            }
            val frames =
                (0 until 22).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val fits =
                (0..2).any { start ->
                    frames.withIndex().all { (i, px) ->
                        val ms = (i - start) * 16.0
                        count(px, ms) == count(null, ms)
                    }
                }
            assertTrue(fits, "paper in the circle per frame: ${frames.map { count(it, 1e9) }}")
            assertTrue(
                frames.withIndex().any { (i, px) ->
                    i > 4 && count(px, 1e9) > 0
                },
                "the roll printed paper at all",
            )
            assertEquals(listOf(PlakatEvent.Print, PlakatEvent.Roll), heard)
        }

    @Test
    fun `under reduced motion it is printed and rolled at once`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            poster(reduce = true, raskat = true, heard = heard)
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val px = frame()
            assertEquals(208.0, px.printed(100, 0, 208, black), .5)
            assertEquals(358.0, px.printed(360, 16, 358, red), .5)
            val field = raskatField(136, 136, RaskatOptions(streak = .5, dpr = 1.0))
            val ink = raskatInkB(field, .58, 12.0)
            // The final state: the circle's paper where the field at the seam says, pixel for pixel.
            for (j in 4 until 132 step 7) {
                for (i in 4 until 78 step 5) {
                    if (hypot(i + .5 - 68, j + .5 - 68) > 66) continue
                    assertEquals(ink[j * 136 + i], px[312 + i, 40 + j].green > .5f, "($i, $j)")
                }
            }
            assertEquals(listOf(PlakatEvent.Print, PlakatEvent.Roll), heard)
        }

    @Test
    fun `a new seed reprints at once and a poster not printing is there from the first frame`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var seed by mutableIntStateOf(47)
            poster(heard = heard, seed = { seed })
            mainClock.advanceTimeBy(1000)
            seed = 48
            mainClock.advanceTimeByFrame()
            // Seed 48's Круг: its own composition, whole on the first frame after the change.
            val after = frame()
            mainClock.advanceTimeBy(1000)
            assertEquals(
                frame().let { f ->
                    (0 until 390).sumOf { x ->
                        f[x, 100].green.toDouble()
                    }
                },
                (0 until 390).sumOf { x ->
                    after[x, 100].green.toDouble()
                },
                .01,
            )
            assertEquals(listOf(PlakatEvent.Print), heard)
        }

    @Test
    fun `without print it is printed from the first frame and says nothing`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            poster(print = false, heard = heard)
            mainClock.advanceTimeByFrame()
            val px = frame()
            assertEquals(208.0, px.printed(100, 0, 208, black), .5)
            assertEquals(emptyList(), heard)
        }

    @Test
    fun `the title is a heading and the slogan a 48 dp button`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    PlakatPoster(
                        listOf("ЧИТАЙ", "ВЕСЬ"),
                        "ЧИТАТЬ",
                        {},
                        layout = PlakatPosterLayout.Banner,
                        print = false,
                    )
                }
            }
            onNodeWithContentDescription(
                "ЧИТАЙ ВЕСЬ",
            ).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            onNodeWithText("ЧИТАТЬ").assertHeightIsEqualTo(48.dp)
        }
}
