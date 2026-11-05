package io.github.youndie.plakat.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.agit.PlakatGeneratedImage
import io.github.youndie.plakat.agit.PlakatHalftoneMode
import io.github.youndie.plakat.agit.PlakatPreset
import io.github.youndie.plakat.agit.PlakatPresetImage
import io.github.youndie.plakat.agit.PlakatRapportFill
import io.github.youndie.plakat.agit.PlakatRaskatDisc
import io.github.youndie.plakat.agit.PlakatSkeleton
import io.github.youndie.plakat.agit.plakatHalftone
import io.github.youndie.plakat.components.PlakatBadge
import io.github.youndie.plakat.components.PlakatBadgedBox
import io.github.youndie.plakat.components.PlakatButton
import io.github.youndie.plakat.components.PlakatCard
import io.github.youndie.plakat.components.PlakatCheckbox
import io.github.youndie.plakat.components.PlakatChip
import io.github.youndie.plakat.components.PlakatCircularProgress
import io.github.youndie.plakat.components.PlakatCounter
import io.github.youndie.plakat.components.PlakatCounterColumn
import io.github.youndie.plakat.components.PlakatDangerButton
import io.github.youndie.plakat.components.PlakatDialog
import io.github.youndie.plakat.components.PlakatDialogAction
import io.github.youndie.plakat.components.PlakatDivider
import io.github.youndie.plakat.components.PlakatDividerWeight
import io.github.youndie.plakat.components.PlakatFab
import io.github.youndie.plakat.components.PlakatIconButton
import io.github.youndie.plakat.components.PlakatLinearProgress
import io.github.youndie.plakat.components.PlakatListItem
import io.github.youndie.plakat.components.PlakatNavigationBar
import io.github.youndie.plakat.components.PlakatNavigationItem
import io.github.youndie.plakat.components.PlakatOutlinedButton
import io.github.youndie.plakat.components.PlakatPictogram
import io.github.youndie.plakat.components.PlakatPoster
import io.github.youndie.plakat.components.PlakatPosterLayout
import io.github.youndie.plakat.components.PlakatRadioButton
import io.github.youndie.plakat.components.PlakatScreenWipe
import io.github.youndie.plakat.components.PlakatSnackbar
import io.github.youndie.plakat.components.PlakatStretch
import io.github.youndie.plakat.components.PlakatSwitch
import io.github.youndie.plakat.components.PlakatTab
import io.github.youndie.plakat.components.PlakatTabContent
import io.github.youndie.plakat.components.PlakatTabs
import io.github.youndie.plakat.components.PlakatTextButton
import io.github.youndie.plakat.components.PlakatTextField
import io.github.youndie.plakat.components.PlakatTopAppBar
import io.github.youndie.plakat.components.PlakatWindow
import io.github.youndie.plakat.components.PlakatWindowPanel
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme

// Every component, grouped as the design's pages group them. [beat] moves what would otherwise only show on a click:
// it advances by itself while the sample runs, so a number ticks and a window turns with nobody touching them.

private val Width = 358.dp

/** A heading over its [content]. */
@Composable
internal fun KitGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val type = PlakatTheme.typography
    Column(
        Modifier.widthIn(max = 720.dp).testTag("group-$title"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BasicText(type.headline.text(title), style = type.headline.style.copy(color = PlakatTheme.colors.ink))
        PlakatDivider(weight = PlakatDividerWeight.Rule)
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Wrap(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        content()
    }
}

/** The Core page: Button, TextField, Checkbox, Switch, Card, Snackbar, Плакат. */
@Composable
fun KitCore() {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        KitGroup("Кнопка") {
            Wrap {
                PlakatButton("ЧИТАТЬ", {})
                PlakatButton("ЗАГРУЗКА", {}, loading = true)
                PlakatButton("НЕЛЬЗЯ", {}, enabled = false)
                PlakatOutlinedButton("В ЗАКЛАДКИ", {})
                PlakatTextButton("ПОЗЖЕ", {})
                PlakatDangerButton("СТЕРЕТЬ", {})
            }
        }
        KitGroup("Поле") {
            var value by remember { mutableStateOf("") }
            PlakatTextField(value, { value = it }, Modifier.width(Width), label = "Имя", placeholder = "Как вас зовут")
            PlakatTextField("лист", {}, Modifier.width(Width), label = "Адрес", error = "Такого адреса нет")
        }
        KitGroup("Флажок, выбор, переключатель") {
            Wrap {
                var checked by remember { mutableStateOf(true) }
                var on by remember { mutableStateOf(true) }
                PlakatCheckbox(checked, { checked = it })
                PlakatCheckbox(false, null, enabled = false)
                PlakatSwitch(on, { on = it })
                PlakatSwitch(false, null, enabled = false)
                var radio by remember { mutableIntStateOf(0) }
                Row(Modifier.selectableGroup()) {
                    repeat(3) { i -> PlakatRadioButton(radio == i, { radio = i }) }
                }
                PlakatRadioButton(true, null, enabled = false)
            }
        }
        KitGroup("Карточка") {
            PlakatCard({}, Modifier.width(Width)) {
                PlakatGeneratedImage(71, Modifier.size(64.dp), frameWidth = 64.dp, frameHeight = 64.dp)
                BasicText(
                    "Город из фанеры",
                    style =
                        PlakatTheme.typography.bodyS.style
                            .copy(color = PlakatTheme.colors.ink),
                )
            }
        }
        KitGroup("Телеграмма") {
            PlakatSnackbar("Сохранено в лист", Modifier.width(Width), actionLabel = "ОТМЕНА")
        }
        KitGroup("Плакат") {
            Wrap {
                PlakatPoster(
                    listOf("ЧИТАЙ", "ВЕСЬ", "НОЯБРЬ"),
                    "ПОДПИСАТЬСЯ",
                    {},
                    body = "12 новых выпусков.",
                    seed = 1919,
                )
                PlakatPoster(listOf("ЧИТАЙ", "ВЕСЬ", "НОЯБРЬ"), "ПОДПИСАТЬСЯ", {
                }, body = "12 новых выпусков.", seed = 1919, raskat = true)
                PlakatPoster(listOf("ВЫПУСК", "№ 47"), "ЧИТАТЬ", {}, layout = PlakatPosterLayout.Banner, seed = 1925)
            }
        }
    }
}

/** The MVP page: IconButton to Счётчик and the change of screen. */
@Composable
fun KitComponents(beat: Int) {
    val icons = listOf(PlakatIcons.Home, PlakatIcons.Search, PlakatIcons.Share, PlakatIcons.Settings)
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        KitGroup("Кнопки-значки") {
            Wrap {
                for (icon in PlakatIcons.All.take(6)) PlakatIconButton(icon, icon.name, {})
                PlakatFab(PlakatIcons.Add, "Добавить", {})
            }
        }
        KitGroup("Метки") {
            Wrap {
                var selected by remember { mutableIntStateOf(0) }
                for ((i, word) in listOf("ШРИФТ", "ГОРОД", "ДИЗАЙН").withIndex()) {
                    PlakatChip(word, selected == i, { selected = i })
                }
                PlakatBadgedBox(beat) { PlakatIconButton(PlakatIcons.Share, "Обмен", {}) }
                PlakatBadge(null)
            }
        }
        KitGroup("Ход") {
            PlakatLinearProgress((beat % 5) / 4f, Modifier.width(Width))
            Wrap {
                PlakatCircularProgress(null)
                PlakatCircularProgress((beat % 5) / 4f)
            }
        }
        KitGroup("Шапка") {
            PlakatTopAppBar("ЛИСТ", Modifier.width(Width), subtitle = "№ 47")
        }
        KitGroup("Навигация") {
            var nav by remember { mutableIntStateOf(0) }
            PlakatNavigationBar(
                icons.zip(listOf("ЛЕНТА", "ПОИСК", "ОБМЕН", "ОПЦИИ")).map { (icon, word) ->
                    PlakatNavigationItem(icon, word)
                },
                nav,
                {
                    nav =
                        it
                },
                Modifier.width(Width),
            )
        }
        KitGroup("Вкладки") {
            val tab = beat % 3
            PlakatTabs(
                listOf(PlakatTab("ЛЕНТА"), PlakatTab("АРХИВ"), PlakatTab("ЗАКЛАДКИ")),
                tab,
                {},
                Modifier.width(Width),
            )
            PlakatTabContent(3, tab, Modifier.width(Width).height(64.dp)) {
                PlakatRapportFill(1923 + it * 2, Modifier.fillMaxSize())
            }
        }
        KitGroup("Строки") {
            Column(Modifier.width(Width)) {
                PlakatListItem("Шрифт", supporting = "5 мин", number = "01", onClick = {})
                PlakatListItem("Дайджест", trailing = { PlakatSwitch(true, {}) })
            }
        }
        KitGroup("Объявление") {
            var open by remember { mutableStateOf(false) }
            PlakatOutlinedButton("ОТКРЫТЬ", { open = true }, Modifier.testTag("open-dialog"))
            Box(Modifier.width(Width).height(if (open) 420.dp else 1.dp)) {
                PlakatDialog(open, "УДАЛИТЬ", { open = false }, text = "Вернуть можно.") {
                    PlakatDialogAction("ОТМЕНА", { open = false })
                    PlakatDialogAction("СТЕРЕТЬ", { open = false }, danger = true)
                }
            }
        }
        KitGroup("Окно") {
            val panels =
                remember {
                    listOf(300, 317, 334).mapIndexed { i, seed ->
                        PlakatWindowPanel(listOf("ЧИТАЙ", "СОХРАНЯЙ", "ДЕЛИСЬ")[i], "Панель ${i + 1}.") {
                            PlakatGeneratedImage(seed, Modifier.fillMaxSize(), 171.dp, 160.dp)
                        }
                    }
                }
            PlakatWindow(panels, beat % 2, Modifier.width(Width), panelsInView = 2)
        }
        KitGroup("Счётчик") {
            Row(Modifier.width(Width), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlakatCounter(39 + beat * 8, "СТАТЕЙ", Modifier.weight(1f), legend = "= 5 статей", perPictogram = 5)
                PlakatCounterColumn(39 + beat, "ДНЕЙ", Modifier.weight(1f))
            }
        }
        KitGroup("Смена экрана") {
            val depth = beat % 2 + 1
            PlakatScreenWipe(depth, depth, Modifier.width(Width).height(240.dp)) {
                PlakatPresetImage(if (it == 1) PlakatPreset.Klin else PlakatPreset.Os, Modifier.fillMaxSize())
            }
        }
    }
}

/** The Agit page and the Stretch page. */
@Composable
fun KitAgit() {
    val colors = PlakatTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        KitGroup("Заготовки") {
            Wrap { for (preset in PlakatPreset.entries) PlakatPresetImage(preset, Modifier.size(180.dp, 120.dp)) }
        }
        KitGroup("Генератор") {
            Wrap { for (seed in 1919..1924) PlakatGeneratedImage(seed, Modifier.size(180.dp, 120.dp)) }
        }
        KitGroup("Раппорты") {
            Wrap { for (seed in listOf(1923, 1925, 1927, 1929)) PlakatRapportFill(seed, Modifier.size(180.dp, 96.dp)) }
            PlakatRapportFill(1923, Modifier.width(Width).height(24.dp), cell = 4.dp)
            PlakatSkeleton(1923, Modifier.size(64.dp))
        }
        KitGroup("Фотомонтаж") {
            val source = remember { standIn() }
            Wrap {
                Box(
                    Modifier
                        .size(
                            180.dp,
                        ).clip(
                            androidx.compose.foundation.shape.CircleShape,
                        ).plakatHalftone(source, colors.red, colors.paper),
                )
                Box(Modifier.size(180.dp).plakatHalftone(source, colors.ink, colors.paper, PlakatHalftoneMode.Mask))
                PlakatRaskatDisc(Modifier.size(180.dp))
            }
        }
        KitGroup("Растяжка") {
            PlakatStretch("ЛИСТ", Modifier.width(Width).height(120.dp))
        }
    }
}

/** A picture to print, in place of the photographs the kit does not ship. */
private fun standIn(): ImageBitmap {
    val image = ImageBitmap(340, 420)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(340f, 420f)) {
        drawRect(
            Brush.radialGradient(
                0f to Color(0xFF151515),
                .55f to Color(0xFF8A8A8A),
                1f to Color(0xFFF4F4F4),
                center = Offset(187f, 168f),
                radius = 210f,
            ),
        )
    }
    return image
}
