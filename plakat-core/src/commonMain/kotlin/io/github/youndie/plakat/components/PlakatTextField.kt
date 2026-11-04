package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.plakatHatch
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The caret: a red bar 3 × 24 two pixels after the text (the Core page's `gap:2px`, `width:3px;height:24px`). */
private val CaretWidth = PlakatLines.Rule
private val CaretHeight = 24.dp
private val CaretGap = 2.dp

/** The caret's blink, the spec row `caret`: on 500, off 300, period 800, no fade. */
internal const val CARET_ON_MS = 500L
internal const val CARET_OFF_MS = 300L

/** The error's jolt: 2 px and back, 100 ms each way linear (the page's `translateX(2px)`, `100ms linear`). */
private val ShakeDistance = 2.dp
internal const val SHAKE_MS = 100

/** The error message's line: the Core page's `font-size:14px;line-height:1.35`. */
private const val MESSAGE_LINE = 1.35f

/** The ink weight of the word «ОШИБКА.» in the message (`<b>`): the body face's bold cut. */
private val MessageWordWeight = FontWeight.SemiBold

/**
 * A text field (`plakat/size/field`, 56 dp) with its [label] in capitals above and a 3 dp rule under
 * it. Focused, an 8 dp bar grows over the rule from the left in 120 ms (`underline`) and a square red
 * caret blinks 500 on, 300 off. With an [error] the field jolts 2 dp and back, a red hatch opens under
 * it from the left in 200 ms (`hatch`), and the message follows the word «ОШИБКА.» — colour is never
 * the only sign. Under reduced motion the bar and the hatch appear at once, nothing jolts and the caret
 * stands. The field takes the width it is given, as a CSS block does.
 */
@Composable
public fun PlakatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
) {
    // The String API over a TextFieldValue, as Material's: the caret needs the selection.
    var state by remember { mutableStateOf(TextFieldValue(value)) }
    val shown = state.copy(text = value)
    SideEffect { if (shown.selection != state.selection || shown.composition != state.composition) state = shown }
    var last by remember(value) { mutableStateOf(value) }
    PlakatTextFieldFrame(
        shown,
        { next ->
            state = next
            val changed = last != next.text
            last = next.text
            if (changed) onValueChange(next.text)
        },
        modifier,
        label,
        placeholder,
        error,
        enabled,
        keyboardOptions,
        keyboardActions,
        visualTransformation,
        interactionSource,
    )
}

@Composable
internal fun PlakatTextFieldFrame(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier,
    label: String?,
    placeholder: String?,
    error: String?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
    forceFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focusedNow by source.collectIsFocusedAsState()
    val focused = focusedNow || forceFocused
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val ink = if (enabled) colors.ink else colors.inkMuted

    // Focus: the bar grows from the left, 120 ms linear, and shrinks back the same way.
    val bar = remember { Animatable(if (focused) 1f else 0f) }
    var focusSeen by remember { mutableStateOf(focused) }
    LaunchedEffect(focused, reduce) {
        if (focused && !focusSeen) events.onEvent(PlakatEvent.Underline)
        focusSeen = focused
        val target = if (focused) 1f else 0f
        if (reduce ||
            forceFocused
        ) {
            bar.snapTo(target)
        } else {
            bar.animateTo(target, tween(PlakatDurations.QUICK, easing = LinearEasing))
        }
    }

    // Error: the hatch opens from the left in 200 ms linear while the field jolts 2 dp and back.
    val failing = error != null
    val hatch = remember { Animatable(if (failing) 1f else 0f) }
    val shake = remember { Animatable(0f) }
    var errorSeen by remember { mutableStateOf(failing) }
    LaunchedEffect(failing, reduce) {
        val appeared = failing && !errorSeen
        errorSeen = failing
        if (!failing) {
            hatch.snapTo(0f)
            return@LaunchedEffect
        }
        if (appeared) events.onEvent(PlakatEvent.Hatch)
        if (reduce || !appeared) {
            hatch.snapTo(1f)
            return@LaunchedEffect
        }
        hatch.snapTo(0f)
        coroutineScope {
            launch { hatch.animateTo(1f, tween(PlakatDurations.NORMAL, easing = LinearEasing)) }
            shake.animateTo(1f, tween(SHAKE_MS, easing = LinearEasing))
            shake.animateTo(0f, tween(SHAKE_MS, easing = LinearEasing))
        }
    }

    // The caret blinks while focused, starting on again whenever the text or the caret moves.
    var caretOn by remember { mutableStateOf(true) }
    LaunchedEffect(focused, value.text, value.selection, reduce) {
        caretOn = true
        if (!focused || reduce) return@LaunchedEffect
        while (true) {
            delay(CARET_ON_MS)
            caretOn = false
            delay(CARET_OFF_MS)
            caretOn = true
        }
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    // The page sets no line height: the field's text and the message's word are on `normal` lines.
    val fieldStyle =
        type.bodyL.style
            .copy(color = ink)
            .cssLine(PlakatFaceMetrics.Body)

    Column(
        modifier.semantics { if (error != null) error(error) },
        verticalArrangement = Arrangement.spacedBy(PlakatGrid.Step),
    ) {
        if (label != null) {
            BasicText(type.label.text(label), style = type.label.style.copy(color = ink), maxLines = 1)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(PlakatSizes.Field)
                .graphicsLayer { translationX = shake.value * ShakeDistance.toPx() }
                .drawWithContent {
                    drawContent()
                    drawRule(enabled, ink, bar.value)
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value,
                onValueChange,
                Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        val cursor = layout?.getCursorRect(value.selection.end)
                        if (focused && caretOn && enabled && cursor != null) {
                            val h = CaretHeight.toPx()
                            drawRect(
                                colors.red,
                                Offset(cursor.left + CaretGap.toPx(), (size.height - h) / 2),
                                Size(CaretWidth.toPx(), h),
                            )
                        }
                    },
                enabled = enabled,
                textStyle = fieldStyle,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = true,
                visualTransformation = visualTransformation,
                onTextLayout = { layout = it },
                interactionSource = source,
                cursorBrush = SolidColor(Color.Transparent),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.text.isEmpty() && placeholder != null) {
                            BasicText(placeholder, style = fieldStyle.copy(color = colors.inkMuted), maxLines = 1)
                        }
                        inner()
                    }
                },
            )
        }
        if (error != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(PlakatLines.Bar)
                    .drawWithContent {
                        clipRect(
                            right = size.width * hatch.value,
                        ) { this@drawWithContent.drawContent() }
                    }.plakatHatch(colors.red, Color.Transparent),
            )
            val message = type.bodyS.style
            BasicText(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = MessageWordWeight)) {
                        append(PlakatTheme.strings.error.uppercase() + ".")
                    }
                    append(" ")
                    append(error)
                },
                style = message.copy(color = ink).cssLine(PlakatFaceMetrics.Body, MESSAGE_LINE),
            )
        }
    }
}

/** The 3 dp rule under the field, the bar grown over it by [bar], or a dashed hair when disabled. */
private fun DrawScope.drawRule(
    enabled: Boolean,
    ink: Color,
    bar: Float,
) {
    if (!enabled) {
        val w = PlakatLines.Hair.toPx()
        dashedLine(ink, Offset(0f, size.height - w / 2), Offset(size.width, size.height - w / 2), w)
        return
    }
    val rule = PlakatLines.Rule.toPx()
    drawRect(ink, Offset(0f, size.height - rule), Size(size.width, rule))
    if (bar > 0f) {
        val h = PlakatLines.Bar.toPx()
        drawRect(ink, Offset(0f, size.height - h), Size(size.width * bar, h))
    }
}
