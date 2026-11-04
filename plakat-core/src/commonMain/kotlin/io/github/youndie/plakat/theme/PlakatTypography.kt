package io.github.youndie.plakat.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.tokens.PlakatFamily
import io.github.youndie.plakat.tokens.PlakatTypeRole
import io.github.youndie.plakat.tokens.PlakatTypeScale
import io.github.youndie.plakat.type.PlakatFonts
import io.github.youndie.plakat.type.plakatFonts

/**
 * One role of the type scale as Compose draws it: its [style], and whether the role is set in
 * capitals. Compose has no `text-transform`, so a component sets its text through [text], which
 * upper-cases it where the role asks (Display, Headline, Label, buttons — never body text).
 */
@Immutable
public class PlakatText(
    public val style: TextStyle,
    public val upperCase: Boolean,
) {
    /** [value] as this role sets it. */
    public fun text(value: String): String = if (upperCase) value.uppercase() else value
}

/** The type scale (`plakat/type`) in the kit's two families. */
@Immutable
public class PlakatTypography internal constructor(
    public val displayL: PlakatText,
    public val displayM: PlakatText,
    public val headline: PlakatText,
    public val title: PlakatText,
    public val bodyL: PlakatText,
    public val bodyM: PlakatText,
    public val bodyS: PlakatText,
    public val label: PlakatText,
    public val button: PlakatText,
)

/**
 * A role as a text style: size in sp, the CSS line height as a multiple of it, the half-leading
 * split evenly above and below as CSS does, tracking in em.
 */
internal fun PlakatTypeRole.toText(fonts: PlakatFonts): PlakatText =
    PlakatText(
        TextStyle(
            fontFamily = if (family == PlakatFamily.Display) fonts.display else fonts.body,
            fontWeight = FontWeight(weight),
            fontSize = size.sp,
            lineHeight = (size * lineHeight).sp,
            letterSpacing = tracking.em,
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
        ),
        upperCase,
    )

@Composable
internal fun plakatTypography(): PlakatTypography {
    val fonts = plakatFonts()
    return with(PlakatTypeScale) {
        PlakatTypography(
            displayL = DisplayL.toText(fonts),
            displayM = DisplayM.toText(fonts),
            headline = Headline.toText(fonts),
            title = Title.toText(fonts),
            bodyL = BodyL.toText(fonts),
            bodyM = BodyM.toText(fonts),
            bodyS = BodyS.toText(fonts),
            label = Label.toText(fonts),
            button = Button.toText(fonts),
        )
    }
}

/**
 * A face's vertical metrics (`hhea`, per em), which Blink rounds and Compose does not. Held to the
 * bundled files by `PlakatFontMetricsTest`.
 */
internal class FaceMetrics(
    val ascent: Float,
    val descent: Float,
)

internal object PlakatFaceMetrics {
    /** Golos Text, both cuts: ascender 980, descender −220 of 1000. */
    val Body = FaceMetrics(.98f, .22f)

    /** Rubik Mono One: ascender 932, descender −306 of 1000. */
    val Display = FaceMetrics(.932f, .306f)
}

/**
 * This style laid out as Blink lays a CSS line box, so a text's baseline lands on Chrome's pixel.
 * [lineHeight] is the CSS `line-height` as a multiple of the size, null for `normal`.
 *
 * Blink rounds the ascent and the descent to whole pixels each, takes `normal` as their sum, and
 * floors the half-leading; Compose centres the face's unrounded metrics in the line. Each puts the
 * baseline in a different pixel (a field's text, an error message: a pixel low). The line height is
 * Blink's, and the share of the leading above the text is chosen so the baseline falls where Blink's
 * does (cmp-ui-parity's catalogue, "Blink rounds ascent and descent").
 */
internal fun TextStyle.cssLine(
    face: FaceMetrics,
    lineHeight: Float? = null,
): TextStyle {
    val px = fontSize.value
    val ascent = face.ascent * px
    val content = kotlin.math.round(ascent) + kotlin.math.round(face.descent * px)
    val line = lineHeight?.let { it * px } ?: content
    val baseline = kotlin.math.floor((line - content) / 2) + kotlin.math.round(ascent)
    val leading = line - (face.ascent + face.descent) * px
    val ratio = if (leading <= 0.01f) 0f else ((baseline - ascent) / leading).coerceIn(0f, 1f)
    return copy(
        lineHeight = line.sp,
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment(ratio), LineHeightStyle.Trim.None),
    )
}
