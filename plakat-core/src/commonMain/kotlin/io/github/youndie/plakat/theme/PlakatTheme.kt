package io.github.youndie.plakat.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.plakat.tokens.PlakatPrint

/**
 * The print layers a theme switches (`plakat/print`): each is a separate layer over a flat form, and
 * off it leaves the form as it is. [raskat] off prints a raskat shape in its first ink.
 */
@Immutable
public class PlakatPrintSwitches(
    public val texture: Boolean = PlakatPrint.PAPER_TEXTURE_ON,
    public val nonprint: Boolean = PlakatPrint.NONPRINT_ON,
    public val registration: Boolean = PlakatPrint.REGISTRATION_ON,
    public val raskat: Boolean = PlakatPrint.RASKAT_ON,
)

/**
 * The colour of the content in place, the pages' CSS `currentColor`: a component that fills a surface
 * provides the ink that prints on it, and icons and text inside take it. Unspecified outside any,
 * where they take the theme's ink.
 */
public val LocalPlakatContentColor: ProvidableCompositionLocal<Color> = compositionLocalOf { Color.Unspecified }

private val LocalPlakatColors = staticCompositionLocalOf { PlakatPalettes.Paper }
private val LocalPlakatTypography = staticCompositionLocalOf<PlakatTypography?> { null }
private val LocalPlakatReduceMotion = staticCompositionLocalOf { false }
private val LocalPlakatPrint = staticCompositionLocalOf { PlakatPrintSwitches() }
private val LocalPlakatStrings = staticCompositionLocalOf { PlakatStrings() }

/**
 * The kit's theme. [colors] picks the paper: [PlakatPalettes.Paper] or [PlakatPalettes.BlackPaper].
 *
 * @param reduceMotion each animation switches to its quiet variant, as its spec row says (research
 *   D9). `null`, the default, follows the platform's setting; a fixture or a screen passes it to
 *   force one. Components read it from [PlakatTheme.reduceMotion], never from the platform.
 * @param print the print layers; texture, non-print, registration and raskat are on by default, as
 *   the design ships them.
 * @param strings the words of the kit's own controls, for localisation.
 */
@Composable
public fun PlakatTheme(
    colors: PlakatColors = PlakatPalettes.Paper,
    reduceMotion: Boolean? = null,
    print: PlakatPrintSwitches = PlakatPrintSwitches(),
    strings: PlakatStrings = PlakatStrings(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPlakatColors provides colors,
        LocalPlakatTypography provides plakatTypography(),
        LocalPlakatReduceMotion provides (reduceMotion ?: platformReducedMotion()),
        LocalPlakatPrint provides print,
        LocalPlakatStrings provides strings,
        content = content,
    )
}

/** What the nearest [PlakatTheme] provides. */
public object PlakatTheme {
    public val colors: PlakatColors
        @Composable @ReadOnlyComposable
        get() = LocalPlakatColors.current

    public val typography: PlakatTypography
        @Composable @ReadOnlyComposable
        get() = checkNotNull(LocalPlakatTypography.current) { "plakat-ui components need a PlakatTheme above them" }

    public val reduceMotion: Boolean
        @Composable @ReadOnlyComposable
        get() = LocalPlakatReduceMotion.current

    public val print: PlakatPrintSwitches
        @Composable @ReadOnlyComposable
        get() = LocalPlakatPrint.current

    public val strings: PlakatStrings
        @Composable @ReadOnlyComposable
        get() = LocalPlakatStrings.current
}
