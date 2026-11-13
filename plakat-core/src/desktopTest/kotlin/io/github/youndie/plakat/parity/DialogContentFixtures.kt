package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatDialogAction
import io.github.youndie.plakat.components.PlakatDialogFrame
import io.github.youndie.plakat.components.PlakatParagraph
import io.github.youndie.plakat.components.PlakatRadioButton
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-79: designed here (D13), as the page draws no dialog with content: pyatiletka's appearance dialog, a skin and a
// theme each a group of radio rows, then DONE, over the dialog stage's concrete rows.

/** A group of radio rows under its label, the first chosen. */
@Composable
internal fun ColumnScope.RadioGroup(
    label: String,
    words: List<String>,
) {
    PlakatParagraph(label, PlakatTheme.typography.label, Modifier.padding(top = 8.dp))
    for ((i, word) in words.withIndex()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlakatRadioButton(i == 0, {})
            PlakatParagraph(word, PlakatTheme.typography.bodyM)
        }
    }
}

@Composable
private fun Cell(colors: PlakatColors) =
    PlakatTheme(colors = colors, reduceMotion = true) {
        Box(Modifier.size(390.dp, 560.dp).background(PlakatTheme.colors.paper)) {
            Column(
                Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(6) { Spacer(Modifier.fillMaxWidth().height(72.dp).background(PlakatTheme.colors.concrete)) }
            }
            PlakatDialogFrame(
                true,
                "VIEW",
                {},
                Modifier,
                null,
                { PlakatDialogAction("DONE", {}) },
                playIn = false,
                content = {
                    RadioGroup("Skin", listOf("Plakat", "Toon"))
                    RadioGroup("Theme", listOf("Light", "Dark"))
                },
            )
        }
    }

@ViddikScreenshot(group = "Dialog", name = "Content_Light", width = 390, height = 560)
@Composable
fun DialogContentLight() = Cell(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Dialog", name = "Content_Dark", width = 390, height = 560)
@Composable
fun DialogContentDark() = Cell(PlakatPalettes.BlackPaper)
