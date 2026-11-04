package io.github.youndie.plakat.sample

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-01: the sample composes through the library's dependency, as a consumer would. */
@OptIn(ExperimentalTestApi::class)
class SampleAppTest {
    @Test
    fun `the sample shows the kit name in capitals on paper`() =
        runComposeUiTest {
            setContent { PlakatUiSample() }
            onNodeWithTag("sample-root").assertIsDisplayed()
            onNodeWithText("PLAKAT-UI").assertIsDisplayed()
        }

    @Test
    fun `the label switches the paper texture off and on`() =
        runComposeUiTest {
            setContent { PlakatUiSample() }

            // B-07: a row of the paper away from the text, textured, then flat, then textured again.
            fun paperRow() =
                onNodeWithTag("sample-root").captureToImage().toPixelMap().let { px ->
                    (0 until px.width).map { px[it, px.height - 2] }.toSet()
                }
            assertTrue(paperRow().size > 3, "textured paper")
            onNodeWithText("ТЕКСТУРА: ВКЛ").performClick()
            assertEquals(setOf(PlakatPalettes.Paper.paper), paperRow(), "flat paper")
            onNodeWithText("ТЕКСТУРА: ВЫКЛ").performClick()
            assertTrue(paperRow().size > 3, "textured again")
        }
}
