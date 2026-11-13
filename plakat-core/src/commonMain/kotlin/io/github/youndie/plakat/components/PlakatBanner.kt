package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.plakatHatchBrush
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlin.math.min

// Designed here (D13, B-81): no page draws a banner. The tokens give each kind its form — warning an ocher fill, info a
// slate fill, danger the hatch with its word — and the danger banner is the snackbar's error plate (B-18) standing in
// the screen. Every number is listed in the item.

/** A banner's height at the least: the issue's canvas, 40. */
private val BannerHeight = 40.dp

/** Danger's hatched strip over the plate: the snackbar error's `height: 8px` of red and ink, a period of 6. */
private val DangerStrip = PlakatLines.Bar
private val DangerHatchPeriod = 6.dp

/** The action's word over its rule, in `button`: as the dialog's plain action, 3 dp above a 3 dp rule, 12 dp each side. */
private val ActionRuleGap = 3.dp
private val ActionInset = 12.dp

/**
 * A banner: a warning that stands in the screen until its condition passes, full width, pushing what is under it down
 * rather than lying over it as a [PlakatSnackbar] does. [kind] is warning, danger or info: the theme's word for it
 * ([io.github.youndie.plakat.theme.PlakatStrings]) in `label`, then [text], then, with [action], a text action calling
 * [onAction], a 48 dp target with the keyboard's frame.
 *
 * Warning is an ocher fill and info a slate one, their words in whichever of the paper's two inks reads on them; danger is
 * an ink plate under the hatch of red, as the snackbar's error is, so it is told from the others without colour. 40 dp at
 * the least, it grows with its text, which wraps rather than being cut: a warning is read whole. It has no motion of its
 * own; the screen it stands in moves.
 *
 * @throws IllegalArgumentException for [PlakatStatusKind.Primary] or [PlakatStatusKind.Success], which do not warn.
 */
@Composable
public fun PlakatBanner(
    kind: PlakatStatusKind,
    text: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    require(kind == PlakatStatusKind.Warning || kind == PlakatStatusKind.Danger || kind == PlakatStatusKind.Info) {
        "a banner warns: warning, danger or info, not $kind"
    }
    val colors = PlakatTheme.colors
    val strings = PlakatTheme.strings
    val dark = if (colors.ink.luminance() < colors.paper.luminance()) colors.ink else colors.paper
    val light = if (dark == colors.ink) colors.paper else colors.ink
    val (ground, on, word) =
        when (kind) {
            PlakatStatusKind.Warning -> Triple(colors.ocher, dark, strings.warning)
            PlakatStatusKind.Info -> Triple(colors.slate, light, strings.info)
            else -> Triple(colors.ink, colors.paper, strings.error)
        }
    CompositionLocalProvider(LocalPlakatContentColor provides on) {
        Column(modifier.fillMaxWidth().background(ground)) {
            if (kind == PlakatStatusKind.Danger) {
                Box(
                    Modifier.fillMaxWidth().height(DangerStrip).drawBehind {
                        drawRect(plakatHatchBrush(size, colors.red, colors.ink, DangerHatchPeriod.toPx()))
                    },
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = BannerHeight)
                    .padding(
                        start = PlakatGrid.Margin,
                        end =
                            if (action ==
                                null
                            ) {
                                PlakatGrid.Margin
                            } else {
                                PlakatGrid.Step
                            },
                    ),
                horizontalArrangement = Arrangement.spacedBy(PlakatGrid.Step),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlakatParagraph(
                    word,
                    PlakatTheme.typography.label,
                    Modifier.padding(vertical = PlakatGrid.Step),
                    maxLines = 1,
                )
                PlakatParagraph(
                    text,
                    PlakatTheme.typography.bodyS,
                    Modifier.weight(1f).padding(vertical = PlakatGrid.Step),
                )
                if (action != null) BannerAction(action, onAction, ground, on)
            }
        }
    }
}

/** The action: its word over a rule in the banner's ink, in a 48 dp target; pressed, the two inks trade places. */
@Composable
private fun BannerAction(
    label: String,
    onClick: () -> Unit,
    ground: Color,
    on: Color,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val ink = if (pressed) ground else on
    Box(
        Modifier
            // At most half of what the row has, so the sentence always has the rest (B-65, as the snackbar's action).
            .layout { measurable, constraints ->
                val inner =
                    if (constraints.hasBoundedWidth) {
                        val most = constraints.maxWidth / 2
                        constraints.copy(minWidth = min(constraints.minWidth, most), maxWidth = most)
                    } else {
                        constraints
                    }
                val placeable = measurable.measure(inner)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }.heightIn(min = PlakatSizes.Touch)
            .widthIn(min = PlakatSizes.Touch)
            .plakatFocusFrame(source, on)
            .clickable(source, indication = null, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {}
            .drawBehind { if (pressed) drawRect(on) }
            .padding(horizontal = ActionInset),
        contentAlignment = Alignment.Center,
    ) {
        PlakatParagraph(
            label,
            PlakatTheme.typography.button,
            Modifier
                .drawBehind {
                    val rule = PlakatLines.Rule.toPx()
                    drawRect(ink, Offset(0f, size.height - rule), Size(size.width, rule))
                }.padding(bottom = ActionRuleGap + PlakatLines.Rule),
            color = ink,
            maxLines = 1,
        )
    }
}
