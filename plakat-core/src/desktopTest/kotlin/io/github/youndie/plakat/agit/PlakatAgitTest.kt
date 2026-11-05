package io.github.youndie.plakat.agit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-33: what the pictures hold apart from their references: title zones, palettes, the skeleton's clock, the cache. */
@OptIn(ExperimentalTestApi::class)
class PlakatAgitTest {
    private val paper = PlakatPalettes.Paper

    private fun ComposeUiTest.show(
        colors: PlakatColors = paper,
        reduce: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = colors, reduceMotion = reduce) {
                    Box(Modifier.testTag("s")) { content() }
                }
            }
        }
        mainClock.advanceTimeBy(32)
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("s").captureToImage().toPixelMap()

    /** The same but for a circle's rim, whose antialiasing moves by a level or two with its place on the canvas. */
    private fun near(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < .02f && abs(a.green - b.green) < .02f && abs(a.blue - b.blue) < .02f

    @Test
    fun `no ink and no red enters a preset's title zone`() =
        runComposeUiTest {
            var preset by mutableStateOf(PlakatPreset.Voshod)
            show { PlakatPresetImage(preset) }
            assertEquals(6, PlakatPreset.entries.size)
            for (p in PlakatPreset.entries) {
                preset = p
                mainClock.advanceTimeByFrame()
                val px = frame()
                assertEquals(360, px.width, p.name)
                assertEquals(240, px.height, p.name)
                val z = p.titleZone
                assertEquals(216f, (z.right - z.left).value, p.name)
                assertEquals(88f, (z.bottom - z.top).value, p.name)
                for (y in z.top.value.toInt() until z.bottom.value.toInt()) {
                    for (x in z.left.value.toInt() until z.right.value.toInt()) {
                        val c = px[x, y]
                        assertTrue(c == paper.paper || c == paper.concrete, "${p.name} at $x, $y: $c")
                    }
                }
                val reds = (0 until 240 step 4).sumOf { y -> (0 until 360 step 4).count { x -> px[x, y] == paper.red } }
                assertTrue(reds > 0, "${p.name} has its red")
            }
        }

    @Test
    fun `a preset takes the theme's palette and scales to its box`() =
        runComposeUiTest {
            val dark = PlakatPalettes.BlackPaper
            show(colors = dark) { PlakatPresetImage(PlakatPreset.Kolonny, Modifier.size(180.dp, 120.dp)) }
            val px = frame()
            assertEquals(180, px.width)
            assertEquals(dark.paper, px[8, 8])
            // Kolonny: the red circle at 332, 96 of 360 × 240; the ground of 8 along the foot.
            assertEquals(dark.red, px[166, 48])
            assertEquals(dark.ink, px[20, 118])
        }

    @Test
    fun `a box of another shape is covered from the middle`() =
        runComposeUiTest {
            show { PlakatPresetImage(PlakatPreset.Kolonny, Modifier.size(180.dp, 240.dp)) }
            val px = frame()
            // Scale 1, so 90 of the frame's 360 go on each side: the circle at 332 − 90 is past the box's right.
            assertEquals(paper.paper, px[170, 40])
            assertEquals(paper.ink, px[20, 236], "the ground")
            assertEquals(paper.ink, px[122, 220], "the first column, at 200 − 90")
        }

    @Test
    fun `a picture speaks when it is given words and is an image`() =
        runComposeUiTest {
            show { PlakatPresetImage(PlakatPreset.Klin, contentDescription = "Клин") }
            onNodeWithContentDescription("Клин")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
        }

    @Test
    fun `a composition is its seed's, every time`() {
        fun picture(seed: Int): PixelMap {
            var out: PixelMap? = null
            runComposeUiTest {
                show { PlakatGeneratedImage(seed, Modifier.size(180.dp, 120.dp)) }
                out = frame()
            }
            return out!!
        }
        val a = picture(1919)
        val b = picture(1919)
        val c = picture(1920)
        var same = true
        var differs = false
        for (y in 0 until a.height) {
            for (x in 0 until a.width) {
                if (a[x, y] != b[x, y]) same = false
                if (a[x, y] != c[x, y]) differs = true
            }
        }
        assertTrue(same, "the same seed")
        assertTrue(differs, "another seed")
    }

    @Test
    fun `a composition keeps its zone clear, on its ground`() =
        runComposeUiTest {
            show {
                PlakatGeneratedImage(
                    301,
                    Modifier.size(358.dp, 360.dp),
                    frameWidth = 358.dp,
                    frameHeight = 360.dp,
                    titleZone = PlakatTitleZone.at(DpRect(0.dp, 0.dp, 72.dp, 40.dp)),
                    ground = paper.concrete,
                )
            }
            val px = frame()
            for (y in 0 until 40) for (x in 0 until 72) assertEquals(paper.concrete, px[x, y], "in the zone at $x, $y")
            assertTrue((0 until 360).any { y -> (0 until 358).any { x -> px[x, y] == paper.red } }, "its red")
        }

    @Test
    fun `the zone asked for is the one kept, wherever it stands`() {
        // Across the seeds, a zone in the frame's middle holds no ink and no red, where the generator's own would.
        for (seed in 301..306) {
            runComposeUiTest {
                show {
                    PlakatGeneratedImage(
                        seed,
                        Modifier.size(358.dp, 360.dp),
                        frameWidth = 358.dp,
                        frameHeight = 360.dp,
                        titleZone = PlakatTitleZone.at(DpRect(120.dp, 120.dp, 280.dp, 240.dp)),
                    )
                }
                val px = frame()
                for (y in 120 until 240 step 2) {
                    for (x in 120 until 280 step 2) {
                        assertTrue(px[x, y] != paper.ink && px[x, y] != paper.red, "seed $seed at $x, $y")
                    }
                }
            }
        }
    }

    @Test
    fun `no zone lets a thumbnail use its whole frame`() {
        fun picture(zone: PlakatTitleZone): PixelMap {
            var out: PixelMap? = null
            runComposeUiTest {
                show {
                    PlakatGeneratedImage(
                        71,
                        Modifier.size(64.dp),
                        frameWidth = 64.dp,
                        frameHeight = 64.dp,
                        titleZone = zone,
                    )
                }
                out = frame()
            }
            return out!!
        }
        val free = picture(PlakatTitleZone.None)
        val kept = picture(PlakatTitleZone.Generated)
        assertTrue((0 until 64).any { y -> (0 until 64).any { x -> free[x, y] != kept[x, y] } }, "the zone changes it")
    }

    @Test
    fun `a rapport on a smaller cell has the page's smaller tile`() =
        runComposeUiTest {
            // Seed 1923 on a cell of 4: a tile of 32 × 24.
            show { PlakatRapportFill(1923, Modifier.size(120.dp, 48.dp), cell = 4.dp) }
            val px = frame()
            for (y in 0 until 24 step 2) {
                for (x in 0 until 80 step 2) assertTrue(near(px[x, y], px[x + 32, y]), "across at $x, $y")
            }
        }

    @Test
    fun `a raskat disc prints in its two inks, round`() =
        runComposeUiTest {
            show { PlakatRaskatDisc(Modifier.size(120.dp)) }
            val px = frame()
            assertEquals(Color.Transparent, px[1, 1], "outside the circle")
            val inks = (20 until 100).flatMap { y -> (20 until 100).map { x -> px[x, y] } }.toSet()
            assertTrue(paper.red in inks && paper.paper in inks, "both inks: ${inks.size}")
        }

    @Test
    fun `a rapport repeats without a seam`() =
        runComposeUiTest {
            // Seed 1923: a tile of 192 × 144.
            show { PlakatRapportFill(1923, Modifier.size(400.dp, 300.dp)) }
            val px = frame()
            for (y in 0 until 150 step 3) {
                for (x in 0 until 200 step 3) {
                    assertTrue(near(px[x, y], px[x + 192, y]), "across at $x, $y")
                    assertTrue(near(px[x, y], px[x, y + 144]), "down at $x, $y")
                }
            }
        }

    @Test
    fun `the skeleton moves a sixth of a cell every 200 ms and starts over`() =
        runComposeUiTest {
            show(reduce = false) { PlakatSkeleton(1923, Modifier.size(64.dp).testTag("p")) }

            fun pattern() = onNodeWithTag("p").captureToImage().toPixelMap()

            fun PixelMap.isShiftOf(
                other: PixelMap,
                by: Int,
            ) = (0 until height).all { y -> (by until width).all { x -> near(this[x, y], other[x - by, y]) } }
            val frames =
                (0 until 90).map {
                    mainClock.advanceTimeByFrame()
                    pattern()
                }
            // Where the picture changes, by the frame.
            val changes =
                (1 until frames.size).filter { i ->
                    val a = frames[i - 1]
                    val b = frames[i]
                    (0 until b.height).any { y -> (0 until b.width).any { x -> a[x, y] != b[x, y] } }
                }
            assertTrue(changes.size >= 6, "changes at $changes")
            changes.zipWithNext { a, b -> b - a }.forEach { gap ->
                assertTrue(gap in 12..13, "a step of $gap frames: $changes")
            }
            val first = changes.first()
            assertTrue(frames[first].isShiftOf(frames[first - 1], 4), "by 4 at $first")
            // Six steps on it is back where it was: the wrap from 20 to 0.
            assertTrue(frames[first + 75].isShiftOf(frames[first], 0), "a cycle of 1200 ms")
            onNodeWithTag("p").assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo.Indeterminate,
                ),
            )
        }

    @Test
    fun `under reduced motion the skeleton stands`() =
        runComposeUiTest {
            show(reduce = true) { PlakatSkeleton(1923, Modifier.size(64.dp)) }
            val before = frame()
            mainClock.advanceTimeBy(800)
            val after = frame()
            for (y in 0 until 64) for (x in 0 until 64) assertEquals(before[x, y], after[x, y], "at $x, $y")
        }

    /** A flat source of [color], or one [color] on the left half and [right] on the right. */
    private fun source(
        color: Color,
        right: Color = color,
        w: Int = 80,
        h: Int = 40,
    ) = ImageBitmap(w, h).also { image ->
        val canvas = Canvas(image)
        canvas.drawRect(0f, 0f, w / 2f, h.toFloat(), Paint().apply { this.color = color })
        canvas.drawRect(w / 2f, 0f, w.toFloat(), h.toFloat(), Paint().apply { this.color = right })
    }

    @Test
    fun `a source covers the frame from its middle, undistorted`() {
        // 80 × 40 into 20 × 20: the middle 40 × 40, half of each, its edge down the frame's middle.
        val px = coverPixels(source(Color.Black, Color.White), 20, 20)
        assertEquals(Color.Black.toArgb(), px[10 * 20 + 2])
        assertEquals(Color.White.toArgb(), px[10 * 20 + 17])
        // What is past the middle does not come in: red outer quarters, blue between.
        val banded = ImageBitmap(80, 40)
        val canvas = Canvas(banded)
        canvas.drawRect(0f, 0f, 80f, 40f, Paint().apply { color = Color.Red })
        canvas.drawRect(20f, 0f, 60f, 40f, Paint().apply { color = Color.Blue })
        assertEquals(setOf(Color.Blue.toArgb()), coverPixels(banded, 20, 20).toSet())
        // 40 × 80 into 20 × 20 is the middle too, along the other axis.
        val tall = coverPixels(source(Color.Black, Color.White, w = 40, h = 80), 20, 20)
        assertEquals(Color.Black.toArgb(), tall[10 * 20 + 2])
    }

    @Test
    fun `dark prints ink, light prints paper, a mask only those two`() =
        runComposeUiTest {
            var mode by mutableStateOf(PlakatHalftoneMode.Dots)
            val dark = source(Color(0xFF101010))
            val light = source(Color(0xFFF8F8F8))
            show {
                Row {
                    Box(Modifier.size(60.dp).plakatHalftone(dark, Color.Red, Color.White, mode))
                    Box(Modifier.size(60.dp).plakatHalftone(light, Color.Red, Color.White, mode))
                }
            }
            var px = frame()
            assertEquals(Color.Red, px[30, 30], "the screen closes up on black")
            assertTrue(
                (0 until 60).all { x -> (0 until 60).all { y -> px[60 + x, y].green > .5f } },
                "no solid ink on white",
            )
            mode = PlakatHalftoneMode.Mask
            mainClock.advanceTimeByFrame()
            px = frame()
            for (y in 0 until 60) {
                for (x in 0 until 60) {
                    assertEquals(Color.Red, px[x, y], "ink at $x, $y")
                    assertEquals(Color.White, px[60 + x, y], "paper at ${60 + x}, $y")
                }
            }
        }

    @Test
    fun `it is worked out once for a source and a size`() =
        runComposeUiTest {
            val image = source(Color.Black, w = 40, h = 40)
            var redraws by mutableIntStateOf(0)
            var wide by mutableIntStateOf(60)
            show {
                Box(
                    Modifier.size(80.dp, 60.dp).drawBehind {
                        drawRect(if (redraws % 2 == 0) Color.White else Color.Gray, Offset(79f, 0f), Size(1f, 1f))
                    },
                ) {
                    Box(
                        Modifier
                            .size(wide.dp, 60.dp)
                            .plakatHalftone(image, Color.Red, Color.White, PlakatHalftoneMode.Mask),
                    )
                }
            }
            assertEquals(Color.Red, frame()[30, 30])
            // The source is painted over and the parent redrawn: what was worked out stands.
            Canvas(image).drawRect(0f, 0f, 40f, 40f, Paint().apply { color = Color.White })
            redraws++
            mainClock.advanceTimeByFrame()
            assertEquals(Color.Gray, frame()[79, 0], "redrawn")
            assertEquals(Color.Red, frame()[30, 30], "kept across a redraw")
            // Another size is worked out anew, and finds the white source.
            wide = 70
            mainClock.advanceTimeByFrame()
            assertEquals(Color.White, frame()[30, 30], "worked out anew")
        }
}
