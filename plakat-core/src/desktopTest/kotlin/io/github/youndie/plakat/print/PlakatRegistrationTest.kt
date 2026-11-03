package io.github.youndie.plakat.print

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-10: the red layer is there with registration on and gone with it off; the ink is the same either way. */
@OptIn(ExperimentalTestApi::class)
class PlakatRegistrationTest {
    private fun reds(registration: Boolean): Int {
        var count = 0
        runComposeUiTest {
            setContent {
                PlakatTheme(print = PlakatPrintSwitches(registration = registration)) {
                    Box(Modifier.size(200.dp, 80.dp).background(PlakatTheme.colors.concrete).testTag("t")) {
                        PlakatRegisteredText(
                            "ЛИСТ",
                            TextStyle(fontFamily = PlakatTheme.typography.headline.style.fontFamily, fontSize = 40.sp),
                        )
                    }
                }
            }
            val px = onNodeWithTag("t").captureToImage().toPixelMap()
            val red = PlakatPalettes.Paper.red
            for (y in 0 until px.height) {
                for (x in 0 until px.width) if (close(px[x, y], red)) count++
            }
        }
        return count
    }

    private fun close(
        a: Color,
        b: Color,
    ) = abs(a.red - b.red) < .03f && abs(a.green - b.green) < .03f && abs(a.blue - b.blue) < .03f

    @Test
    fun `registration on prints a red edge and off prints none`() {
        val on = reds(registration = true)
        assertTrue(on > 40, "red pixels with registration on: $on")
        assertEquals(0, reds(registration = false), "red pixels with registration off")
    }
}
