package io.github.youndie.plakat.print

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/** B-10: a print layer lays its tile edge to edge from the element's corner, over the content and only over it. */
@OptIn(ExperimentalTestApi::class)
class PlakatPrintLayerTest {
    @Test
    fun `the tile repeats from the top-left over the content and stops at the box`() {
        // A 2 × 2 tile: black at its top-left pixel, transparent elsewhere.
        val tile = ImageBitmap(2, 2)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(tile), Size(2f, 2f)) {
            drawRect(Color.Black, size = Size(1f, 1f))
        }
        runComposeUiTest {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    Box(Modifier.size(12.dp).background(Color.White).testTag("outer")) {
                        Box(
                            Modifier
                                .padding(1.dp)
                                .size(9.dp)
                                .background(Color.White)
                                .plakatPrintLayer(tile, 2.dp, 1f, BlendMode.SrcOver),
                        )
                    }
                }
            }
            val px = onNodeWithTag("outer").captureToImage().toPixelMap()
            for (y in 0 until 12) {
                for (x in 0 until 12) {
                    val inBox = x in 1..9 && y in 1..9
                    val dot = inBox && (x - 1) % 2 == 0 && (y - 1) % 2 == 0
                    assertEquals(if (dot) Color.Black else Color.White, px[x, y], "pixel $x, $y")
                }
            }
        }
    }
}
