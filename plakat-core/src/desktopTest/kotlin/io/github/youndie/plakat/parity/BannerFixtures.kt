package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatBanner
import io.github.youndie.plakat.components.PlakatStatusKind
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-81: designed here (D13), so no reference: the three kinds a banner warns with, one with the issue's action and one
// whose sentence wraps, on each paper, for the owner's review at the end.

@Composable
private fun Banners(colors: PlakatColors) =
    PlakatTheme(colors = colors) {
        Column(
            Modifier.fillMaxSize().background(PlakatTheme.colors.paper),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlakatBanner(
                PlakatStatusKind.Warning,
                "Snapshot of 11 November, 21:17: no connection to GitHub",
                action = "Retry",
            )
            PlakatBanner(PlakatStatusKind.Danger, "Two checks failed on main")
            PlakatBanner(PlakatStatusKind.Info, "A new version is published")
            PlakatBanner(
                PlakatStatusKind.Warning,
                "The board was built from a snapshot taken before the last three merges, so the numbers below are " +
                    "older than the repository and will catch up once the connection returns",
            )
        }
    }

@ViddikScreenshot(group = "Banner", name = "Kinds_Light", width = 390, height = 320)
@Composable
fun BannerKindsLight() = Banners(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Banner", name = "Kinds_Dark", width = 390, height = 320)
@Composable
fun BannerKindsDark() = Banners(PlakatPalettes.BlackPaper)
