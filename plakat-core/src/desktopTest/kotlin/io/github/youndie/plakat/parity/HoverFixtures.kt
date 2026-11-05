package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.ButtonLook
import io.github.youndie.plakat.components.ChipLook
import io.github.youndie.plakat.components.PlakatButtonFrame
import io.github.youndie.plakat.components.PlakatButtonKind
import io.github.youndie.plakat.components.PlakatChipFrame
import io.github.youndie.plakat.components.PlakatIconButtonFrame
import io.github.youndie.plakat.components.PlakatListItemFrame
import io.github.youndie.plakat.components.PlakatPoster
import io.github.youndie.plakat.components.PlakatPosterLayout
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-39: designed here (D13), so no reference: goldens of each control under a pointer and of the wide poster, for the
// owner's review at the end.

@Composable
private fun Hovered(colors: PlakatColors) =
    PlakatTheme(colors = colors, reduceMotion = true) {
        Column(Modifier.fillMaxSize().background(PlakatTheme.colors.paper).padding(16.dp)) {
            Row {
                for (kind in PlakatButtonKind.entries) {
                    PlakatButtonFrame(
                        kind,
                        "ЧИТАТЬ",
                        {},
                        Modifier.padding(end = 8.dp),
                        enabled = true,
                        loading = false,
                        interactionSource = null,
                        look = ButtonLook.Hovered,
                    )
                }
            }
            Row {
                PlakatIconButtonFrame(
                    PlakatIcons.Home,
                    "Лента",
                    {},
                    Modifier,
                    true,
                    false,
                    null,
                    look = ButtonLook.Hovered,
                )
                PlakatIconButtonFrame(
                    PlakatIcons.Home,
                    "Лента",
                    {},
                    Modifier,
                    true,
                    true,
                    null,
                    look = ButtonLook.Hovered,
                )
                PlakatChipFrame("ШРИФТ", false, {}, Modifier.padding(start = 8.dp), true, null, look = ChipLook.Hovered)
                PlakatChipFrame("ШРИФТ", true, {}, Modifier.padding(start = 8.dp), true, null, look = ChipLook.Hovered)
            }
            Box(Modifier.size(358.dp, 56.dp)) {
                PlakatListItemFrame("Шрифт", Modifier, null, null, null, {}, true, true, null, forceHovered = true)
            }
        }
    }

@ViddikScreenshot(group = "Hover", name = "Controls_Light", width = 540, height = 200)
@Composable
fun HoverControlsLight() = Hovered(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Hover", name = "Controls_Dark", width = 540, height = 200)
@Composable
fun HoverControlsDark() = Hovered(PlakatPalettes.BlackPaper)

@Composable
private fun Wide(colors: PlakatColors) =
    PlakatTheme(colors = colors, reduceMotion = true) {
        PlakatPoster(
            listOf("ЧИТАЙ", "ВЕСЬ", "НОЯБРЬ"),
            "ПОДПИСАТЬСЯ",
            {},
            Modifier.size(720.dp, 392.dp),
            layout = PlakatPosterLayout.Wide,
            body = "12 новых выпусков. Мы собрали их в одну ленту — читай по порядку.",
            seed = 1919,
        )
    }

@ViddikScreenshot(group = "Poster", name = "Wide_Light", width = 720, height = 392)
@Composable
fun PosterWideLight() = Wide(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Poster", name = "Wide_Dark", width = 720, height = 392)
@Composable
fun PosterWideDark() = Wide(PlakatPalettes.BlackPaper)
