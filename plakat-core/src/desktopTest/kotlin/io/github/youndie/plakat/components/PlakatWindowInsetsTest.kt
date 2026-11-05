package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * B-54: what owns an edge of the screen runs its ground under the system's bar and stands inside it. A desktop test's window
 * has no such bar, so each is given one: 24 at the top, 34 at the foot. That the defaults ask for the system's own is seen on
 * the device, `docs/backlog/B-54-window-insets.md`.
 */
@OptIn(ExperimentalTestApi::class)
class PlakatWindowInsetsTest {
    private val paper = PlakatPalettes.Paper
    private val top = WindowInsets(top = 24.dp)
    private val foot = WindowInsets(bottom = 34.dp)
    private val items =
        listOf(PlakatNavigationItem(PlakatIcons.Home, "ЛЕНТА"), PlakatNavigationItem(PlakatIcons.Search, "ПОИСК"))

    private fun ComposeUiTest.screen(content: @Composable BoxScope.() -> Unit) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(colors = paper, reduceMotion = true, print = PlakatPrintSwitches(texture = false)) {
                    Box(Modifier.size(390.dp, 400.dp).background(Color.White).testTag("box")) { content() }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.frame(): PixelMap = onNodeWithTag("box").captureToImage().toPixelMap()

    @Test
    fun `a top app bar's paper runs under the bar, the rest of it 24 lower`() =
        runComposeUiTest {
            screen { PlakatTopAppBar("ЛИСТ", Modifier.testTag("t"), windowInsets = top) }
            onNodeWithTag("t").assertHeightIsEqualTo(24.dp + 128.dp)
            val px = frame()
            assertEquals(paper.paper, px[200, 0], "its paper at the very top")
            assertEquals(paper.paper, px[200, 23])
            assertEquals(Color.White, px[200, 24 + 128], "and its foot 24 lower than without")
        }

    @Test
    fun `no inset leaves a top app bar as it was`() =
        runComposeUiTest {
            screen { PlakatTopAppBar("ЛИСТ", Modifier.testTag("t"), windowInsets = WindowInsets(0, 0, 0, 0)) }
            onNodeWithTag("t").assertHeightIsEqualTo(128.dp)
        }

    @Test
    fun `a masthead stands 24 lower, its bar with it`() =
        runComposeUiTest {
            screen { PlakatMasthead("ЛИСТ", Modifier.testTag("t"), windowInsets = top) }
            onNodeWithTag("t").assertHeightIsEqualTo(219.dp)
            val px = frame()
            assertEquals(paper.paper, px[100, 10], "paper under the system's bar")
            assertEquals(paper.ink, px[100, 24 + 171], "the masthead's bar")
            assertEquals(paper.paper, px[100, 24 + 170])
        }

    @Test
    fun `a navigation bar's ground runs under the bar, its strip above it`() =
        runComposeUiTest {
            screen { PlakatNavigationBar(items, 0, {}, Modifier.fillMaxWidth().testTag("n"), windowInsets = foot) }
            onNodeWithTag("n").assertHeightIsEqualTo(64.dp + 34.dp)
            val px = frame()
            assertEquals(paper.ink, px[10, 97], "its ground to the foot")
            assertEquals(paper.ink, px[10, 70], "and over the strip")
            // The target is the strip's, not under the system's bar.
            val slot = onNodeWithContentDescription("ЛЕНТА").fetchSemanticsNode().boundsInRoot
            assertEquals(0f, slot.top)
            assertEquals(64f, slot.bottom)
        }

    @Test
    fun `a bottom sheet's ground runs to the foot and its content stops above the bar`() =
        runComposeUiTest {
            screen {
                PlakatBottomSheet(true, {}, ground = PlakatSheetGround.Concrete, windowInsets = foot) {
                    Box(Modifier.fillMaxWidth().height(100.dp).background(Color.Blue))
                }
            }
            val px = frame()
            assertEquals(paper.concrete, px[10, 399], "its ground at the foot")
            assertEquals(paper.concrete, px[10, 366])
            assertEquals(Color.Blue, px[10, 365], "the content's last row, 34 up")
            assertEquals(Color.Blue, px[10, 266], "100 high")
            assertEquals(paper.concrete, px[10, 265])
        }

    @Test
    fun `a drawer's ground runs its whole height and its rows stand inside the bars`() =
        runComposeUiTest {
            screen { PlakatNavigationDrawer(true, items, 0, {}, {}, windowInsets = top) }
            val px = frame()
            assertEquals(paper.ink, px[100, 0], "its ground at the top")
            val row = onNodeWithContentDescription("ЛЕНТА").fetchSemanticsNode().boundsInRoot
            assertEquals(24f + 16f, row.top, "under the bar and its own margin")
            // The active square has moved with its row: 12 down in its 56.
            assertEquals(paper.red, px[18, 24 + 16 + 12])
            assertEquals(paper.ink, px[18, 24 + 16 + 11])
        }

    @Test
    fun `a bar's foot stands where the box's alignment puts it`() =
        runComposeUiTest {
            screen {
                PlakatNavigationBar(
                    items,
                    0,
                    {},
                    Modifier.fillMaxWidth().align(Alignment.BottomStart),
                    windowInsets = foot,
                )
            }
            val px = frame()
            assertEquals(paper.ink, px[10, 399])
            assertEquals(Color.White, px[10, 400 - 64 - 34 - 1])
        }
}
