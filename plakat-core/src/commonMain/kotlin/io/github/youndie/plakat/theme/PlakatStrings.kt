package io.github.youndie.plakat.theme

import androidx.compose.runtime.Immutable

/**
 * The words the kit's own controls carry: what a pull-to-refresh and a loading control say to a reader,
 * and the word the design prints beside the hatch so that colour is never the only carrier of meaning
 * (the Tokens page: danger is a hatch plus «ОШИБКА»). Set once, through [PlakatTheme], so an app
 * localises them in one place. The defaults are English (B-57), and the fixtures pass the design's
 * Russian; a role in capitals upper-cases them itself.
 */
@Immutable
public class PlakatStrings(
    public val refresh: String = "Refresh",
    public val loading: String = "Loading",
    public val error: String = "Error",
)
