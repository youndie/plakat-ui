package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import io.github.youndie.plakat.parity.TYPE_SPECIMENS
import io.github.youndie.plakat.parity.TypeSpecimen
import io.github.youndie.plakat.theme.PlakatTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * B-78: a consumer's words in a role sit where Chrome puts them. Each role's specimen, set by `PlakatParagraph` in a box
 * of its reference's size, inks the reference's rows; set by `BasicText` in the role's own style, `displayM` inks lower.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatParagraphTest {
    /** The first and last row with ink in them, ink being a pixel nearer the ink than the paper. */
    private class Rows(
        val first: Int,
        val last: Int,
    ) {
        override fun toString() = "$first..$last"
    }

    private fun rows(
        luma: (Int, Int) -> Float,
        width: Int,
        height: Int,
    ): Rows {
        val inked = (0 until height).filter { y -> (0 until width).any { x -> luma(x, y) < .5f } }
        return Rows(inked.first(), inked.last())
    }

    private fun png(path: String): Rows {
        val png = ImageIO.read(File(path))
        return rows({ x, y ->
            val c = Color(png.getRGB(x, y))
            .2126f * c.red + .7152f * c.green + .0722f * c.blue
        }, png.width, png.height)
    }

    /** Chrome's specimen (scripts/design-references.mjs). */
    private fun reference(s: TypeSpecimen) = png("src/desktopTest/snapshots/design/Type_${s.name}_Light.png")

    /** The fixture's golden, which `viddikVerify` holds to what `PlakatParagraph` draws today. */
    private fun golden(s: TypeSpecimen) = png("src/desktopTest/snapshots/Type_${s.name}_Light.png")

    /**
     * The cell in a scene of its own at density 1, rendered until the fonts are in. It lands a fraction of a pixel higher
     * than viddik's capture of the same fixture, so it is only compared with itself.
     */
    private fun captured(
        s: TypeSpecimen,
        basic: Boolean,
    ): Rows {
        val scene =
            ImageComposeScene(s.width, s.height, Density(1f)) {
                PlakatTheme {
                    val role = s.role(PlakatTheme.typography)
                    Box(Modifier.size(s.width.dp, s.height.dp).background(PlakatTheme.colors.paper)) {
                        if (basic) {
                            BasicText(role.text(s.text), style = role.style.copy(color = PlakatTheme.colors.ink))
                        } else {
                            PlakatParagraph(s.text, role)
                        }
                    }
                }
            }
        try {
            repeat(FRAMES) { scene.render(it * FRAME_NANOS) }
            val px = scene.render(FRAMES * FRAME_NANOS).toComposeImageBitmap().toPixelMap()
            return rows(
                { x, y -> px[x, y].let { .2126f * it.red + .7152f * it.green + .0722f * it.blue } },
                px.width,
                px.height,
            )
        } finally {
            scene.close()
        }
    }

    @Test
    fun `every role inks the rows its Chrome reference inks`() {
        val off =
            TYPE_SPECIMENS.mapNotNull { s ->
                val want = reference(s)
                val got = golden(s)
                if (got.first != want.first || got.last != want.last) "${s.name}: $got, Chrome $want" else null
            }
        assertTrue(off.isEmpty(), off.joinToString("; "))
    }

    @Test
    fun `the same words through BasicText land lower than PlakatParagraph sets them`() {
        val shifts =
            TYPE_SPECIMENS.associate { s ->
                s.name to captured(s, basic = true).first - captured(s, basic = false).first
            }
        println("BasicText's first inked row below PlakatParagraph's: $shifts")
        assertTrue(shifts.getValue("DisplayM") >= DISPLAY_SHIFT, "displayM: $shifts")
        for (role in listOf("DisplayL", "Headline", "Button")) assertTrue(shifts.getValue(role) > 0, "$role: $shifts")
        // Golos on a line taller than its face: Compose's centring and Blink's floored half-leading agree.
        for (role in listOf("Title", "BodyM", "BodyS")) assertEquals(0, shifts.getValue(role), "$role: $shifts")
    }

    @Test
    fun `past maxLines it stops on a whole line and a reader reads every word`() =
        runComposeUiTest {
            val s = TYPE_SPECIMENS.first { it.name == "BodyM" }
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        Box(Modifier.size(560.dp, 200.dp)) {
                            PlakatParagraph(s.text, PlakatTheme.typography.bodyM, Modifier.testTag("p"), maxLines = 1)
                        }
                    }
                }
            }
            // One line of 16 × 1.4 = 22.4, rounded as Blink snaps what stands under it.
            assertEquals(22.dp, onNodeWithTag("p").getUnclippedBoundsInRoot().height)
            onNodeWithText(s.text).assertExists()
        }

    @Test
    fun `an ellipsis and a clip end the last line differently`() {
        fun last(overflow: TextOverflow): Int {
            var inked = 0
            runComposeUiTest {
                setContent {
                    CompositionLocalProvider(LocalDensity provides Density(1f)) {
                        PlakatTheme {
                            Box(Modifier.size(200.dp, 40.dp).background(PlakatTheme.colors.paper).testTag("cell")) {
                                PlakatParagraph(
                                    "Павильон стоит три месяца, потом его разбирают",
                                    PlakatTheme.typography.bodyM,
                                    maxLines = 1,
                                    overflow = overflow,
                                )
                            }
                        }
                    }
                }
                val px = onNodeWithTag("cell").captureToImage().toPixelMap()
                inked = (0 until px.width).last { x -> (0 until px.height).any { y -> px[x, y].red < .5f } }
            }
            return inked
        }
        assertNotEquals(last(TextOverflow.Ellipsis), last(TextOverflow.Clip), "the ellipsis's dots move the last ink")
    }

    @Test
    fun `given no width it is one line as long as its words`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        Row(Modifier.horizontalScroll(rememberScrollState())) {
                            PlakatParagraph(
                                "ПЛАН ВЫПОЛНЕН НА 95 %",
                                PlakatTheme.typography.displayM,
                                Modifier.testTag("p"),
                            )
                        }
                    }
                }
            }
            val bounds = onNodeWithTag("p").getUnclippedBoundsInRoot()
            // 48 × 0.95 = 45.6, one line.
            assertEquals(46.dp, bounds.height)
            assertTrue(bounds.width > 600.dp, "one line: ${bounds.width}")
        }

    @Test
    fun `in a wider box it is as wide as its words, as BasicText is`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        Box(Modifier.size(400.dp, 100.dp)) {
                            PlakatParagraph("ИНФО", PlakatTheme.typography.label, Modifier.testTag("p"))
                        }
                    }
                }
            }
            val width = onNodeWithTag("p").getUnclippedBoundsInRoot().width
            assertTrue(width in 1.dp..80.dp, "a word's width, not the box's: $width")
        }

    private companion object {
        const val FRAMES = 8
        const val FRAME_NANOS = 16_000_000L

        // The issue measured 6 px for displayM against a Chrome canvas; this scene shows 8 (B-78's findings).
        const val DISPLAY_SHIFT = 6
    }
}
