package io.github.youndie.plakat.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.youndie.plakat.tokens.PlakatPalettes

/**
 * The sample's root, shared by the desktop window and the browser. Until the theme (B-09) exists it
 * is a sheet of the light paper with the kit's name.
 */
@Composable
fun PlakatUiSample() {
    Box(
        Modifier.fillMaxSize().background(PlakatPalettes.Paper.paper).testTag("sample-root"),
        contentAlignment = Alignment.Center,
    ) {
        BasicText("plakat-ui")
    }
}
