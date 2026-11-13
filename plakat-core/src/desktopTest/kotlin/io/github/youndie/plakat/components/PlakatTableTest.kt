package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import io.github.youndie.plakat.parity.BOARD_COLUMNS
import io.github.youndie.plakat.parity.BOARD_ROWS
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-76: columns that stay aligned, the header's rule and the rows' hairs, and what does not fit. */
@OptIn(ExperimentalTestApi::class)
class PlakatTableTest {
    private val px: (Dp) -> Int = { it.value.toInt() }

    @Test
    fun `fixed columns take their width and the flexible ones share the rest`() {
        val columns = listOf(PlakatColumn("a", 40.dp), PlakatColumn("b", weight = 1f), PlakatColumn("c", weight = 3f))
        // 200 less two gutters of 8 and the fixed 40: 144, a quarter and three quarters.
        assertEquals(listOf(40, 36, 108), columnWidths(columns, listOf(0, 0, 0), 200, 8, px))
    }

    @Test
    fun `a numeric column is at least its widest number, and the flexible ones give way`() {
        val columns = listOf(PlakatColumn("#", 32.dp, numeric = true), PlakatColumn("item"))
        assertEquals(listOf(32, 160), columnWidths(columns, listOf(20, 0), 200, 8, px))
        assertEquals(listOf(45, 147), columnWidths(columns, listOf(45, 0), 200, 8, px))
        assertEquals(listOf(300, 0), columnWidths(columns, listOf(300, 0), 200, 8, px), "never below zero")
    }

    @Test
    fun `given no width a flexible column is its widest text`() {
        val columns = listOf(PlakatColumn("#", 32.dp, numeric = true), PlakatColumn("item"))
        assertEquals(listOf(32, 120), columnWidths(columns, listOf(10, 120), Constraints.Infinity, 8, px))
    }

    @Test
    fun `twelve rows of five stay aligned`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        PlakatTable(BOARD_COLUMNS, Modifier.width(390.dp)) {
                            BOARD_ROWS.forEachIndexed { r, item ->
                                row {
                                    for (c in 0 until 5) {
                                        cell {
                                            Box(Modifier.size(if (c == 1) (10 + r * 3).dp else 8.dp).testTag("r$r-c$c"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (c in 0 until 5) {
                val lefts = BOARD_ROWS.indices.map { onNodeWithTag("r$it-c$c").getUnclippedBoundsInRoot().left }
                val rights = BOARD_ROWS.indices.map { onNodeWithTag("r$it-c$c").getUnclippedBoundsInRoot().right }
                // Start-aligned columns line up on their left, end-aligned ones on their right.
                val edges = if (BOARD_COLUMNS[c].align == androidx.compose.ui.Alignment.End) rights else lefts
                assertEquals(1, edges.toSet().size, "column $c: $edges")
            }
            // The fixed columns' boxes: 32, the item, 72, 72, 40 with gutters of 8 and 8 at each side, in 390.
            val item = onNodeWithTag("r0-c1").getUnclippedBoundsInRoot().left
            val state = onNodeWithTag("r0-c2").getUnclippedBoundsInRoot().left
            assertEquals(8.dp + 32.dp + 8.dp, item)
            assertEquals(390.dp - 8.dp - 40.dp - 8.dp - 72.dp - 8.dp - 72.dp, state)
        }

    @Test
    fun `an ink rule under the header and a concrete hair between rows, none under the last`() =
        runComposeUiTest {
            val colors = PlakatPalettes.Paper
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme(colors = colors) {
                        Box(Modifier.size(200.dp, 160.dp).background(colors.paper).testTag("t")) {
                            PlakatTable(listOf(PlakatColumn("A"), PlakatColumn("B")), Modifier.width(200.dp)) {
                                row {
                                    cell("one")
                                    cell("1")
                                }
                                row {
                                    cell("two")
                                    cell("2")
                                }
                            }
                        }
                    }
                }
            }
            val map = onNodeWithTag("t").captureToImage().toPixelMap()

            fun near(
                a: Color,
                b: Color,
            ) = abs(a.red - b.red) < .02f && abs(a.green - b.green) < .02f && abs(a.blue - b.blue) < .02f

            // Each row of pixels in a column with no words in it (the gutter's middle), top to bottom.
            val column = (0 until map.height).map { map[100, it] }
            val ink = column.indices.filter { near(column[it], colors.ink) }
            val concrete = column.indices.filter { near(column[it], colors.concrete) }
            // Header: 8 + 14 (a label line of 14.4) + 8, then the rule of 3; a row of 48, a hair; a row of 48.
            assertEquals(listOf(30, 31, 32), ink, "the header's rule")
            assertEquals(listOf(81), concrete, "one hair, between the rows")
        }

    @Test
    fun `a long word ends in its column and a number keeps every digit`() =
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        PlakatTable(BOARD_COLUMNS, Modifier.width(390.dp)) {
                            row {
                                cell("12345")
                                cell("Electrification of the whole country")
                                cell("postponed")
                                cell("")
                                cell("365")
                            }
                        }
                    }
                }
            }
            // The number is laid whole, wider than its column's 32; the word is held to its column of 72, on one line.
            assertTrue(onNodeWithText("12345").getUnclippedBoundsInRoot().width > 32.dp)
            val word = onNodeWithText("postponed").getUnclippedBoundsInRoot()
            assertTrue(word.width <= 72.dp, "${word.width}")
            assertEquals(22.dp, word.height, "one line of bodyM")
        }

    @Test
    fun `given no width it does not throw`() =
        runComposeUiTest {
            setContent {
                PlakatTheme {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        PlakatTable(BOARD_COLUMNS, Modifier.testTag("t")) {
                            row {
                                cell("1")
                                cell("Ploughing")
                                cell("open")
                                cell { PlakatLinearProgress(.5f) }
                                cell("3")
                            }
                        }
                    }
                }
            }
            val bounds = onNodeWithTag("t").getUnclippedBoundsInRoot()
            assertTrue(bounds.width > 0.dp && bounds.height > 0.dp, "$bounds")
        }

    @Test
    fun `a clickable row is a button`() =
        runComposeUiTest {
            var clicks = 0
            setContent {
                PlakatTheme {
                    Box(Modifier.fillMaxSize()) {
                        PlakatTable(BOARD_COLUMNS, Modifier.width(390.dp)) {
                            row(onClick = { clicks++ }) { cell("Ploughing") }
                        }
                    }
                }
            }
            onNodeWithText("Ploughing").performClick()
            assertEquals(1, clicks)
        }
}
