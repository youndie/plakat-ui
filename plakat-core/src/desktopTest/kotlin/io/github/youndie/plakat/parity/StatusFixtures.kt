package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatStatus
import io.github.youndie.plakat.components.PlakatStatusKind
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-75: designed here (D13), so no reference: the five kinds' signs beside a board's words, on each paper, for the
// owner's review at the end.

/** A board's words for each kind (pyatiletka's statuses, issue #19). */
internal val STATUS_WORDS =
    mapOf(
        PlakatStatusKind.Primary to "Open",
        PlakatStatusKind.Danger to "Failing",
        PlakatStatusKind.Success to "Merged",
        PlakatStatusKind.Warning to "Stale",
        PlakatStatusKind.Info to "Draft",
    )

@Composable
private fun Statuses(colors: PlakatColors) =
    PlakatTheme(colors = colors) {
        Column(
            Modifier.fillMaxSize().background(PlakatTheme.colors.paper).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for ((kind, word) in STATUS_WORDS) PlakatStatus(kind, word)
        }
    }

@ViddikScreenshot(group = "Status", name = "Kinds_Light", width = 160, height = 148)
@Composable
fun StatusKindsLight() = Statuses(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Status", name = "Kinds_Dark", width = 160, height = 148)
@Composable
fun StatusKindsDark() = Statuses(PlakatPalettes.BlackPaper)
