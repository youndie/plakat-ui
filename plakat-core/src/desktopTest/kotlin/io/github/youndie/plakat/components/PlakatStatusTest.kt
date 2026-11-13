package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import io.github.youndie.plakat.parity.STATUS_WORDS
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-75: each kind's sign is its token's form, danger differs from primary without colour, and a reader hears the word. */
@OptIn(ExperimentalTestApi::class)
class PlakatStatusTest {
    /** The 20 × 20 sign of [kind] on [colors]' paper, captured at density 1. */
    private fun sign(
        kind: PlakatStatusKind,
        colors: PlakatColors,
    ): PixelMap {
        var px: PixelMap? = null
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = colors) {
                        Box(Modifier.size(200.dp, 20.dp).background(colors.paper).testTag("cell")) {
                            PlakatStatus(kind, STATUS_WORDS.getValue(kind))
                        }
                    }
                }
            }
            px = onNodeWithTag("cell").captureToImage().toPixelMap()
        }
        return px!!
    }

    private fun PixelMap.inSign() = (0 until SIGN).flatMap { y -> (0 until SIGN).map { x -> this[x, y] } }

    private fun near(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < .02f && abs(a.green - b.green) < .02f && abs(a.blue - b.blue) < .02f

    private fun luma(c: Color) = (255 * (.2126f * c.red + .7152f * c.green + .0722f * c.blue)).roundToInt()

    @Test
    fun `a fill fills the sign with its kind's colour`() {
        for (colors in listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)) {
            for ((kind, fill) in listOf(
                PlakatStatusKind.Primary to colors.red,
                PlakatStatusKind.Warning to colors.ocher,
                PlakatStatusKind.Info to colors.slate,
            )) {
                val off = sign(kind, colors).inSign().count { !near(it, fill) }
                assertEquals(0, off, "$kind: pixels of the sign not its fill")
            }
        }
    }

    @Test
    fun `danger is the hatch of red and paper`() {
        for (colors in listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)) {
            val px = sign(PlakatStatusKind.Danger, colors).inSign()
            val red = px.count { near(it, colors.red) }
            val paper = px.count { near(it, colors.paper) }
            assertEquals(SIGN * SIGN, red + paper, "every pixel one paint or the other")
            // A square of 20 holds no whole number of 6 dp periods along its diagonal, so not exactly half.
            assertTrue(red in SIGN * SIGN * 2 / 5..SIGN * SIGN * 3 / 5, "red $red, paper $paper")
            // Along a row the paints alternate every 3 dp of the 45° stripe's width, √2 · 3 ≈ 4.2 px across.
            val row =
                sign(PlakatStatusKind.Danger, colors).let { m ->
                    (0 until SIGN).map { near(m[it, SIGN / 2], colors.red) }
                }
            val runs = row.zipWithNext().count { (a, b) -> a != b } + 1
            assertTrue(runs >= 4, "stripes across a row: $row")
        }
    }

    @Test
    fun `success is the ink wedge-check on the paper`() {
        for (colors in listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)) {
            val px = sign(PlakatStatusKind.Success, colors)
            // Outside: the top-left corner and the bottom-right; inside: the long arm's top and the short arm's foot.
            for ((x, y) in listOf(2 to 2, 18 to 18)) assertTrue(near(px[x, y], colors.paper), "($x, $y) is paper")
            for ((x, y) in listOf(18 to 4, 7 to 15)) assertTrue(near(px[x, y], colors.ink), "($x, $y) is ink")
        }
    }

    @Test
    fun `danger is told from primary without colour`() {
        val colors = PlakatPalettes.Paper
        val danger = sign(PlakatStatusKind.Danger, colors).inSign().map(::luma).toSet()
        val primary = sign(PlakatStatusKind.Primary, colors).inSign().map(::luma).toSet()
        assertEquals(1, primary.size, "primary is one grey: $primary")
        assertTrue(danger.size >= 2 && (danger.max() - danger.min()) > 60, "danger's stripes in grey: $danger")
    }

    @Test
    fun `it is 20 dp high, taller with a large font, and a reader hears the word`() =
        runComposeUiTest {
            var scale by mutableFloatStateOf(1f)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, scale)) {
                    PlakatTheme {
                        Box(Modifier.size(300.dp, 200.dp)) {
                            PlakatStatus(PlakatStatusKind.Danger, "Failing", Modifier.testTag("s"))
                        }
                    }
                }
            }
            assertEquals(20.dp, onNodeWithTag("s").getUnclippedBoundsInRoot().height)
            onNodeWithContentDescription("Failing").assertExists()
            scale = 2f
            waitForIdle()
            assertTrue(onNodeWithTag("s").getUnclippedBoundsInRoot().height > 20.dp, "grows with the word")
        }

    private companion object {
        const val SIGN = 20
    }
}
