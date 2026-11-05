package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.agit.PlakatGeneratedImage
import io.github.youndie.plakat.agit.PlakatPreset
import io.github.youndie.plakat.agit.PlakatPresetImage
import io.github.youndie.plakat.agit.PlakatRapportFill
import io.github.youndie.plakat.agit.PlakatSkeleton
import io.github.youndie.plakat.agit.plakatHalftone
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot
import kotlin.math.min

// B-33: the presets and rapports against their own SVGs, the Agit page's compositions, its skeleton and photomontage.

@Composable
private fun Preset(
    preset: PlakatPreset,
    colors: PlakatColors,
) = PlakatTheme(colors = colors, reduceMotion = true) { PlakatPresetImage(preset, Modifier.fillMaxSize()) }

@ViddikScreenshot(group = "Preset", name = "Voshod_Light", width = 360, height = 240)
@Composable
fun PresetVoshodLight() = Preset(PlakatPreset.Voshod, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Preset", name = "Voshod_Dark", width = 360, height = 240)
@Composable
fun PresetVoshodDark() = Preset(PlakatPreset.Voshod, PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Preset", name = "Klin_Light", width = 360, height = 240)
@Composable
fun PresetKlinLight() = Preset(PlakatPreset.Klin, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Preset", name = "Klin_Dark", width = 360, height = 240)
@Composable
fun PresetKlinDark() = Preset(PlakatPreset.Klin, PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Preset", name = "Kolonny_Light", width = 360, height = 240)
@Composable
fun PresetKolonnyLight() = Preset(PlakatPreset.Kolonny, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Preset", name = "Kolonny_Dark", width = 360, height = 240)
@Composable
fun PresetKolonnyDark() = Preset(PlakatPreset.Kolonny, PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Preset", name = "Os_Light", width = 360, height = 240)
@Composable
fun PresetOsLight() = Preset(PlakatPreset.Os, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Preset", name = "Os_Dark", width = 360, height = 240)
@Composable
fun PresetOsDark() = Preset(PlakatPreset.Os, PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Preset", name = "Shag_Light", width = 360, height = 240)
@Composable
fun PresetShagLight() = Preset(PlakatPreset.Shag, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Preset", name = "Shag_Dark", width = 360, height = 240)
@Composable
fun PresetShagDark() = Preset(PlakatPreset.Shag, PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Preset", name = "Razryv_Light", width = 360, height = 240)
@Composable
fun PresetRazryvLight() = Preset(PlakatPreset.Razryv, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Preset", name = "Razryv_Dark", width = 360, height = 240)
@Composable
fun PresetRazryvDark() = Preset(PlakatPreset.Razryv, PlakatPalettes.BlackPaper)

@Composable
private fun Rapport(
    seed: Int,
    colors: PlakatColors,
) = PlakatTheme(colors = colors, reduceMotion = true) { PlakatRapportFill(seed, Modifier.fillMaxSize()) }

@ViddikScreenshot(group = "Rapport", name = "S1923_Light", width = 192, height = 144)
@Composable
fun Rapport1923Light() = Rapport(1923, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Rapport", name = "S1923_Dark", width = 192, height = 144)
@Composable
fun Rapport1923Dark() = Rapport(1923, PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Rapport", name = "S1925_Light", width = 96, height = 144)
@Composable
fun Rapport1925Light() = Rapport(1925, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Rapport", name = "S1925_Dark", width = 96, height = 144)
@Composable
fun Rapport1925Dark() = Rapport(1925, PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Rapport", name = "S1927_Light", width = 144, height = 96)
@Composable
fun Rapport1927Light() = Rapport(1927, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Rapport", name = "S1927_Dark", width = 144, height = 96)
@Composable
fun Rapport1927Dark() = Rapport(1927, PlakatPalettes.BlackPaper)

@ViddikScreenshot(group = "Rapport", name = "S1929_Light", width = 144, height = 144)
@Composable
fun Rapport1929Light() = Rapport(1929, PlakatPalettes.Paper)

@ViddikScreenshot(group = "Rapport", name = "S1929_Dark", width = 144, height = 144)
@Composable
fun Rapport1929Dark() = Rapport(1929, PlakatPalettes.BlackPaper)

@Composable
private fun Composition(seed: Int) =
    PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) {
        PlakatGeneratedImage(seed, Modifier.fillMaxSize())
    }

@ViddikScreenshot(group = "Composition", name = "S1919", width = 180, height = 120)
@Composable
fun Composition1919() = Composition(1919)

@ViddikScreenshot(group = "Composition", name = "S1920", width = 180, height = 120)
@Composable
fun Composition1920() = Composition(1920)

@ViddikScreenshot(group = "Composition", name = "S1921", width = 180, height = 120)
@Composable
fun Composition1921() = Composition(1921)

@ViddikScreenshot(group = "Composition", name = "S1922", width = 180, height = 120)
@Composable
fun Composition1922() = Composition(1922)

@ViddikScreenshot(group = "Skeleton", name = "Pattern", width = 64, height = 64)
@Composable
fun SkeletonPattern() =
    PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) { PlakatSkeleton(1923, Modifier.fillMaxSize()) }

/** The page's `standIn(680, 840, fx, fy)`: a radial gradient of 680 × .62 from dark to light. */
private fun standIn(
    fx: Float,
    fy: Float,
): ImageBitmap {
    val w = 680
    val h = 840
    val image = ImageBitmap(w, h)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(w.toFloat(), h.toFloat())) {
        drawRect(
            Brush.radialGradient(
                0f to Color(0xFF151515),
                .3f to Color(0xFF202020),
                .55f to Color(0xFF8A8A8A),
                .8f to Color(0xFFDCDCDC),
                1f to Color(0xFFF4F4F4),
                center = Offset(w * fx, h * fy),
                radius = min(w, h) * .62f,
            ),
        )
    }
    return image
}

@Composable
private fun Montage(content: @Composable () -> Unit) =
    PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) {
        Box(Modifier.fillMaxSize().background(PlakatTheme.colors.paper)) { content() }
    }

@ViddikScreenshot(group = "Photomontage", name = "Circle", width = 340, height = 420)
@Composable
fun PhotomontageCircle() =
    Montage {
        val colors = PlakatTheme.colors
        Box(
            Modifier
                .offset(20.dp, 60.dp)
                .size(300.dp)
                .clip(CircleShape)
                .plakatHalftone(standIn(.55f, .4f), colors.red, colors.paper),
        )
    }

@ViddikScreenshot(group = "Photomontage", name = "Tilt", width = 340, height = 420)
@Composable
fun PhotomontageTilt() =
    Montage {
        val colors = PlakatTheme.colors
        Box(
            Modifier
                .offset(60.dp, 60.dp)
                .size(220.dp, 300.dp)
                .rotate(-17f)
                .plakatHalftone(standIn(.5f, .34f), colors.ink, colors.paper),
        )
    }

@ViddikScreenshot(group = "Photomontage", name = "Cut", width = 340, height = 420)
@Composable
fun PhotomontageCut() =
    Montage {
        val colors = PlakatTheme.colors
        Box(
            Modifier
                .size(340.dp, 420.dp)
                .clip(
                    GenericShape { size, _ ->
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, 296f)
                        lineTo(0f, 400f)
                        close()
                    },
                ).plakatHalftone(standIn(.6f, .36f), colors.red, colors.paper),
        )
    }
