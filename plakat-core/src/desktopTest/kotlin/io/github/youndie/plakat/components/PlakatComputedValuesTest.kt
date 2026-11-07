package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.agit.PlakatGeneratedImage
import io.github.youndie.plakat.agit.PlakatHalftoneMode
import io.github.youndie.plakat.agit.PlakatRapportFill
import io.github.youndie.plakat.agit.plakatHalftone
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/** B-66: a value a consumer can compute is held to its range, and nothing hangs. */
@OptIn(ExperimentalTestApi::class)
class PlakatComputedValuesTest {
    private val paper = PlakatPalettes.Paper

    private fun Color.isRed() =
        abs(red - paper.red.red) < .02f && abs(green - paper.red.green) < .02f && abs(blue - paper.red.blue) < .02f

    private fun PixelMap.reds(): Int {
        var n = 0
        for (y in 0 until height) for (x in 0 until width) if (this[x, y].isRed()) n++
        return n
    }

    private fun ComposeUiTest.on(content: @Composable () -> Unit) {
        mainClock.autoAdvance = false
        setContent { PlakatTheme(reduceMotion = true) { Box(Modifier.fillMaxSize().padding(40.dp)) { content() } } }
        mainClock.advanceTimeBy(500)
    }

    private fun ComposeUiTest.rootReds() = onAllNodes(isRoot())[0].captureToImage().toPixelMap().reds()

    /** Fails rather than freezes: what this holds used to loop for ever on the main thread. */
    private fun finishes(
        name: String,
        content: @Composable () -> Unit,
    ) {
        var thrown: Throwable? = null
        val run =
            Thread {
                runCatching {
                    runComposeUiTest {
                        on(content)
                        onAllNodes(isRoot())[0].captureToImage()
                    }
                }.onFailure { thrown = it }
            }.apply {
                isDaemon = true
                start()
            }
        run.join(30_000)
        if (run.isAlive) fail("$name does not finish")
        thrown?.let { throw it }
    }

    @Test
    fun `NaN is nothing done yet in both progresses`() =
        runComposeUiTest {
            on {
                val nothing = Float.NaN
                Column {
                    PlakatLinearProgress(nothing, Modifier.testTag("line"))
                    PlakatCircularProgress(nothing, Modifier.testTag("ring"))
                }
            }
            assertEquals(0, rootReds(), "a progress of NaN shows something done")
            for (tag in listOf("line", "ring")) {
                assertEquals(
                    0f,
                    onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current,
                )
            }
        }

    @Test
    fun `a slider stands at the start on NaN and on a range with nothing in it`() {
        for ((value, range) in listOf(Float.NaN to 0f..1f, .5f to 1f..0f, .5f to Float.NaN..1f)) {
            runComposeUiTest {
                on { PlakatSlider(value, {}, Modifier.width(200.dp).testTag("s"), valueRange = range) }
                val info = onNodeWithTag("s").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
                assertEquals(info.range.start, info.current, "$value in $range")
                assertEquals(0, rootReds(), "no fill for $value in $range")
            }
        }
    }

    @Test
    fun `a tick past the largest Int goes through and ends on its number`() {
        val tick = PlakatTick(2_000_000_000, 1_999_999_999)
        var ms = 0.0
        while (ms <= tick.totalMs) {
            tick.valueAt(ms)
            tick.textAt(ms)
            ms += 8.0
        }
        assertEquals(1_999_999_999, tick.valueAt(tick.totalMs.toDouble()))
        assertEquals("1999999999", tick.textAt(tick.totalMs.toDouble()))
    }

    private val two = listOf("ONE", "TWO")

    private fun noMark(row: @Composable (Int) -> Unit) {
        for (index in listOf(-1, 2, 9)) {
            runComposeUiTest {
                on { row(index) }
                assertEquals(0, rootReds(), "a mark for index $index")
            }
        }
    }

    @Test
    fun `tabs with no choice in them show no bar`() =
        noMark { i -> PlakatTabs(two.map { PlakatTab(it) }, i, {}, Modifier.width(240.dp)) }

    @Test
    fun `a navigation bar with no choice in it shows no square`() =
        noMark { i ->
            PlakatNavigationBar(
                two.map { PlakatNavigationItem(PlakatIcons.Home, it) },
                i,
                {},
                Modifier.width(240.dp),
            )
        }

    @Test
    fun `a drawer with no choice in it shows no square`() =
        noMark { i ->
            Box(Modifier.size(300.dp, 300.dp)) {
                PlakatNavigationDrawer(true, two.map { PlakatNavigationItem(PlakatIcons.Home, it) }, i, {}, {})
            }
        }

    @Test
    fun `an empty row shows no mark`() =
        runComposeUiTest {
            on {
                Column {
                    PlakatTabs(emptyList(), 0, {}, Modifier.width(240.dp))
                    PlakatNavigationBar(emptyList(), 0, {}, Modifier.width(240.dp))
                }
            }
            assertEquals(0, rootReds())
        }

    @Test
    fun `a segmented button with no choice in it fills nothing`() =
        runComposeUiTest {
            on { PlakatSegmentedButton(two, -1, {}, Modifier.testTag("s")) }
            val map = onAllNodes(isRoot())[0].captureToImage().toPixelMap()
            val box = onNodeWithTag("s").getBoundsInRoot()
            val y = ((box.top + box.bottom) / 2).value.toInt()
            // Left of the box, where -1 would run, and inside its first word's margin, nothing is drawn.
            assertEquals(0f, map[box.left.value.toInt() + 6, y].alpha, "the first segment is filled")
            val outside = (0 until box.left.value.toInt()).count { map[it, y].alpha > 0f }
            assertEquals(0, outside, "ink left of the box")
        }

    @Test
    fun `from nothing chosen the bar comes to its place without a run, and with no rail`() =
        runComposeUiTest {
            var index by mutableIntStateOf(-1)
            val heard = mutableListOf<PlakatEvent>()
            mainClock.autoAdvance = false
            setContent {
                PlakatTheme(reduceMotion = false, events = { heard += it }) {
                    PlakatTabs(two.map { PlakatTab(it) }, index, {}, Modifier.width(240.dp).testTag("t"))
                }
            }
            mainClock.advanceTimeBy(500)
            index = 1
            mainClock.advanceTimeByFrame()
            mainClock.advanceTimeByFrame()
            val map = onNodeWithTag("t").captureToImage().toPixelMap()
            val y = map.height - 3
            assertTrue(map[60, y].isRed().not(), "the bar ran from the first slot")
            assertTrue(map[180, y].isRed(), "the bar is not at its place")
            assertEquals(emptyList(), heard)
        }

    @Test
    fun `a change between two choices still runs with its rail`() =
        runComposeUiTest {
            var index by mutableIntStateOf(0)
            val heard = mutableListOf<PlakatEvent>()
            mainClock.autoAdvance = false
            setContent {
                PlakatTheme(reduceMotion = false, events = { heard += it }) {
                    PlakatTabs(two.map { PlakatTab(it) }, index, {}, Modifier.width(240.dp))
                }
            }
            mainClock.advanceTimeBy(500)
            index = 1
            mainClock.advanceTimeBy(500)
            assertEquals(listOf<PlakatEvent>(PlakatEvent.Rail), heard)
        }

    @Test
    fun `a counter with a number too large for its pictograms keeps its caption and says the rest`() =
        runComposeUiTest {
            on {
                PlakatCounter(20_000, "ARTICLES", Modifier.width(240.dp).testTag("tile"))
            }
            onNodeWithText("+19900").assertIsDisplayed()
            onNodeWithText("ARTICLES").assertIsDisplayed()
            val tile = onNodeWithTag("tile").getBoundsInRoot()
            assertTrue(tile.bottom - tile.top < 300.dp, "a tile ${tile.bottom - tile.top} high")
        }

    @Test
    fun `a counter under the most is as it was`() =
        runComposeUiTest {
            on { PlakatCounter(47, "ARTICLES", perPictogram = 5) }
            onNodeWithText("+2").assertIsDisplayed()
        }

    @Test
    fun `a rapport with a cell of nothing finishes`() {
        for (cell in listOf(0.dp, (-4).dp, Dp.Unspecified)) {
            finishes("a rapport of $cell") { PlakatRapportFill(1, Modifier.fillMaxWidth().height(48.dp), cell = cell) }
        }
    }

    @Test
    fun `a halftone with a pitch of nothing finishes`() {
        val source = ImageBitmap(16, 16).also { Canvas(it) }
        for (pitch in listOf(0.dp, (-4).dp, Dp.Unspecified)) {
            finishes("a halftone of $pitch") {
                Box(
                    Modifier
                        .size(
                            32.dp,
                        ).plakatHalftone(source, Color.Black, Color.White, PlakatHalftoneMode.Dots, pitch),
                )
            }
        }
    }

    @Test
    fun `a generated image takes a frame turned inside out as an empty one`() =
        runComposeUiTest {
            on { PlakatGeneratedImage(1, Modifier.size(50.dp), frameWidth = (-8).dp, frameHeight = 16.dp) }
            onAllNodes(isRoot())[0].captureToImage()
        }

    @Test
    fun `a window outside its panels shows the nearest one`() {
        val panels = listOf("FIRST", "LAST").map { PlakatWindowPanel(it) { Box(Modifier.fillMaxSize()) } }
        for ((page, shown) in listOf(9 to "LAST", -3 to "FIRST")) {
            runComposeUiTest {
                on { PlakatWindow(panels, page, Modifier.width(240.dp)) }
                val at = onNodeWithText(shown).getBoundsInRoot()
                assertEquals(40.dp, at.left, "the page $page shows $shown")
            }
        }
    }

    @Test
    fun `tab content outside its pages shows the nearest one`() {
        for ((index, shown) in listOf(-1 to "P0", 9 to "P1")) {
            runComposeUiTest {
                on { PlakatTabContent(2, index) { BasicText("P$it") } }
                onNodeWithText(shown).assertIsDisplayed()
            }
        }
    }
}
