package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-70: a display title stays on its sheet, whatever its length and the system's font size. */
@OptIn(ExperimentalTestApi::class)
class PlakatFixedSheetsTest {
    private val long = listOf("NOVEMBERNOVEMBER", "NOVEMBERNOVEMBER", "NOVEMBERNOVEMBER", "NOVEMBERNOVEMBER")
    private val short = listOf("READ", "THIS")

    @Test
    fun `a title that fits keeps its size and one that does not stops at the first edge it reaches`() {
        val room = Rect(0f, 0f, 400f, 200f)
        assertEquals(1f, posterTitleFit(listOf(100f), 50f, 50f, Offset(16f, 16f), 0f, room))
        assertEquals(
            .5f,
            posterTitleFit(listOf(768f), 50f, 50f, Offset(16f, 16f), 0f, room),
            1e-4f,
            "by the right edge",
        )
        assertEquals(.46f, posterTitleFit(List(8) { 10f }, 50f, 50f, Offset(16f, 16f), 0f, room), 1e-4f, "by the foot")
        // Turned up by 90° about (20, 100), a line of 200 reaches the top at half.
        assertEquals(.5f, posterTitleFit(listOf(200f), 10f, 10f, Offset(20f, 100f), -90f, room), 1e-4f)
    }

    /** The box of the pixels a [title] draws on the [layout]'s sheet, read as the difference from the same sheet with none. */
    private fun titleBox(
        layout: PlakatPosterLayout,
        title: List<String>,
        fontScale: Float = 1f,
        body: String? = null,
    ): Rect {
        var box = Rect.Zero
        runComposeUiTest {
            var words by mutableStateOf(listOf(""))
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, fontScale)) {
                    PlakatTheme(reduceMotion = true, print = PlakatPrintSwitches(texture = false)) {
                        Box(Modifier.size(layout.width, layout.height).testTag("p")) {
                            PlakatPoster(words, "GO", {}, layout = layout, body = body, seed = 7, print = false)
                        }
                    }
                }
            }

            fun shot(): PixelMap = onNodeWithTag("p").captureToImage().toPixelMap()
            val none = shot()
            words = title
            waitForIdle()
            val with = shot()
            var left = Int.MAX_VALUE
            var top = Int.MAX_VALUE
            var right = -1
            var bottom = -1
            for (y in 0 until none.height) {
                for (x in 0 until none.width) {
                    if (none[x, y] != with[x, y]) {
                        left = min(left, x)
                        top = min(top, y)
                        right = max(right, x)
                        bottom = max(bottom, y)
                    }
                }
            }
            box = Rect(left.toFloat(), top.toFloat(), right + 1f, bottom + 1f)
        }
        return box
    }

    private fun assertInside(
        box: Rect,
        right: Float,
        bottom: Float,
        what: String,
    ) {
        assertTrue(box.width > 0f, "$what drew nothing")
        // The registration stands 1 off the ink, and an edge is antialiased.
        assertTrue(box.right <= right + 2f, "$what reaches $box, its room ends at $right")
        assertTrue(box.bottom <= bottom + 2f, "$what reaches $box, its room ends at $bottom")
    }

    @Test
    fun `a long title stays on each layout's sheet and clear of the slogan`() {
        assertInside(titleBox(PlakatPosterLayout.Banner, long), 374f, 96f, "Banner")
        assertInside(titleBox(PlakatPosterLayout.Wide, long), 284f, 320f, "Wide")
        assertInside(titleBox(PlakatPosterLayout.Circle, long), 390f, 320f, "Circle")
        assertInside(titleBox(PlakatPosterLayout.Background, long), 390f, 436f, "Background")
    }

    @Test
    fun `at font scale 2 a banner's second line clears its slogan`() {
        assertInside(titleBox(PlakatPosterLayout.Banner, short, fontScale = 2f), 374f, 96f, "Banner")
    }

    @Test
    fun `a title leaves room for a body`() {
        assertInside(titleBox(PlakatPosterLayout.Circle, long, body = "Twelve new issues."), 390f, 252f, "Circle")
    }

    private fun ComposeUiTest.bar(
        fontScale: Float,
        content: @Composable () -> Unit,
    ) = setContent {
        CompositionLocalProvider(LocalDensity provides Density(1f, fontScale)) {
            PlakatTheme(reduceMotion = true) { Box(Modifier.size(390.dp, 300.dp)) { content() } }
        }
    }

    @Test
    fun `at font scale 2 the bar's title clears its rapport strip`() =
        runComposeUiTest {
            bar(2f) { PlakatTopAppBar("NOVEMBER") }
            val title = onNodeWithText("NOVEMBER", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            assertTrue(
                title.bottom <= 104f,
                "the title's foot at font scale 2: ${title.bottom}, the strip's top at 104",
            )
        }
}
