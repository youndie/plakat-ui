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
