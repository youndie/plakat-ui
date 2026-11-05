package io.github.youndie.plakat.components

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
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-39: Широкий. The title and the slogan on the left half, the composition on the right, as wide as its box. */
@OptIn(ExperimentalTestApi::class)
class PlakatPosterWideTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.wide(
        width: Int,
        onAction: () -> Unit = {},
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = true, print = PlakatPrintSwitches(registration = false)) {
                    PlakatPoster(
                        listOf("ЧИТАЙ", "ВЕСЬ"),
                        "ПОДПИСАТЬСЯ",
                        onAction,
                        Modifier.width(width.dp).testTag("p"),
                        layout = PlakatPosterLayout.Wide,
                        body = "Двенадцать выпусков.",
                        seed = 1919,
                    )
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("p").captureToImage().toPixelMap()

    @Test
    fun `it is as wide as its box and 392 high`() =
        runComposeUiTest {
            wide(800)
            onNodeWithTag("p").assertWidthIsEqualTo(800.dp).assertHeightIsEqualTo(392.dp)
        }

    @Test
    fun `the composition stands on the right half and against its edge, however wide`() {
        for (width in listOf(600, 720, 900)) {
            runComposeUiTest {
                wide(width)
                val px = frame()
                val half = width / 2
                // The red circle is the composition's, and the slogan the only other red; above the slogan's top.
                val redLeft =
                    (0 until half).any { x -> (0 until 300).any { y -> px[x, y] == paper.red } }
                assertTrue(!redLeft, "no composition on the left at $width")
                val redRight =
                    (half until width).any { x -> (0 until 392).any { y -> px[x, y] == paper.red } }
                assertTrue(redRight, "the composition on the right at $width")
            }
        }
    }

    @Test
    fun `the title, its body and the slogan stand on the left`() =
        runComposeUiTest {
            wide(720)
            val px = frame()
            assertTrue((16 until 200).any { x -> (32 until 80).any { y -> px[x, y] == paper.ink } }, "the title")
            // The slogan 24 up from the foot, 48 high, 240 wide from the margin.
            assertEquals(paper.red, px[20, 392 - 24 - 4])
            assertEquals(paper.red, px[250, 392 - 24 - 4])
            assertEquals(paper.paper, px[262, 392 - 24 - 4], "past its 240")
        }

    @Test
    fun `its words are read and the slogan acts`() =
        runComposeUiTest {
            var acted = 0
            wide(720) { acted++ }
            onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertIsDisplayed()
            onNodeWithText("ПОДПИСАТЬСЯ").assertHeightIsEqualTo(48.dp).performClick()
            waitForIdle()
            assertEquals(1, acted)
        }
}
