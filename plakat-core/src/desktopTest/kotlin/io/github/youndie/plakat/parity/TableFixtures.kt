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
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-76: designed here (D13), so no reference: a board's twelve rows of five columns on each paper, and a table whose
// words and numbers are too long for their columns, for the owner's review at the end.

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
private fun Board(colors: PlakatColors) =
    PlakatTheme(colors = colors) {
        Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) {
            PlakatTable(BOARD_COLUMNS, Modifier.fillMaxWidth()) {
                BOARD_ROWS.forEachIndexed { i, item ->
                    row {
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
