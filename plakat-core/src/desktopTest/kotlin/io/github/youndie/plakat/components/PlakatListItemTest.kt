package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
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

/** B-23: `stamp`, the row inverted on the frame it lands and back in 60 ms, reduced motion or not («то же»). */
@OptIn(ExperimentalTestApi::class)
class PlakatListItemTest {
    private val paper = PlakatPalettes.Paper

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

    private fun ComposeUiTest.row(
        reduce: Boolean,
        heard: MutableList<PlakatEvent>,
        shown: MutableList<Color> = mutableListOf(),
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.background(Color.White)) {
                        PlakatListItem(
                            "Город",
                            Modifier.width(300.dp).testTag("r"),
                            supporting = "Анна",
                            number = "01",
                            trailing = { shown += LocalPlakatContentColor.current },
                            onClick = {},
                        )
                    }
                }
            }
        }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("r").captureToImage().toPixelMap()

    /** The ground's share at a clear part of the row: 0 the white stage, 1 the ink. */
    private fun PixelMap.ground() = ((1f - this[280, 8].green) / (1f - paper.ink.green)).toDouble()

    private fun returns(reduce: Boolean) =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            val shown = mutableListOf<Color>()
            row(reduce, heard, shown)
            assertEquals(0.0, frame().ground(), .01)
            onNodeWithTag("r").performTouchInput { down(center) }
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            assertEquals(1.0, frame().ground(), .01)
            assertEquals(paper.paper, shown.last())
            onNodeWithTag("r").performTouchInput { up() }
            val way =
                (0 until 7).map {
                    mainClock.advanceTimeByFrame()
                    frame().ground()
                }
            val fits =
                (0..2).any { start ->
                    way.withIndex().all { (i, g) ->
                        abs(g - (1 - hardStop(((i - start) * 16.0 / 60).coerceIn(0.0, 1.0)))) < .03
                    }
                }
            assertTrue(fits, "the ground per frame: $way")
            // The words change the moment the finger lifts.
            assertEquals(paper.ink, shown.last())
            assertEquals(listOf(PlakatEvent.Stamp, PlakatEvent.Release), heard)
        }

    @Test
    fun `pressed the row inverts on one frame and its ground returns in 60 ms`() = returns(reduce = false)

    @Test
    fun `under reduced motion it does the same`() = returns(reduce = true)

    @Test
    fun `a row is 73 with a supporting line and 57 without counting its divider and a tap target`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    Column {
                        PlakatListItem("А", Modifier.testTag("two"), supporting = "Б", onClick = {})
                        PlakatListItem("А", Modifier.testTag("one"), onClick = {})
                        PlakatListItem("А", Modifier.testTag("bare"), divider = false)
                        PlakatListItem("А", Modifier.testTag("off"), onClick = {}, enabled = false)
                    }
                }
            }
            onNodeWithTag("two")
                .assertHeightIsEqualTo(73.dp)
                .assertHasClickAction()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            onNodeWithTag("one").assertHeightIsEqualTo(57.dp)
            onNodeWithTag("bare").assertHeightIsEqualTo(56.dp).assertHasNoClickAction()
            onNodeWithTag("off").assertIsNotEnabled()
        }

    @Test
    fun `a divider is a full-width ink line of its weight`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper) {
                        Column(Modifier.width(100.dp)) {
                            PlakatDivider(Modifier.testTag("hair"))
                            PlakatDivider(Modifier.testTag("rule"), PlakatDividerWeight.Rule)
                            PlakatDivider(Modifier.testTag("bar"), PlakatDividerWeight.Bar)
                        }
                    }
                }
            }
            onNodeWithTag("hair").assertHeightIsEqualTo(1.dp)
            onNodeWithTag("rule").assertHeightIsEqualTo(3.dp)
            onNodeWithTag("bar").assertHeightIsEqualTo(8.dp)
            assertEquals(paper.ink, onNodeWithTag("bar").captureToImage().toPixelMap()[99, 7])
        }
}
