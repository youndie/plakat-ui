package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** B-32: the projection's faces, its wipe, and that its picture is made once. The meshes are in `PlakatWarpTest`, the cells in parity. */
@OptIn(ExperimentalTestApi::class)
class PlakatProjectionTest {
    private val paper = PlakatPalettes.Paper
    private val horn = PlakatProjectionForm.Horn(16.dp, 312.dp, 358.dp, 56.dp)
    private val box = PlakatProjectionForm.Box(16.dp, 152.dp, 260.dp, 160.dp, 96.dp, 184.dp, 288.dp)

    private fun ComposeUiTest.projection(
        lines: () -> List<String>,
        form: () -> PlakatProjectionForm,
        width: () -> Dp = { 390.dp },
        reduce: Boolean = true,
        print: () -> Boolean = { false },
        density: Float = 1f,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        onBake: () -> Unit = {},
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                PlakatTheme(
                    colors = paper,
                    reduceMotion = reduce,
                    print =
                        PlakatPrintSwitches(
                            texture = false,
                        ),
                    events = {
                        heard += it
                    },
                ) {
                    Box(Modifier.size(width(), 392.dp).background(Color.White).testTag("p")) {
                        PlakatProjectionFrame(lines(), form(), Modifier.size(width(), 392.dp), print(), onBake)
                    }
                }
            }
        }
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("p").captureToImage().toPixelMap()

    @Test
    fun `a box has a red front, an ink side and a concrete top, the text breaking over the edge`() =
        runComposeUiTest {
            projection({ listOf("НОВОЕ") }, { box })
            val px = frame()
            assertEquals(paper.red, px[100, 165], "the front, over the text's band")
            assertEquals(paper.concrete, px[150, 145], "the top")
            assertEquals(paper.ink, px[285, 300], "the side, under the text's band")
            assertEquals(Color.White, px[390 - 8, 100], "past the box")
            // The letters in the paper's colour, on the front and then past the edge on the side.
            assertTrue((16 until 276).any { px[it, 240] == paper.paper }, "on the front")
            assertTrue((277 until 368).any { x -> (184 until 280).any { y -> px[x, y] == paper.paper } }, "on the side")
        }

    @Test
    fun `a box folded over the top has an ink lid, and letters on it`() =
        runComposeUiTest {
            val lid =
                PlakatProjectionForm.Box(
                    16.dp,
                    180.dp,
                    300.dp,
                    132.dp,
                    96.dp,
                    200.dp,
                    300.dp,
                    PlakatBoxFold.Top,
                )
            projection({ listOf("НОВОЕ") }, { lid })
            val px = frame()
            assertEquals(paper.ink, px[200, 160], "the lid, behind the letters' tops")
            assertTrue(
                (60 until 340).any { x ->
                    (160 until 178).any { y -> px[x, y] == paper.paper }
                },
                "the letters' tops",
            )
            assertTrue((24 until 300).any { px[it, 250] == paper.paper }, "and the rest of them on the front")
        }

    @Test
    fun `a cone's mouth is ink over its red, a column's top is concrete, a sphere is red`() =
        runComposeUiTest {
            var form by mutableStateOf<PlakatProjectionForm>(
                PlakatProjectionForm.Cone(40.dp, 200.dp, 320.dp, 20.dp, 92.dp, 16f),
            )
            projection({ listOf("СЛУШАЙ") }, { form })
            assertEquals(paper.ink, frame()[360, 200], "the mouth")
            assertEquals(paper.red, frame()[300, 125], "the body")
            form = PlakatProjectionForm.Column(264.dp, 96.dp, 112.dp, 312.dp, 168.dp, 264.dp)
            waitForIdle()
            assertEquals(paper.concrete, frame()[264, 112], "the top")
            assertEquals(paper.red, frame()[264, 150], "the body")
            assertEquals(paper.red, frame()[264, 325], "the foot's near half, below its bottom")
            form = PlakatProjectionForm.Sphere(254.dp, 210.dp, 100.dp, 72f)
            waitForIdle()
            assertEquals(paper.red, frame()[254, 120], "over the belt")
            assertEquals(Color.White, frame()[160, 120], "round")
        }

    @Test
    fun `stairs go a line to a riser, from the top down, the last taking the rest`() =
        runComposeUiTest {
            val stairs = PlakatProjectionForm.Stairs(16.dp, 312.dp, 2, 280.dp, 80.dp, 40.dp)
            projection({ listOf("ШАГ", "ЗА", "ШАГОМ") }, { stairs })
            onNodeWithContentDescription("ШАГ ЗА ШАГОМ").assertExists()
            val px = frame()
            assertEquals(paper.red, px[20, 234], "the lower riser's edge")
            assertTrue((30 until 280).any { px[it, 272] == paper.paper }, "the rest on the lower riser")
            assertEquals(paper.concrete, px[100, 225], "the lower tread")
        }

    /** How many of the row's columns are off the white, by a row inside the form. */
    private fun PixelMap.reach(y: Int) = (0 until width).lastOrNull { this[it, y] != Color.White } ?: -1

    @Test
    fun `it appears by a wipe from the left, linear over 120 ms, and is heard`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            mainClock.autoAdvance = false
            projection({ listOf("НОЯБРЬ") }, { horn }, reduce = false, print = { true }, heard = heard)
            val frames =
                (0 until 12).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            val reach = frames.map { it.reach(300) }
            val fits =
                (0..2).any { start ->
                    reach.withIndex().all { (i, got) ->
                        val edge = 390 * (((i - start) * 16.0) / 120).coerceIn(0.0, 1.0)
                        abs(got - edge.coerceAtMost(373.0)) <= 2 || (edge < 16 && got == -1)
                    }
                }
            assertTrue(fits, "the row's reach per frame: $reach")
            assertEquals(listOf(PlakatEvent.Print), heard)
            // Whole at the end, and what shows meanwhile is the same picture cut, never scaled.
            val last = frames.last()
            val mid = frames[4]
            val cut = mid.reach(300)
            assertTrue(cut in 30..340, "a frame in the middle: $reach")
            for (x in 0 until cut - 1) assertEquals(last[x, 300], mid[x, 300], "at $x")
        }

    @Test
    fun `under reduced motion it is there from the first frame, as it is when not printing`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            mainClock.autoAdvance = false
            projection({ listOf("НОЯБРЬ") }, { horn }, reduce = true, print = { true }, heard = heard)
            mainClock.advanceTimeByFrame()
            assertEquals(373, frame().reach(300))
            assertTrue(heard.isEmpty(), "nothing moves, nothing is heard: $heard")
        }

    @Test
    fun `not printing it is there from the first frame under motion too`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            mainClock.autoAdvance = false
            projection({ listOf("НОЯБРЬ") }, { horn }, reduce = false, print = { false }, heard = heard)
            mainClock.advanceTimeByFrame()
            assertEquals(373, frame().reach(300))
            assertTrue(heard.isEmpty())
        }

    @Test
    fun `its picture is made once, and again only for a new text, form or size`() =
        runComposeUiTest {
            var bakes = 0
            var asked = 0
            var tick by mutableIntStateOf(0)
            var word by mutableStateOf("НОЯБРЬ")
            var form by mutableStateOf<PlakatProjectionForm>(horn)
            var width by mutableStateOf(390.dp)
            var printing by mutableStateOf(true)
            mainClock.autoAdvance = false
            projection(
                {
                    asked = tick + 1
                    listOf(word)
                },
                { form },
                { width },
                reduce = false,
                print = { printing },
                onBake = { bakes++ },
            )
            repeat(12) { mainClock.advanceTimeByFrame() }
            assertEquals(1, bakes, "once, through the whole wipe")
            tick = 1
            form = horn.copy()
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(2, asked, "it has recomposed")
            assertEquals(1, bakes, "a recomposition with an equal list and form is not a change")
            printing = false
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(1, bakes, "nor is one that comes through for its other parameters")
            word = "ДЕКАБРЬ"
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(2, bakes, "a new text")
            form = horn.copy(narrow = 64.dp)
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(3, bakes, "a new form")
            width = 380.dp
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertEquals(4, bakes, "a new size")
        }

    @Test
    fun `at a density of 2 the form stands at the same dp`() =
        runComposeUiTest {
            projection({ listOf("НОВОЕ") }, { box }, density = 2f)
            val px = frame()
            assertEquals(780, px.width)
            assertEquals(paper.red, px[200, 330], "the front, 2 × 165")
            assertEquals(paper.concrete, px[300, 290], "the top, 2 × 145")
            assertEquals(paper.ink, px[570, 600], "the side, 2 × 300")
            assertEquals(Color.White, px[764, 200], "past the box")
        }

    @Test
    fun `it is read as a heading of its words, in capitals`() =
        runComposeUiTest {
            projection({ listOf("Читай весь", "ноябрь") }, { horn })
            onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
                .assert(hasContentDescription("ЧИТАЙ ВЕСЬ НОЯБРЬ"))
        }

    @Test
    fun `the design's rules are refused`() {
        assertFailsWith<IllegalArgumentException> { horn.copy(spread = 31f) }
        assertFailsWith<IllegalArgumentException> {
            PlakatProjectionForm.Column(264.dp, 96.dp, 112.dp, 312.dp, 168.dp, 264.dp, arc = 71f)
        }
        assertFailsWith<IllegalArgumentException> { box.copy(fold = PlakatBoxFold.Top, at = .21f) }
        horn.copy(spread = 30f)
        box.copy(at = .6f)
    }

    @Test
    fun `lines beyond three, or none, are refused`() {
        assertFailsWith<IllegalArgumentException> {
            runComposeUiTest { projection({ listOf("А", "Б", "В", "Г") }, { horn }) }
        }
        assertFailsWith<IllegalArgumentException> {
            runComposeUiTest { projection({ listOf(" ") }, { horn }) }
        }
    }

    @Test
    fun `a horn fitted to a box stays in it, scaled down and never up`() {
        val tall = DpRect(12.dp, 12.dp, 248.dp, 192.dp)
        val up =
            PlakatProjectionForm
                .Horn(0.dp, 0.dp, 236.dp, 28.dp, axis = PlakatProjectionAxis.Upright)
                .fittedIn(tall, Alignment.Bottom)
        assertEquals(180f, up.length.value, 1e-3f)
        assertEquals(192f, up.y.value, 1e-3f, "its foot on the box's bottom")
        val small = PlakatProjectionForm.Horn(0.dp, 0.dp, 100.dp, 28.dp).fittedIn(tall, Alignment.Top)
        assertEquals(100.dp, small.length, "never up")
        val high = small.narrow.value + 100 * kotlin.math.tan(17 * kotlin.math.PI / 180).toFloat()
        assertEquals(12f + high, small.y.value, 1e-3f, "its top on the box's top")
        assertEquals(12f + (236 - 100) / 2f, small.x.value, 1e-3f, "centred across")
    }

    @Test
    fun `a cone fitted to a box is centred in it with its ends`() {
        val form =
            PlakatProjectionForm
                .Cone(0.dp, 0.dp, 320.dp, 20.dp, 92.dp, 16f, 62f, PlakatProjectionAxis.Tilted)
                .fittedIn(DpRect(24.dp, 120.dp, 374.dp, 304.dp))
        assertTrue(form.endRadius < 92.dp, "scaled down: ${form.endRadius}")
        assertEquals(form.startRadius.value / 20f, form.endRadius.value / 92f, 1e-3f, "by one factor")
        assertEquals(form.startRadius.value / 20f, form.length.value / 320f, 1e-3f)
    }
}
