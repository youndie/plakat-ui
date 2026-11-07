package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.agit.PosterRecipes
import io.github.youndie.plakat.agit.PosterRole
import io.github.youndie.plakat.agit.PosterShape
import io.github.youndie.plakat.agit.drawPosterShape
import io.github.youndie.plakat.agit.posterComposition
import io.github.youndie.plakat.print.PlakatWipe
import io.github.youndie.plakat.print.RaskatOptions
import io.github.youndie.plakat.print.RaskatRollImage
import io.github.youndie.plakat.print.drawRaskat
import io.github.youndie.plakat.print.plakatGrain
import io.github.youndie.plakat.print.plakatWipe
import io.github.youndie.plakat.print.raskatField
import io.github.youndie.plakat.print.raskatInkB
import io.github.youndie.plakat.print.raskatRollT0
import io.github.youndie.plakat.theme.FaceMetrics
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatPrint
import io.github.youndie.plakat.tokens.PlakatShape
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/** The poster's three layouts, as the Core page draws them; each has its fixed size. */
public enum class PlakatPosterLayout(
    internal val width: Dp,
    internal val height: Dp,
) {
    /** Круг, 390 × 392: a composition with a red circle (a photo in it, or a raskat), the title at −17°. */
    Circle(390.dp, 392.dp),

    /** Фон, 390 × 500: a photo under a slanted concrete plate, the title in ink stripes at −17°. */
    Background(390.dp, 500.dp),

    /** Баннер, 390 × 160, for a feed: the title upright, the composition in a window on the right. */
    Banner(390.dp, 160.dp),

    /**
     * Широкий, designed here (B-39), 392 high and at least 600 wide, as wide as its box: the title and the slogan on the
     * left half, upright, the composition on the right.
     */
    Wide(600.dp, 392.dp),

    /**
     * Растяжка, 390 × 600, from the Stretch page: the title's three lines each stretched across the width, 136, 120 and 80
     * high, a red bar under the first, and a circle going off the right edge. No diagonal: here the vertical leads.
     */
    Stretch(390.dp, 600.dp),
}

/** A seed from the poster's words, so the same content prints the same poster on every target. */
public fun plakatPosterSeed(title: List<String>): Int = title.joinToString("\n").hashCode()

// Where the page puts each part, per layout (its inline styles), 1 px = 1 dp.

/** Круг: the composition's frame, `width: 390px; height: 312px; overflow: hidden`. */
private val CircleArtHeight = 312.dp

/** Круг: the title block's corner, `left: 16px; top: 92px`, turned about it. */
private val CircleTitleTop = 92.dp

/** Круг: the title, `font-size: 48px; line-height: .95`. */
private const val CIRCLE_TITLE_SP = 48
private const val CIRCLE_TITLE_LINE = .95f

/** Круг: the body, `left: 16px; top: 252px; width: 290px; font-size: 16px; line-height: 1.4`. */
private val CircleBodyTop = 252.dp
private val CircleBodyWidth = 290.dp
private const val CIRCLE_BODY_SP = 16

/** Круг: the slogan, `left: 16px; right: 16px; bottom: 24px`, its word 16 px. */
private val CircleButtonBottom = 24.dp
private const val CIRCLE_BUTTON_SP = 16

/** Фон: the plate, `polygon(0 0, 100% 0, 100% 300px, 0 419px)`. */
private val PlateRight = 300.dp
private val PlateLeft = 419.dp

/** Фон: the title block's corner, `left: 16px; top: 116px`, its stripes `font-size: 40px; line-height: 1; padding: 4px 10px; gap: 6px`. */
private val StripeTitleTop = 116.dp
private const val STRIPE_SP = 40
private val StripePadX = 10.dp
private val StripePadY = 4.dp
private val StripeGap = 6.dp

/** Фон: the body, `right: 16px; top: 372px; width: 200px; font-size: 15px`, and the slogan `width: 200px` at the bottom right. */
private val BackgroundBodyTop = 372.dp
private val BackgroundColumn = 200.dp
private const val BACKGROUND_BODY_SP = 15

/**
 * Широкий, designed here (B-39), as no page draws it: the composition's frame of 360 × 392, on the 8 grid; the title 32
 * down at Круг's size, a gap of 16 under it, and the slogan where Круг's stands, 240 wide as the Stretch page's poster's.
 */
private val WideFrameWidth = 360.dp
private val WideTitleTop = 32.dp
private val WideBodyGap = 16.dp
private val WideButtonWidth = 240.dp

/** Растяжка: the lines' `top` and `height`, with the bar's `top: 168px; height: 16px` under the first. */
private val StretchLines = listOf(16.dp to 136.dp, 200.dp to 120.dp, 336.dp to 80.dp)
private val StretchBarTop = 168.dp
private val StretchBarHigh = 16.dp

/** Растяжка: the body and the circle at `top: 440px`, the circle of 136 at `right: -56px`, the column `width: 240px`. */
private val StretchLowerTop = 440.dp
private val StretchCircle = 136.dp
private val StretchCircleLeft = 310.dp
private val StretchColumn = 240.dp
private const val STRETCH_BUTTON_SP = 14
private const val STRETCH_ELEMENTS = 2

/** The body's line, `line-height: 1.4`, both layouts. */
private const val BODY_LINE = 1.4f

/** How many lines of a body [px] large fit from [top] to the top of a slogan standing [foot] off the poster's bottom. */
private fun DrawScope.bodyLines(
    top: Float,
    posterHeight: Float,
    px: Float,
    foot: Dp = CircleButtonBottom,
): Int = ((posterHeight - foot.toPx() - PlakatSizes.Button.toPx() - top) / (px * BODY_LINE)).toInt()

/** Баннер: the composition's window, `left: 214px; width: 176px`, and the title `font-size: 32px; line-height: 1`. */
private val BannerWindowLeft = 214.dp
private const val BANNER_TITLE_SP = 32

/** Баннер: the slogan, `width: 180px` at the bottom left. */
private val BannerButtonWidth = 180.dp

// The raskat the page's Круг · раскат prints in the circle: its `drawRaskat` options.
private const val RASKAT_T0 = .58
private const val RASKAT_HARD = 12.0
private const val RASKAT_STREAK = .5

/** The print's wipes: each element's, `120ms linear`, and the delays of the title (150) and the slogan (180). */
private const val WIPE_MS = PlakatDurations.QUICK
private const val TITLE_AT_MS = 150
private const val BUTTON_AT_MS = 180

/**
 * A poster in one of its [layout]s: a composition from the page's generator seeded by [seed]
 * (by default from the [title]), the [title]'s lines in the display face — turned to the −17° axis
 * as a block in Круг and Фон, upright in Баннер, out of register while the theme's registration is
 * on — a [body] under it (Круг and Фон; Баннер has none), and one slogan button, [action], which
 * runs [onAction].
 *
 * Круг's red circle holds [photo], multiplied over the red as the page's halftone is (the kit's
 * halftone processor is [B-33]'s; any content works), or, with [raskat], is printed as a raskat of red
 * into paper. Фон's slanted concrete plate holds [photo] over the whole poster, the composition
 * multiplied over it in a light theme.
 *
 * It prints as it enters the composition (`print`): every element wiped in from the left in 120 ms,
 * linear — the composition's shapes 30 ms apart, the title and body at 150 ms, the slogan at 180 —
 * and the raskat rolls in (`roll`), 320 ms hard-stop. Under reduced motion it is printed and rolled
 * at once. A new [seed] reprints at once. [print] false shows it printed, for a poster that scrolls
 * back into a lazy list.
 *
 * A layout is a fixed sheet, as a printed poster is, so what it is given has to suit it. A [body] too long for its place
 * stops above the slogan, with an ellipsis; a [title] is drawn at the design's size, and one too long for its sheet runs off
 * it or under the slogan, so choose its words for the layout, and Растяжка takes only its first three lines.
 */
@Composable
public fun PlakatPoster(
    title: List<String>,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    layout: PlakatPosterLayout = PlakatPosterLayout.Circle,
    body: String? = null,
    seed: Int = plakatPosterSeed(title),
    raskat: Boolean = false,
    print: Boolean = true,
    photo: (@Composable BoxScope.() -> Unit)? = null,
) {
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val recipe =
        when (layout) {
            PlakatPosterLayout.Circle -> PosterRecipes.Circle

            PlakatPosterLayout.Background -> PosterRecipes.Background

            PlakatPosterLayout.Banner -> PosterRecipes.Banner

            PlakatPosterLayout.Wide -> PosterRecipes.Wide

            // It draws no composition of the generator's.
            PlakatPosterLayout.Stretch -> PosterRecipes.Thumbnail
        }
    val composition = remember(seed, layout) { posterComposition(seed, recipe) }
    val rolls = raskat && layout == PlakatPosterLayout.Circle && composition.circle != null && PlakatTheme.print.raskat
    // Ms into the print, on one clock: every wipe and the roll are closed forms of it.
    val elements =
        if (layout == PlakatPosterLayout.Stretch) {
            STRETCH_ELEMENTS
        } else {
            composition.shapes.size +
                if (layout == PlakatPosterLayout.Background || composition.circle != null) 1 else 0
        }
    val end = remember(elements, rolls) { posterPrintMs(elements, rolls) }
    val age = remember { Animatable(if (print) 0f else Float.MAX_VALUE) }
    LaunchedEffect(Unit) {
        if (!print) return@LaunchedEffect
        events.onEvent(PlakatEvent.Print)
        if (rolls) events.onEvent(PlakatEvent.Roll)
        if (reduce) {
            age.snapTo(Float.MAX_VALUE)
        } else {
            age.animateTo(end.toFloat(), tween(end, easing = PlakatEasings.Linear))
            age.snapTo(Float.MAX_VALUE)
        }
    }
    val wipe = { start: Int -> { ((age.value - start) / WIPE_MS).coerceIn(0f, 1f) } }
    val measurer = rememberTextMeasurer()
    val display =
        PlakatTheme.typography.displayL.style
            .plain()
    val text =
        PlakatTheme.typography.bodyM.style
            .plain()
    val registration = PlakatTheme.print.registration

    Box(
        modifier
            .then(
                if (layout == PlakatPosterLayout.Wide) {
                    Modifier.widthIn(min = layout.width).fillMaxWidth().height(layout.height)
                } else {
                    Modifier.size(layout.width, layout.height)
                },
            ).clipToBounds()
            .drawBehind { drawRect(if (layout == PlakatPosterLayout.Banner) colors.concrete else colors.paper) }
            .plakatGrain(),
    ) {
        when (layout) {
            PlakatPosterLayout.Circle -> {
                Box(Modifier.size(layout.width, CircleArtHeight).clipToBounds()) {
                    Shapes(composition.shapes, colors, colors.concrete, wipe, first = 0)
                    composition.circle?.let { circle ->
                        Box(
                            Modifier.matchParentSize().plakatWipe(
                                PlakatWipe.Left,
                                wipe(composition.shapes.size * PlakatDurations.CASCADE),
                            ),
                        ) {
                            PhotoCircle(
                                circle,
                                colors,
                                photo,
                                if (rolls) ({ raskatRollT0(age.value.toDouble(), RASKAT_T0) }) else null,
                            )
                        }
                    }
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).title(title)) {
                    Canvas(Modifier.matchParentSize()) {
                        val origin = Offset(PlakatGrid.Margin.toPx(), CircleTitleTop.toPx())
                        rotate(PlakatShape.AXIS, origin) {
                            drawLines(
                                measurer,
                                title,
                                display.copy(fontSize = CIRCLE_TITLE_SP.sp),
                                PlakatFaceMetrics.Display,
                                CIRCLE_TITLE_LINE,
                                origin,
                                colors,
                                registration,
                                snap = false,
                            )
                        }
                    }
                }
                if (body != null) {
                    Box(
                        Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).semantics {
                            contentDescription =
                                body
                        },
                    ) {
                        Canvas(Modifier.matchParentSize()) {
                            val origin = Offset(PlakatGrid.Margin.toPx(), CircleBodyTop.toPx())
                            drawParagraph(
                                measurer,
                                body,
                                text.copy(fontSize = CIRCLE_BODY_SP.sp),
                                CircleBodyWidth.toPx(),
                                origin,
                                colors.ink,
                                BODY_LINE,
                                maxLines = bodyLines(origin.y, size.height, CIRCLE_BODY_SP.sp.toPx()),
                            )
                        }
                    }
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(BUTTON_AT_MS))) {
                    PlakatButtonFrame(
                        PlakatButtonKind.Filled,
                        action,
                        onAction,
                        Modifier
                            .offset(PlakatGrid.Margin, layout.height - CircleButtonBottom - PlakatSizes.Button)
                            .size(layout.width - PlakatGrid.Margin * 2, PlakatSizes.Button),
                        enabled = true,
                        loading = false,
                        interactionSource = null,
                        metrics = ButtonMetrics(labelSize = CIRCLE_BUTTON_SP.sp),
                    )
                }
            }

            PlakatPosterLayout.Background -> {
                val plate =
                    remember {
                        GenericShape { size, _ ->
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(
                                size.width,
                                PlateRight.value / PlakatPosterLayout.Background.height.value * size.height,
                            )
                            lineTo(0f, PlateLeft.value / PlakatPosterLayout.Background.height.value * size.height)
                            close()
                        }
                    }
                // The plate is printed first, the composition over it, all inside the plate.
                Box(
                    Modifier
                        .matchParentSize()
                        .plakatWipe(PlakatWipe.Left, wipe(0))
                        .graphicsLayer {
                            clip = true
                            shape = plate
                            compositingStrategy = CompositingStrategy.Offscreen
                        }.drawBehind { drawRect(colors.concrete) },
                ) {
                    if (photo != null) Box(Modifier.matchParentSize().multiplied()) { photo() }
                    val blend =
                        if (colors.ink.luminance() <
                            colors.paper.luminance()
                        ) {
                            BlendMode.Multiply
                        } else {
                            BlendMode.SrcOver
                        }
                    Shapes(composition.shapes, colors, colors.concrete, wipe, first = 1, blend = blend)
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).title(title)) {
                    Canvas(Modifier.matchParentSize()) {
                        val origin = Offset(PlakatGrid.Margin.toPx(), StripeTitleTop.toPx())
                        rotate(PlakatShape.AXIS, origin) {
                            drawStripes(
                                measurer,
                                title,
                                display.copy(fontSize = STRIPE_SP.sp),
                                origin,
                                colors,
                                registration,
                            )
                        }
                    }
                }
                if (body != null) {
                    Box(
                        Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).semantics {
                            contentDescription =
                                body
                        },
                    ) {
                        Canvas(Modifier.matchParentSize()) {
                            val origin =
                                Offset(
                                    size.width - PlakatGrid.Margin.toPx() - BackgroundColumn.toPx(),
                                    BackgroundBodyTop.toPx(),
                                )
                            drawParagraph(
                                measurer,
                                body,
                                text.copy(fontSize = BACKGROUND_BODY_SP.sp),
                                BackgroundColumn.toPx(),
                                origin,
                                colors.ink,
                                BODY_LINE,
                                maxLines =
                                    bodyLines(
                                        origin.y,
                                        size.height,
                                        BACKGROUND_BODY_SP.sp.toPx(),
                                        PlakatGrid.Margin,
                                    ),
                            )
                        }
                    }
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(BUTTON_AT_MS))) {
                    PlakatButtonFrame(
                        PlakatButtonKind.Filled,
                        action,
                        onAction,
                        Modifier
                            .offset(
                                layout.width - PlakatGrid.Margin - BackgroundColumn,
                                layout.height - PlakatGrid.Margin - PlakatSizes.Button,
                            ).size(BackgroundColumn, PlakatSizes.Button),
                        enabled = true,
                        loading = false,
                        interactionSource = null,
                    )
                }
            }

            PlakatPosterLayout.Wide -> {
                // The composition's frame against the right edge, however wide the poster is.
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxWidth(.5f)
                        .height(layout.height)
                        .clipToBounds(),
                ) {
                    Box(Modifier.align(Alignment.TopEnd).requiredSize(WideFrameWidth, layout.height)) {
                        Shapes(composition.shapes, colors, colors.concrete, wipe, first = 0)
                    }
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).title(title)) {
                    Canvas(Modifier.matchParentSize()) {
                        val origin = Offset(PlakatGrid.Margin.toPx(), WideTitleTop.toPx())
                        drawLines(
                            measurer,
                            title,
                            display.copy(fontSize = CIRCLE_TITLE_SP.sp),
                            PlakatFaceMetrics.Display,
                            CIRCLE_TITLE_LINE,
                            origin,
                            colors,
                            registration,
                            snap = true,
                        )
                    }
                }
                if (body != null) {
                    Box(
                        Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).semantics {
                            contentDescription = body
                        },
                    ) {
                        Canvas(Modifier.matchParentSize()) {
                            val top =
                                WideTitleTop.toPx() + title.size * CIRCLE_TITLE_SP.sp.toPx() * CIRCLE_TITLE_LINE +
                                    WideBodyGap.toPx()
                            drawParagraph(
                                measurer,
                                body,
                                text.copy(fontSize = CIRCLE_BODY_SP.sp),
                                size.width / 2 - 2 * PlakatGrid.Margin.toPx(),
                                Offset(PlakatGrid.Margin.toPx(), top),
                                colors.ink,
                                BODY_LINE,
                                maxLines = bodyLines(top, size.height, CIRCLE_BODY_SP.sp.toPx()),
                            )
                        }
                    }
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(BUTTON_AT_MS))) {
                    PlakatButtonFrame(
                        PlakatButtonKind.Filled,
                        action,
                        onAction,
                        Modifier
                            .offset(PlakatGrid.Margin, layout.height - CircleButtonBottom - PlakatSizes.Button)
                            .size(WideButtonWidth, PlakatSizes.Button),
                        enabled = true,
                        loading = false,
                        interactionSource = null,
                    )
                }
            }

            PlakatPosterLayout.Stretch -> {
                Box(
                    Modifier
                        .matchParentSize()
                        .plakatWipe(PlakatWipe.Left, wipe(0))
                        .drawBehind {
                            drawRect(
                                colors.red,
                                Offset(PlakatGrid.Margin.toPx(), StretchBarTop.toPx()),
                                Size(size.width - 2 * PlakatGrid.Margin.toPx(), StretchBarHigh.toPx()),
                            )
                        },
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .plakatWipe(PlakatWipe.Left, wipe(PlakatDurations.CASCADE))
                        .drawBehind {
                            val r = StretchCircle.toPx() / 2
                            drawCircle(colors.ink, r, Offset(StretchCircleLeft.toPx() + r, StretchLowerTop.toPx() + r))
                        },
                )
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).title(title)) {
                    title.take(StretchLines.size).forEachIndexed { i, line ->
                        val (top, high) = StretchLines[i]
                        PlakatStretch(
                            line,
                            Modifier
                                .offset(PlakatGrid.Margin, top)
                                .size(layout.width - PlakatGrid.Margin * 2, high)
                                .clearAndSetSemantics {},
                        )
                    }
                }
                if (body != null) {
                    Box(
                        Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).semantics {
                            contentDescription = body
                        },
                    ) {
                        Canvas(Modifier.matchParentSize()) {
                            drawParagraph(
                                measurer,
                                body,
                                text.copy(fontSize = CIRCLE_BODY_SP.sp),
                                StretchColumn.toPx(),
                                Offset(PlakatGrid.Margin.toPx(), StretchLowerTop.toPx()),
                                colors.ink,
                                BODY_LINE,
                                maxLines = bodyLines(StretchLowerTop.toPx(), size.height, CIRCLE_BODY_SP.sp.toPx()),
                            )
                        }
                    }
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(BUTTON_AT_MS))) {
                    PlakatButtonFrame(
                        PlakatButtonKind.Filled,
                        action,
                        onAction,
                        Modifier
                            .offset(PlakatGrid.Margin, layout.height - CircleButtonBottom - PlakatSizes.Button)
                            .size(StretchColumn, PlakatSizes.Button),
                        enabled = true,
                        loading = false,
                        interactionSource = null,
                        metrics = ButtonMetrics(labelSize = STRETCH_BUTTON_SP.sp),
                    )
                }
            }

            PlakatPosterLayout.Banner -> {
                Box(
                    Modifier
                        .offset(
                            BannerWindowLeft,
                        ).size(layout.width - BannerWindowLeft, layout.height)
                        .clipToBounds(),
                ) {
                    Shapes(composition.shapes, colors, colors.paper, wipe, first = 0, shift = BannerWindowLeft)
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(TITLE_AT_MS)).title(title)) {
                    Canvas(Modifier.matchParentSize()) {
                        val origin = Offset(PlakatGrid.Margin.toPx(), PlakatGrid.Margin.toPx())
                        drawLines(
                            measurer,
                            title,
                            display.copy(fontSize = BANNER_TITLE_SP.sp),
                            PlakatFaceMetrics.Display,
                            1f,
                            origin,
                            colors,
                            registration,
                            snap = true,
                        )
                    }
                }
                Box(Modifier.matchParentSize().plakatWipe(PlakatWipe.Left, wipe(BUTTON_AT_MS))) {
                    PlakatButtonFrame(
                        PlakatButtonKind.Filled,
                        action,
                        onAction,
                        Modifier
                            .offset(PlakatGrid.Margin, layout.height - PlakatGrid.Margin - PlakatSizes.Button)
                            .size(BannerButtonWidth, PlakatSizes.Button),
                        enabled = true,
                        loading = false,
                        interactionSource = null,
                    )
                }
            }
        }
    }
}

/** How long a print lasts: the last shape's wipe, the slogan's (180 + 120), or the roll's 320. */
internal fun posterPrintMs(
    elements: Int,
    rolls: Boolean,
): Int =
    maxOf(
        (elements - 1).coerceAtLeast(0) * PlakatDurations.CASCADE + WIPE_MS,
        BUTTON_AT_MS + WIPE_MS,
        if (rolls) PlakatDurations.SCENE else 0,
    )

private fun TextStyle.plain() =
    copy(letterSpacing = 0.sp, lineHeight = TextUnit.Unspecified, lineHeightStyle = LineHeightStyle.Default)

private fun Modifier.title(title: List<String>) =
    semantics {
        heading()
        contentDescription = title.joinToString(" ")
    }

/** Multiplies its content over what is under it in the enclosing isolated group (`mix-blend-mode: multiply`). */
private fun Modifier.multiplied() =
    graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
        blendMode = BlendMode.Multiply
    }

private fun PlakatColors.of(
    role: PosterRole,
    gray: Color,
) = when (role) {
    PosterRole.Red -> red
    PosterRole.Ink -> ink
    PosterRole.Gray -> gray
}

/** The composition's shapes, each wiped in on its own, 30 ms after the one before, from index [first]. */
@Composable
private fun BoxScope.Shapes(
    shapes: List<PosterShape>,
    colors: PlakatColors,
    gray: Color,
    wipe: (Int) -> () -> Float,
    first: Int,
    shift: Dp = 0.dp,
    blend: BlendMode = DrawScope.DefaultBlendMode,
) {
    Canvas(Modifier.matchParentSize()) {
        shapes.forEachIndexed { i, s ->
            // The wipe's edge runs across the whole poster: here, from the window's own left.
            val edge =
                wipe((first + i) * PlakatDurations.CASCADE)() * PlakatPosterLayout.Circle.width.toPx() - shift.toPx()
            clipRect(right = edge) { drawPosterShape(s, colors.of(s.role, gray), blend) }
        }
    }
}

/** Круг's red circle: the photo multiplied over the red, or a raskat of red into paper at [t0]. */
@Composable
private fun PhotoCircle(
    circle: PosterShape.Round,
    colors: PlakatColors,
    photo: (@Composable BoxScope.() -> Unit)?,
    t0: (() -> Double)?,
) {
    val d = circle.d.dp
    Box(
        Modifier
            .offset(circle.x.dp, circle.y.dp)
            .size(d)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .clip(CircleShape)
            .drawBehind { drawRect(colors.red) },
    ) {
        if (t0 != null) {
            // The field is the costly part and depends only on the size: once per size, and each frame only compares
            // against it (B-51).
            val density = LocalDensity.current.density
            val px = with(LocalDensity.current) { d.toPx() }.roundToInt()
            val field =
                remember(
                    px,
                    density,
                ) { raskatField(px, px, RaskatOptions(streak = RASKAT_STREAK, dpr = density.toDouble())) }
            val roll = remember(px, colors) { RaskatRollImage(px, px, colors.red, colors.paper) }
            Canvas(Modifier.matchParentSize()) {
                roll.advance(raskatInkB(field, t0(), RASKAT_HARD))
                drawRaskat(
                    roll.image,
                    Path().apply {
                        addOval(
                            androidx.compose.ui.geometry
                                .Rect(Offset.Zero, size),
                        )
                    },
                    IntOffset.Zero,
                )
            }
        } else if (photo != null) {
            Box(Modifier.matchParentSize().multiplied()) { photo() }
        }
    }
}

/**
 * [lines] one under another as CSS line boxes of [lineHeight] × the size, each line on its own so its
 * box keeps the fraction Blink keeps (Skia would lay them on whole pixels); with [registered] (the
 * theme's registration), a red copy 1 dp right and down under the ink.
 */
private fun DrawScope.drawLines(
    measurer: TextMeasurer,
    lines: List<String>,
    style: TextStyle,
    face: FaceMetrics,
    lineHeight: Float,
    origin: Offset,
    colors: PlakatColors,
    registered: Boolean,
    snap: Boolean,
) {
    val px = style.fontSize.toPx()
    val line = px * lineHeight
    val baseline = cssBaseline(face, px, line)
    val layouts = lines.map { measurer.line(it, style, face) }
    val reg = Offset(PlakatPrint.RegistrationX.toPx(), PlakatPrint.RegistrationY.toPx())
    if (registered) {
        layouts.forEachIndexed {
            i,
            l,
            ->
            drawOnBaseline(l, face, origin.x + reg.x, origin.y + reg.y + i * line, baseline, colors.red, snap)
        }
    }
    layouts.forEachIndexed {
        i,
        l,
        ->
        drawOnBaseline(l, face, origin.x, origin.y + i * line, baseline, colors.ink, snap)
    }
}

/** Фон's title: each line on an ink stripe, `padding: 4px 10px`, 6 apart, a red stripe under each when registered. */
private fun DrawScope.drawStripes(
    measurer: TextMeasurer,
    lines: List<String>,
    style: TextStyle,
    origin: Offset,
    colors: PlakatColors,
    registered: Boolean,
) {
    val px = style.fontSize.toPx()
    val padX = StripePadX.toPx()
    val padY = StripePadY.toPx()
    val baseline = cssBaseline(PlakatFaceMetrics.Display, px, px)
    val reg = Offset(PlakatPrint.RegistrationX.toPx(), PlakatPrint.RegistrationY.toPx())
    val layouts = lines.map { measurer.line(it, style, PlakatFaceMetrics.Display) }
    val pitch = px + 2 * padY + StripeGap.toPx()
    val boxes = layouts.mapIndexed { i, l -> Size(l.getLineRight(0) + 2 * padX, px + 2 * padY) to origin.y + i * pitch }
    if (registered) {
        boxes.forEach { (size, top) -> drawRect(colors.red, Offset(origin.x, top) + reg, size) }
    }
    boxes.forEachIndexed { i, (size, top) ->
        drawRect(colors.ink, Offset(origin.x, top), size)
        drawOnBaseline(
            layouts[i],
            PlakatFaceMetrics.Display,
            origin.x + padX,
            top + padY,
            baseline,
            colors.paper,
            snap = false,
        )
    }
}
