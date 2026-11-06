package io.github.youndie.plakat.theme

import androidx.compose.runtime.Immutable

/**
 * The words the kit's own controls carry: the accessible names of its icon actions and the words the
 * design prints beside a colour so that colour is never the only carrier of meaning (the Tokens page:
 * danger is a hatch plus «ОШИБКА», warning ochre plus «ВНИМАНИЕ», info slate plus «ИНФО»). Set once,
 * through [PlakatTheme], so an app localises them in one place. The defaults are English (B-57), and the
 * fixtures pass the design's Russian; a role in capitals upper-cases them itself.
 */
@Immutable
public class PlakatStrings(
    public val back: String = "Back",
    public val close: String = "Close",
    public val menu: String = "Menu",
    public val clear: String = "Clear",
    public val refresh: String = "Refresh",
    public val loading: String = "Loading",
    public val dismiss: String = "Dismiss",
    public val error: String = "Error",
    public val warning: String = "Warning",
    public val info: String = "Info",
)
