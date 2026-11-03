package io.github.youndie.plakat.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

/** The sample in a browser: the same `PlakatUiSample` as the desktop window. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) { PlakatUiSample() }
}
