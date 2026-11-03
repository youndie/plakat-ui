package io.github.youndie.plakat.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag

/**
 * The sample's root, shared by the desktop window and the browser. Until the tokens (B-08) and the
 * theme (B-09) exist it is a sheet of the light paper with the kit's name; the colour is the
 * `plakat/color/paper` light value, written here once and replaced by the token.
 */
@Composable
fun PlakatUiSample() {
    Box(
        Modifier.fillMaxSize().background(Color(0xFFEEE5D1)).testTag("sample-root"),
        contentAlignment = Alignment.Center,
    ) {
        BasicText("plakat-ui")
    }
}
