package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
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

    /** Rows and columns the ink reaches inside the box, whose corner stands at 16, 16. */
    private fun PixelMap.inkBox(): Pair<IntRange, IntRange> {
        val xs = (0 until width).filter { x -> (0 until height).any { y -> dark(x, y) } }
        val ys = (0 until height).filter { y -> (0 until width).any { x -> dark(x, y) } }
        return (xs.first() - 16..xs.last() - 16) to (ys.first() - 16..ys.last() - 16)
    }

    @Test
    fun `too short a word for its box keeps the height and comes short of the width, from the left`() =
        runComposeUiTest {
            // The page's bad cell, ЛЕНТА at ×0.8: flattened, were it to fill. Held at 1.5, 240 × 0.8 / 1.5 of the width.
            val factor = plakatStretchFactor("ЛЕНТА", 240.dp, 32.dp)
            assertTrue(factor < 1.5f, "$factor")
            line("ЛЕНТА", 240, 32)
            val (xs, ys) = frame().inkBox()
            val wide = (240 * factor / 1.5f).toInt()
            assertTrue(xs.first in 0..1, "from the left: $xs")
            assertTrue(abs(xs.last - wide) <= 2, "$wide wide: $xs")
            assertTrue(ys.first in 0..2 && ys.last in 30..32, "the height kept: $ys")
        }

    @Test
    fun `too long a word for its box keeps the width and comes short of the height, its baseline at the foot`() =
        runComposeUiTest {
            // Letters torn, were it to fill, as the page's other bad cell, ДА at ×5.2. Held at 4.
            val factor = plakatStretchFactor("HH", 40.dp, 200.dp)
            assertTrue(factor > 4f, "$factor")
            line("HH", 40, 200)
            val (xs, ys) = frame().inkBox()
            val high = (200 * 4f / factor).toInt()
            assertTrue(xs.first in 0..1 && xs.last in 38..40, "the width kept: $xs")
            assertTrue(ys.last in 198..200, "the foot: $ys")
            assertTrue(abs(ys.last - ys.first + 1 - high) <= 2, "$high high: $ys")
        }

    @Test
    fun `the fit is the stretch held at its limits, and nothing inside them`() {
        val ink = assertNotNull(stretchInk("ЛЕНТА"))

        fun stretch(
            w: Float,
            h: Float,
        ): Float {
            val fit = stretchFit(ink, w, h)
            return (fit.height / (0.7f + ink.over)) / (fit.width / (ink.right - ink.left))
        }
        for (h in listOf(10f, 40f, 120f, 400f, 900f, 3000f)) {
            val s = stretch(240f, h)
            assertTrue(s in 1.4999f..4.0001f, "at $h: $s")
        }
        val inside = stretchFit(ink, 240f, 80f)
        assertEquals(240f, inside.width)
        assertEquals(80f, inside.height)
    }

    @Test
    fun `a masthead draws at every width, with every length of word`() =
        runComposeUiTest {
            var width by mutableIntStateOf(320)
            var word by mutableStateOf("ЛЕС")
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) {
                        Box(Modifier.width(width.dp).testTag("s")) { PlakatMasthead(word) }
                    }
                }
            }
            // Drawing is where it used to throw, so each is captured, down to the phone's 411 and the page's own word.
            for (w in listOf(320, 360, 390, 411, 480, 600, 840)) {
                for (text in listOf("ЛЕС", "ЛИСТ", "ВЫПУСК", "ПРОЛЕТАРИЙ", "ОБЩЕЖИТИЕКОМ")) {
                    width = w
                    word = text
                    waitForIdle()
                    val px = frame()
                    assertEquals(w, px.width)
                    assertTrue((0 until w).any { x -> (39 until 159).any { y -> px.dark(x, y) } }, "$text at $w")
                }
            }
        }

    @Test
    fun `the page's own bad cell, with a descender, draws in its box's width`() =
        runComposeUiTest {
            line("ДА", 40, 88)
            val (xs, _) = frame().inkBox()
            assertTrue(xs.first in 0..1 && xs.last in 38..40, "$xs")
        }

    @Test
    fun `a blank line and an empty box draw nothing`() =
        runComposeUiTest {
            line("   ", 40, 400)
            assertEquals(Color.White, frame()[30, 60])
            setContent { PlakatTheme { PlakatStretch("ДА") } }
            waitForIdle()
        }
}
