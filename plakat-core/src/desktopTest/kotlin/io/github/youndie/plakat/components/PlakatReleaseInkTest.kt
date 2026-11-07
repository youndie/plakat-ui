package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.down
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * B-68: on release a word turns over with its ground halfway back, so no frame has it ink on ink. The
 * design's CSS changed it the moment the finger lifted, and the worst frame was 1:1.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatReleaseInkTest {
    private val papers = listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)

    private fun contrast(
        a: Color,
        b: Color,
    ): Float {
        val x = a.luminance() + .05f
        val y = b.luminance() + .05f
        return max(x, y) / min(x, y)
    }

    @Test
    fun `at every point of the way back a button's word reads on its ground`() {
        for (colors in papers) {
            for (kind in PlakatButtonKind.entries) {
                val worst =
                    (0..100).minOf { percent ->
                        val paint = buttonPaint(kind, colors, enabled = true, pressed = percent / 100f)
                        contrast(paint.ink, paint.ground.compositeOver(colors.paper))
                    }
                assertTrue(worst >= WORST_CONTRAST, "$kind: $worst")
            }
        }
    }

    /** The best contrast of anything in the middle of the frame against its ground, read at [groundAt]. */
    private fun PixelMap.wordContrast(groundAt: (Int, Int) -> Pair<Int, Int>): Float {
        val (gx, gy) = groundAt(width, height)
        val ground = this[gx, gy]
        var best = 1f
        for (y in height / 4 until height * 3 / 4) {
            for (x in width / 10 until width * 9 / 10) best = max(best, contrast(this[x, y], ground))
        }
        return best
    }

    private fun releases(
        colors: PlakatColors,
        name: String,
        groundAt: (Int, Int) -> Pair<Int, Int>,
        control: @Composable (Modifier) -> Unit,
    ) = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = colors, reduceMotion = false) {
                    Box(Modifier.background(colors.paper).padding(8.dp)) { control(Modifier.testTag("c")) }
                }
            }
        }
        mainClock.advanceTimeBy(100)
        onNodeWithTag("c").performTouchInput { down(center) }
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        onNodeWithTag("c").performTouchInput { up() }
        val frames =
            (0 until 7).map {
                mainClock.advanceTimeByFrame()
                onNodeWithTag("c").captureToImage().toPixelMap().wordContrast(groundAt)
            }
        assertTrue(frames.all { it >= WORST_CONTRAST }, "$name per frame: $frames")
    }

    private val nearTheFarCorner: (Int, Int) -> Pair<Int, Int> = { w, h -> w - 10 to h - 10 }

    @Test
    fun `a filled button on black paper`() =
        releases(PlakatPalettes.BlackPaper, "filled", nearTheFarCorner) {
            PlakatButton("читать", {}, it.width(164.dp))
        }

    @Test
    fun `an outlined button on either paper`() =
        papers.forEach { colors ->
            releases(colors, "outlined", nearTheFarCorner) { PlakatOutlinedButton("читать", {}, it.width(164.dp)) }
        }

    @Test
    fun `a list item on either paper`() =
        papers.forEach { colors ->
            releases(colors, "list item", nearTheFarCorner) {
                PlakatListItem("Заметка о печати", it.width(280.dp), onClick = {})
            }
        }

    @Test
    fun `an icon button on either paper`() =
        papers.forEach { colors ->
            releases(colors, "icon button", { _, _ -> 3 to 3 }) { PlakatIconButton(PlakatIcons.Menu, "Меню", {}, it) }
        }

    @Test
    fun `a FAB on black paper`() =
        releases(PlakatPalettes.BlackPaper, "FAB", { w, h -> w / 8 to h / 2 }) {
            PlakatFab(PlakatIcons.Search, "Найти", {}, it, rideIn = false)
        }

    @Test
    fun `a drawer row on either paper`() =
        papers.forEach { colors ->
            runComposeUiTest {
                mainClock.autoAdvance = false
                val items =
                    listOf(PlakatIcons.Home to "ЛЕНТА", PlakatIcons.Search to "ПОИСК").map { (i, w) ->
                        PlakatNavigationItem(i, w)
                    }
                setContent {
                    CompositionLocalProvider(LocalDensity provides Density(1f)) {
                        PlakatTheme(colors = colors, reduceMotion = false) {
                            Box(Modifier.size(390.dp, 400.dp).testTag("c")) {
                                PlakatNavigationDrawer(true, items, 0, {}, {})
                            }
                        }
                    }
                }
                mainClock.advanceTimeBy(1_000)
                // The second row, not chosen: 56 high from 72, in a panel 280 wide.
                onNodeWithTag("c").performTouchInput { down(Offset(140f, 100f)) }
                mainClock.advanceTimeByFrame()
                mainClock.advanceTimeByFrame()
                onNodeWithTag("c").performTouchInput { up() }
                val frames =
                    (0 until 7).map {
                        mainClock.advanceTimeByFrame()
                        val px = onNodeWithTag("c").captureToImage().toPixelMap()
                        val ground = px[270, 100]
                        (80 until 120).maxOf { y -> (8 until 262).maxOf { x -> contrast(px[x, y], ground) } }
                    }
                assertTrue(frames.all { it >= WORST_CONTRAST }, "drawer row per frame: $frames")
            }
        }

    private companion object {
        /** Under a text's 4.5:1, and for a frame or two of 60 ms: the point is that it never reaches 1:1. */
        const val WORST_CONTRAST = 2f
    }
}
