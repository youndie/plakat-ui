package io.github.youndie.plakat.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatFab
import io.github.youndie.plakat.components.PlakatIconButton
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-77: the kit's own stencils, the outline of a stroke, and a stencil an app makes itself. */
@OptIn(ExperimentalTestApi::class)
class PlakatKitIconsTest {
    private fun shot(
        colors: PlakatColors = PlakatPalettes.Paper,
        size: Int = 48,
        content: @Composable () -> Unit,
    ): PixelMap {
        lateinit var px: PixelMap
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = colors) {
                        Box(Modifier.size(size.dp).background(colors.paper).testTag("cell")) { content() }
                    }
                }
            }
            px = onNodeWithTag("cell").captureToImage().toPixelMap()
        }
        return px
    }

    private fun near(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < .03f && abs(a.green - b.green) < .03f && abs(a.blue - b.blue) < .03f

    /** The pixels at least half covered by [ink]: nearer it than the paper. */
    private fun PixelMap.mask(
        ink: Color,
        paper: Color,
    ): Set<Pair<Int, Int>> =
        buildSet {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val c = this@mask[x, y]
                    val toInk = abs(c.red - ink.red) + abs(c.green - ink.green) + abs(c.blue - ink.blue)
                    val toPaper = abs(c.red - paper.red) + abs(c.green - paper.green) + abs(c.blue - paper.blue)
                    if (toInk < toPaper) add(x to y)
                }
            }
        }

    /** The stroke ↗ came from, as `scripts/kit-icons.json` records it. */
    private val stroke =
        Regex(""""stroke":\s*\{\s*"d":\s*"([^"]+)"""")
            .find(File("../scripts/kit-icons.json").readText())!!
            .groupValues[1]

    @Test
    fun `the outlined arrow is within a pixel of the stroke it came from`() {
        val colors = PlakatPalettes.Paper
        val filled =
            shot {
                PlakatIcon(
                    PlakatIcons.OpensElsewhere,
                    null,
                    Modifier.size(48.dp),
                )
            }.mask(colors.ink, colors.paper)
        val stroked =
            shot {
                Canvas(Modifier.size(48.dp)) {
                    scale(2f, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                        drawPath(
                            stencilPath(stroke),
                            colors.ink,
                            style = Stroke(3f, cap = StrokeCap.Butt, join = StrokeJoin.Miter),
                        )
                    }
                }
            }.mask(colors.ink, colors.paper)

        fun Set<Pair<Int, Int>>.within(other: Set<Pair<Int, Int>>) =
            filter { (x, y) -> (-1..1).none { dx -> (-1..1).any { dy -> (x + dx to y + dy) in other } } }
        assertTrue(stroked.size > 300, "the stroke drew: ${stroked.size}")
        assertEquals(emptyList(), filled.within(stroked), "filled pixels more than a pixel from the stroke")
        assertEquals(emptyList(), stroked.within(filled), "stroked pixels more than a pixel from the fill")
        assertTrue(abs(filled.size - stroked.size) < filled.size / 20, "${filled.size} against ${stroked.size}")
    }

    @Test
    fun `more is three squares of 4 on the 24 grid`() {
        val colors = PlakatPalettes.Paper
        val px = shot(size = 24) { PlakatIcon(PlakatIcons.More, null, Modifier.size(24.dp)) }
        for (y in listOf(3, 6, 10, 13, 17, 20)) {
            for (x in 10..13) assertTrue(near(px[x, y], colors.ink), "($x, $y) is ink")
        }
        for (y in listOf(1, 8, 9, 15, 16, 22)) assertTrue(near(px[11, y], colors.paper), "(11, $y) is paper")
        for (x in listOf(8, 15)) assertTrue(near(px[x, 12], colors.paper), "($x, 12) is paper")
    }

    @Test
    fun `both take a filled button's on-colour on both papers`() {
        for (colors in listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)) {
            for (icon in listOf(PlakatIcons.More, PlakatIcons.OpensElsewhere)) {
                val px = shot(colors, size = 120) { PlakatFab(icon, icon.name, {}, rideIn = false) }
                val onRed =
                    (0 until px.height).sumOf { y ->
                        (0 until px.width).count { x -> near(px[x, y], colors.onRed) }
                    }
                assertTrue(onRed > 40, "$icon on the FAB in ${colors.paper}: $onRed on-red pixels")
            }
        }
    }

    @Test
    fun `an app's own stencil draws in an icon button`() {
        val colors = PlakatPalettes.Paper
        val square = PlakatStencil("app/icon/square", "M6 6H18V18H6Z")
        val px = shot { PlakatIconButton(square, "Square", {}) }
        assertTrue(near(px[24, 24], colors.ink), "the square's middle is ink")
        assertTrue(near(px[24, 13], colors.paper), "above the square is paper")
    }
}
