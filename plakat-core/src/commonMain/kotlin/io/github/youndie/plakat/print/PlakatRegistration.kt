package io.github.youndie.plakat.print

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPrint

/**
 * Display and Headline text printed in two inks out of register: the red layer drawn first, offset by
 * `plakat/print/registration` (1 dp right and down, research D14), the ink layer over it — the Tokens
 * page's «ЛИСТ», `position:absolute; left:1px; top:1px; color:#BE2F18` under the ink copy. Off in the
 * theme ([PlakatTheme.print]), the red layer is not drawn. Body text and icons never take it.
 */
@Composable
public fun PlakatRegisteredText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = PlakatTheme.colors.ink,
    under: Color = PlakatTheme.colors.red,
) {
    Box(modifier) {
        if (PlakatTheme.print.registration) {
            BasicText(
                text,
                Modifier.offset(PlakatPrint.RegistrationX, PlakatPrint.RegistrationY),
                style = style.copy(color = under),
            )
        }
        BasicText(text, style = style.copy(color = color))
    }
}
