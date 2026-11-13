package io.github.youndie.plakat.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.plakatHatchBrush
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatGrid

/**
 * The semantic kinds of `plakat/semantic`, each with the form its token gives it, so that none is told from another by
 * colour alone where the form differs.
 */
public enum class PlakatStatusKind {
    /** `primary`: a red fill. */
    Primary,

    /** `danger`: the 45° hatch of red and paper, a step of 6. */
    Danger,

    /** `success`: the ink wedge-check. */
    Success,

    /** `warning`: an ocher fill. */
    Warning,

    /** `info`: a slate fill. */
    Info,
}

// Designed here (D13, B-75): no page draws a status mark. The sign is the Tokens page's 20 px wedge-check, and the other
// kinds' forms at the same size: as tall as a badge, so a status stands in a line of text or beside a badge without
// moving it. Between the sign and the word, one step of the grid.

/** The sign's square: the Tokens page's success wedge, `width: 20px; height: 20px`, and the badge's height. */
private val SignSize = 20.dp

/** The danger hatch's period: `repeating-linear-gradient(45deg, red 0 3px, paper 3px 6px)`. */
private val HatchPeriod = 6.dp

/**
 * The wedge-check, the Tokens page's `clip-path: polygon(0 55%, 30% 55%, 40% 75%, 100% 0, 100% 30%, 40% 100%)`, as
 * fractions of its square.
 */
private val Wedge = listOf(0f to .55f, .3f to .55f, .4f to .75f, 1f to 0f, 1f to .3f, .4f to 1f)

/**
 * A status: the sign of [kind], then [word] in `label` (upper-cased, as the role is). 20 dp high, taller only when a large
 * font's word needs it; the word wraps rather than being cut. The sign is the kind's form from its token — danger's
 * hatch, success's wedge-check, a fill for the rest — so danger and primary, both red, differ without colour. Primary,
 * warning and info are each a fill: like the tokens' plates, they are told apart by colour and by the word, which is the
 * caller's.
 *
 * A reader hears [word]; the sign is drawn only. The word takes the content colour in place, else the theme's ink.
 */
@Composable
public fun PlakatStatus(
    kind: PlakatStatusKind,
    word: String,
    modifier: Modifier = Modifier,
) {
    val colors = PlakatTheme.colors
    val ink = LocalPlakatContentColor.current.takeOrElse { colors.ink }
    Row(
        modifier
            .heightIn(min = SignSize)
            .clearAndSetSemantics { contentDescription = word },
        horizontalArrangement = Arrangement.spacedBy(PlakatGrid.Step),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(SignSize)) { drawSign(kind, colors) }
        PlakatParagraph(word, PlakatTheme.typography.label, color = ink)
    }
}

/** [kind]'s sign on a square of [size]. */
internal fun DrawScope.drawSign(
    kind: PlakatStatusKind,
    colors: PlakatColors,
    size: Size = this.size,
) {
    when (kind) {
        PlakatStatusKind.Primary -> {
            drawRect(colors.red, size = size)
        }

        PlakatStatusKind.Danger -> {
            drawRect(plakatHatchBrush(size, colors.red, colors.paper, HatchPeriod.toPx()), size = size)
        }

        PlakatStatusKind.Success -> {
            val path = Path()
            Wedge.forEachIndexed { i, (x, y) ->
                if (i ==
                    0
                ) {
                    path.moveTo(x * size.width, y * size.height)
                } else {
                    path.lineTo(x * size.width, y * size.height)
                }
            }
            path.close()
            drawPath(path, colors.ink)
        }

        PlakatStatusKind.Warning -> {
            drawRect(colors.ocher, size = size)
        }

        PlakatStatusKind.Info -> {
            drawRect(colors.slate, size = size)
        }
    }
}
