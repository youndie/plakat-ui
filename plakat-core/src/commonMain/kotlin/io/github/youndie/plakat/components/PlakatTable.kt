package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlin.math.ceil
import kotlin.math.max

/**
 * A column of a [PlakatTable]: its [label] in the header, its width and how its cells sit in it.
 *
 * A column is [width] wide, or, with no width, flexible: the flexible columns share what the fixed ones and the gutters
 * leave, by [weight]. A [numeric] column holds numbers, which never lose a digit: it is at least as wide as the widest
 * number in its text cells, whatever [width] says, so every row's number shows whole and the columns stay aligned.
 */
@Immutable
public class PlakatColumn(
    public val label: String,
    public val width: Dp = Dp.Unspecified,
    public val weight: Float = 1f,
    public val align: Alignment.Horizontal = Alignment.Start,
    public val numeric: Boolean = false,
)

/** The rows of a [PlakatTable], added in order. */
public interface PlakatTableScope {
    /**
     * A row of cells, one per column in order, added in [cells]. With [onClick] the row is a button: it takes the kit's
     * press and the keyboard's frame.
     */
    public fun row(
        onClick: (() -> Unit)? = null,
        cells: PlakatRowScope.() -> Unit,
    )
}

/** The cells of one row of a [PlakatTable]. */
public interface PlakatRowScope {
    /** A cell of words, set in `bodyM` on one line; a word too long for its column ends in an ellipsis, a number never. */
    public fun cell(text: String)

    /** A cell holding anything (a [PlakatStatus], a [PlakatLinearProgress]), placed by the column's alignment. */
    public fun cell(content: @Composable () -> Unit)
}

private sealed interface Cell {
    class Text(
        val text: String,
    ) : Cell

    class Slot(
        val content: @Composable () -> Unit,
    ) : Cell
}

private class TableRow(
    val onClick: (() -> Unit)?,
    val cells: List<Cell>,
)

private class Rows :
    PlakatTableScope,
    PlakatRowScope {
    val rows = mutableListOf<TableRow>()
    private var cells = mutableListOf<Cell>()

    override fun row(
        onClick: (() -> Unit)?,
        cells: PlakatRowScope.() -> Unit,
    ) {
        this.cells = mutableListOf()
        cells()
        rows += TableRow(onClick, this.cells)
    }

    override fun cell(text: String) {
        cells += Cell.Text(text)
    }

    override fun cell(content: @Composable () -> Unit) {
        cells += Cell.Slot(content)
    }
}

// Designed here (D13, B-76), from the issue's canvas: no page draws a table. Every number is listed in the item.

/** The header's words stand on the rule this far above it, and as far below the table's top. */
private val HeaderPadding = PlakatGrid.Step

/** A row's height at the least: a touch target, as a row may be a button. */
private val RowHeight = PlakatSizes.Touch

/** Around a row's cells: the grid's step at each side, and above and below where the words grow past the row. */
private val CellPadding = PlakatGrid.Step

/** Between two columns. */
private val Gutter = PlakatGrid.Gutter

/**
 * A table: a header of the [columns]' labels in `label` over a 3 dp ink rule, then the rows [content] adds, a concrete
 * hair between each two. Every row lays its cells in the same columns, so twelve rows of five stay aligned.
 *
 * Given a width, the fixed columns take theirs, the numeric ones at least their widest number, and the flexible ones share
 * the rest; given none, a flexible column is as wide as its widest text cell. A row is 48 dp at the least and grows with
 * its tallest cell.
 */
@Composable
public fun PlakatTable(
    columns: List<PlakatColumn>,
    modifier: Modifier = Modifier,
    content: PlakatTableScope.() -> Unit,
) {
    val rows = Rows().apply(content).rows
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // What a numeric column needs, and what a flexible column wants when nothing bounds it: its widest text, measured
    // once in the cells' own style.
    val texts = columns.indices.map { c -> rows.mapNotNull { (it.cells.getOrNull(c) as? Cell.Text)?.text } }
    val widest =
        remember(texts, type, density) {
            texts.map { column ->
                column.maxOfOrNull {
                    ceil(
                        measurer
                            .measure(it, type.bodyM.style)
                            .size.width
                            .toFloat(),
                    ).toInt()
                } ?: 0
            }
        }
    Layout(
        modifier = modifier,
        content = {
            // Child 0: the header; then one child per row.
            Column {
                TableLine(columns, widest, Modifier.padding(vertical = HeaderPadding)) { c ->
                    PlakatParagraph(columns[c].label, type.label, maxLines = 1)
                }
                Spacer(Modifier.fillMaxWidth().height(PlakatLines.Rule).background(colors.ink))
            }
            rows.forEachIndexed { i, row ->
                TableRowLine(columns, widest, row, divider = i < rows.lastIndex)
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minHeight = 0)) }
        val width = placeables.maxOf { it.width }.coerceIn(constraints.minWidth, constraints.maxWidth)
        var y = 0
        layout(width, placeables.sumOf { it.height }) {
            for (p in placeables) {
                p.place(0, y)
                y += p.height
            }
        }
    }
}

@Composable
private fun TableRowLine(
    columns: List<PlakatColumn>,
    widest: List<Int>,
    row: TableRow,
    divider: Boolean,
) {
    val colors = PlakatTheme.colors
    val source = remember { MutableInteractionSource() }
    val state = rememberPlakatPress(source, sameUnderReducedMotion = true)
    val press = state.press.value
    val ink = if (state.inked) colors.paper else colors.ink
    val focus = row.onClick != null && state.focusShown()
    val click =
        row.onClick?.let { Modifier.clickable(source, indication = null, role = Role.Button, onClick = it) } ?: Modifier
    CompositionLocalProvider(LocalPlakatContentColor provides ink) {
        Column(
            Modifier
                .drawBehind {
                    drawRect(srgbLerp(colors.ink.copy(alpha = 0f), colors.ink, press))
                    if (focus) drawFocusFrame(colors.ink)
                }.then(click),
        ) {
            TableLine(columns, widest, Modifier.heightIn(min = RowHeight).padding(vertical = CellPadding)) { c ->
                when (val cell = row.cells.getOrNull(c)) {
                    is Cell.Text -> {
                        PlakatParagraph(
                            cell.text,
                            PlakatTheme.typography.bodyM,
                            maxLines = 1,
                        )
                    }

                    is Cell.Slot -> {
                        cell.content()
                    }

                    null -> {
                        Unit
                    }
                }
            }
            if (divider) Spacer(Modifier.fillMaxWidth().height(PlakatLines.Hair).background(colors.concrete))
        }
    }
}

/** One line of the table: [cell] of each column in its column's box, the boxes laid by [columnWidths]. */
@Composable
private fun TableLine(
    columns: List<PlakatColumn>,
    widest: List<Int>,
    modifier: Modifier,
    cell: @Composable (Int) -> Unit,
) {
    Layout(
        modifier = modifier.padding(horizontal = CellPadding),
        content = {
            for (c in columns.indices) {
                Box(contentAlignment = Alignment.CenterStart) { cell(c) }
            }
        },
    ) { measurables, constraints ->
        val widths = columnWidths(columns, widest, constraints.maxWidth, Gutter.roundToPx()) { it.roundToPx() }
        val placeables = measurables.mapIndexed { c, m -> m.measure(Constraints(maxWidth = widths[c])) }
        val total = widths.sum() + Gutter.roundToPx() * (columns.size - 1).coerceAtLeast(0)
        val width = if (constraints.hasBoundedWidth) max(constraints.maxWidth, constraints.minWidth) else total
        val height = max(constraints.minHeight, placeables.maxOfOrNull { it.height } ?: 0)
        layout(width, height) {
            var x = 0
            placeables.forEachIndexed { c, p ->
                val dx = columns[c].align.align(p.width, widths[c], layoutDirection)
                p.place(x + dx, (height - p.height) / 2)
                x += widths[c] + Gutter.roundToPx()
            }
        }
    }
}

/**
 * Each column's width in pixels for a line [available] wide (unbounded for none): a fixed column its width, a numeric one
 * at least its [widest] number, the flexible ones the rest by weight, never below zero; with no bound, a flexible column
 * its widest text.
 */
internal fun columnWidths(
    columns: List<PlakatColumn>,
    widest: List<Int>,
    available: Int,
    gutter: Int,
    px: (Dp) -> Int,
): List<Int> {
    val fixed =
        columns.mapIndexed { c, col ->
            when {
                col.width != Dp.Unspecified -> max(px(col.width), if (col.numeric) widest[c] else 0)
                col.numeric -> widest[c]
                else -> null
            }
        }
    if (available == Constraints.Infinity) return fixed.mapIndexed { c, w -> w ?: widest[c] }
    val gutters = gutter * (columns.size - 1).coerceAtLeast(0)
    val left = (available - gutters - fixed.sumOf { it ?: 0 }).coerceAtLeast(0)
    val weights = columns.filterIndexed { c, _ -> fixed[c] == null }.sumOf { it.weight.toDouble() }
    return fixed.mapIndexed { c, w -> w ?: if (weights > 0) (left * columns[c].weight / weights).toInt() else 0 }
}
