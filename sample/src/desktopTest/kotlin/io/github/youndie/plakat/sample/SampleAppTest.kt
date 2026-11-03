package io.github.youndie.plakat.sample

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

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
}
