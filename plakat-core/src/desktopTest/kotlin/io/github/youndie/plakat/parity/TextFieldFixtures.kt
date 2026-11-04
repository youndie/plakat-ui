package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatTextFieldFrame
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-14: the Core page's TextField cells, a 224 px column of a theme card from the top of the label to
// the cell's caption (16 px under the rule, 8 under the error message). The focused cell is the page's
// static picture of focus, so the fixture forces the look; the caret stands, as under reduced motion.

private const val W = 224
private const val H = 95
private const val ERROR_H = 149

@Composable
private fun Cell(
    colors: PlakatColors,
    text: String,
    height: Int = H,
    placeholder: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    focused: Boolean = false,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(W.dp, height.dp).background(PlakatTheme.colors.paper)) {
        PlakatTextFieldFrame(
            TextFieldValue(text, TextRange(text.length)),
            {},
            Modifier.width(W.dp),
            label = "почта",
            placeholder = placeholder,
            error = error,
            enabled = enabled,
            forceFocused = focused,
        )
    }
}

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "TextField", name = "Default_Light", width = W, height = H)
@Composable
fun TextFieldDefaultLight() = Cell(light, "", placeholder = "name@mail.ru")

@ViddikScreenshot(group = "TextField", name = "Default_Dark", width = W, height = H)
@Composable
fun TextFieldDefaultDark() = Cell(dark, "", placeholder = "name@mail.ru")

@ViddikScreenshot(group = "TextField", name = "Focused_Light", width = W, height = H)
@Composable
fun TextFieldFocusedLight() = Cell(light, "anna", focused = true)

@ViddikScreenshot(group = "TextField", name = "Focused_Dark", width = W, height = H)
@Composable
fun TextFieldFocusedDark() = Cell(dark, "anna", focused = true)

@ViddikScreenshot(group = "TextField", name = "Filled_Light", width = W, height = H)
@Composable
fun TextFieldFilledLight() = Cell(light, "anna@list.ru")

@ViddikScreenshot(group = "TextField", name = "Filled_Dark", width = W, height = H)
@Composable
fun TextFieldFilledDark() = Cell(dark, "anna@list.ru")

@ViddikScreenshot(group = "TextField", name = "Error_Light", width = W, height = ERROR_H)
@Composable
fun TextFieldErrorLight() = Cell(light, "anna@list", ERROR_H, error = "Допишите адрес целиком.")

@ViddikScreenshot(group = "TextField", name = "Error_Dark", width = W, height = ERROR_H)
@Composable
fun TextFieldErrorDark() = Cell(dark, "anna@list", ERROR_H, error = "Допишите адрес целиком.")

@ViddikScreenshot(group = "TextField", name = "Disabled_Light", width = W, height = H)
@Composable
fun TextFieldDisabledLight() = Cell(light, "anna@list.ru", enabled = false)

@ViddikScreenshot(group = "TextField", name = "Disabled_Dark", width = W, height = H)
@Composable
fun TextFieldDisabledDark() = Cell(dark, "anna@list.ru", enabled = false)
