package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatColumn
import io.github.youndie.plakat.components.PlakatLinearProgress
import io.github.youndie.plakat.components.PlakatTable
import io.github.youndie.plakat.theme.PlakatDensity
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-76: designed here (D13), so no reference: a board's twelve rows of five columns on each paper, and a table whose
// words and numbers are too long for their columns, for the owner's review at the end. B-82: the same board on a pointer
// surface, its fourth row the selected one.

/** A board's columns: a number, the item, its state, its progress, its days. */
internal val BOARD_COLUMNS =
    listOf(
        PlakatColumn("#", width = 32.dp, align = Alignment.End, numeric = true),
        PlakatColumn("Item"),
        PlakatColumn("State", width = 72.dp),
        PlakatColumn("Done", width = 72.dp),
        PlakatColumn("Days", width = 40.dp, align = Alignment.End, numeric = true),
    )

internal val BOARD_ROWS =
    listOf(
        "Ploughing",
        "Sowing",
        "Tractors",
        "Canal",
        "Bridge",
        "Furnace",
        "Rails",
        "Turbine",
        "School",
        "Library",
        "Dam",
        "Harvest",
    )

private val STATES = listOf("open", "done", "late", "draft")

@Composable
private fun Board(
    colors: PlakatColors,
    density: PlakatDensity = PlakatDensity.Touch,
    selected: Int = -1,
) = PlakatTheme(colors = colors) {
    Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
        PlakatTable(BOARD_COLUMNS, Modifier.fillMaxWidth(), density = density) {
            BOARD_ROWS.forEachIndexed { i, item ->
                row(onClick = if (selected >= 0) ({}) else null, selected = i == selected) {
                    cell("${i + 1}")
                    cell(item)
                    cell(STATES[i % STATES.size])
                    cell { PlakatLinearProgress((i % 5) / 4f) }
                    cell("${(i * 7) % 30}")
                }
            }
        }
    }
}

@ViddikScreenshot(group = "Table", name = "Board_Light", width = 390, height = 620)
@Composable
fun TableBoardLight() = Board(PlakatPalettes.Paper)

@ViddikScreenshot(group = "Table", name = "Board_Dark", width = 390, height = 620)
@Composable
fun TableBoardDark() = Board(PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Table", name = "Pointer_Light", width = 390, height = 476)
@Composable
fun TablePointerLight() = Board(PlakatPalettes.Paper, PlakatDensity.Pointer, selected = 3)

@ViddikScreenshot(group = "Table", name = "Pointer_Dark", width = 390, height = 476)
@Composable
fun TablePointerDark() = Board(PlakatPalettes.BlackPaper, PlakatDensity.Pointer, selected = 3)

@Composable
private fun TooLong(colors: PlakatColors) =
    PlakatTheme(colors = colors) {
        Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
            PlakatTable(BOARD_COLUMNS, Modifier.fillMaxWidth()) {
                row {
                    cell("12345")
                    cell("Electrification of the whole country")
                    cell("postponed")
                    cell { PlakatLinearProgress(.5f) }
                    cell("365")
                }
                row {
                    cell("7")
                    cell("Bridge")
                    cell("open")
                    cell { PlakatLinearProgress(1f) }
                    cell("2")
                }
            }
        }
    }

@ViddikScreenshot(group = "Table", name = "TooLong_Light", width = 390, height = 130)
@Composable
fun TableTooLongLight() = TooLong(PlakatPalettes.Paper)
