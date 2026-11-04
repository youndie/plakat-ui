package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.ButtonLook
import io.github.youndie.plakat.components.PlakatButtonFrame
import io.github.youndie.plakat.components.PlakatButtonKind
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-13: the Core page's Button cells, 164 × 48 in a theme card, «ЧИТАТЬ». A pressed or focused cell is
// the page's static picture of that state, so the fixture forces the look rather than pressing.

private const val W = 164
private const val H = 48

/** The focus ring reaches 6 px past the button: the reference is cropped 6 px wider each side. */
private const val RING = 6

@Composable
private fun Cell(
    colors: PlakatColors,
    kind: PlakatButtonKind,
    enabled: Boolean = true,
    loading: Boolean = false,
    look: ButtonLook? = null,
    ring: Int = 0,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size((W + 2 * ring).dp, (H + 2 * ring).dp).background(PlakatTheme.colors.paper)) {
        PlakatButtonFrame(
            kind,
            "читать",
            {},
            Modifier.padding(ring.dp).fillMaxWidth(),
            enabled,
            loading,
            null,
            look = look,
            spinnerAtMs = 100,
        )
    }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "Button", name = "FilledDefault_Light", width = W, height = H)
@Composable
fun ButtonFilledDefaultLight() = Cell(light, PlakatButtonKind.Filled)

@ViddikScreenshot(group = "Button", name = "FilledDefault_Dark", width = W, height = H)
@Composable
fun ButtonFilledDefaultDark() = Cell(dark, PlakatButtonKind.Filled)

@ViddikScreenshot(group = "Button", name = "FilledPressed_Light", width = W, height = H)
@Composable
fun ButtonFilledPressedLight() = Cell(light, PlakatButtonKind.Filled, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Button", name = "FilledPressed_Dark", width = W, height = H)
@Composable
fun ButtonFilledPressedDark() = Cell(dark, PlakatButtonKind.Filled, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Button", name = "FilledFocused_Light", width = W + 2 * RING, height = H + 2 * RING)
@Composable
fun ButtonFilledFocusedLight() = Cell(light, PlakatButtonKind.Filled, look = ButtonLook.Focused, ring = RING)

@ViddikScreenshot(group = "Button", name = "FilledFocused_Dark", width = W + 2 * RING, height = H + 2 * RING)
@Composable
fun ButtonFilledFocusedDark() = Cell(dark, PlakatButtonKind.Filled, look = ButtonLook.Focused, ring = RING)

@ViddikScreenshot(group = "Button", name = "FilledDisabled_Light", width = W, height = H)
@Composable
fun ButtonFilledDisabledLight() = Cell(light, PlakatButtonKind.Filled, enabled = false)

@ViddikScreenshot(group = "Button", name = "FilledDisabled_Dark", width = W, height = H)
@Composable
fun ButtonFilledDisabledDark() = Cell(dark, PlakatButtonKind.Filled, enabled = false)

@ViddikScreenshot(group = "Button", name = "FilledLoading_Light", width = W, height = H)
@Composable
fun ButtonFilledLoadingLight() = Cell(light, PlakatButtonKind.Filled, loading = true)

@ViddikScreenshot(group = "Button", name = "FilledLoading_Dark", width = W, height = H)
@Composable
fun ButtonFilledLoadingDark() = Cell(dark, PlakatButtonKind.Filled, loading = true)

@ViddikScreenshot(group = "Button", name = "OutlinedDefault_Light", width = W, height = H)
@Composable
fun ButtonOutlinedDefaultLight() = Cell(light, PlakatButtonKind.Outlined)

@ViddikScreenshot(group = "Button", name = "OutlinedDefault_Dark", width = W, height = H)
@Composable
fun ButtonOutlinedDefaultDark() = Cell(dark, PlakatButtonKind.Outlined)

@ViddikScreenshot(group = "Button", name = "OutlinedPressed_Light", width = W, height = H)
@Composable
fun ButtonOutlinedPressedLight() = Cell(light, PlakatButtonKind.Outlined, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Button", name = "OutlinedPressed_Dark", width = W, height = H)
@Composable
fun ButtonOutlinedPressedDark() = Cell(dark, PlakatButtonKind.Outlined, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Button", name = "OutlinedDisabled_Light", width = W, height = H)
@Composable
fun ButtonOutlinedDisabledLight() = Cell(light, PlakatButtonKind.Outlined, enabled = false)

@ViddikScreenshot(group = "Button", name = "OutlinedDisabled_Dark", width = W, height = H)
@Composable
fun ButtonOutlinedDisabledDark() = Cell(dark, PlakatButtonKind.Outlined, enabled = false)

@ViddikScreenshot(group = "Button", name = "TextDefault_Light", width = W, height = H)
@Composable
fun ButtonTextDefaultLight() = Cell(light, PlakatButtonKind.Text)

@ViddikScreenshot(group = "Button", name = "TextDefault_Dark", width = W, height = H)
@Composable
fun ButtonTextDefaultDark() = Cell(dark, PlakatButtonKind.Text)

@ViddikScreenshot(group = "Button", name = "TextPressed_Light", width = W, height = H)
@Composable
fun ButtonTextPressedLight() = Cell(light, PlakatButtonKind.Text, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Button", name = "TextPressed_Dark", width = W, height = H)
@Composable
fun ButtonTextPressedDark() = Cell(dark, PlakatButtonKind.Text, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Button", name = "TextDisabled_Light", width = W, height = H)
@Composable
fun ButtonTextDisabledLight() = Cell(light, PlakatButtonKind.Text, enabled = false)

@ViddikScreenshot(group = "Button", name = "TextDisabled_Dark", width = W, height = H)
@Composable
fun ButtonTextDisabledDark() = Cell(dark, PlakatButtonKind.Text, enabled = false)

// The danger button has no cell on the Core page: it is the Demo page's «ВЫЙТИ ИЗ АККАУНТА» (hatch,
// word on a paper plate), with its pressed and disabled looks designed here (D13). Goldens only.

@Composable
private fun DangerCell(
    colors: PlakatColors,
    enabled: Boolean = true,
    look: ButtonLook? = null,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(240.dp, H.dp).background(PlakatTheme.colors.paper)) {
        PlakatButtonFrame(PlakatButtonKind.Danger, "выйти из аккаунта", {
        }, Modifier.fillMaxWidth(), enabled, false, null, look = look)
    }
}

@ViddikScreenshot(group = "Button", name = "DangerDefault_Light", width = 240, height = H)
@Composable
fun ButtonDangerDefaultLight() = DangerCell(light)

@ViddikScreenshot(group = "Button", name = "DangerDefault_Dark", width = 240, height = H)
@Composable
fun ButtonDangerDefaultDark() = DangerCell(dark)

@ViddikScreenshot(group = "Button", name = "DangerPressed_Light", width = 240, height = H)
@Composable
fun ButtonDangerPressedLight() = DangerCell(light, look = ButtonLook.Pressed)

@ViddikScreenshot(group = "Button", name = "DangerDisabled_Light", width = 240, height = H)
@Composable
fun ButtonDangerDisabledLight() = DangerCell(light, enabled = false)
