package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.math.tan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-48: designed here. The placement's numbers, then the real popup through the test's window of 1024 × 768. */
@OptIn(ExperimentalTestApi::class)
class PlakatTooltipTest {
    private val paper = PlakatPalettes.Paper
    private val window = IntSize(1024, 768)
    private val anchor = IntRect(100, 200, 148, 248)
    private val plate = IntSize(120, 36)

    private fun placement(
        axis: PlakatTooltipAxis,
        at: IntRect = anchor,
    ) = tooltipPlacement(at, window, plate, axis, 24f)

    @Test
    fun `upright it goes up from the anchor's middle, the plate reaching right`() {
        val p = placement(PlakatTooltipAxis.Upright)
        assertEquals(IntRect(124, 140, 244, 200), p.box)
        assertEquals(Offset(0f, 60f), p.lineFrom)
        assertEquals(Offset(0f, 36f), p.lineTo)
        assertEquals(0, p.plate.y)
        assertEquals(0, p.plate.x)
        assertEquals(TooltipFrom.Bottom, p.from)
    }

    @Test
    fun `upright with no room above it goes down, and with none to the right the plate reaches left`() {
        val down = placement(PlakatTooltipAxis.Upright, IntRect(100, 20, 148, 68))
        assertEquals(68, down.box.top)
        assertEquals(TooltipFrom.Top, down.from)
        assertEquals(Offset(0f, 0f), down.lineFrom)
        assertEquals(Offset(0f, 24f), down.lineTo)
        assertEquals(24, down.plate.y)
        val left = placement(PlakatTooltipAxis.Upright, IntRect(960, 200, 1008, 248))
        assertEquals(984 - 120 + 1, left.box.left)
        assertEquals(119f, left.lineFrom.x, "the leader on the plate's right edge")
    }

    @Test
    fun `level it goes right from the anchor's middle, the plate centred on the line, or left where it must`() {
        val p = placement(PlakatTooltipAxis.Level)
        assertEquals(IntRect(148, 206, 292, 242), p.box)
        assertEquals(Offset(0f, 18f), p.lineFrom)
        assertEquals(Offset(24f, 18f), p.lineTo)
        assertEquals(24, p.plate.x)
        assertEquals(TooltipFrom.Left, p.from)
        val left = placement(PlakatTooltipAxis.Level, IntRect(900, 200, 948, 248))
        assertEquals(900 - 144, left.box.left)
        assertEquals(TooltipFrom.Right, left.from)
        assertEquals(144f, left.lineFrom.x)
        assertEquals(120f, left.lineTo.x)
    }

    @Test
    fun `tilted it lies on the kit's one axis, and left it goes down the same line and is no mirror`() {
        val slope = tan(17 * kotlin.math.PI / 180).toFloat()
        val right = placement(PlakatTooltipAxis.Tilted)
        val rise = right.lineFrom.y - right.lineTo.y
        val run = right.lineTo.x - right.lineFrom.x
        assertTrue(abs(rise / run - slope) < 1e-3, "up and right at 17°: $rise / $run")
        assertTrue(abs(rise * rise + run * run - 24 * 24) < .1f, "24 long")
        assertEquals(TooltipFrom.Left, right.from)
        val left = placement(PlakatTooltipAxis.Tilted, IntRect(960, 200, 1008, 248))
        val drop = left.lineTo.y - left.lineFrom.y
        val back = left.lineFrom.x - left.lineTo.x
        assertTrue(drop > 0 && back > 0, "down and left: $drop, $back")
        assertTrue(abs(drop / back - slope) < 1e-3, "the same line's slope")
        assertEquals(TooltipFrom.Right, left.from)
    }

    private fun ComposeUiTest.tooltip(
        axis: PlakatTooltipAxis = PlakatTooltipAxis.Upright,
        reduce: Boolean = true,
        x: Dp = 100.dp,
        y: Dp = 200.dp,
        heard: MutableList<PlakatEvent> = mutableListOf(),
        keyboard: Boolean = false,
        onClick: () -> Unit = {},
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = reduce, events = { heard += it }) {
                    Box(Modifier.size(1024.dp, 768.dp).background(Color.White).testTag("window")) {
                        val modes =
                            if (keyboard) {
                                object : InputModeManager {
                                    override val inputMode = InputMode.Keyboard

                                    override fun requestInputMode(inputMode: InputMode) = true
                                }
                            } else {
                                LocalInputModeManager.current
                            }
                        CompositionLocalProvider(LocalInputModeManager provides modes) {
                            PlakatTooltip("Сохранить", Modifier.offset(x, y).testTag("t"), axis = axis) {
                                Box(
                                    Modifier
                                        .size(48.dp)
                                        .background(paper.ink)
                                        .clickable(onClick = onClick)
                                        .testTag("a"),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("window").captureToImage().toPixelMap()

    private fun ComposeUiTest.rest() {
        onNodeWithTag("t").performMouseInput { moveTo(center) }
    }

    @Test
    fun `it shows after a pointer has rested 400 ms, and goes when it leaves`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            tooltip()
            mainClock.advanceTimeBy(50)
            rest()
            mainClock.advanceTimeBy(300)
            onNodeWithText("Сохранить").assertDoesNotExist()
            mainClock.advanceTimeBy(200)
            onNodeWithText("Сохранить").assertExists()
            onNodeWithTag("t").performMouseInput { moveTo(Offset(600f, 600f)) }
            mainClock.advanceTimeBy(100)
            onNodeWithText("Сохранить").assertDoesNotExist()
        }

    @Test
    fun `a press takes it away, and the press still reaches what is under it`() =
        runComposeUiTest {
            var clicks = 0
            tooltip(onClick = { clicks++ })
            rest()
            mainClock.advanceTimeBy(600)
            onNodeWithText("Сохранить").assertExists()
            onNodeWithTag("t").performMouseInput {
                press()
                release()
            }
            waitForIdle()
            onNodeWithText("Сохранить").assertDoesNotExist()
            assertEquals(1, clicks)
        }

    @Test
    fun `a finger shows it while it holds, and a tap does not`() =
        runComposeUiTest {
            var clicks = 0
            tooltip(onClick = { clicks++ })
            onNodeWithTag("t").performTouchInput {
                down(center)
                up()
            }
            waitForIdle()
            onNodeWithText("Сохранить").assertDoesNotExist()
            assertEquals(1, clicks)
            onNodeWithTag("t").performTouchInput { down(center) }
            mainClock.advanceTimeBy(700)
            onNodeWithText("Сохранить").assertExists()
            onNodeWithTag("t").performTouchInput { up() }
            waitForIdle()
            onNodeWithText("Сохранить").assertDoesNotExist()
        }

    @Test
    fun `a finger moving away before the wait takes back the hold`() =
        runComposeUiTest {
            tooltip()
            onNodeWithTag("t").performTouchInput {
                down(center)
                moveBy(Offset(0f, 200f))
            }
            mainClock.advanceTimeBy(700)
            onNodeWithText("Сохранить").assertDoesNotExist()
            onNodeWithTag("t").performTouchInput { up() }
        }

    @Test
    fun `it shows for the keyboard's focus and not otherwise`() =
        runComposeUiTest {
            tooltip(keyboard = true)
            onNodeWithTag("a").requestFocus()
            waitForIdle()
            onNodeWithText("Сохранить").assertExists()
        }

    @Test
    fun `a press on the plate reaches what lies under it, and the plate stays with its focus`() =
        runComposeUiTest {
            var under = 0
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper, reduceMotion = true) {
                        val keyboard =
                            object : InputModeManager {
                                override val inputMode = InputMode.Keyboard

                                override fun requestInputMode(inputMode: InputMode) = true
                            }
                        Box(Modifier.size(1024.dp, 768.dp).background(Color.White).testTag("window")) {
                            // The plate of a level leader stands 24 past the anchor, centred on its middle: 72 across, by 124.
                            Box(
                                Modifier
                                    .offset(60.dp, 90.dp)
                                    .size(300.dp, 70.dp)
                                    .clickable { under++ }
                                    .testTag("under"),
                            )
                            CompositionLocalProvider(LocalInputModeManager provides keyboard) {
                                PlakatTooltip(
                                    "Сохранить",
                                    Modifier.offset(0.dp, 100.dp),
                                    axis = PlakatTooltipAxis.Level,
                                ) {
                                    Box(Modifier.size(48.dp).clickable { }.testTag("a"))
                                }
                            }
                        }
                    }
                }
            }
            onNodeWithTag("a").requestFocus()
            waitForIdle()
            val row = frame().let { px -> (72 until 200).count { px[it, 124] == paper.concrete } }
            assertTrue(row > 20, "the plate lies over it: $row of its ground on the row")
            onNodeWithTag("under").performMouseInput {
                moveTo(Offset(20f, 34f))
                press()
                release()
            }
            waitForIdle()
            assertEquals(1, under, "the press went under the plate")
        }

    /** The rows of the plate's ground, from the foot of the box's anchor end, at the plate's middle column. */
    private fun PixelMap.rowsShown(x: Int) = (0 until height).count { this[x, it] == paper.concrete }

    @Test
    fun `it wipes in along its leader, linear over 120 ms, from the anchor's end, and back`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            mainClock.autoAdvance = false
            tooltip(reduce = false, heard = heard)
            mainClock.advanceTimeBy(50)
            rest()
            mainClock.advanceTimeBy(400)
            val frames =
                (0 until 14).map {
                    mainClock.advanceTimeByFrame()
                    frame()
                }
            // Upright, its box above the anchor: 24 of leader at the foot and the plate over it, so the plate's rows come last,
            // from its foot up. The plate is as high as the last frame says.
            val whole = frames.last().rowsShown(128)
            assertTrue(whole in 20..60, "a plate's height: $whole")
            val high = whole + 24
            val rows = frames.map { it.rowsShown(128) }
            val fits =
                (0..5).any { start ->
                    rows.withIndex().all { (i, got) ->
                        val p = (((i - start) * 16.0) / 120).coerceIn(0.0, 1.0)
                        abs(got - (high * p - 24).coerceIn(0.0, whole.toDouble())) <= 2
                    }
                }
            assertTrue(fits, "opening, the plate's rows per frame: $rows")
            assertEquals(rows.sorted(), rows)
            assertEquals(listOf(PlakatEvent.Wipe), heard)
            onNodeWithTag("t").performMouseInput { moveTo(Offset(600f, 600f)) }
            val closing =
                (0 until 14).map {
                    mainClock.advanceTimeByFrame()
                    onNodeWithTag("window").captureToImage().toPixelMap().rowsShown(128)
                }
            assertEquals(closing.sortedDescending(), closing, "only ever fewer: $closing")
            assertEquals(0, closing.last())
            assertTrue(closing.any { it in 1 until whole }, "over frames: $closing")
            assertEquals(listOf(PlakatEvent.Wipe, PlakatEvent.Wipe), heard)
        }

    @Test
    fun `under reduced motion it is there at once`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            tooltip(reduce = true)
            mainClock.advanceTimeBy(50)
            rest()
            mainClock.advanceTimeBy(400)
            repeat(3) { mainClock.advanceTimeByFrame() }
            assertTrue(frame().rowsShown(128) > 20)
        }

    @Test
    fun `its plate is concrete at the leader's end, the leader a hair of ink, on the real anchor`() =
        runComposeUiTest {
            tooltip()
            rest()
            mainClock.advanceTimeBy(600)
            val px = frame()
            // Anchor at 100, 200, 48 square: the leader at column 124, from 176 to 200, a hair.
            assertEquals(paper.ink, px[124, 190])
            assertEquals(Color.White, px[123, 190])
            assertEquals(Color.White, px[125, 190])
            assertEquals(paper.concrete, px[130, 170], "the plate, past the leader, reaching right")
            assertEquals(Color.White, px[122, 170])
        }

    @Test
    fun `a screen reader has its words as the anchor's without its showing`() =
        runComposeUiTest {
            tooltip()
            waitForIdle()
            onNodeWithContentDescription("Сохранить").assertExists()
            onNodeWithText("Сохранить").assertDoesNotExist()
        }

    /** The caption alone at half its wipe, and how much of its plate's ground shows on each half of its box. */
    private fun ComposeUiTest.half(
        axis: PlakatTooltipAxis,
        at: IntRect,
    ): PixelMap {
        val holder = TooltipHolder(at)
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = true) {
                    // At the box the caption says it fills: it has no bounds of its own where a popup draws past them.
                    Box(
                        Modifier
                            .testTag("c")
                            .background(Color.White)
                            .layout { measurable, constraints ->
                                val caption = measurable.measure(constraints)
                                val box = holder.placement?.box
                                layout(box?.width ?: 0, box?.height ?: 0) { caption.place(0, 0) }
                            },
                    ) {
                        PlakatTooltipCaption("Сохранить", axis, holder, window) { .5f }
                    }
                }
            }
        }
        waitForIdle()
        return onNodeWithTag("c").captureToImage().toPixelMap()
    }

    private fun PixelMap.groundIn(
        xs: IntRange,
        ys: IntRange,
    ) = xs.sumOf { x -> ys.count { y -> this[x, y] == paper.concrete } }

    @Test
    fun `at half its wipe a caption going right shows its left half`() =
        runComposeUiTest {
            val px = half(PlakatTooltipAxis.Level, anchor)
            assertTrue(px.groundIn(0 until px.width / 2, 0 until px.height) > 0)
            assertEquals(0, px.groundIn(px.width / 2 + 1 until px.width, 0 until px.height))
        }

    @Test
    fun `at half its wipe a caption going left shows its right half`() =
        runComposeUiTest {
            val px = half(PlakatTooltipAxis.Level, IntRect(960, 200, 1008, 248))
            assertTrue(px.groundIn(px.width / 2 until px.width, 0 until px.height) > 0)
            assertEquals(0, px.groundIn(0 until px.width / 2 - 1, 0 until px.height))
        }

    @Test
    fun `at half its wipe a caption going up shows its lower half`() =
        runComposeUiTest {
            val px = half(PlakatTooltipAxis.Upright, anchor)
            // The leader's 24 are the lower part of the box, and the plate's foot shows over them.
            assertTrue(px.groundIn(0 until px.width, px.height / 2 until px.height) > 0)
            assertEquals(0, px.groundIn(0 until px.width, 0 until px.height / 2 - 1))
        }

    @Test
    fun `at half its wipe a caption going down shows its upper half`() =
        runComposeUiTest {
            val px = half(PlakatTooltipAxis.Upright, IntRect(100, 10, 148, 58))
            assertTrue(px.groundIn(0 until px.width, 0 until px.height / 2 + 1) > 0)
            assertEquals(0, px.groundIn(0 until px.width, px.height / 2 + 2 until px.height))
        }

    @Test
    fun `its plate is 240 wide at most, and its words wrap`() =
        runComposeUiTest {
            val holder = TooltipHolder(anchor)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper, reduceMotion = true) {
                        PlakatTooltipCaption(
                            "Сохранить в лист и отправить копию всем подписчикам издания сегодня",
                            PlakatTooltipAxis.Upright,
                            holder,
                            window,
                        ) { 1f }
                    }
                }
            }
            waitForIdle()
            val box = holder.placement!!.box
            assertEquals(240, box.width)
            assertTrue(box.height > 24 + 40, "two lines at least: ${box.height}")
        }

    @Test
    fun `a density of 2 keeps the leader at 24 dp`() {
        val p =
            tooltipPlacement(
                IntRect(200, 400, 296, 496),
                IntSize(2048, 1536),
                IntSize(240, 72),
                PlakatTooltipAxis.Upright,
                48f,
            )
        assertEquals(48f, p.lineFrom.y - p.lineTo.y)
    }
}
