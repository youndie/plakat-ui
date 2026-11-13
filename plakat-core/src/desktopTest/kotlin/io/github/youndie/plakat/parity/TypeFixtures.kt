package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatParagraph
import io.github.youndie.plakat.theme.PlakatText
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.PlakatTypography
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-78: the type scale's specimens on the Tokens page, each set by `PlakatParagraph` in its role, ink on paper. The
// references are the element after each role's caption, label and button given the role's line height (the page
// leaves them `normal`); the page draws no body/l.

/** A role's specimen: its reference's stem, the role, the page's words, and the reference's size. */
internal class TypeSpecimen(
    val name: String,
    val role: (PlakatTypography) -> PlakatText,
    val text: String,
    val width: Int,
    val height: Int,
)

internal val TYPE_SPECIMENS =
    listOf(
        TypeSpecimen("DisplayL", { it.displayL }, "ЧИТАЙ", 916, 53),
        TypeSpecimen("DisplayM", { it.displayM }, "ВЕСЬ НОЯБРЬ", 916, 45),
        TypeSpecimen("Headline", { it.headline }, "СВЕЖИЙ ВЫПУСК", 916, 32),
        TypeSpecimen("Title", { it.title }, "Город из фанеры: как строят временные павильоны", 916, 25),
        TypeSpecimen(
            "BodyM",
            { it.bodyM },
            "Павильон стоит три месяца, потом его разбирают и везут в другой город. Мы спросили архитекторов, " +
                "как это устроено. Latin: Plywood city.",
            560,
            67,
        ),
        TypeSpecimen("BodyS", { it.bodyS }, "Анна Ветрова · 8 минут чтения", 916, 20),
        TypeSpecimen("Label", { it.label }, "АРХИТЕКТУРА · 8 МИН", 916, 14),
        TypeSpecimen("Button", { it.button }, "ПОДПИСАТЬСЯ", 916, 14),
    )

/** [specimen] on paper at its reference's size, set by `PlakatParagraph`. */
@Composable
internal fun TypeCell(specimen: TypeSpecimen) =
    PlakatTheme {
        Box(Modifier.size(specimen.width.dp, specimen.height.dp).background(PlakatTheme.colors.paper)) {
            PlakatParagraph(specimen.text, specimen.role(PlakatTheme.typography))
        }
    }

private fun specimen(name: String) = TYPE_SPECIMENS.first { it.name == name }

@ViddikScreenshot(group = "Type", name = "DisplayL_Light", width = 916, height = 53)
@Composable
fun TypeDisplayLLight() = TypeCell(specimen("DisplayL"))

@ViddikScreenshot(group = "Type", name = "DisplayM_Light", width = 916, height = 45)
@Composable
fun TypeDisplayMLight() = TypeCell(specimen("DisplayM"))

@ViddikScreenshot(group = "Type", name = "Headline_Light", width = 916, height = 32)
@Composable
fun TypeHeadlineLight() = TypeCell(specimen("Headline"))

@ViddikScreenshot(group = "Type", name = "Title_Light", width = 916, height = 25)
@Composable
fun TypeTitleLight() = TypeCell(specimen("Title"))

@ViddikScreenshot(group = "Type", name = "BodyM_Light", width = 560, height = 67)
@Composable
fun TypeBodyMLight() = TypeCell(specimen("BodyM"))

@ViddikScreenshot(group = "Type", name = "BodyS_Light", width = 916, height = 20)
@Composable
fun TypeBodySLight() = TypeCell(specimen("BodyS"))

@ViddikScreenshot(group = "Type", name = "Label_Light", width = 916, height = 14)
@Composable
fun TypeLabelLight() = TypeCell(specimen("Label"))

@ViddikScreenshot(group = "Type", name = "Button_Light", width = 916, height = 14)
@Composable
fun TypeButtonLight() = TypeCell(specimen("Button"))
