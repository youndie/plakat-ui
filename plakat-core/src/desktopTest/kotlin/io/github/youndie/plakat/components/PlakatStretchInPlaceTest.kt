package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-53: the Stretch page's poster and masthead. The masthead rides up and is never scaled; the ordinary bar stays. */
@OptIn(ExperimentalTestApi::class)
class PlakatStretchInPlaceTest {
    private val paper = PlakatPalettes.Paper

    private val stark =
        PlakatColors(
            paper = Color.White,
            concrete = Color.Black,
            ink = Color.Blue,
            inkMuted = Color.Gray,
            red = Color.Red,
            onRed = Color.White,
            ocher = Color.Yellow,
            slate = Color.Cyan,
        )

    private fun ComposeUiTest.poster(onAction: () -> Unit = {}) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = true, print = PlakatPrintSwitches(texture = false)) {
                    PlakatPoster(
                        listOf("ЧИТАЙ", "ВЕСЬ", "НОЯБРЬ"),
                        "ПОДПИСАТЬСЯ",
                        onAction,
                        Modifier.testTag("p"),
                        layout = PlakatPosterLayout.Stretch,
                        body = "Двенадцать выпусков.",
                    )
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.posterFrame(): PixelMap = onNodeWithTag("p").captureToImage().toPixelMap()

    /** The rows of [px] with a dark pixel in the columns 16..373, as first and last. */
    private fun inkRows(
        px: PixelMap,
        rows: IntRange,
    ) = rows.filter { y -> (16 until 374).any { x -> px[x, y] == paper.ink } }.let { it.first()..it.last() }

    @Test
    fun `the poster is 390 by 600, its lines at their heights and its bar between`() =
        runComposeUiTest {
            poster()
            onNodeWithTag("p").assertWidthIsEqualTo(390.dp).assertHeightIsEqualTo(600.dp)
            val px = posterFrame()
            // A Й's mark takes the first line's top, so all three reach their boxes' edges, a round letter a row past.
            assertTrue(inkRows(px, 10..160).let { it.first in 15..17 && it.last in 150..153 }, "the first line, 136")
            assertEquals(paper.red, px[100, 170])
            assertEquals(paper.red, px[100, 183])
            assertEquals(paper.paper, px[100, 186], "the bar is 16")
            assertTrue(inkRows(px, 192..330).let { it.first in 199..201 && it.last in 318..322 }, "the second, 120")
            assertTrue(inkRows(px, 330..425).let { it.first in 335..337 && it.last in 414..418 }, "the third, 80")
        }

    @Test
    fun `its circle goes off the right edge and its slogan stands under the body`() =
        runComposeUiTest {
            poster()
            val px = posterFrame()
            // The circle of 136 at 310, 440: its middle at 378, 508, and past the edge of 390.
            assertEquals(paper.ink, px[380, 508])
            assertEquals(paper.ink, px[389, 508])
            assertEquals(paper.paper, px[305, 508])
            assertEquals(paper.paper, px[320, 445], "round, so a corner of its box is empty")
            // The slogan: 24 up from the foot, 240 wide from the margin.
            assertEquals(paper.red, px[20, 600 - 24 - 4])
            assertEquals(paper.red, px[250, 600 - 24 - 4])
            assertEquals(paper.paper, px[262, 600 - 24 - 4])
        }

    @Test
    fun `its words are read and its slogan acts`() =
        runComposeUiTest {
            var acted = 0
            poster { acted++ }
            onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertIsDisplayed()
            // The title once, as one phrase: its lines are not read over again one by one.
            onNodeWithContentDescription("ЧИТАЙ ВЕСЬ НОЯБРЬ").assertIsDisplayed()
            onNodeWithText("ЧИТАЙ").assertDoesNotExist()
            onNodeWithContentDescription("Двенадцать выпусков.").assertIsDisplayed()
            onNodeWithText("ПОДПИСАТЬСЯ").assertHeightIsEqualTo(48.dp).performClick()
            waitForIdle()
            assertEquals(1, acted)
        }

    /** On the poster's row [y] from [from], how many of [span] columns [ink] is printed, its edge counted in part. */
    private fun PixelMap.printed(
        y: Int,
        from: Int,
        span: Int,
        ink: (Color) -> Float,
    ) = (from until from + span).sumOf { ink(this[it, y]).coerceIn(0f, 1f).toDouble() }

    @Test
    fun `its bar, its circle, its title and its slogan are wiped in from the left, one after another`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            mainClock.autoAdvance = false
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(
                        colors = stark,
                        events = { heard += it },
                        print = PlakatPrintSwitches(texture = false),
                    ) {
                        PlakatPoster(
                            listOf("ЧИТАЙ", "ВЕСЬ", "НОЯБРЬ"),
                            "ПОДПИСАТЬСЯ",
                            {},
                            Modifier.testTag("p"),
                            layout = PlakatPosterLayout.Stretch,
                            body = "Двенадцать выпусков.",
                        )
                    }
                }
            }
            val frames =
                (0 until 22).map {
                    mainClock.advanceTimeByFrame()
                    posterFrame()
                }
            val red = { c: Color -> c.red - c.green }
            val black = { c: Color -> 1 - c.green }
            val read =
                frames.map {
                    listOf(
                        it.printed(175, 16, 358, red),
                        it.printed(508, 310, 80, black),
                        it.printed(600 - 24 - 4, 16, 240, red),
                    )
                }
            val fits =
                (0..2).any { start ->
                    read.withIndex().all { (i, r) ->
                        val ms = (i - start) * 16.0

                        fun edge(at: Int) = 390 * ((ms - at) / 120).coerceIn(0.0, 1.0)
                        abs(r[0] - (edge(0) - 16).coerceIn(0.0, 358.0)) < 1.5 &&
                            abs(r[1] - (edge(30) - 310).coerceIn(0.0, 80.0)) < 1.5 &&
                            abs(r[2] - (edge(180) - 16).coerceIn(0.0, 240.0)) < 1.5
                    }
                }
            assertTrue(fits, "bar, circle, slogan per frame: ${read.map { r -> r.map { it.toInt() } }}")

            // The title and the body go by their last ink, which stands up to a letter short of the edge and never past it.
            fun PixelMap.reach(
                rows: IntRange,
                upTo: Int,
            ) = rows.maxOf { y -> (0 until upTo).lastOrNull { x -> this[x, y].red < .5f } ?: -1 }
            val reached = frames.map { it.reach(16 until 152, 390) to it.reach(440 until 500, 300) }
            val (wholeTitle, wholeBody) = reached.last()
            assertTrue(wholeTitle > 360 && wholeBody > 100, "whole at the end: $reached")
            val words =
                (0..2).any { start ->
                    reached.withIndex().all { (i, r) ->
                        val edge = 390 * (((i - start) * 16.0 - 150) / 120).coerceIn(0.0, 1.0)

                        fun held(
                            got: Int,
                            whole: Int,
                        ) = got <= edge + 1.5 && got >= minOf(edge, whole.toDouble()) - 40
                        held(r.first, wholeTitle) && held(r.second, wholeBody)
                    }
                }
            assertTrue(words, "title and body per frame: $reached")
            assertEquals(listOf(PlakatEvent.Print), heard)
        }

    private fun ComposeUiTest.masthead(
        fold: Float,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        collapsed: String = "СВЁРНУТО",
        density: Float = 1f,
    ): PlakatTopAppBarState {
        val state = PlakatTopAppBarState()
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                PlakatTheme(
                    colors = paper,
                    reduceMotion = true,
                    print =
                        PlakatPrintSwitches(
                            texture = false,
                        ),
                    events = {
                        heard += it
                    },
                ) {
                    Box {
                        PlakatMasthead(
                            "ЛИСТ",
                            Modifier.width(390.dp).testTag("m"),
                            startLabel = "№ 47",
                            endLabel = "НОЯБРЬ",
                            collapsedTitle = collapsed,
                            state = state,
                        )
                    }
                }
            }
        }
        waitForIdle()
        state.foldTo(fold * density, 136f * density)
        waitForIdle()
        return state
    }

    private fun ComposeUiTest.mastheadFrame() = onNodeWithTag("m").captureToImage().toPixelMap()

    @Test
    fun `open it is 195 high, the title 120 under its labels and the bar under that`() =
        runComposeUiTest {
            masthead(0f)
            onNodeWithTag("m").assertHeightIsEqualTo(195.dp)
            val px = mastheadFrame()
            // 16 + 15 of labels + 8, then 120 of title, 12, then the bar of 8.
            assertEquals(paper.paper, px[100, 170])
            assertEquals(paper.ink, px[100, 171])
            assertEquals(paper.ink, px[100, 178])
            assertEquals(paper.paper, px[100, 179])
            // The word's ink fills the 120, the round С's overshoot reaching its top row and the flat Л's top 2 under it.
            assertTrue((16 until 374).any { px[it, 39] == paper.ink }, "the title's top row")
            assertTrue((16 until 374).none { px[it, 38] == paper.ink }, "and nothing above it")
            assertEquals(41..158, stems(px, 0, 165), "the Л's stem, to the baseline")
            assertTrue((16 until 60).any { x -> (16 until 31).any { y -> px[x, y] == paper.ink } }, "the start label")
            assertTrue((300 until 374).any { x -> (16 until 31).any { y -> px[x, y] == paper.ink } }, "the end label")
        }

    /** The rows of the Л's right stem, at column [STEM], between [from] and [until]. */
    private fun stems(
        px: PixelMap,
        from: Int,
        until: Int,
    ) = (from until until).filter { y -> px[STEM, y] == paper.ink }.let { it.first()..it.last() }

    @Test
    fun `folding it rides up whole, at its height, and is not scaled`() =
        runComposeUiTest {
            masthead(60f)
            onNodeWithTag("m").assertHeightIsEqualTo(135.dp)
            val px = mastheadFrame()
            // Everything 60 up: the bar at 111…118, the title's foot at 98 and the title still running above the top.
            assertEquals(paper.paper, px[100, 110])
            assertEquals(paper.ink, px[100, 111])
            assertEquals(paper.ink, px[100, 118])
            assertEquals(paper.paper, px[100, 119])
            assertEquals(0..98, stems(px, 0, 105), "120 high still, 21 of it gone over the top")
            assertEquals(paper.paper, px[20, 133], "of the rule, 60 / 136 of its 3")
            assertEquals(paper.ink, px[20, 134])
        }

    @Test
    fun `at a density of 2 it rides by the same dp`() =
        runComposeUiTest {
            masthead(60f, density = 2f)
            val px = mastheadFrame()
            assertEquals(135 * 2, px.height)
            assertEquals(paper.paper, px[200, 219])
            assertEquals(paper.ink, px[200, 222])
            assertEquals(paper.ink, px[200, 236])
            assertEquals(paper.paper, px[200, 239])
        }

    @Test
    fun `the ordinary bar comes down in the fold's last 56, over what rides up`() =
        runComposeUiTest {
            // No word, so the row's paper reads alone. Its foot is the fold less 80.
            val state = masthead(80f, collapsed = "")
            assertEquals(paper.ink, mastheadFrame()[STEM, 0], "at 80 it has not begun")
            for (fold in listOf(90f, 100f, 108f, 115f)) {
                state.foldTo(fold, 136f)
                waitForIdle()
                val px = mastheadFrame()
                val foot = (fold - 80).toInt()
                assertEquals(paper.paper, px[STEM, 0], "its paper at the top, at $fold")
                assertEquals(paper.paper, px[STEM, foot - 1], "its paper to its foot, at $fold")
                assertEquals(paper.ink, px[STEM, foot], "and the title from there on, at $fold")
            }
        }

    @Test
    fun `until it comes down the ordinary bar is not read and takes no focus`() =
        runComposeUiTest {
            val state = masthead(80f)
            onNodeWithText("СВЁРНУТО").assertDoesNotExist()
            state.foldTo(81f, 136f)
            waitForIdle()
            onNodeWithText("СВЁРНУТО").assertExists()
        }

    @Test
    fun `folded it is 59 high with its row and rule, and the fold is heard at its end`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            masthead(136f, heard)
            onNodeWithTag("m").assertHeightIsEqualTo(59.dp)
            val px = mastheadFrame()
            assertEquals(paper.paper, px[200, 55], "above the rule")
            assertEquals(paper.ink, px[200, 56], "the rule of 3")
            assertEquals(paper.ink, px[200, 58])
            assertEquals(paper.paper, px[370, 30], "the row's paper, over what rode up")
            assertTrue((0 until 300).any { x -> (10 until 46).any { y -> px[x, y] == paper.ink } }, "its word")
            assertEquals(listOf(PlakatEvent.Fold), heard)
        }

    @Test
    fun `its title is the heading`() =
        runComposeUiTest {
            masthead(0f)
            onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertIsDisplayed()
        }

    private companion object {
        /** A column through the right stem of the masthead's Л. */
        const val STEM = 80
    }
}
