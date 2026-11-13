package io.github.youndie.plakat.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/** The icons' grid: 24 units, drawn at 24 dp (`plakat-icons.js`: "24 dp grid, live area 2–22"). */
private const val GRID = 24f

/**
 * A stencil icon: [pathData], an SVG path on the 24-unit grid, filled nonzero, holes counter-clockwise.
 * [name] names it, `plakat/icon/<id>` for the kit's; an app's own is any name of its own
 * (`app/icon/<id>`). [PlakatIcons] holds the kit's; an app makes one more with this constructor and
 * draws it with [PlakatIcon] or a [io.github.youndie.plakat.components.PlakatIconButton], which size,
 * tint and press it as the kit's own (B-77).
 *
 * The path is a fill, never a stroke, of absolute or relative moves, lines (`M L H V Z`) and arcs with
 * no rotation; anything else is refused when it is first drawn. A stroke is outlined to a fill first,
 * as `scripts/icons.mjs` does for the kit's `OpensElsewhere`.
 *
 * Not an `ImageVector`: Compose's vector parser turns an SVG arc into cubics, which Skia flattens
 * differently from the conics Chrome draws an arc with, and every round icon then differed from its
 * cell by up to 81 levels on its edge. A stencil draws its arcs as conics, as the design's page does
 * (B-11).
 */
@Immutable
public class PlakatStencil(
    public val name: String,
    public val pathData: String,
) {
    internal val path: Path by lazy { stencilPath(pathData) }

    override fun toString(): String = name
}

/**
 * Draws [stencil] in [tint]: by default the colour of the content it sits in ([LocalPlakatContentColor],
 * which a filled button sets to its on-colour), the theme's ink outside any. 24 dp unless [modifier]
 * sizes it; the design draws the set at 48, 24 and 16.
 */
@Composable
public fun PlakatIcon(
    stencil: PlakatStencil,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalPlakatContentColor.current.takeOrElse { PlakatTheme.colors.ink },
) {
    val described =
        if (contentDescription == null) {
            Modifier
        } else {
            Modifier.semantics {
                this.contentDescription = contentDescription
                role = Role.Image
            }
        }
    Box(
        modifier
            .size(GRID.dp)
            .then(described)
            .drawBehind {
                scale(size.width / GRID, size.height / GRID, Offset.Zero) { drawPath(stencil.path, tint) }
            },
    )
}

/**
 * [d] as a path whose arcs are `arcTo`s, conics on Skia. The icons use moves, lines and unrotated
 * arcs only (scripts/icons.mjs refuses anything else), so nothing else is handled.
 */
internal fun stencilPath(d: String): Path {
    val path = Path()
    var x = 0f
    var y = 0f
    var startX = 0f
    var startY = 0f

    fun arcTo(
        rx: Float,
        ry: Float,
        rotation: Float,
        large: Boolean,
        sweep: Boolean,
        toX: Float,
        toY: Float,
    ) {
        require(rotation == 0f) { "a rotated arc in $d" }
        // SVG 1.1, F.6.5: the endpoint form to the centre form, for an unrotated ellipse.
        val hx = (x - toX) / 2
        val hy = (y - toY) / 2
        var a = abs(rx)
        var b = abs(ry)
        val lambda = hx * hx / (a * a) + hy * hy / (b * b)
        if (lambda > 1) {
            a *= sqrt(lambda)
            b *= sqrt(lambda)
        }
        val num = a * a * b * b - a * a * hy * hy - b * b * hx * hx
        val den = a * a * hy * hy + b * b * hx * hx
        val k = sqrt(maxOf(0f, num / den)) * if (large == sweep) -1 else 1
        val cx = k * a * hy / b + (x + toX) / 2
        val cy = -k * b * hx / a + (y + toY) / 2
        val from = atan2((y - cy) / b, (x - cx) / a)
        var turn = atan2((toY - cy) / b, (toX - cx) / a) - from
        if (sweep && turn < 0) turn += (2 * PI).toFloat()
        if (!sweep && turn > 0) turn -= (2 * PI).toFloat()
        path.arcTo(Rect(cx - a, cy - b, cx + a, cy + b), degrees(from), degrees(turn), false)
        x = toX
        y = toY
    }

    fun moveTo(
        toX: Float,
        toY: Float,
    ) {
        x = toX
        y = toY
        startX = toX
        startY = toY
        path.moveTo(toX, toY)
    }

    fun lineTo(
        toX: Float,
        toY: Float,
    ) {
        x = toX
        y = toY
        path.lineTo(toX, toY)
    }

    for (node in addPathNodes(d)) {
        when (node) {
            is PathNode.MoveTo -> {
                moveTo(node.x, node.y)
            }

            is PathNode.RelativeMoveTo -> {
                moveTo(x + node.dx, y + node.dy)
            }

            is PathNode.LineTo -> {
                lineTo(node.x, node.y)
            }

            is PathNode.RelativeLineTo -> {
                lineTo(x + node.dx, y + node.dy)
            }

            is PathNode.HorizontalTo -> {
                lineTo(node.x, y)
            }

            is PathNode.RelativeHorizontalTo -> {
                lineTo(x + node.dx, y)
            }

            is PathNode.VerticalTo -> {
                lineTo(x, node.y)
            }

            is PathNode.RelativeVerticalTo -> {
                lineTo(x, y + node.dy)
            }

            is PathNode.ArcTo -> {
                arcTo(
                    node.horizontalEllipseRadius,
                    node.verticalEllipseRadius,
                    node.theta,
                    node.isMoreThanHalf,
                    node.isPositiveArc,
                    node.arcStartX,
                    node.arcStartY,
                )
            }

            is PathNode.RelativeArcTo -> {
                arcTo(
                    node.horizontalEllipseRadius,
                    node.verticalEllipseRadius,
                    node.theta,
                    node.isMoreThanHalf,
                    node.isPositiveArc,
                    x + node.arcStartDx,
                    y + node.arcStartDy,
                )
            }

            PathNode.Close -> {
                path.close()
                x = startX
                y = startY
            }

            else -> {
                error("$node in $d: a stencil has moves, lines and arcs only")
            }
        }
    }
    return path
}

private fun degrees(radians: Float): Float = (radians * 180 / PI).toFloat()
