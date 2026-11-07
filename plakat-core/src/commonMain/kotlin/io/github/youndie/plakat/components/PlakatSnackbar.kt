package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.print.PlakatWipe
import io.github.youndie.plakat.print.plakatHatchBrush
import io.github.youndie.plakat.print.plakatWipe
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

/** The message's size: the Core page's `font-size: 15px`, Golos 400, line `normal`. */
private const val MESSAGE_SP = 15

/** The action's size: `font-family: 'Rubik Mono One'; font-size: 13px`, no tracking. */
private const val ACTION_SP = 13

/** The action's target: `height: 56px; min-width: 96px`, its word centred in it. */
private val ActionMinWidth = 96.dp

/** Below the action's word, before its rule: `padding-bottom: 3px`. */
private val ActionRuleGap = 3.dp

/** The error's hatched strip above the message: `height: 8px`, `plakat/line/bar`. */
private val ErrorStrip = PlakatLines.Bar

/** The hatch's period there: `repeating-linear-gradient(45deg, red 0 3px, ink 3px 6px)`. */
private val ErrorHatchPeriod = 6.dp

/** The plate rides from this far below its stop: `translateY(calc(100% + 24px))`, its height and 24. */
private val RideBelow = 24.dp

/** Show and hide under reduced motion: the spec row's `fade 100`, linear. */
private const val FADE_MS = 100

/**
 * A snackbar, the telegram: an ink plate with [message] in paper, at least 56 dp tall, 16 dp in from
 * its edges. [actionLabel] puts an action at its end, a 96 × 56 target with the word in the display
 * face over a 3 dp rule; [onAction] runs when it is clicked. [error] hatches an 8 dp strip red over
 * the plate's top and prints the theme's error word before the message in 600 — a hatch and a word,
 * never colour alone.
 *
 * It shows as it enters the composition with [visible] true, or when [visible] becomes true
 * (`telegram`): the plate rides up from below its stop (its height and 24 dp) in 200 ms hard-stop,
 * then its words open by a wipe from the left in 120 ms, linear. Hidden, the words go at once and the
 * plate rides back down the same way, after which [onHidden] is called. Under reduced motion it
 * fades in and out in 100 ms, linear. The plate is not clipped: whatever holds it at the bottom of
 * the screen hides what has ridden off.
 */
@Composable
public fun PlakatSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    error: Boolean = false,
    onHidden: () -> Unit = {},
) {
    PlakatSnackbarFrame(message, modifier, visible, actionLabel, onAction, error, onHidden)
}

@Composable
internal fun PlakatSnackbarFrame(
    message: String,
    modifier: Modifier,
    visible: Boolean,
    actionLabel: String?,
    onAction: () -> Unit,
    error: Boolean,
    onHidden: () -> Unit,
    playIn: Boolean = true,
    forcePressed: Boolean = false,
) {
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val hidden by rememberUpdatedState(onHidden)
    val start = if (visible && !playIn) 1f else 0f
    // The plate's way up to its stop, the words' wipe, and the fade that stands for both under reduced motion.
    val rise = remember { Animatable(start) }
    val words = remember { Animatable(start) }
    val fade = remember { Animatable(if (reduce) start else 1f) }
    var seen by remember { mutableStateOf(!playIn && visible) }
    LaunchedEffect(visible, reduce) {
        val changed = visible != seen
        seen = visible
        if (changed) events.onEvent(PlakatEvent.Telegram)
        val target = if (visible) 1f else 0f
        if (reduce) {
            rise.snapTo(1f)
            words.snapTo(1f)
            fade.animateTo(target, tween(FADE_MS, easing = PlakatEasings.Linear))
        } else {
            fade.snapTo(1f)
            if (visible) {
                coroutineScope {
                    launch { rise.animateTo(1f, tween(PlakatDurations.NORMAL, easing = PlakatEasings.HardStop)) }
                    launch {
                        words.animateTo(1f, tween(PlakatDurations.QUICK, PlakatDurations.NORMAL, PlakatEasings.Linear))
                    }
                }
            } else {
                words.snapTo(0f)
                rise.animateTo(0f, tween(PlakatDurations.NORMAL, easing = PlakatEasings.HardStop))
            }
        }
        if (changed && !visible) hidden()
    }
    // Hidden, at rest, nothing of it is composed or takes input: under reduced motion it fades in place, so its action
    // would stay there, invisible, and in any mode the keyboard would reach it.
    val settled by remember { derivedStateOf { rise.value == 0f || fade.value == 0f } }
    if (!visible && settled) return

    val body = PlakatTheme.typography.bodyM.style
    val text =
        body
            .copy(
                fontSize = MESSAGE_SP.sp,
                fontWeight = FontWeight.Normal,
                color = colors.paper,
            ).cssLine(PlakatFaceMetrics.Body)
    val wipe = Modifier.plakatWipe(PlakatWipe.Left) { words.value }
    val word = PlakatTheme.strings.error.uppercase()
    Column(
        modifier
            .graphicsLayer {
                translationY = (1f - rise.value) * (size.height + RideBelow.toPx())
                alpha = fade.value
            }.semantics { liveRegion = LiveRegionMode.Polite }
            .background(colors.ink),
    ) {
        if (error) {
            Box(
                Modifier.fillMaxWidth().height(ErrorStrip).drawBehind {
                    drawRect(plakatHatchBrush(size, colors.red, colors.ink, ErrorHatchPeriod.toPx()))
                },
            )
        }
        Row(
            Modifier
                .heightIn(min = if (error) PlakatSizes.Touch else PlakatSizes.Field)
                .padding(
                    PaddingValues(
                        start = PlakatGrid.Margin,
                        end =
                            if (actionLabel ==
                                null
                            ) {
                                PlakatGrid.Margin
                            } else {
                                0.dp
                            },
                    ),
                ),
            horizontalArrangement = Arrangement.spacedBy(PlakatGrid.Margin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                buildAnnotatedString {
                    if (error) {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("$word.") }
                        append(' ')
                    }
                    append(message)
                },
                Modifier.weight(1f).padding(vertical = PlakatGrid.Step).then(wipe),
                style = text,
            )
            if (actionLabel != null) SnackbarAction(actionLabel, onAction, visible, wipe, forcePressed)
        }
    }
}

/** The action: the word over its rule, centred in a 96 × 56 target; pressed, inverted on a paper plate. */
@Composable
private fun SnackbarAction(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    wipe: Modifier,
    forcePressed: Boolean,
) {
    val colors = PlakatTheme.colors
    val source = remember { MutableInteractionSource() }
    val held by source.collectIsPressedAsState()
    val pressed = forcePressed || held
    val ink = if (pressed) colors.ink else colors.paper
    Box(
        Modifier
            .height(PlakatSizes.Field)
            // At most half of what the row has, so the message always has the rest; the 96 can give way on a narrow one.
            .layout { measurable, constraints ->
                val least = ActionMinWidth.roundToPx()
                val inner =
                    if (constraints.hasBoundedWidth) {
                        val most = constraints.maxWidth / 2
                        constraints.copy(minWidth = min(least, most), maxWidth = most)
                    } else {
                        constraints.copy(minWidth = least)
                    }
                val placeable = measurable.measure(inner)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }.then(wipe)
            .plakatFocusFrame(source, colors.paper)
            .clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .drawBehind { if (pressed) drawRect(colors.paper) },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            Modifier
                .drawBehind {
                    val rule = PlakatLines.Rule.toPx()
                    drawRect(ink, Offset(0f, size.height - rule), Size(size.width, rule))
                }.padding(bottom = ActionRuleGap + PlakatLines.Rule),
            style =
                PlakatTheme.typography.button.style
                    .copy(fontSize = ACTION_SP.sp, letterSpacing = 0.sp, color = ink)
                    .cssLine(PlakatFaceMetrics.Display),
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            maxLines = 1,
        )
    }
}
