package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.print.PlakatCutShape
import io.github.youndie.plakat.print.PlakatRegisteredText
import io.github.youndie.plakat.print.plakatHatch
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-10: the print primitives against the cells that draw them alone. Each fixture is the reference's
// crop of a larger element, so the element is laid out at its page size and shifted under a clip.

/** [width] × [height] of an element [fullWidth] wide whose part from [cropX] is shown. */
@Composable
private fun Crop(
    width: Int,
    height: Int,
    fullWidth: Int,
    cropX: Int,
    ground: Color,
    element: Modifier,
) {
    Box(Modifier.size(width.dp, height.dp).clipToBounds().background(ground)) {
        Box(
            Modifier
                .wrapContentWidth(Alignment.Start, unbounded = true)
                .offset(x = (-cropX).dp)
                .size(fullWidth.dp, height.dp)
                .then(element),
        )
    }
}

// The Core page's filled plate, 164 × 48 in its card; its left 40 px hold the cut and no text.
@ViddikScreenshot(group = "Cut", name = "Plate_Light", width = 40, height = 48)
@Composable
fun CutPlateLight() =
    Crop(40, 48, 164, 0, PlakatPalettes.Paper.paper, Modifier.background(PlakatPalettes.Paper.red, PlakatCutShape()))

@ViddikScreenshot(group = "Cut", name = "Plate_Dark", width = 40, height = 48)
@Composable
fun CutPlateDark() =
    Crop(
        40,
        48,
        164,
        0,
        PlakatPalettes.BlackPaper.paper,
        Modifier.background(PlakatPalettes.BlackPaper.red, PlakatCutShape()),
    )

// The Tokens page's danger swatch, 216 × 48, `repeating-linear-gradient(45deg, #BE2F18 0 3px, #EEE5D1
// 3px 6px)`; from 120 px on it is hatch alone, past the swatch's word.
@ViddikScreenshot(group = "Hatch", name = "Danger_Light", width = 96, height = 48)
@Composable
fun HatchDangerLight() =
    Crop(
        96,
        48,
        216,
        120,
        PlakatPalettes.Paper.paper,
        Modifier.plakatHatch(PlakatPalettes.Paper.red, PlakatPalettes.Paper.paper),
    )

// The Tokens page's registration cell: «ЛИСТ» in Rubik Mono One 40 px, ink over red moved 1 px right
// and down, centred on a 272 × 140 concrete panel.
@ViddikScreenshot(group = "Registration", name = "List_Light", width = 272, height = 140)
@Composable
fun RegistrationListLight() =
    PlakatTheme {
        Box(
            Modifier.size(272.dp, 140.dp).background(PlakatTheme.colors.concrete),
            contentAlignment = Alignment.Center,
        ) {
            PlakatRegisteredText(
                "ЛИСТ",
                TextStyle(fontFamily = PlakatTheme.typography.headline.style.fontFamily, fontSize = 40.sp),
            )
        }
    }
