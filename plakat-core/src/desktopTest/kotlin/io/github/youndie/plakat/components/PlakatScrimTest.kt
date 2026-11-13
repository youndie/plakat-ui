package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-80: the public scrim is the dialog's screen at every frame, takes clicks and never the keyboard. */
@OptIn(ExperimentalTestApi::class)
class PlakatScrimTest {
    /** The frames of the open, in ms: its start, the thirds and the end of the scene's 320, and one past. */
    private val frames = listOf(0L, 16L, 107L, 160L, 213L, 304L, 320L, 400L)

    /** The strip [FROM]..[HIGH] of a 390 × [HIGH] window, where the dialog's sheet never reaches, at each frame. */
    private fun strip(
        colors: PlakatColors,
        dialog: Boolean,
    ): List<PixelMap> {
        val shots = mutableListOf<PixelMap>()
        runComposeUiTest {
            mainClock.autoAdvance = false
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = colors) {
                        Box(Modifier.size(390.dp, HIGH.dp).testTag("window")) {
                            if (dialog) {
                                PlakatDialog(true, "T", {}) { PlakatDialogAction("OK", {}) }
                            } else {
                                PlakatScrim(true, {})
                            }
                        }
                    }
                }
            }
            var at = 0L
            for (ms in frames) {
                mainClock.advanceTimeBy(ms - at)
                at = ms
                shots += onNodeWithTag("window").captureToImage().toPixelMap()
            }
        }
        return shots
    }

    @Test
    fun `it looks as the dialog's screen at every frame of the open, on both papers`() {
        for (colors in listOf(PlakatPalettes.Paper, PlakatPalettes.BlackPaper)) {
            val scrim = strip(colors, dialog = false)
            val dialog = strip(colors, dialog = true)
            frames.forEachIndexed { i, ms ->
                var off = 0
                for (y in FROM until HIGH) {
                    for (x in 0 until 390) if (scrim[i][x, y] != dialog[i][x, y]) off++
                }
                assertEquals(0, off, "$ms ms on ${colors.paper}: pixels of the strip that differ")
            }
            // And the strip is not empty all along: by the end the screen is drawn. A tile of 4 has two rows wholly ink,
            // so a whole pitch of rows is looked at.
            val last = scrim.last()
            val varies = (FROM until FROM + 4).any { y -> (0 until 389).any { last[it, y] != last[it + 1, y] } }
            assertTrue(varies, "the dots are there")
        }
    }

    @Test
    fun `a click calls onClick, and a reader may dismiss it`() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                PlakatTheme {
                    Box(Modifier.size(390.dp, 400.dp)) { PlakatScrim(true, { clicks++ }, Modifier.testTag("scrim")) }
                }
            }
            onNodeWithTag("scrim").assert(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)).performClick()
            assertEquals(1, clicks)
        }

    @Test
    fun `Tab never lands on it`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    Box(Modifier.size(390.dp, 400.dp)) {
                        PlakatScrim(true, {}, Modifier.testTag("scrim"))
                        PlakatButton("OK", {}, Modifier.testTag("ok"))
                    }
                }
            }
            // The button is the only stop: Tab after Tab comes back to it, never to the screen (whose semantics are
            // cleared, so its own focus cannot be asked).
            repeat(3) {
                onRoot().performKeyInput { pressKey(Key.Tab) }
                waitForIdle()
                onNodeWithTag("ok").assertIsFocused()
            }
        }

    @Test
    fun `closed, it leaves the composition after its scene`() =
        runComposeUiTest {
            var open by mutableStateOf(true)
            mainClock.autoAdvance = false
            setContent {
                PlakatTheme {
                    Box(Modifier.size(390.dp, 400.dp)) { PlakatScrim(open, {}, Modifier.testTag("scrim")) }
                }
            }
            mainClock.advanceTimeBy(400)
            onNodeWithTag("scrim").assertExists()
            open = false
            mainClock.advanceTimeBy(200)
            onNodeWithTag("scrim").assertExists()
            mainClock.advanceTimeBy(200)
            onNodeWithTag("scrim").assertDoesNotExist()
        }

    private companion object {
        const val HIGH = 700
        const val FROM = 560
    }
}
