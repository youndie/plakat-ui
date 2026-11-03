package io.github.youndie.plakat.type

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.youndie.plakat.resources.Res
import io.github.youndie.plakat.resources.golos_text_regular
import io.github.youndie.plakat.resources.golos_text_semibold
import io.github.youndie.plakat.resources.rubik_mono_one_regular
import org.jetbrains.compose.resources.Font

/**
 * The kit's two families (research §1.4, D7): [display] for Display, Headline, buttons and numbers,
 * [body] for everything else (the Tokens page's rule 06).
 *
 * | Role | Face | Weights |
 * |---|---|---|
 * | display | Rubik Mono One, as published | 400, the only one |
 * | body | Golos Text, static instances cut by `scripts/fonts.py` | 400, 600 |
 *
 * Both carry full Cyrillic, so no run of text needs a backing face. Golos ships as two static files
 * rather than the variable one because a `wght` setting needs Android API 26.
 */
@Immutable
internal class PlakatFonts(
    val display: FontFamily,
    val body: FontFamily,
)

/** The bundled families, loaded through compose resources on every target. */
@Composable
internal fun plakatFonts(): PlakatFonts =
    PlakatFonts(
        display = FontFamily(Font(Res.font.rubik_mono_one_regular, FontWeight.Normal)),
        body =
            FontFamily(
                Font(Res.font.golos_text_regular, FontWeight.Normal),
                Font(Res.font.golos_text_semibold, FontWeight.SemiBold),
            ),
    )
