package io.github.youndie.plakat.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.plakatPaper
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes

/**
 * The sample's root, shared by the desktop window and the browser. The theme and reduce motion are
 * parameters, so the kit's switches reach every screen the sample shows (cmp-ui-library §7). The
 * paper texture starts as [texture] and is switched by the label in the corner.
 */
@Composable
fun PlakatUiSample(
    colors: PlakatColors = PlakatPalettes.Paper,
    reduceMotion: Boolean? = null,
    texture: Boolean = true,
) {
    var textureOn by remember { mutableStateOf(texture) }
    PlakatTheme(colors = colors, reduceMotion = reduceMotion, print = PlakatPrintSwitches(texture = textureOn)) {
        Box(Modifier.fillMaxSize().plakatPaper().testTag("sample-root")) {
            val headline = PlakatTheme.typography.headline
            BasicText(
                headline.text("plakat-ui"),
                Modifier.align(Alignment.Center),
                style = headline.style.copy(color = PlakatTheme.colors.ink),
            )
            val label = PlakatTheme.typography.label
            BasicText(
                label.text(if (textureOn) "Текстура: вкл" else "Текстура: выкл"),
                Modifier
                    .align(Alignment.TopEnd)
                    .clickable { textureOn = !textureOn }
                    .padding(16.dp)
                    .testTag("sample-texture"),
                style = label.style.copy(color = PlakatTheme.colors.ink),
            )
        }
    }
}
