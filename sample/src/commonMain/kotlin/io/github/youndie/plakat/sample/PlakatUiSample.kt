package io.github.youndie.plakat.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes

/**
 * The sample's root, shared by the desktop window and the browser. The theme and reduce motion are
 * parameters, so the kit's switches reach every screen the sample shows (cmp-ui-library §7).
 */
@Composable
fun PlakatUiSample(
    colors: PlakatColors = PlakatPalettes.Paper,
    reduceMotion: Boolean? = null,
) {
    PlakatTheme(colors = colors, reduceMotion = reduceMotion) {
        Box(
            Modifier.fillMaxSize().background(PlakatTheme.colors.paper).testTag("sample-root"),
            contentAlignment = Alignment.Center,
        ) {
            val headline = PlakatTheme.typography.headline
            BasicText(headline.text("plakat-ui"), style = headline.style.copy(color = PlakatTheme.colors.ink))
        }
    }
}
