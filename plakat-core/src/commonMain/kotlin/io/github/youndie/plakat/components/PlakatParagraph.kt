package io.github.youndie.plakat.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.isSpecified
import io.github.youndie.plakat.theme.FaceMetrics
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatText
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.math.round

/**
 * [text] set in a [role] of the type scale as the design's pages set it: each line a CSS line box of the role's line
 * height, its baseline on the pixel Chrome puts it on. `BasicText(text, style = role.style)` centres the face's
 * unrounded metrics in the line instead and lands lower — `displayM` 6 px, `headline` 4 px — so a screen of the kit's
 * own words and a consumer's sits on two baselines (B-78). The kit's components set their words this way.
 *
 * The text is upper-cased where the role asks ([PlakatText.text]) and read as it is given. It is as wide as its widest
 * line, and breaks where it is given less; given no width, it is one line as long as the text. Past [maxLines] it stops,
 * the last line ending in an ellipsis unless [overflow] is [TextOverflow.Clip]; a block never cuts a line in half.
 *
 * The baseline is placed with the metrics of the kit's two faces: a role in the display family with Rubik Mono One's, any
 * other with Golos Text's, so a role copied from [PlakatTheme.typography] keeps its place, and a family of the app's own
 * does not get one.
 *
 * @param color the text's colour; by default the content colour in place, else the theme's ink.
 */
@Composable
public fun PlakatParagraph(
    text: String,
    role: PlakatText,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val ink = color.takeOrElse { LocalPlakatContentColor.current.takeOrElse { PlakatTheme.colors.ink } }
    val face = faceOf(role)
    CssParagraph(
        text = role.text(text),
        style = role.style,
        lineHeight = lineOf(role, face),
        color = ink,
        modifier = modifier,
        face = face,
        maxLines = maxLines,
        ellipsis = overflow != TextOverflow.Clip,
        fill = false,
    )
}

/** Rubik Mono One's metrics for a role in the theme's display family, Golos Text's for any other. */
@Composable
private fun faceOf(role: PlakatText): FaceMetrics =
    if (role.style.fontFamily == PlakatTheme.typography.displayL.style.fontFamily) {
        PlakatFaceMetrics.Display
    } else {
        PlakatFaceMetrics.Body
    }

/** The role's line height as a multiple of its size; with none, CSS `normal`: the face's ascent and descent, rounded. */
private fun lineOf(
    role: PlakatText,
    face: FaceMetrics,
): Float {
    val size = role.style.fontSize.value
    val line = role.style.lineHeight
    return when {
        line.isSpecified && line.isSp -> line.value / size
        line.isSpecified && line.isEm -> line.value
        else -> (round(face.ascent * size) + round(face.descent * size)) / size
    }
}
