package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
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
 * B-18: the telegram — the plate rides up 200 ms hard-stop from its height and 24 dp below its stop,
 * then the words open from the left in 120 ms, linear; hidden, the words go at once and the plate
 * rides down the same way; under reduced motion a 100 ms fade both ways.
 *
 * The plate stands at y 60 in a 300 × 200 red stage, 56 tall: hidden it is 80 lower. The plate is
 * read on the red channel (red stage, near-black ink) and its words on the green one, which the
 * stage has none of, so the stage never reads as words.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatSnackbarTest {
    private val paper = PlakatPalettes.Paper
    private val stage = Color.Red

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

    private fun ComposeUiTest.stage(): PixelMap = onNodeWithTag("stage").captureToImage().toPixelMap()

    /** How much of the plate's ink covers column 2 of row [y]: 0 the stage, 1 the plate. */
    private fun PixelMap.plate(y: Int) =
        ((stage.red - this[2, y].red) / (stage.red - paper.ink.red)).coerceIn(0f, 1f).toDouble()

    /** The plate's top, from the centre of its coverage down column 2; NaN when nothing of it shows. */
    private fun PixelMap.top(): Double {
        val c = (0 until height).map { plate(it) }
        val sum = c.sum()
        return if (sum < 1) Double.NaN else c.withIndex().sumOf { (y, v) -> (y + .5) * v } / sum - 28
    }

    /** The words' ink: paper-light pixels anywhere on the stage, columns from [from] to [to]. */
    private fun PixelMap.words(
        from: Int = 0,
        to: Int = width,
    ) = (0 until height).sumOf { y ->
        (from until to).sumOf { x ->
            ((this[x, y].green - paper.ink.green) / (paper.paper.green - paper.ink.green)).coerceIn(0f, 1f).toDouble()
        }
    }

    private fun ComposeUiTest.telegram(
        visible: () -> Boolean,
        reduce: Boolean,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        hidden: MutableList<Long> = mutableListOf(),
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Column(Modifier.size(300.dp, 200.dp).background(stage).testTag("stage")) {
                        Spacer(Modifier.height(60.dp))
                        PlakatSnackbar(
                            "Статья сохранена в закладки",
                            Modifier.fillMaxWidth(),
                            visible = visible(),
                            onHidden = { hidden += mainClock.currentTime },
                        )
                    }
                }
            }
        }
    }

    /** True when [way] is [curve] at the age of each 16 ms frame, from one of the first frames. */
    private fun follows(
        way: List<Double>,
        tolerance: Double,
        curve: (Double) -> Double,
    ) = (0..2).any { start ->
        way.withIndex().all { (i, v) ->
            !v.isNaN() &&
                abs(v - curve((i - start) * 16.0)) < tolerance
        }
    }

    @Test
    fun `the plate rides up on the hard-stop, then its words open from the left`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var visible by mutableStateOf(true)
            telegram({ visible }, reduce = false, heard = heard)
            val frames =
                (0 until 24).map {
                    mainClock.advanceTimeByFrame()
                    stage()
                }
            val done = frames.last()
            val full = done.words()
            assertTrue(full > 300, "the words are drawn at rest: $full")
            val tops = frames.map { it.top() }
            assertTrue(
                follows(tops, .6) { ms -> 60 + 80 * (1 - hardStop((ms / 200).coerceIn(0.0, 1.0))) },
                "the plate's top per frame: $tops",
            )
            // The words' share, against the share of the finished words left of the wiped edge: the
            // message box runs from 16 to 284, so at p the edge is at 16 + 268 p.
            val shares = frames.map { it.words() / full }
            assertTrue(
                follows(shares, .04) { ms ->
                    val p = ((ms - 200) / 120).coerceIn(0.0, 1.0)
                    done.words(0, (16 + 268 * p).toInt()) / full
                },
                "the words' share per frame: $shares",
            )
            assertEquals(listOf(PlakatEvent.Telegram), heard)
        }

    @Test
    fun `hidden, the words go at once and the plate rides down the same way`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            val hidden = mutableListOf<Long>()
            var visible by mutableStateOf(true)
            telegram({ visible }, reduce = false, heard = heard, hidden = hidden)
            mainClock.advanceTimeBy(600)
            visible = false
            val frames =
                (0 until 18).map {
                    mainClock.advanceTimeByFrame()
                    stage()
                }
            // From the first frame the plate moves, its words are gone (none left above the plate's ink).
            val moving = frames.indexOfFirst { abs(it.top() - 60) > .01 }
            assertTrue(moving in 0..2, "the plate starts down at frame $moving")
            assertEquals(0.0, frames[moving].words(), 1.0)
            val tops = frames.map { it.top() }
            assertTrue(
                follows(tops.takeWhile { !it.isNaN() }, .6) { ms -> 60 + 80 * hardStop((ms / 200).coerceIn(0.0, 1.0)) },
                "the plate's top per frame: $tops",
            )
            // Once down, nothing of it is drawn, and the owner heard once.
            assertTrue(frames.last().top().isNaN())
            assertEquals(1, hidden.size)
            assertEquals(listOf(PlakatEvent.Telegram, PlakatEvent.Telegram), heard)
        }

    @Test
    fun `under reduced motion it fades in and out over 100 ms at its stop`() =
        runComposeUiTest {
            var visible by mutableStateOf(true)
            val hidden = mutableListOf<Long>()
            telegram({ visible }, reduce = true, hidden = hidden)

            fun alpha(px: PixelMap) = px.plate(88)
            val fadeIn =
                (0 until 9).map {
                    mainClock.advanceTimeByFrame()
                    stage()
                }
            assertTrue(
                follows(
                    fadeIn.map {
                        alpha(it)
                    },
                    .02,
                ) { ms -> (ms / 100).coerceIn(0.0, 1.0) },
                "in: ${fadeIn.map { alpha(it) }}",
            )
            // At its stop all along: nothing of it below row 116.
            for (f in fadeIn) assertEquals(0.0, f.plate(118))
            visible = false
            val fadeOut =
                (0 until 9).map {
                    mainClock.advanceTimeByFrame()
                    stage()
                }
            assertTrue(
                follows(fadeOut.map { alpha(it) }, .02) { ms -> 1 - (ms / 100).coerceIn(0.0, 1.0) },
                "out: ${fadeOut.map { alpha(it) }}",
            )
            assertEquals(1, hidden.size)
        }

    @Test
    fun `the action is a 96 by 56 target that answers, inverted while pressed`() =
        runComposeUiTest {
            var acted = 0
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper) {
                        PlakatSnackbar(
                            "Статья удалена",
                            Modifier
                                .size(
                                    300.dp,
                                    56.dp,
                                ).testTag("s"),
                            actionLabel = "ВЕРНУТЬ",
                            onAction = {
                                acted++
                            },
                        )
                    }
                }
            }
            onNodeWithTag(
                "s",
            ).assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
            onNodeWithText("ВЕРНУТЬ").assertWidthIsEqualTo(96.dp).assertHeightIsEqualTo(56.dp).performClick()
            assertEquals(1, acted)
            onNodeWithText("ВЕРНУТЬ").performTouchInput { down(center) }
            waitForIdle()
            // The action's box is the last 96 of the plate: its corner on paper while pressed.
            assertEquals(paper.paper, onNodeWithTag("s").captureToImage().toPixelMap()[206, 2])
            onNodeWithText("ВЕРНУТЬ").performTouchInput { up() }
            waitForIdle()
            assertEquals(paper.ink, onNodeWithTag("s").captureToImage().toPixelMap()[206, 2])
        }

    @Test
    fun `an error carries the theme's word before the message`() =
        runComposeUiTest {
            setContent { PlakatTheme { PlakatSnackbar("Нет сети — повторим сами.", error = true) } }
            onNodeWithText("ОШИБКА. Нет сети — повторим сами.").assertIsDisplayed()
        }
}
