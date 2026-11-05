package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-17: print (a diagonal wipe from the top-left, 200 linear, cascade 30; reduced motion fade 100)
 * and tear (a ride of 110 % to the right, 200 hard-stop, then the place closes up, 200 hard-stop).
 * Cards are 200 × 88 here, on a black ground so the concrete sheet reads with a wide margin.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatCardTest {
    private val paper = PlakatPalettes.Paper
    private val ground = Color.Black

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

    /** How much of row [y] of card [tag] the sheet covers, its antialiased edge counted in part. */
    private fun ComposeUiTest.covered(
        tag: String,
        y: Int,
    ): Double {
        val px = onNodeWithTag(tag).captureToImage().toPixelMap()
        return (0 until px.width).sumOf { x -> (px[x, y].green / paper.concrete.green).coerceIn(0f, 1f).toDouble() }
    }

    private fun ComposeUiTest.cards(
        reduce: Boolean,
        heard: MutableList<PlakatEvent>,
        vararg orders: Int?,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Column(Modifier.background(ground)) {
                        orders.forEachIndexed { i, order ->
                            PlakatCard({}, Modifier.width(200.dp).testTag("c$i"), printOrder = order) {
                                Spacer(Modifier.size(64.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    /** The wipe's edge at [p] crosses row [y] of an 88 high card this far from the left: `p (w + 60) − 60 y / h`. */
    private fun edge(
        p: Double,
        y: Int,
    ) = (p * 260 - 60 * (y + .5) / 88).coerceIn(0.0, 200.0)

    @Test
    fun `cards print by a diagonal wipe in 200 ms, linear, each 30 ms after the one before`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            cards(reduce = false, heard = heard, 0, 2)
            val way =
                (0 until 20).map {
                    mainClock.advanceTimeByFrame()
                    listOf(covered("c0", 0), covered("c0", 87), covered("c1", 0), covered("c1", 87))
                }
            // The frame the play starts on is not pinned; it is one of the first three.
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, r) ->
                        val ms = (i - start) * 16.0
                        val first = (ms / 200).coerceIn(0.0, 1.0)
                        val second = ((ms - 60) / 200).coerceIn(0.0, 1.0)
                        abs(r[0] - edge(first, 0)) < 1.5 &&
                            abs(r[1] - edge(first, 87)) < 1.5 &&
                            abs(r[2] - edge(second, 0)) < 1.5 &&
                            abs(r[3] - edge(second, 87)) < 1.5
                    }
                }
            assertTrue(fits, "rows 0 and 87 of each card, per frame: ${way.map { r -> r.map { it.toInt() } }}")
            assertEquals(listOf(PlakatEvent.Print, PlakatEvent.Print), heard)
        }

    @Test
    fun `under reduced motion a card fades in over 100 ms, linear, with no cascade`() =
        runComposeUiTest {
            cards(reduce = true, heard = mutableListOf(), 3)
            val alphas =
                (0 until 9).map {
                    mainClock.advanceTimeByFrame()
                    val px = onNodeWithTag("c0").captureToImage().toPixelMap()
                    // Whole rows at once: no wipe, the sheet over black at the fade's opacity.
                    assertEquals(px[0, 0], px[199, 87])
                    (px[100, 44].green / paper.concrete.green).toDouble()
                }
            val fits =
                (0..2).any { start ->
                    alphas.withIndex().all { (i, a) -> abs(a - ((i - start) * 16.0 / 100).coerceIn(0.0, 1.0)) < .02 }
                }
            assertTrue(fits, "opacity per frame: $alphas")
        }

    @Test
    fun `a card without a print order is there at once`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            cards(reduce = false, heard = heard, null)
            mainClock.advanceTimeByFrame()
            assertEquals(200.0, covered("c0", 0), .01)
            assertEquals(200.0, covered("c0", 87), .01)
            assertEquals(emptyList(), heard)
        }

    private fun ComposeUiTest.tearing(
        reduce: Boolean,
        heard: MutableList<PlakatEvent>,
        gone: MutableList<Long>,
        dismissed: () -> Boolean,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Column(Modifier.background(ground).testTag("list")) {
                        PlakatCard(
                            {},
                            Modifier.width(200.dp).testTag("c"),
                            printOrder = null,
                            dismissed = dismissed(),
                            onDismissed = { gone += mainClock.currentTime },
                        ) { Spacer(Modifier.size(64.dp)) }
                        Spacer(Modifier.size(200.dp, 8.dp).testTag("next"))
                    }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    @Test
    fun `a dismissed card rides 110 percent right in 200 ms hard-stop, then its place closes up in 200`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            val gone = mutableListOf<Long>()
            var dismissed by mutableStateOf(false)
            tearing(reduce = false, heard = heard, gone = gone, dismissed = { dismissed })
            dismissed = true
            // Where the sheet's left edge is, and how tall the card's place is (where the next one
            // starts: the card's own bounds are clipped to the root once it has ridden out), per frame.
            val way =
                (0 until 30).map {
                    mainClock.advanceTimeByFrame()
                    val h =
                        onNodeWithTag("next")
                            .getBoundsInRoot()
                            .top.value
                            .toDouble()
                    // Read on the list: the card's own capture is empty once it has ridden out.
                    val left = if (h > 44) 200 - covered("list", 44) else Double.NaN
                    left to h
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, r) ->
                        val ms = (i - start) * 16.0
                        val ride = 220 * hardStop((ms / 200).coerceIn(0.0, 1.0))
                        val height = 88 * (1 - hardStop(((ms - 200) / 200).coerceIn(0.0, 1.0)))
                        (r.first.isNaN() || abs(r.first - ride.coerceAtMost(200.0)) < 1.5) &&
                            abs(r.second - height) <= 1.0
                    }
                }
            assertTrue(fits, "left edge and height per frame: ${way.map { "%.1f/%.1f".format(it.first, it.second) }}")
            // The next one has closed up under it; the owner heard once.
            assertEquals(0f, onNodeWithTag("next").getBoundsInRoot().top.value)
            assertEquals(1, gone.size)
            assertEquals(listOf(PlakatEvent.Tear), heard)
            // Put back, it is there at once.
            dismissed = false
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            onNodeWithTag("c").assertHeightIsEqualTo(88.dp)
            assertEquals(200.0, covered("c", 44), .01)
        }

    @Test
    fun `under reduced motion a dismissed card is gone at once`() =
        runComposeUiTest {
            val gone = mutableListOf<Long>()
            var dismissed by mutableStateOf(false)
            tearing(reduce = true, heard = mutableListOf(), gone = gone, dismissed = { dismissed })
            dismissed = true
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(0f, onNodeWithTag("next").getBoundsInRoot().top.value)
            assertEquals(1, gone.size)
        }

    @Test
    fun `pressed, the sheet turns ink and its content paper`() =
        runComposeUiTest {
            var content = Color.Unspecified
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper) {
                        Box(Modifier.background(ground)) {
                            PlakatCard({}, Modifier.width(200.dp).testTag("c"), printOrder = null) {
                                content = LocalPlakatContentColor.current
                                Spacer(Modifier.size(64.dp))
                            }
                        }
                    }
                }
            }
            assertEquals(paper.ink, content)
            onNodeWithTag("c").performTouchInput { down(center) }
            waitForIdle()
            assertEquals(paper.paper, content)
            assertEquals(paper.ink, onNodeWithTag("c").captureToImage().toPixelMap()[4, 4])
            onNodeWithTag("c").performTouchInput { up() }
            waitForIdle()
            assertEquals(paper.ink, content)
            assertEquals(paper.concrete, onNodeWithTag("c").captureToImage().toPixelMap()[4, 4])
        }

    @Test
    fun `the sheet is at least 48 tall, a button, selected when selected, and only shows without a handler`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    Column {
                        PlakatCard(
                            {},
                            Modifier.testTag("c"),
                            selected = true,
                            printOrder = null,
                        ) { Spacer(Modifier.size(8.dp)) }
                        PlakatCard(null, Modifier.testTag("shown"), printOrder = null) { Spacer(Modifier.size(8.dp)) }
                    }
                }
            }
            onNodeWithTag("c")
                .assertHeightIsEqualTo(48.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
            onNodeWithTag("shown").assertHasNoClickAction()
        }
}
