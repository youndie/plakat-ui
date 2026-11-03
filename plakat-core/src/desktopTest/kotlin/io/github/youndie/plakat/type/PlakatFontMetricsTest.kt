package io.github.youndie.plakat.type

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Typeface
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-02: the metrics later items rest on, pinned to the files. The stretch text (B-31) fits a line
 * from cap top to baseline with the cap height; the counter (B-30) relies on Rubik Mono One being
 * monospace. A changed font file changes one of these numbers and fails here first.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatFontMetricsTest {
    /** At 1000 px a metric reads in font units. */
    private fun font(face: Typeface) = Font(face, 1000f)

    @Test
    fun `Rubik Mono One has a 700 cap height and an 850 advance for every glyph`() {
        val f = font(BundledFonts.rubikMonoOne)
        assertEquals(700f, f.metrics.capHeight, 0.5f, "cap height")
        val glyphs = BundledFonts.rubikMonoOne.getStringGlyphs("AMWi1ЛИСТЖщ.!")
        assertEquals(setOf(850f), f.getWidths(glyphs).toSet(), "advances")
    }

    @Test
    fun `Golos Text has a 700 cap height and a 530 x-height in both instances`() {
        for (face in listOf(BundledFonts.golosRegular, BundledFonts.golosSemiBold)) {
            val f = font(face)
            assertEquals(700f, f.metrics.capHeight, 0.5f, "${face.familyName} cap height")
            assertEquals(530f, f.metrics.xHeight, 0.5f, "${face.familyName} x-height")
        }
    }

    @Test
    fun `the Golos instances are static and weigh 400 and 600`() {
        assertEquals(400, BundledFonts.golosRegular.fontStyle.weight)
        assertEquals(600, BundledFonts.golosSemiBold.fontStyle.weight)
        // A static instance draws its weight with no axis setting, which is the point on Android 24.
        // Golos keeps one advance across weights, so the weight shows in the ink, not the width.
        val regular = ink(BundledFonts.golosRegular)
        val semi = ink(BundledFonts.golosSemiBold)
        assertTrue(semi > regular * 1.15f, "600 inks more than 400: $semi vs $regular pixels")
    }

    /** Dark pixels of "Н" drawn at 100 px in black on white. */
    private fun ink(face: Typeface): Int {
        val surface = Surface.makeRasterN32Premul(120, 120)
        surface.canvas.clear(0xFFFFFFFF.toInt())
        surface.canvas.drawString("Н", 10f, 100f, Font(face, 100f), Paint().apply { color = 0xFF000000.toInt() })
        val pixels = Bitmap().apply { allocN32Pixels(120, 120) }
        surface.readPixels(pixels, 0, 0)
        return (0 until 120).sumOf { y -> (0 until 120).count { x -> (pixels.getColor(x, y) and 0xFF) < 128 } }
    }

    @Test
    fun `the families load through compose resources and lay out with the file's advances`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(
                    LocalDensity provides Density(1f),
                ) {
                    val fonts = plakatFonts()
                    BasicText(
                        "ЛИСТ",
                        Modifier.testTag("display"),
                        style = TextStyle(fontFamily = fonts.display, fontSize = 20.sp),
                    )
                    BasicText(
                        "ЛИСТ",
                        Modifier.testTag("body"),
                        style = TextStyle(fontFamily = fonts.body, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
                    )
                }
            }
            // Four monospace glyphs at 850/1000 of 20 sp, at density 1.
            val display = onNodeWithTag("display").getBoundsInRoot()
            val width = (display.right - display.left).value
            assertTrue(abs(width - 68f) <= 1f, "Rubik Mono One laid out 'ЛИСТ' at $width dp, not 68")
            val f = font(BundledFonts.golosSemiBold)
            val expected =
                f.getWidths(BundledFonts.golosSemiBold.getStringGlyphs("ЛИСТ")).sum() / 1000f * 20f
            val body = onNodeWithTag("body").getBoundsInRoot()
            val bodyWidth = (body.right - body.left).value
            assertTrue(abs(bodyWidth - expected) <= 1f, "Golos 600 laid out at $bodyWidth dp, file says $expected")
        }
}
