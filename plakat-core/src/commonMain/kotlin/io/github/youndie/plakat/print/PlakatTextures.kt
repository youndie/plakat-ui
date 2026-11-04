package io.github.youndie.plakat.print

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.resources.Res
import io.github.youndie.plakat.resources.concrete_light
import io.github.youndie.plakat.resources.nonprint_mask
import io.github.youndie.plakat.resources.paper_dark
import io.github.youndie.plakat.resources.paper_grain
import io.github.youndie.plakat.resources.paper_light
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.plakat.tokens.PlakatPrint
import org.jetbrains.compose.resources.imageResource

// B-07: the design's texture tiles (its `textures/`). paper-dark and nonprint-mask are as the designer
// delivered them; paper-light, concrete-light and paper-grain are made from paper-dark by
// scripts/textures.py (research D16). Every layer is switched by the theme's print switches.

/** The tiles are @2x: 512 px is 256 dp (the Textures page, "512 px = 256 dp"). */
private const val TILE_PX_PER_DP = 2

private val ImageBitmap.tileSize: Dp get() = (width / TILE_PX_PER_DP).dp

/** On the web a resource arrives a frame late, as an empty bitmap; until then no layer is drawn. */
private fun ImageBitmap.loaded(): ImageBitmap? = takeIf { width >= TILE_PX_PER_DP }

/**
 * The theme's paper as the element's ground, under its content: with the texture on, the opaque tile
 * of the theme (`paper-light` under «Бумага», `paper-dark` under «Чёрная бумага»); off, the flat paper.
 */
@Composable
public fun Modifier.plakatPaper(): Modifier {
    val colors = PlakatTheme.colors
    val tile =
        when (colors.paper) {
            PlakatPalettes.Paper.paper -> imageResource(Res.drawable.paper_light)
            PlakatPalettes.BlackPaper.paper -> imageResource(Res.drawable.paper_dark)
            else -> null
        }
    return plakatGround(colors.paper, tile?.loaded()?.takeIf { PlakatTheme.print.texture })
}

/**
 * The theme's concrete (cards, panels) as the element's ground: the opaque `concrete-light` tile under
 * «Бумага» with the texture on; flat otherwise. The design has no dark concrete tile, so «Чёрная
 * бумага» concrete stays flat.
 */
@Composable
public fun Modifier.plakatConcrete(): Modifier {
    val colors = PlakatTheme.colors
    val tile =
        if (colors.concrete ==
            PlakatPalettes.Paper.concrete
        ) {
            imageResource(Res.drawable.concrete_light)
        } else {
            null
        }
    return plakatGround(colors.concrete, tile?.loaded()?.takeIf { PlakatTheme.print.texture })
}

/**
 * The paper's grain over a flat ink (a plate, a raskat): `paper-grain` multiplied over the content at
 * the token's opacity (`plakat/print/paper-texture`, 0.45), while the texture is on.
 */
@Composable
public fun Modifier.plakatGrain(): Modifier {
    val tile = imageResource(Res.drawable.paper_grain).loaded()
    if (tile == null || !PlakatTheme.print.texture) return this
    return plakatPrintLayer(tile, tile.tileSize, PlakatPrint.PAPER_TEXTURE_OPACITY, BlendMode.Multiply)
}

/**
 * The non-print: cream spots where the red did not take, over a red plate's content at the token's
 * opacity (`plakat/print/nonprint`, 0.55), while the non-print is on.
 */
@Composable
public fun Modifier.plakatNonprint(): Modifier {
    val tile = imageResource(Res.drawable.nonprint_mask).loaded()
    if (tile == null || !PlakatTheme.print.nonprint) return this
    return plakatPrintLayer(tile, tile.tileSize, PlakatPrint.NONPRINT_OPACITY, BlendMode.SrcOver)
}

private fun Modifier.plakatGround(
    color: Color,
    tile: ImageBitmap?,
): Modifier =
    drawBehind {
        drawRect(color)
        if (tile != null) drawTiles(tile, tile.tileSize, 1f, BlendMode.SrcOver)
    }
