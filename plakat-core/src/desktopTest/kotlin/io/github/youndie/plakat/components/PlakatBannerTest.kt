package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import io.github.youndie.plakat.theme.PlakatStrings
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** B-81: each kind's ground and word on both papers, a sentence that wraps, the action's target, danger without colour. */
@OptIn(ExperimentalTestApi::class)
class PlakatBannerTest {
    private val papers = listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)

    private fun shot(
        colors: PlakatColors,
        kind: PlakatStatusKind,
    ): PixelMap {
        lateinit var px: PixelMap
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = colors) {
                        Box(Modifier.width(390.dp).background(colors.paper).testTag("b")) {
                            PlakatBanner(kind, "Two checks failed")
                        }
                    }
                }
            }
            px = onNodeWithTag("b").captureToImage().toPixelMap()
        }
        return px
    }

    private fun near(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < .02f && abs(a.green - b.green) < .02f && abs(a.blue - b.blue) < .02f

    private fun luma(c: Color) = (255 * (.2126f * c.red + .7152f * c.green + .0722f * c.blue)).roundToInt()

    @Test
    fun `each kind stands on its ground on both papers, with the theme's word`() {
        for (colors in papers) {
            assertTrue(near(shot(colors, PlakatStatusKind.Warning)[2, 2], colors.ocher), "warning's ocher")
            assertTrue(near(shot(colors, PlakatStatusKind.Info)[2, 2], colors.slate), "info's slate")
            val danger = shot(colors, PlakatStatusKind.Danger)
            assertTrue(near(danger[2, 20], colors.ink), "danger's plate")
            assertTrue((0 until 40).any { near(danger[it, 3], colors.red) }, "danger's hatch of red over the plate")
        }
        runComposeUiTest {
            setContent {
                PlakatTheme(strings = PlakatStrings(warning = "Внимание", info = "Инфо")) {
                    PlakatBanner(PlakatStatusKind.Warning, "Words")
                    PlakatBanner(PlakatStatusKind.Info, "Words")
                    PlakatBanner(PlakatStatusKind.Danger, "Words")
                }
            }
            onNodeWithText("ВНИМАНИЕ").assertExists()
            onNodeWithText("ИНФО").assertExists()
            onNodeWithText("ERROR").assertExists()
        }
    }

    @Test
    fun `danger is told from warning without colour`() {
        val colors = PlakatPalettes.Paper

        // The top rows: danger's hatch is two greys far apart; warning's ground is one.
        fun greys(px: PixelMap) = (0 until 6).flatMap { y -> (0 until 60).map { x -> luma(px[x, y]) } }.toSet()
        val danger = greys(shot(colors, PlakatStatusKind.Danger))
        val warning = greys(shot(colors, PlakatStatusKind.Warning))
        assertEquals(1, warning.size, "warning's top rows: $warning")
        assertTrue(danger.max() - danger.min() > 40, "danger's stripes in grey: $danger")
    }

    @Test
    fun `a long sentence wraps and the banner grows`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        Box(Modifier.width(390.dp)) {
                            PlakatBanner(
                                PlakatStatusKind.Warning,
                                "The board was built from a snapshot taken before the last three merges, so the numbers " +
                                    "below are older than the repository and will catch up once the connection returns",
                                Modifier.testTag("b"),
                                action = "Retry",
                            )
                        }
                    }
                }
            }
            assertTrue(onNodeWithTag("b").getUnclippedBoundsInRoot().height > 80.dp, "a banner of several lines")
            val action = onNodeWithText("RETRY").getUnclippedBoundsInRoot()
            assertTrue(action.right <= 390.dp, "the action stays in the banner")
        }

    @Test
    fun `the action is a 48 dp target that calls onAction`() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                PlakatTheme {
                    Box(Modifier.width(390.dp)) {
                        PlakatBanner(
                            PlakatStatusKind.Warning,
                            "No connection",
                            action = "Retry",
                            onAction = { clicks++ },
                        )
                    }
                }
            }
            val target = onNodeWithText("RETRY").getUnclippedBoundsInRoot()
            assertTrue(target.height >= 48.dp && target.width >= 48.dp, "the target: $target")
            onNodeWithText("RETRY").performClick()
            assertEquals(1, clicks)
        }

    @Test
    fun `a long action takes at most half the row, so the sentence keeps the rest`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        Box(Modifier.width(390.dp)) {
                            PlakatBanner(
                                PlakatStatusKind.Warning,
                                "No connection to GitHub",
                                action = "Retry the connection to GitHub once more",
                            )
                        }
                    }
                }
            }
            // The row: 390 less the word; the action at most half of what is left to it, the sentence the other half.
            val sentence = onNodeWithText("No connection to GitHub").getUnclippedBoundsInRoot()
            assertTrue(sentence.width > 100.dp, "the sentence keeps its share: ${sentence.width}")
        }

    @Test
    fun `a kind that does not warn is refused`() {
        for (kind in listOf(PlakatStatusKind.Primary, PlakatStatusKind.Success)) {
            assertFailsWith<IllegalArgumentException> {
                runComposeUiTest { setContent { PlakatTheme { PlakatBanner(kind, "Words") } } }
            }
        }
    }
}
