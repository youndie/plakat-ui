package io.github.youndie.plakat.print

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-07: each texture layer follows its print switch, and the papers lay their tile at 256 dp. */
@OptIn(ExperimentalTestApi::class)
class PlakatTexturesTest {
    private fun shot(
        colors: PlakatColors = PlakatPalettes.Paper,
        print: PlakatPrintSwitches,
        density: Float = 1f,
        element: @Composable () -> Modifier,
    ): PixelMap {
        lateinit var px: PixelMap
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(density)) {
                    PlakatTheme(colors = colors, print = print) {
                        Box(Modifier.size(300.dp, 40.dp).then(element()).testTag("t"))
                    }
                }
            }
            px = onNodeWithTag("t").captureToImage().toPixelMap()
        }
        return px
    }

    private fun PixelMap.colours(): Set<Color> =
        buildSet {
            for (y in 0 until height) for (x in 0 until width) add(this@colours[x, y])
        }

    private fun close(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < 1.5f / 255 && abs(a.green - b.green) < 1.5f / 255 && abs(a.blue - b.blue) < 1.5f / 255

    private val on = PlakatPrintSwitches(texture = true, nonprint = true)
    private val off = PlakatPrintSwitches(texture = false, nonprint = false)

    @Test
    fun `the paper is its tile with the texture on and flat with it off`() {
        for (colors in listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)) {
            assertEquals(setOf(colors.paper), shot(colors, off) { Modifier.plakatPaper() }.colours(), "flat paper")
            val textured = shot(colors, on) { Modifier.plakatPaper() }
            assertTrue(textured.colours().size > 10, "textured paper has grain: ${textured.colours().size} colours")
            // Most of the tile is the paper's own colour within a level: a tile of the other theme is not.
            val near = (0 until 300).count { close(textured[it, 20], colors.paper) }
            assertTrue(near > 200, "pixels of the row near the paper's colour: $near of 300")
        }
    }

    @Test
    fun `the paper's tile repeats every 256 dp at any density`() {
        for (density in listOf(1f, 2f)) {
            val px = shot(print = on, density = density) { Modifier.plakatPaper() }
            val period = (256 * density).toInt()
            val rows = (0 until px.height).toList()
            for (x in 0 until px.width - period) {
                for (y in rows) assertEquals(px[x, y], px[x + period, y], "density $density at $x, $y")
            }
            // Not a pattern with a shorter period that would pass as well.
            val half = period / 2
            assertTrue((0 until half).any { x -> rows.any { y -> px[x, y] != px[x + half, y] } }, "half a tile differs")
        }
    }

    @Test
    fun `concrete is textured under Paper only`() {
        assertTrue(shot(print = on) { Modifier.plakatConcrete() }.colours().size > 10, "light concrete")
        assertEquals(setOf(PlakatPalettes.Paper.concrete), shot(print = off) { Modifier.plakatConcrete() }.colours())
        assertEquals(
            setOf(PlakatPalettes.BlackPaper.concrete),
            shot(PlakatPalettes.BlackPaper, on) { Modifier.plakatConcrete() }.colours(),
            "the design has no dark concrete tile",
        )
    }

    @Test
    fun `the grain and the non-print print over a red plate only while switched on`() {
        val red = PlakatPalettes.Paper.red
        for (layer in listOf<@Composable () -> Modifier>({ Modifier.plakatGrain() }, { Modifier.plakatNonprint() })) {
            assertEquals(setOf(red), shot(print = off) { Modifier.background(red).then(layer()) }.colours())
            assertTrue(shot(print = on) { Modifier.background(red).then(layer()) }.colours().size > 5)
        }
        // Each follows its own switch.
        assertEquals(
            setOf(red),
            shot(print = PlakatPrintSwitches(texture = true, nonprint = false)) {
                Modifier.background(red).plakatNonprint()
            }.colours(),
        )
        assertEquals(
            setOf(red),
            shot(print = PlakatPrintSwitches(texture = false, nonprint = true)) {
                Modifier.background(red).plakatGrain()
            }.colours(),
        )
    }

    @Test
    fun `the grain only darkens`() {
        // Multiply: no pixel of a grained plate is lighter than the plate.
        val red = PlakatPalettes.Paper.red
        val px = shot(print = on) { Modifier.background(red).plakatGrain() }
        for (c in px.colours()) assertTrue(c.red <= red.red + 1e-3f && c.green <= red.green + 1e-3f, "$c over $red")
    }
}
