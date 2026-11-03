package io.github.youndie.plakat.theme

import androidx.compose.runtime.Immutable

/**
 * The words the kit's own controls carry: the accessible names of its icon actions and the words the
 * design prints beside a colour so that colour is never the only carrier of meaning (the Tokens page:
 * danger is a hatch plus «ОШИБКА», warning ochre plus «ВНИМАНИЕ», info slate plus «ИНФО»). Set once,
 * through [PlakatTheme], so an app localises them in one place. The defaults are the design's Russian;
 * a role in capitals upper-cases them itself.
 */
@Immutable
public class PlakatStrings(
    public val back: String = "Назад",
    public val close: String = "Закрыть",
    public val menu: String = "Меню",
    public val clear: String = "Очистить",
    public val refresh: String = "Обновить",
    public val loading: String = "Загрузка",
    public val dismiss: String = "Убрать",
    public val error: String = "Ошибка",
    public val warning: String = "Внимание",
    public val info: String = "Инфо",
)
