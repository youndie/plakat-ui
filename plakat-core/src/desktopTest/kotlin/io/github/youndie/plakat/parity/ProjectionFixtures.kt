package io.github.youndie.plakat.parity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.components.ButtonMetrics
import io.github.youndie.plakat.components.PlakatButtonFrame
import io.github.youndie.plakat.components.PlakatButtonKind
import io.github.youndie.plakat.components.PlakatHornAnchor
import io.github.youndie.plakat.components.PlakatProjection
import io.github.youndie.plakat.components.PlakatProjectionAxis
import io.github.youndie.plakat.components.PlakatProjectionForm
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin

// B-32: the Stretch page's projection cells, each `Плакат / Проекция / …` the page's `frame` and `btn` round a form. The
// composition is the cell's own, not a component's: «своё слово и своя композиция». The page's `box-shadow: inset` lies under
// the canvas, whose paper covers it.

private const val CELL_H = 392
private const val POSITION_W = 260
private const val POSITION_H = 200

/** The page's `frame`, the title at 12 px and the heading lines at 32, baselines at 28 and 72 + 34 i, and the ground. */
@Composable
private fun Cell(
    head: List<String> = emptyList(),
    scenery: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit = {},
    button: Pair<String, Dp>? = null,
    content: @Composable BoxScope.() -> Unit,
) = PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true, print = PlakatPrintSwitches(texture = false)) {
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    val measurer = rememberTextMeasurer()
    Box(
        Modifier.fillMaxSize().background(colors.paper),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val body = PlakatFaceMetrics.Body
            val display = PlakatFaceMetrics.Display
            val title =
                measurer.measure(
                    "ЛИСТ · № 47",
                    type.bodyS.style
                        .copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        .cssLine(body),
                    density = Density(1f),
                    layoutDirection = LayoutDirection.Ltr,
                )
            drawText(title, colors.ink, Offset(16f, 28 - round(body.ascent * 12f)))
            head.forEachIndexed { i, line ->
                val text: TextStyle =
                    type.displayL.style
                        .copy(fontSize = 32.sp)
                        .cssLine(display)
                val laid = measurer.measure(line, text, density = Density(1f))
                drawText(laid, colors.ink, Offset(16f, 72f + i * 34 - round(display.ascent * 32f)))
            }
            drawRect(colors.ink, Offset(0f, 312f), Size(size.width, 8f))
            scenery()
        }
        content()
        if (button != null) {
            PlakatButtonFrame(
                PlakatButtonKind.Filled,
                button.first,
                {},
                Modifier.offset(16.dp, 328.dp).size(button.second, 48.dp),
                enabled = true,
                loading = false,
                interactionSource = null,
                metrics = ButtonMetrics(labelSize = 14.sp, tracked = false),
            )
        }
    }
}

@ViddikScreenshot(group = "Projection", name = "Horn_Light", width = 390, height = CELL_H)
@Composable
fun ProjectionHornLight() =
    Cell(
        listOf("ЧИТАЙ", "ВЕСЬ"),
        scenery = { drawCircle(PlakatPalettes.Paper.ink, 40f, Offset(326f, 72f)) },
        button = "ПОДПИСАТЬСЯ" to 358.dp,
    ) {
        PlakatProjection(
            listOf("НОЯБРЬ"),
            PlakatProjectionForm.Horn(16.dp, 312.dp, 358.dp, 56.dp),
            Modifier.fillMaxSize(),
            print = false,
        )
    }

@ViddikScreenshot(group = "Projection", name = "Column_Light", width = 390, height = CELL_H)
@Composable
fun ProjectionColumnLight() =
    Cell(listOf("ВЫПУСК"), button = "ЧИТАТЬ" to 200.dp) {
        PlakatProjection(
            listOf("ЛИСТ·47"),
            PlakatProjectionForm.Column(264.dp, 96.dp, 112.dp, 312.dp, 168.dp, 264.dp),
            Modifier.fillMaxSize(),
            print = false,
        )
        Box(Modifier.offset(16.dp, 232.dp).size(72.dp, 80.dp).background(PlakatTheme.colors.concrete))
    }

// The big letters' curved edges differ from the runner's macOS by 99 of 152880 px (0.06 %, channel delta 27), past the
// default 0.05 %, and no runner has the macOS the goldens were recorded on. Only this fixture, by the owner's decision.
@ViddikScreenshot(group = "Projection", name = "Box_Light", width = 390, height = CELL_H, tolerancePercent = 0.07)
@Composable
fun ProjectionBoxLight() =
    Cell(listOf("ВЫПУСК"), button = "ЧИТАТЬ" to 358.dp) {
        PlakatProjection(
            listOf("НОВОЕ"),
            PlakatProjectionForm.Box(16.dp, 152.dp, 260.dp, 160.dp, 96.dp, 184.dp, 288.dp),
            Modifier.fillMaxSize(),
            print = false,
        )
    }

@ViddikScreenshot(group = "Projection", name = "Cone_Light", width = 390, height = CELL_H)
@Composable
fun ProjectionConeLight() =
    Cell(listOf("ЭФИР", "В 19:00"), button = "НАПОМНИТЬ" to 358.dp) {
        PlakatProjection(
            listOf("СЛУШАЙ"),
            PlakatProjectionForm
                .Cone(0.dp, 0.dp, 320.dp, 20.dp, 92.dp, 16f, 62f, PlakatProjectionAxis.Tilted)
                .fittedIn(DpRect(24.dp, 120.dp, 374.dp, 304.dp)),
            Modifier.fillMaxSize(),
            print = false,
        )
    }

@ViddikScreenshot(group = "Projection", name = "Sphere_Light", width = 390, height = CELL_H)
@Composable
fun ProjectionSphereLight() =
    Cell(listOf("МИР ЗА", "НЕДЕЛЮ"), button = "ЧИТАТЬ" to 358.dp) {
        Box(Modifier.offset(16.dp, 232.dp).size(64.dp, 80.dp).background(PlakatTheme.colors.concrete))
        PlakatProjection(
            listOf("КРУГОЗОР"),
            PlakatProjectionForm.Sphere(254.dp, 210.dp, 100.dp, 72f),
            Modifier.fillMaxSize(),
            print = false,
        )
    }

/** The page's `paint` for stairs in an area of 358 × 264 at (16, 48), by its own sizes. */
private val stairs =
    run {
        val rad = 17 * kotlin.math.PI / 180
        val dx = (40 * cos(rad)).toFloat()
        val dy = (40 * sin(rad)).toFloat()
        val riser = min(96f, (264 - dy * 2) / 3)
        PlakatProjectionForm.Stairs(16.dp, 312.dp, 3, (358 - dx * 2).dp, riser.dp, 40.dp)
    }

@ViddikScreenshot(group = "Projection", name = "Stairs_Light", width = 390, height = CELL_H)
@Composable
fun ProjectionStairsLight() =
    Cell(button = "НАЧАТЬ КУРС" to 358.dp) {
        PlakatProjection(listOf("ШАГ", "ЗА", "ШАГОМ"), stairs, Modifier.fillMaxSize(), print = false)
    }

@ViddikScreenshot(group = "Projection", name = "HornLines_Light", width = 390, height = CELL_H)
@Composable
fun ProjectionHornLinesLight() =
    Cell(
        scenery = { drawCircle(PlakatPalettes.Paper.ink, 32f, Offset(326f, 64f)) },
        button = "ПОДПИСАТЬСЯ" to 358.dp,
    ) {
        PlakatProjection(
            listOf("ЧИТАЙ ВЕСЬ", "НОЯБРЬ"),
            PlakatProjectionForm
                .Horn(0.dp, 0.dp, 358.dp, 40.dp, axis = PlakatProjectionAxis.Tilted, anchor = PlakatHornAnchor.Axis)
                .fittedIn(DpRect(16.dp, 96.dp, 374.dp, 304.dp)),
            Modifier.fillMaxSize(),
            print = false,
        )
    }

/** The four positions: on the ground, or hung from the top, the horn fitted to what is left. */
@Composable
private fun Position(
    axis: PlakatProjectionAxis,
    anchor: PlakatHornAnchor,
    vertical: Alignment.Vertical,
) = PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true, print = PlakatPrintSwitches(texture = false)) {
    val colors = PlakatTheme.colors
    val ceiling = anchor == PlakatHornAnchor.Ceiling
    Box(
        Modifier.fillMaxSize().background(colors.paper).drawWithContent {
            drawRect(colors.ink, Offset(0f, if (ceiling) 0f else 192f), Size(size.width, 8f))
            drawContent()
        },
    ) {
        val top = if (ceiling) 8.dp else 12.dp
        PlakatProjection(
            listOf("НОЯБРЬ"),
            PlakatProjectionForm
                .Horn(0.dp, 0.dp, 236.dp, 28.dp, axis = axis, anchor = anchor)
                .fittedIn(DpRect(12.dp, top, 248.dp, top + 180.dp), vertical),
            Modifier.fillMaxSize(),
            print = false,
        )
    }
}

@ViddikScreenshot(group = "Projection", name = "Floor_Light", width = POSITION_W, height = POSITION_H)
@Composable
fun ProjectionFloorLight() = Position(PlakatProjectionAxis.Level, PlakatHornAnchor.Floor, Alignment.Bottom)

@ViddikScreenshot(group = "Projection", name = "Ceiling_Light", width = POSITION_W, height = POSITION_H)
@Composable
fun ProjectionCeilingLight() = Position(PlakatProjectionAxis.Level, PlakatHornAnchor.Ceiling, Alignment.Top)

@ViddikScreenshot(group = "Projection", name = "Axis_Light", width = POSITION_W, height = POSITION_H)
@Composable
fun ProjectionAxisLight() = Position(PlakatProjectionAxis.Tilted, PlakatHornAnchor.Axis, Alignment.CenterVertically)

@ViddikScreenshot(group = "Projection", name = "Upright_Light", width = POSITION_W, height = POSITION_H)
@Composable
fun ProjectionUprightLight() = Position(PlakatProjectionAxis.Upright, PlakatHornAnchor.Floor, Alignment.Bottom)
