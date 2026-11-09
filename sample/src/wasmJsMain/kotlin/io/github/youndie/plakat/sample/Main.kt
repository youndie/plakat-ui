package io.github.youndie.plakat.sample

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import io.github.youndie.plakat.components.PlakatChip
import io.github.youndie.plakat.print.plakatPaper
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * The sample in a browser: the same `PlakatUiSample` as the desktop window.
 * `?scene=app` opens the three demo screens alone, without the sheet around them, for a page to embed.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val embedded = window.location.search.contains("scene=app")
    ComposeViewport(document.body!!) {
        if (embedded) EmbeddedDemo() else PlakatUiSample()
    }
}

/** The demo's screens under one row of chips: which screen, and which paper. */
@Composable
private fun EmbeddedDemo() {
    var shown by remember { mutableStateOf(SampleSection.Feed) }
    var dark by remember { mutableStateOf(false) }
    PlakatTheme(colors = if (dark) PlakatPalettes.BlackPaper else PlakatPalettes.Paper) {
        Column(Modifier.fillMaxSize().plakatPaper()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (s in listOf(SampleSection.Feed, SampleSection.Onboarding, SampleSection.Settings)) {
                    PlakatChip(s.title, s == shown, { shown = s })
                }
                PlakatChip("BLACK PAPER", dark, { dark = !dark })
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (shown) {
                    SampleSection.Onboarding -> DemoOnboarding()
                    SampleSection.Settings -> DemoSettings(dark, { dark = it })
                    else -> DemoFeed()
                }
            }
        }
    }
}
