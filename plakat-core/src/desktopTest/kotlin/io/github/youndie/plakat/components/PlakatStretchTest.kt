package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatDisplayInk
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import java.awt.Font
import java.awt.font.FontRenderContext
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** B-31: the fit, as the Stretch page's canvas measures it, and the letters standing in their box to its edges. */
@OptIn(ExperimentalTestApi::class)
class PlakatStretchTest {
    private fun ComposeUiTest.line(
        text: String,
        width: Int,
        height: Int,
        density: Float = 1f,
        colors: PlakatColors = PlakatPalettes.Paper,
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                PlakatTheme(colors = colors) {
                    Box(Modifier.testTag("s").background(Color.White).padding(16.dp)) {
                        PlakatStretch(text, Modifier.size(width.dp, height.dp))
                    }
                }
            }
        }
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("s").captureToImage().toPixelMap()

    private fun PixelMap.dark(
        x: Int,
        y: Int,
    ) = this[x, y].red < .5f

    /** What Chrome's canvas says of the page's lines at 100 px: `actualBoundingBoxLeft`, `…Right`, `…Ascent`. */
    @Test
    fun `the ink is what the page's canvas measures`() {
        val chrome =
            mapOf(
                "РАСТЯЖКА" to Triple(-12.6f, 678.1f, 71f),
                "ЧИТАЙ" to Triple(-6.5f, 416.5f, 85.5f),
                "ВЕСЬ" to Triple(-8.5f, 338.1f, 71f),
                "НОЯБРЬ" to Triple(-6.5f, 508.1f, 71f),
                "ЛИСТ" to Triple(-3.7f, 334.7f, 71f),
                "47" to Triple(-6.6f, 160f, 70f),
                "АРХИТЕКТУРА" to Triple(-2.5f, 933.1f, 70f),
                "ДА" to Triple(-3.9f, 168.1f, 70f),
                "HELLO" to Triple(-6.5f, 419.7f, 71f),
                "Ё" to Triple(-9f, 77.6f, 85f),
            )
        for ((text, want) in chrome) {
            val ink = assertNotNull(stretchInk(text))
            assertEquals(-want.first, ink.left * 100, .01f, "$text left")
            assertEquals(want.second, ink.right * 100, .01f, "$text right")
            assertEquals(want.third - 70, ink.over * 100, .01f, "$text over")
        }
        // A space holds its cell and has no ink: the line's ink starts past it, and ends before the last.
        val spaced = assertNotNull(stretchInk(" H "))
        assertEquals(85 + 6.5f, spaced.left * 100, .01f)
        assertEquals(85 + 78.5f, spaced.right * 100, .01f)
        assertNull(stretchInk("   "))
        assertNull(stretchInk(""))
    }

    /** A second reading of the font, through AWT's outlines, row by row, apart from the script that made the table. */
    @Test
    fun `the table is what the font's outlines say`() {
        val font =
            Font
                .createFont(Font.TRUETYPE_FONT, File("src/commonMain/composeResources/font/rubik_mono_one_regular.ttf"))
                .deriveFont(1000f)
        val context = FontRenderContext(null, true, true)
        val rows =
            PlakatDisplayInk.ROWS
                .split(';')
                .filter { it.isNotEmpty() }
                .map { it.split(',') }
        assertEquals(652, rows.size)
        var inked = 0
        for (row in rows) {
            val code = row[0].toInt(16)
            val glyph = font.createGlyphVector(context, String(Character.toChars(code)))
            val bounds = glyph.getGlyphOutline(0).bounds2D
            val label = "U+${row[0]}"
            // AWT gives a control or format character no advance of its own.
            if (code >= 0x20 && !Character.isIdentifierIgnorable(code)) {
                assertEquals(if (row[1] == "1") 850f else 0f, glyph.getGlyphMetrics(0).advance, .5f, "$label advance")
            }
            if (bounds.isEmpty) {
                assertEquals(row[2], row[3], "$label is blank")
                continue
            }
            inked++
            // A glyph's box takes in its curves' control points, so a rare one (Ѵ) stands a little outside its
            // outline; for the kit's own letters and figures it is the outline.
            val slack = if (Regex("[A-ZА-ЯЁ0-9]").matches(String(Character.toChars(code)))) 1.0 else 8.0
            assertTrue(bounds.minX - row[2].toInt() in -1.0..slack, "$label left ${bounds.minX} vs ${row[2]}")
            assertTrue(row[3].toInt() - bounds.maxX in -1.0..slack, "$label right ${bounds.maxX} vs ${row[3]}")
            assertTrue(row[4].toInt() + bounds.minY in -1.0..slack, "$label top ${-bounds.minY} vs ${row[4]}")
        }
        assertTrue(inked > 600, "inked: $inked")
        val h = font.createGlyphVector(context, "H").getGlyphOutline(0).bounds2D
        assertEquals(PlakatDisplayInk.CAP_HEIGHT.toDouble(), -h.minY, .5, "the caps")
    }

    /** The page's figures: its usage captions' ×1,6, its own ×1,6–2,3 for the poster, and its two bad cells. */
    @Test
    fun `the stretch is the page's`() {
        fun k(
            text: String,
            w: Int,
            h: Int,
        ) = plakatStretchFactor(text, w.dp, h.dp)
        assertEquals(1.6f, k("ЛИСТ", 358, 120), .05f)
        assertEquals(1.82f, k("ЧИТАЙ", 358, 136), .01f)
        assertEquals(1.6f, k("НОЯБРЬ", 358, 80), .05f)
        assertEquals(1.6f, k("лист", 358, 120), .05f)
        assertEquals(5.16f, k("ДА", 40, 88), .01f)
        assertEquals(.8f, k("ЛЕНТА", 240, 32), .05f)
        assertEquals(0f, k(" ", 40, 88))
    }

    @Test
    fun `the letters reach the box on every side`() =
        runComposeUiTest {
            // H: stems at both ends, a flat top and a flat foot. 100 × 96 at 16 of padding.
            line("HH", 100, 96)
            val px = frame()
            assertEquals(132, px.width)
            assertEquals(128, px.height)
            // Within half a pixel of each edge of the box, and nothing past it.
            for (y in listOf(20, 60, 108)) {
                assertTrue(px.dark(16, y), "left stem at $y")
                assertTrue(!px.dark(14, y), "left of the box at $y")
                assertTrue(px.dark(115, y), "right stem at $y")
                assertTrue(!px.dark(117, y), "right of the box at $y")
            }
            assertTrue(px.dark(20, 16), "the caps' top")
            assertTrue(!px.dark(20, 14), "over the box")
            assertTrue(px.dark(20, 111), "the baseline")
            assertTrue(!px.dark(20, 113), "under the box")
        }

    @Test
    fun `it fills the same box at any density`() =
        runComposeUiTest {
            line("HH", 100, 96, density = 2f)
            val px = frame()
            assertEquals(264, px.width)
            assertEquals(256, px.height)
            for (y in listOf(40, 120, 216)) {
                assertTrue(px.dark(32, y), "left stem at $y")
                assertTrue(!px.dark(29, y), "left of the box at $y")
                assertTrue(px.dark(231, y), "right stem at $y")
                assertTrue(!px.dark(234, y), "right of the box at $y")
            }
            assertTrue(px.dark(40, 32), "the caps' top")
            assertTrue(!px.dark(40, 29), "over the box")
            assertTrue(px.dark(40, 223), "the baseline")
            assertTrue(!px.dark(40, 226), "under the box")
        }

    @Test
    fun `a Yo keeps its dots in the box and the line goes down for them`() =
        runComposeUiTest {
            // Ё stands 850 tall to E's 700: the mark takes 150 of 850 of the box's 170, the caps the rest.
            line("ЁЁ", 170, 170)
            val px = frame()
            val column = (16 until 186).first { x -> (16 until 186).any { px.dark(x, it) } } + 4
            val inkRows = (0 until px.height).filter { y -> (16 until 186).any { x -> px.dark(x, y) } }
            assertTrue(inkRows.first() in 15..17, "the mark at the box's top: ${inkRows.first()}")
            assertTrue(inkRows.last() in 184..186, "the baseline at its bottom: ${inkRows.last()}")
            // The E's own stem starts 30 down: 150 / 850 × 170, plus the 16 of padding.
            val stem = (16 until 186).first { px.dark(column, it) }
            assertEquals(46f, stem.toFloat(), 2f, "the caps' top")
        }

    @Test
    fun `a descender hangs below the box`() =
        runComposeUiTest {
            // Ц: 124 under the baseline, at 96 / 700 a row: 17, into the padding and past it.
            line("ЦЦ", 100, 96)
            val px = frame()
            assertTrue((16 until 116).any { px.dark(it, 120) }, "under the box")
        }

    @Test
    fun `small letters are set in capitals and read as written`() =
        runComposeUiTest {
            line("лист", 200, 96)
            val small = frame()
            onNodeWithText("лист").assertIsDisplayed()
            assertTrue((16 until 216).any { small.dark(it, 60) })
        }

    @Test
    fun `small letters draw what capitals draw`() {
        var small: PixelMap? = null
        runComposeUiTest {
            line("лист", 200, 96)
            small = frame()
        }
        runComposeUiTest {
            line("ЛИСТ", 200, 96)
            val caps = frame()
            val a = small!!
            for (y in 0 until caps.height) for (x in 0 until caps.width) assertEquals(caps[x, y], a[x, y], "at $x, $y")
        }
    }

    @Test
    fun `its colour is the theme's ink`() =
        runComposeUiTest {
            line("HH", 100, 96, colors = PlakatPalettes.BlackPaper)
            assertEquals(PlakatPalettes.BlackPaper.ink, frame()[20, 60])
        }

    @Test
    fun `the page's two bad cells are refused and its good one is not`() {
        assertFailsWith<IllegalArgumentException> {
            runComposeUiTest {
                line("ДА", 40, 88)
                waitForIdle()
            }
        }
        assertFailsWith<IllegalArgumentException> {
            runComposeUiTest {
                line("ЛЕНТА", 240, 32)
                waitForIdle()
            }
        }
        runComposeUiTest {
            line("ЛЕНТА", 240, 80)
            assertNotEquals(Color.White, frame()[30, 60])
        }
    }

    @Test
    fun `a blank line and an empty box draw nothing and refuse nothing`() =
        runComposeUiTest {
            line("   ", 40, 400)
            assertEquals(Color.White, frame()[30, 60])
            setContent { PlakatTheme { PlakatStretch("ДА") } }
            waitForIdle()
        }
}
