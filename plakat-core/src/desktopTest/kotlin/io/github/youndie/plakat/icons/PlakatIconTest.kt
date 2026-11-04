package io.github.youndie.plakat.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** B-11: an icon is a stencil in the colour of its content, on the 24 dp grid, holes cut. */
@OptIn(ExperimentalTestApi::class)
class PlakatIconTest {
    private val paper = PlakatPalettes.Paper

    private fun shot(content: @Composable () -> Unit): PixelMap {
        lateinit var px: PixelMap
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        Box(Modifier.size(48.dp).background(paper.concrete).testTag("cell")) { content() }
                    }
                }
            }
            px = onNodeWithTag("cell").captureToImage().toPixelMap()
        }
        return px
    }

    private fun PixelMap.inks(): Set<Color> =
        buildSet {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    this@inks[x, y].takeIf { it != paper.concrete }?.let(
                        ::add,
                    )
                }
            }
        }

    private fun close(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < 1f / 255 && abs(a.green - b.green) < 1f / 255 && abs(a.blue - b.blue) < 1f / 255

    @Test
    fun `an icon prints in the content colour and the theme's ink outside any`() {
        // Fully covered pixels of a 48 dp «add» are the tint itself; the rest is the edge's blend.
        val ink = shot { PlakatIcon(PlakatIcons.Add, null, Modifier.size(48.dp)) }
        assertTrue(close(ink[24, 24], paper.ink), "the theme's ink: ${ink[24, 24]}")
        val red =
            shot {
                CompositionLocalProvider(LocalPlakatContentColor provides paper.red) {
                    PlakatIcon(PlakatIcons.Add, null, Modifier.size(48.dp))
                }
            }
        assertTrue(close(red[24, 24], paper.red), "the content colour: ${red[24, 24]}")
        val tinted =
            shot {
                CompositionLocalProvider(LocalPlakatContentColor provides paper.red) {
                    PlakatIcon(PlakatIcons.Add, null, Modifier.size(48.dp), tint = paper.slate)
                }
            }
        assertTrue(close(tinted[24, 24], paper.slate), "an explicit tint wins: ${tinted[24, 24]}")
    }

    @Test
    fun `an icon is 24 dp unless sized and takes its description`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    PlakatIcon(PlakatIcons.Home, "ДОМОЙ")
                    PlakatIcon(PlakatIcons.Home, "БОЛЬШОЙ", Modifier.size(48.dp))
                }
            }
            onNodeWithContentDescription("ДОМОЙ")
                .assertWidthIsEqualTo(24.dp)
                .assertHeightIsEqualTo(24.dp)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            onNodeWithContentDescription("БОЛЬШОЙ").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        }

    @Test
    fun `holes drawn counter-clockwise are cut out`() {
        // «search» at 48 dp: the lens is a ring of radius 7 around a hole of radius 4, centred at (10, 10).
        val px = shot { PlakatIcon(PlakatIcons.Search, null, Modifier.size(48.dp)) }
        assertEquals(paper.concrete, px[20, 20], "the lens's hole")
        assertTrue(close(px[20, 9], paper.ink), "the ring above the hole: ${px[20, 9]}")
        // «delete»: the two slots in the can.
        val can = shot { PlakatIcon(PlakatIcons.Delete, null, Modifier.size(48.dp)) }
        assertEquals(paper.concrete, can[19, 30], "the left slot")
        assertTrue(close(can[24, 30], paper.ink), "between the slots: ${can[24, 30]}")
    }

    @Test
    fun `every stencil stays in the live area and its arcs are circles`() {
        for (stencil in PlakatIcons.All) {
            val b = stencil.path.getBounds()
            assertTrue(
                b.left >= 2f - 1e-3f && b.top >= 2f - 1e-3f && b.right <= 22f + 1e-3f && b.bottom <= 22f + 1e-3f,
                "$stencil: $b",
            )
        }
        // «error» is a circle of radius 10 at the centre, drawn as two half-arcs.
        assertBounds(Rect(2f, 2f, 22f, 22f), PlakatIcons.Error.path.getBounds())
        // A relative arc continues from the current point: half a circle from (2, 12) to (22, 12), clockwise.
        assertBounds(Rect(2f, 2f, 22f, 12f), stencilPath("M2 12a10 10 0 0 1 20 0z").getBounds())
        assertBounds(Rect(2f, 12f, 22f, 22f), stencilPath("M2 12a10 10 0 0 0 20 0z").getBounds())
    }

    @Test
    fun `a stencil refuses a curve it would not draw as the page does`() {
        assertFailsWith<IllegalStateException> { stencilPath("M2 2C4 4 6 6 8 8z") }
        assertFailsWith<IllegalArgumentException> { stencilPath("M2 12a10 5 30 0 1 20 0z") }
    }

    private fun assertBounds(
        want: Rect,
        got: Rect,
    ) {
        val off = listOf(want.left - got.left, want.top - got.top, want.right - got.right, want.bottom - got.bottom)
        assertTrue(off.all { abs(it) < 1e-3f }, "bounds $got, want $want")
    }
}
