package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.print.PlakatCutShape
import io.github.youndie.plakat.print.plakatHatch
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor

/**
 * The primary action: a red plate with its corner cut (`plakat/shape/cut`), an upper-case slogan on
 * it. Pressed, the inks invert in one frame and the slogan moves 1 dp right and down (`stamp`);
 * released, they return in 60 ms hard-stop (`release`). [loading] wipes the slogan off from the left
 * in 120 ms and turns a quarter sector in twelve steps a 1200 ms turn (`engage`); the button then takes
 * no clicks. Under reduced motion each change is instant and the sector stands.
 */
@Composable
public fun PlakatButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatButtonFrame(PlakatButtonKind.Filled, text, onClick, modifier, enabled, loading, interactionSource)
}

/** A secondary action: the slogan in a 3 dp frame. Pressed, the frame fills with ink. */
@Composable
public fun PlakatOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatButtonFrame(PlakatButtonKind.Outlined, text, onClick, modifier, enabled, false, interactionSource)
}

/** The quietest action: the slogan underlined by a 3 dp rule. Pressed, the whole box inverts. */
@Composable
public fun PlakatTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatButtonFrame(PlakatButtonKind.Text, text, onClick, modifier, enabled, false, interactionSource)
}

/**
 * A destructive action, told from the primary one without its colour (`plakat/semantic/danger`): the
 * red-and-paper hatch with the word on a paper plate, as the Demo page's «ВЫЙТИ ИЗ АККАУНТА».
 */
@Composable
public fun PlakatDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatButtonFrame(PlakatButtonKind.Danger, text, onClick, modifier, enabled, false, interactionSource)
}

internal enum class PlakatButtonKind { Filled, Outlined, Text, Danger }

/**
 * What a place that draws the kit's button at its own size changes: the word's size ([labelSize] unset keeps
 * the role's), its tracking, the danger word's plate, the underline's gap and the inset of the word; and
 * [wordWide], as wide as the word's real width and all around it, rounded as Blink snaps a box, for a row
 * laid out from its end, where a width ceiled by the layout moves everything before it.
 */
internal class ButtonMetrics(
    val labelSize: TextUnit = TextUnit.Unspecified,
    val tracked: Boolean = true,
    val dangerPlate: Dp = DangerPlatePadding,
    val underlineGap: Dp = UnderlineGap,
    val inset: Dp = PlakatGrid.Margin,
    val wordWide: Boolean = false,
)

private fun TextStyle.untracked(metrics: ButtonMetrics) = if (metrics.tracked) this else copy(letterSpacing = 0.sp)

/** A state forced from outside, for the fixtures of the page's static cells: the look, not the behaviour. */
internal enum class ButtonLook { Pressed, Focused, Hovered }

/** The loading sector's turn and steps: the spec row `engage`, "цикл 1200 · steps 12". */
internal const val SPINNER_TURN_MS = 1200
internal const val SPINNER_STEPS = 12

/** The sector's disc: 22 px with a 3 px ring, `width:22px;height:22px;box-shadow:inset 0 0 0 3px`. */
private val SpinnerSize = 22.dp

/** The danger word's plate and type: the Demo page's `padding:6px 10px`, `font-size:13px`, `padding:0 8px`. */
private val DangerPlatePadding = 10.dp
private val DangerPlateVertical = 6.dp
private val DangerInset = PlakatGrid.Step
private const val DANGER_FONT_SP = 13

/** The underline's gap below the text: the Core page's `padding-bottom:4px`. */
private val UnderlineGap = 4.dp

/** The focus ring: the Core page's `outline:3px solid; outline-offset:3px`. */
private val FocusOffset = PlakatLines.Rule

/** The text button's disabled underline: `border-bottom:1px dashed`. */
private val DisabledUnderline = PlakatLines.Hair

/**
 * The paints of a button: the ground blended by [pressed] between rest (0) and pressed (1) as its
 * transition runs, the ink and the line as [inkPressed] says.
 */
internal class ButtonPaint(
    val ground: Color,
    val ink: Color,
    val line: Color,
)

internal fun buttonPaint(
    kind: PlakatButtonKind,
    colors: PlakatColors,
    enabled: Boolean,
    pressed: Float,
    held: Boolean = false,
): ButtonPaint {
    if (!enabled) {
        return when (kind) {
            PlakatButtonKind.Filled -> ButtonPaint(colors.concrete, colors.inkMuted, Color.Transparent)
            PlakatButtonKind.Danger -> ButtonPaint(colors.paper, colors.inkMuted, colors.inkMuted)
            else -> ButtonPaint(Color.Transparent, colors.inkMuted, colors.inkMuted)
        }
    }
    val rest =
        when (kind) {
            PlakatButtonKind.Filled -> ButtonPaint(colors.red, colors.onRed, Color.Transparent)
            PlakatButtonKind.Outlined -> ButtonPaint(colors.ink.copy(alpha = 0f), colors.ink, colors.ink)
            PlakatButtonKind.Text -> ButtonPaint(colors.ink.copy(alpha = 0f), colors.ink, colors.ink)
            PlakatButtonKind.Danger -> ButtonPaint(colors.paper, colors.ink, colors.red)
        }
    val down =
        when (kind) {
            PlakatButtonKind.Filled -> ButtonPaint(colors.ink, colors.paper, Color.Transparent)
            PlakatButtonKind.Outlined -> ButtonPaint(colors.ink, colors.paper, colors.ink)
            PlakatButtonKind.Text -> ButtonPaint(colors.ink, colors.paper, colors.paper)
            PlakatButtonKind.Danger -> ButtonPaint(colors.ink, colors.paper, colors.red)
        }
    val now = if (inkPressed(pressed, held)) down else rest
    return ButtonPaint(srgbLerp(rest.ground, down.ground, pressed), now.ink, now.line)
}

/**
 * A CSS colour transition: straight interpolation of premultiplied sRGB, which Compose's own `lerp`
 * (Oklab) is not.
 */
internal fun srgbLerp(
    from: Color,
    to: Color,
    t: Float,
): Color {
    val a = from.alpha + (to.alpha - from.alpha) * t
    if (a == 0f) return Color.Transparent

    fun channel(
        x: Float,
        y: Float,
    ) = (x * from.alpha + (y * to.alpha - x * from.alpha) * t) / a
    return Color(channel(from.red, to.red), channel(from.green, to.green), channel(from.blue, to.blue), a)
}

/** The loading sector's angle [ms] into its turn: twelve 30° steps, CSS `steps(12, end)`. */
internal fun spinnerDegrees(ms: Long): Float {
    val t = (ms % SPINNER_TURN_MS).toFloat() / SPINNER_TURN_MS
    return floor(t * SPINNER_STEPS) * (360f / SPINNER_STEPS)
}

@Composable
internal fun PlakatButtonFrame(
    kind: PlakatButtonKind,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    interactionSource: MutableInteractionSource?,
    look: ButtonLook? = null,
    spinnerAtMs: Long? = null,
    metrics: ButtonMetrics = ButtonMetrics(),
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val state = rememberPlakatPress(source)
    val press = state.press

    // Loading wipes the slogan off from the left (`clip-path: inset(0 0 0 100%)`, 120 ms linear).
    val wipe = remember { Animatable(if (loading) 1f else 0f) }
    var loadingSeen by remember { mutableStateOf(loading) }
    LaunchedEffect(loading, reduce) {
        if (loading && !loadingSeen) events.onEvent(PlakatEvent.Engage)
        loadingSeen = loading
        val target = if (loading) 1f else 0f
        if (reduce) wipe.snapTo(target) else wipe.animateTo(target, tween(PlakatDurations.QUICK, easing = LinearEasing))
    }

    val loadingWord = PlakatTheme.strings.loading
    val pressed = if (look == ButtonLook.Pressed) 1f else press.value
    val paint = buttonPaint(kind, colors, enabled, pressed, state.held || look == ButtonLook.Pressed)
    // Designed here (B-39): on a pointer, in one frame, the frame turns over; a press still reads over it.
    val hovered = enabled && !loading && (look == ButtonLook.Hovered || state.hovered) && pressed < 1f
    val label = PlakatTheme.typography.button
    val style =
        if (kind == PlakatButtonKind.Danger) {
            label.style.copy(fontSize = DANGER_FONT_SP.sp, color = paint.ink).untracked(metrics)
        } else {
            // A poster's slogan button may set its own size (Круг: 16); the line stays one size high.
            val size = if (metrics.labelSize.isSpecified) metrics.labelSize else label.style.fontSize
            val line = if (metrics.labelSize.isSpecified) metrics.labelSize else label.style.lineHeight
            label.style.copy(fontSize = size, lineHeight = line, color = paint.ink).untracked(metrics)
        }
    val shift = pressed
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val wordPx =
        if (metrics.wordWide) {
            remember(text, style, density) { measurer.measure(label.text(text), style).getLineRight(0) }
        } else {
            null
        }
    val fitted =
        if (wordPx != null) {
            val around = (
                metrics.inset.takeIf { kind != PlakatButtonKind.Danger }
                    ?: (DangerInset + metrics.dangerPlate)
            )
            // A short word on a narrow target: the fitted width gives way to the 48 a touch needs.
            with(density) { Modifier.width(max(floor(wordPx + 2 * around.toPx() + .5f).toDp(), PlakatSizes.Touch)) }
        } else {
            Modifier
        }
    val ringShown = look == ButtonLook.Focused || state.focusShown()

    Box(
        modifier
            .then(fitted)
            .defaultMinSize(minWidth = PlakatSizes.Touch, minHeight = PlakatSizes.Button)
            .height(PlakatSizes.Button)
            .drawWithContent {
                drawContent()
                if (hovered && kind == PlakatButtonKind.Danger) {
                    val w = PlakatLines.Rule.toPx()
                    drawRect(colors.ink, Offset(w / 2, w / 2), Size(size.width - w, size.height - w), style = Stroke(w))
                }
                if (ringShown) {
                    val o = FocusOffset.toPx()
                    val w = PlakatLines.Rule.toPx()
                    drawRect(
                        colors.ink,
                        topLeft = Offset(-o - w / 2, -o - w / 2),
                        size = Size(size.width + 2 * o + w, size.height + 2 * o + w),
                        style = Stroke(w),
                    )
                }
            }.drawBehind { drawGround(kind, paint, enabled, colors, hovered) }
            .then(
                if (kind ==
                    PlakatButtonKind.Danger
                ) {
                    Modifier.plakatHatch(if (enabled) colors.red else colors.inkMuted, colors.paper)
                } else {
                    Modifier
                },
            ).clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick,
            ).semantics { if (loading) stateDescription = loadingWord }
            .padding(horizontal = if (kind == PlakatButtonKind.Danger) DangerInset else metrics.inset),
        contentAlignment =
            if (kind == PlakatButtonKind.Danger ||
                metrics.wordWide
            ) {
                Alignment.CenterStart
            } else {
                Alignment.Center
            },
    ) {
        val wiped =
            Modifier.drawWithContent {
                val left = size.width * wipe.value
                clipRect(left = left) { this@drawWithContent.drawContent() }
            }
        val shifted =
            Modifier.graphicsLayer {
                translationX = shift * 1.dp.toPx()
                translationY = shift * 1.dp.toPx()
            }
        when (kind) {
            PlakatButtonKind.Text -> {
                UnderlinedLabel(
                    label.text(text),
                    style,
                    paint.line,
                    enabled,
                    shifted,
                    metrics.underlineGap,
                    wordPx != null,
                    hovered,
                )
            }

            PlakatButtonKind.Danger -> {
                val plate =
                    if (wordPx != null) {
                        with(density) { Modifier.width(floor(wordPx + 2 * metrics.dangerPlate.toPx() + .5f).toDp()) }
                    } else {
                        Modifier
                    }
                BasicText(
                    label.text(text),
                    Modifier
                        .then(shifted)
                        .then(plate)
                        .drawBehind { drawRect(paint.ground) }
                        .padding(horizontal = metrics.dangerPlate, vertical = DangerPlateVertical),
                    style = style,
                    softWrap = false,
                    maxLines = 1,
                    overflow = if (wordPx == null) TextOverflow.Ellipsis else TextOverflow.Visible,
                )
            }

            else -> {
                BasicText(
                    label.text(text),
                    Modifier.then(wiped).then(shifted),
                    style = style.copy(textAlign = TextAlign.Center),
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (loading) Spinner(paint.ink, reduce, spinnerAtMs)
    }
}

private fun DrawScope.drawGround(
    kind: PlakatButtonKind,
    paint: ButtonPaint,
    enabled: Boolean,
    colors: PlakatColors,
    hovered: Boolean,
) {
    when (kind) {
        PlakatButtonKind.Filled -> {
            val outline = PlakatCutShape().createOutline(size, layoutDirection, this)
            val path = (outline as androidx.compose.ui.graphics.Outline.Generic).path
            drawPath(path, paint.ground)
            // The plate gets the ink frame, inside its own cut.
            if (hovered) clipPath(path) { drawPath(path, colors.ink, style = Stroke(2 * PlakatLines.Rule.toPx())) }
        }

        PlakatButtonKind.Outlined -> {
            // The frame fills, with concrete: ink is the press.
            drawRect(if (hovered) colors.concrete else paint.ground)
            val w = PlakatLines.Rule.toPx()
            if (enabled) {
                drawRect(paint.line, Offset(w / 2, w / 2), Size(size.width - w, size.height - w), style = Stroke(w))
            } else {
                // A CSS dashed border: each side its own dashed line from corner to corner, as Blink draws it.
                val h = w / 2
                dashedLine(paint.line, Offset(0f, h), Offset(size.width, h), w)
                dashedLine(paint.line, Offset(0f, size.height - h), Offset(size.width, size.height - h), w)
                dashedLine(paint.line, Offset(h, 0f), Offset(h, size.height), w)
                dashedLine(paint.line, Offset(size.width - h, 0f), Offset(size.width - h, size.height), w)
            }
        }

        PlakatButtonKind.Text -> {
            drawRect(paint.ground)
        }

        // The hatch is `plakatHatch` on the box; the danger ground is the word's plate. Drawn after the hatch, by the box.
        PlakatButtonKind.Danger -> {}
    }
}

@Composable
private fun UnderlinedLabel(
    text: String,
    style: TextStyle,
    line: Color,
    enabled: Boolean,
    shifted: Modifier,
    gap: Dp,
    wordWide: Boolean,
    hovered: Boolean = false,
) {
    // The rule is as long as the text's own advance, a fraction included, as a CSS border under an
    // inline box is: the laid-out width is ceiled to a whole pixel and would draw one column long.
    var advance by remember { mutableStateOf(0f) }
    val thickness: Dp = if (enabled) PlakatLines.Rule else DisabledUnderline
    Column(
        shifted.drawBehind {
            val w = thickness.toPx()
            val y = size.height - w / 2
            if (enabled) {
                // The rule becomes the bar, growing down from where it stood, with the layout left as it was.
                val drawn = if (hovered) PlakatLines.Bar.toPx() else w
                drawLine(line, Offset(0f, y - w / 2 + drawn / 2), Offset(advance, y - w / 2 + drawn / 2), drawn)
            } else {
                dashedLine(line, Offset(0f, y), Offset(advance, y), w)
            }
        },
    ) {
        BasicText(
            text,
            style = style,
            maxLines = 1,
            softWrap = false,
            overflow = if (wordWide) TextOverflow.Visible else TextOverflow.Ellipsis,
            onTextLayout = { advance = it.getLineRight(0) },
        )
        Spacer(Modifier.height(gap + thickness))
    }
}

@Composable
private fun Spinner(
    color: Color,
    reduce: Boolean,
    atMs: Long?,
) {
    val turn = remember { Animatable(atMs?.toFloat() ?: 0f) }
    LaunchedEffect(reduce, atMs) {
        if (reduce || atMs != null) return@LaunchedEffect
        turn.animateTo(
            SPINNER_TURN_MS.toFloat(),
            infiniteRepeatable(tween(SPINNER_TURN_MS, easing = LinearEasing), RepeatMode.Restart),
        )
    }
    Canvas(Modifier.size(SpinnerSize)) {
        val ring = PlakatLines.Rule.toPx()
        drawCircle(color, radius = size.minDimension / 2 - ring / 2, style = Stroke(ring))
        rotate(spinnerDegrees(turn.value.toLong())) {
            drawArc(color, -90f, 90f, useCenter = true)
        }
    }
}

/**
 * A line dashed as Blink dashes a CSS `dashed` border: dashes of twice the width with gaps of one
 * width from 3 px up, three and two widths below, and the gap stretched or shrunk so that the line
 * starts and ends on a dash (`StrokeData::SelectBestDashGap`). A plain dash effect leaves the ends
 * wherever the pattern falls, 12 % of a disabled outlined button off its cell.
 */
internal fun DrawScope.dashedLine(
    color: Color,
    from: Offset,
    to: Offset,
    width: Float,
) {
    val length = (to - from).getDistance()
    drawLine(color, from, to, width, pathEffect = blinkDashes(length, width)?.let { PathEffect.dashPathEffect(it) })
}

/** The dash and gap Blink picks for a dashed line [length] long and [width] wide; null draws it solid. */
internal fun blinkDashes(
    length: Float,
    width: Float,
): FloatArray? {
    val dash = width * if (width >= 3f) 2f else 3f
    val gap = width * if (width >= 3f) 1f else 2f
    if (length <= dash * 2) return null
    val fewer = floor((length + gap) / (dash + gap))
    val more = fewer + 1
    val fewerGap = (length - fewer * dash) / (fewer - 1)
    val moreGap = (length - more * dash) / (more - 1)
    val best = if (moreGap <= 0f || abs(fewerGap - gap) < abs(moreGap - gap)) fewerGap else moreGap
    return floatArrayOf(dash, best)
}
