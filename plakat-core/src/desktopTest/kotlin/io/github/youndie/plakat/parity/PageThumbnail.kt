package io.github.youndie.plakat.parity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.agit.PosterRecipes
import io.github.youndie.plakat.agit.PosterRole
import io.github.youndie.plakat.agit.drawPosterShape
import io.github.youndie.plakat.agit.posterComposition
import io.github.youndie.plakat.theme.PlakatTheme

// B-17: the Core page's 64 × 64 card thumbnail, its own generator's `comp(47 · 7 + i · 131, 64, 64,
// …, { dot: .4 })` (ported in plakat-core since B-19), as the Card cells' demo content.

/** The page's thumbnail for card [index]: paper, and the page's shapes in their order. */
@Composable
internal fun PageThumbnail(index: Int) {
    val colors = PlakatTheme.colors
    val shapes = posterComposition(47 * 7 + index * 131, PosterRecipes.Thumbnail).shapes
    Box(Modifier.size(64.dp).clipToBounds().background(colors.paper)) {
        Canvas(Modifier.size(64.dp)) {
            for (s in shapes) {
                val color =
                    when (s.role) {
                        PosterRole.Red -> colors.red
                        PosterRole.Ink -> colors.ink
                        PosterRole.Gray -> colors.concrete
                    }
                drawPosterShape(s, color)
            }
        }
    }
}
