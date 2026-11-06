package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes

/** The row with a supporting line: the MVP page's `min-height: 72px`; without one it is 56, `plakat/size/field`. */
private val TwoLineHeight = 72.dp

/** The leading column: the number's `width: 24px`, and the marker's `width: 8px; margin: 0 8px` in the same 24. */
private val LeadingWidth = 24.dp
private val MarkerSize = PlakatLines.Bar

/** The number: `font-family: 'Rubik Mono One'; font-size: 14px`. */
private const val NUMBER_SP = 14

/** A divider's weight, from `plakat/line`: a hair and a rule between things, a bar between sections. */
public enum class PlakatDividerWeight(
    internal val thickness: Dp,
) {
    Hair(PlakatLines.Hair),
    Rule(PlakatLines.Rule),
    Bar(PlakatLines.Bar),
}

/** A divider: a straight ink line across its width, 1 or 3 dp, or an 8 dp bar for a section. */
@Composable
public fun PlakatDivider(
    modifier: Modifier = Modifier,
    weight: PlakatDividerWeight = PlakatDividerWeight.Hair,
) {
    Box(modifier.fillMaxWidth().height(weight.thickness).background(PlakatTheme.colors.ink))
}

/**
 * A list row: on the left [number] in the display face, or without one a square marker; then
 * [headline] and under it [supporting]; at the end [trailing], which draws in
 * [LocalPlakatContentColor]. With a supporting line it is 72 dp and the headline 600, without one 56
 * and regular, taller when its text is. A 1 dp ink [divider] runs under it.
 *
 * Pressed (`stamp`), the whole row inverts on the frame the finger lands and its ground returns in
 * 60 ms, and that holds under reduced motion too: the spec row's word is «то же». [onClick] null
 * makes it a row that only shows. Disabled, its words and marker are muted and its divider is not.
 */
@Composable
public fun PlakatListItem(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    number: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    divider: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatListItemFrame(headline, modifier, supporting, number, trailing, onClick, enabled, divider, interactionSource)
}

@Composable
internal fun PlakatListItemFrame(
    headline: String,
    modifier: Modifier,
    supporting: String?,
    number: String?,
    trailing: (@Composable () -> Unit)?,
    onClick: (() -> Unit)?,
    enabled: Boolean,
    divider: Boolean,
    interactionSource: MutableInteractionSource?,
    forcePressed: Boolean = false,
    forceHovered: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = PlakatTheme.colors
    val state = rememberPlakatPress(source, sameUnderReducedMotion = true)
    val press = if (forcePressed) 1f else state.press.value
    val held = forcePressed || state.held
    val ink =
        when {
            !enabled -> colors.inkMuted
            held -> colors.paper
            else -> colors.ink
        }
    val type = PlakatTheme.typography
    val focusFrame = state.focusShown()
    val click =
        if (onClick != null) {
            Modifier.clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
        } else {
            Modifier
        }
    CompositionLocalProvider(LocalPlakatContentColor provides ink) {
        Column(
            modifier
                .fillMaxWidth()
                .drawBehind {
                    // Only the ground is a transition; the words change the moment the finger lifts.
                    drawRect(srgbLerp(colors.ink.copy(alpha = 0f), colors.ink, press))
                    // Designed here (B-39): on a pointer the hair becomes the rule; a row with none gets one.
                    val hovered = enabled && onClick != null && (forceHovered || state.hovered) && !held
                    if (divider || hovered) {
                        val line = (if (hovered) PlakatLines.Rule else PlakatLines.Hair).toPx()
                        drawRect(colors.ink, Offset(0f, size.height - line), Size(size.width, line))
                    }
                    if (focusFrame) drawFocusFrame(colors.ink)
                }.then(click),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (supporting != null) TwoLineHeight else PlakatSizes.Field)
                    .padding(horizontal = PlakatGrid.Margin),
                horizontalArrangement = Arrangement.spacedBy(PlakatGrid.Margin),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (number != null) {
                    BasicText(
                        number,
                        Modifier.width(LeadingWidth),
                        style =
                            type.displayL.style
                                .copy(fontSize = NUMBER_SP.sp, color = ink)
                                .cssLine(PlakatFaceMetrics.Display),
                        softWrap = false,
                    )
                } else {
                    Box(Modifier.width(LeadingWidth), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(MarkerSize).background(ink))
                    }
                }
                Column(Modifier.weight(1f)) {
                    BasicText(
                        headline,
                        style =
                            type.bodyM.style
                                .copy(
                                    fontWeight = if (supporting != null) FontWeight.SemiBold else FontWeight.Normal,
                                    color = ink,
                                ).cssLine(PlakatFaceMetrics.Body),
                    )
                    if (supporting != null) {
                        BasicText(
                            supporting,
                            style =
                                type.bodyS.style
                                    .copy(color = ink)
                                    .cssLine(PlakatFaceMetrics.Body),
                        )
                    }
                }
                trailing?.invoke()
            }
            // The border below the row, adding to its height as `border-bottom` does on a `content-box`.
            if (divider) Box(Modifier.height(PlakatLines.Hair))
        }
    }
}
