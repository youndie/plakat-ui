package io.github.youndie.plakat.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertTrue

/** B-56, the owner's remark: pressed, a card's words in the demo turn paper with its sheet, as its fixture's do. */
@OptIn(ExperimentalTestApi::class)
class DemoCardPressTest {
    private val paper = PlakatPalettes.Paper

    @Test
    fun `a pressed card in the feed has its words in paper on ink`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = paper, reduceMotion = true, print = PlakatPrintSwitches(texture = false)) {
                        Box(Modifier.size(390.dp, 844.dp).testTag("screen")) { DemoFeed() }
                    }
                }
            }
            waitForIdle()
            // The first card's word: where it stands, and the box of its row of the sheet.
            val word = onAllNodesWithText(FIRST_TITLE, substring = true)[0].fetchSemanticsNode().boundsInRoot
            onNodeWithTag("screen").performMouseInput {
                moveTo(Offset(word.left + 20, word.top + 8))
                press()
            }
            waitForIdle()
            val px = onNodeWithTag("screen").captureToImage().toPixelMap()
            val rows = word.top.toInt() until word.bottom.toInt()
            val xs = word.left.toInt() until word.right.toInt()
            val light = xs.sumOf { x -> rows.count { y -> px[x, y] == paper.paper } }
            val ground = xs.sumOf { x -> rows.count { y -> px[x, y] == paper.ink } }
            assertTrue(ground > light, "the sheet is ink under them: $ground, $light")
            assertTrue(light > 40, "and the letters are paper: $light")
            onNodeWithTag("screen").performMouseInput { release() }
        }

    private companion object {
        const val FIRST_TITLE = "Город"
    }
}
