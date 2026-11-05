package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-52: the band, from its edges at 358 and across a wider screen, and the slogan under the words. */
@OptIn(ExperimentalTestApi::class)
class PlakatEmptyStateTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.state(
        width: Int = 358,
        colors: PlakatColors = paper,
        density: Float = 1f,
        onAction: () -> Unit = {},
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                PlakatTheme(colors = colors, reduceMotion = true) {
                    Box(Modifier.testTag("s")) {
                        PlakatEmptyState(
                            "СОХРАНИТЕ",
                            "Закладки появятся здесь.",
                            "К ЛЕНТЕ",
                            onAction,
                            Modifier.width(width.dp),
                        )
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("s").captureToImage().toPixelMap()

    @Test
    fun `the band stands 8 down, 120 high, its bar along the foot and the circle from the right`() =
        runComposeUiTest {
            state()
            val px = frame()
            // Over the band, the block's padding; the band's top on concrete; its foot the 8 of ink.
            assertEquals(paper.concrete, px[10, 8])
            assertEquals(paper.ink, px[10, 8 + 112])
            assertEquals(paper.ink, px[10, 8 + 119])
            // The circle's middle at 300 of 358, 40 down.
            assertEquals(paper.ink, px[300, 48])
            assertEquals(paper.ink, px[300 + 22, 48])
            assertEquals(paper.concrete, px[300 + 27, 8 + 5])
        }

    @Test
    fun `the wedge rises at 17 degrees to the right, red into concrete`() =
        runComposeUiTest {
            state()
            val px = frame()
            // At the left there is no room for it, at the right it is deep; above its edge there is only concrete.
            val rise = 0.30573
            assertEquals(paper.concrete, px[5, 8 + 100])
            assertEquals(paper.red, px[340, 8 + 108])
            for (x in listOf(60, 150, 250, 340)) {
                val edge = 120 - x * rise
                for (y in 0 until (edge - 2).toInt()) {
                    if (hypot(x - 300.0, y - 40.0) > 26) {
                        assertEquals(paper.concrete, px[x, 8 + y], "over the wedge at $x, $y")
                    }
                }
            }
        }

    @Test
    fun `its raskat prints red, at a density of its own pixels`() =
        runComposeUiTest {
            state(density = 2f)
            val px = frame()
            // Well inside the wedge at density 2, where a pixel is the field's own.
            val inks = (360 until 640 step 2).flatMap { x -> (150 until 220 step 2).map { y -> px[x, y] } }.toSet()
            assertTrue(paper.red in inks, "red: $inks")
        }

    @Test
    fun `on another width the circle keeps its place from the right`() =
        runComposeUiTest {
            state(width = 300)
            val px = frame()
            assertEquals(300, px.width)
            assertEquals(paper.ink, px[300 - 58, 48])
        }

    @Test
    fun `it takes the theme's palette`() =
        runComposeUiTest {
            val dark = PlakatPalettes.BlackPaper
            state(colors = dark)
            val px = frame()
            assertEquals(dark.concrete, px[10, 8])
            assertEquals(dark.ink, px[300, 48])
        }

    @Test
    fun `the title is a heading and the slogan is 200 by 48 and acts`() =
        runComposeUiTest {
            var acted = 0
            state(onAction = { acted++ })
            onNodeWithText("СОХРАНИТЕ").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            onNodeWithText("К ЛЕНТЕ").assertHeightIsEqualTo(48.dp).assertWidthIsEqualTo(200.dp).performClick()
            waitForIdle()
            assertEquals(1, acted)
        }
}
