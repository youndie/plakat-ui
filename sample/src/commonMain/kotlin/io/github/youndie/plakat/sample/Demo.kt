package io.github.youndie.plakat.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.agit.PlakatGeneratedImage
import io.github.youndie.plakat.agit.PlakatRapportFill
import io.github.youndie.plakat.agit.PlakatRaskatDisc
import io.github.youndie.plakat.agit.PlakatTitleZone
import io.github.youndie.plakat.components.PlakatButton
import io.github.youndie.plakat.components.PlakatCard
import io.github.youndie.plakat.components.PlakatCounter
import io.github.youndie.plakat.components.PlakatDialogAction
import io.github.youndie.plakat.components.PlakatDivider
import io.github.youndie.plakat.components.PlakatDividerWeight
import io.github.youndie.plakat.components.PlakatIconButton
import io.github.youndie.plakat.components.PlakatNavigationBar
import io.github.youndie.plakat.components.PlakatNavigationItem
import io.github.youndie.plakat.components.PlakatPictogram
import io.github.youndie.plakat.components.PlakatSegmentedButton
import io.github.youndie.plakat.components.PlakatSwitch
import io.github.youndie.plakat.components.PlakatTab
import io.github.youndie.plakat.components.PlakatTabs
import io.github.youndie.plakat.components.PlakatWindow
import io.github.youndie.plakat.components.PlakatWindowPanel
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme

// The Demo page's three screens of the magazine «Лист», from the kit's public API. What the kit has no component for
// yet (the page indicator) is drawn here, as an application would.

private val NavItems =
    listOf(
        PlakatNavigationItem(PlakatIcons.Home, "ЛЕНТА"),
        PlakatNavigationItem(PlakatIcons.Search, "ПОИСК"),
        PlakatNavigationItem(PlakatIcons.Share, "ОБМЕН"),
        PlakatNavigationItem(PlakatIcons.Settings, "ОПЦИИ"),
    )

@Composable
private fun label(size: TextUnit = 12.sp) =
    PlakatTheme.typography.label.style
        .copy(fontSize = size, color = PlakatTheme.colors.ink)

@Composable
private fun display(
    size: TextUnit,
    line: TextUnit = TextUnit.Unspecified,
) = PlakatTheme.typography.displayL.style
    .copy(fontSize = size, lineHeight = line, color = PlakatTheme.colors.ink)

@Composable
private fun body(
    size: TextUnit,
    line: TextUnit = TextUnit.Unspecified,
) = PlakatTheme.typography.bodyS.style
    .copy(fontSize = size, lineHeight = line, color = PlakatTheme.colors.ink)

/** A heading with a rule running on from it: `display: flex; gap: 8px`, the rule 3 high. */
@Composable
private fun RuledLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(text, style = label())
        PlakatDivider(Modifier.weight(1f), PlakatDividerWeight.Rule)
    }
}

/** 01: the window of three panels, an indicator and two buttons. [page] is the first in view. */
@Composable
fun DemoOnboarding(
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
) {
    val colors = PlakatTheme.colors
    var page by remember { mutableIntStateOf(initialPage) }
    val zone = PlakatTitleZone.at(DpRect(0.dp, 0.dp, 72.dp, 40.dp))
    val panels =
        remember(zone) {
            listOf(
                Triple("ЧИТАЙ ПО ПОРЯДКУ", "Каждую неделю — выпуск из 12 статей. Начни с первой.", 301),
                Triple("СОХРАНЯЙ В ЛИСТ", "Закладки открываются без сети — в метро и в самолёте.", 318),
                Triple("ДЕЛИСЬ ВЫПУСКОМ", "Отправь статью другу — он прочтёт её без подписки.", 335),
            ).map { (caption, text, seed) ->
                PlakatWindowPanel(caption, text) {
                    PlakatGeneratedImage(
                        seed,
                        Modifier.fillMaxSize(),
                        frameWidth = 358.dp,
                        frameHeight = 360.dp,
                        titleZone = zone,
                        ground = PlakatTheme.colors.concrete,
                    )
                }
            }
        }
    Box(modifier.fillMaxSize().background(colors.paper)) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(top = 20.dp, start = 16.dp, end = 16.dp).height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText("ЛИСТ", style = display(20.sp))
                BasicText("${page + 1} / 3", style = label())
            }
            Spacer(Modifier.height(12.dp))
            PlakatDivider(Modifier.padding(horizontal = 16.dp), PlakatDividerWeight.Bar)
            Spacer(Modifier.height(24.dp))
            PlakatWindow(panels, page, large = true)
        }
        Row(
            Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .size(24.dp, 8.dp)
                        .background(if (i == page) colors.red else Color.Transparent)
                        .drawBehind {
                            val line = 2.dp.toPx()
                            drawRect(colors.ink, Offset.Zero, Size(size.width, line))
                            drawRect(colors.ink, Offset(0f, size.height - line), Size(size.width, line))
                            drawRect(colors.ink, Offset.Zero, Size(line, size.height))
                            drawRect(colors.ink, Offset(size.width - line, 0f), Size(line, size.height))
                        },
                )
            }
        }
        Row(
            Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 16.dp, bottom = 40.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlakatDialogAction("ПРОПУСТИТЬ", { page = 2 })
            PlakatButton(if (page == 2) "НАЧАТЬ ЧИТАТЬ" else "ДАЛЬШЕ", { page = (page + 1) % 3 }, Modifier.weight(1f))
        }
    }
}

/** 02: a masthead with its strip, tabs, a poster, cards, the navigation bar. */
@Composable
fun DemoFeed(modifier: Modifier = Modifier) {
    val colors = PlakatTheme.colors
    var tab by remember { mutableIntStateOf(0) }
    var nav by remember { mutableIntStateOf(0) }
    Box(modifier.fillMaxSize().background(colors.paper)) {
        Column(Modifier.fillMaxSize().padding(bottom = 64.dp).verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().padding(top = 20.dp, start = 16.dp, end = 16.dp).height(28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText("ЛИСТ", Modifier.wrapContentHeight(unbounded = true), style = display(28.sp))
                BasicText("№ 47 · НОЯБРЬ", Modifier.offset(y = 3.dp), style = label())
            }
            Spacer(Modifier.height(12.dp))
            PlakatRapportFill(1923, Modifier.fillMaxWidth().height(24.dp), cell = 4.dp)
            Spacer(Modifier.height(8.dp))
            PlakatTabs(listOf(PlakatTab("ЛЕНТА"), PlakatTab("АРХИВ"), PlakatTab("ЗАКЛАДКИ")), tab, { tab = it })
            FeedPoster()
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)) {
                RuledLabel("СВЕЖЕЕ")
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedCard("АРХИТЕКТУРА · 8 МИН", "Город из фанеры: как строят временные павильоны", 71)
                    FeedCard("ШРИФТ · 5 МИН", "Буквы, которые читаются с другого конца площади", 72)
                    FeedCard("ДИЗАЙН · 12 МИН", "Семь способов сложить один лист бумаги", 73)
                }
            }
        }
        PlakatNavigationBar(NavItems, nav, { nav = it }, Modifier.align(Alignment.BottomCenter))
    }
}

/** The poster of this issue: a concrete plate, a disc of raskat and the title on the axis. */
@Composable
private fun FeedPoster() {
    Box(Modifier.fillMaxWidth().height(392.dp)) {
        Box(Modifier.offset(0.dp, 168.dp).size(160.dp, 144.dp).background(PlakatTheme.colors.concrete))
        PlakatRaskatDisc(Modifier.offset(250.dp, 192.dp).size(120.dp))
        BasicText(
            "ЧИТАЙ\nВЕСЬ\nНОЯБРЬ",
            Modifier
                // Compose sets this line's baseline 8 lower than Blink does, along the turned block's own height.
                .offset(13.7.dp, 92.4.dp)
                .graphicsLayer {
                    rotationZ = -17f
                    transformOrigin = TransformOrigin(0f, 0f)
                },
            style = display(48.sp, 45.6.sp),
            softWrap = false,
        )
        BasicText(
            "12 новых выпусков. Мы собрали их в одну ленту — читай по порядку.",
            Modifier.offset(16.dp, 255.dp).width(220.dp),
            style = body(16.sp, 21.6.sp),
        )
        PlakatButton(
            "ПОДПИСАТЬСЯ",
            {},
            Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 16.dp, bottom = 24.dp).fillMaxWidth(),
        )
    }
}

@Composable
private fun FeedCard(
    rubric: String,
    title: String,
    seed: Int,
) {
    PlakatCard({}, Modifier.fillMaxWidth(), printOrder = null) {
        PlakatGeneratedImage(
            seed,
            Modifier.size(64.dp),
            frameWidth = 64.dp,
            frameHeight = 64.dp,
            titleZone = PlakatTitleZone.None,
        )
        Column {
            BasicText(rubric, style = label(11.sp))
            Spacer(Modifier.height(4.dp))
            BasicText(
                title,
                style = body(17.sp, 21.25.sp).copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

/** 03: the collapsed bar, two counters, the reading switches, the theme, leaving. */
@Composable
fun DemoSettings(
    dark: Boolean,
    onDarkChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PlakatTheme.colors
    var digest by remember { mutableStateOf(true) }
    var unread by remember { mutableStateOf(dark) }
    var nav by remember { mutableIntStateOf(3) }
    Box(modifier.fillMaxSize().background(colors.paper)) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp, end = 4.dp).height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlakatIconButton(PlakatIcons.Back, "Назад", {})
                BasicText("НАСТРОЙКИ", style = display(16.sp))
            }
            PlakatDivider(weight = PlakatDividerWeight.Rule)
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)) {
                BasicText("ВАШ НОЯБРЬ", style = label())
                Spacer(Modifier.height(12.dp))
                Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        PlakatCounter(
                            47,
                            "СТАТЕЙ ПРОЧИТАНО",
                            Modifier.fillMaxSize(),
                            legend = "= 5 статей",
                            perPictogram = 5,
                            stretched = true,
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        PlakatCounter(
                            6,
                            "ДНЕЙ ПОДРЯД",
                            Modifier.fillMaxSize(),
                            legend = "= день, красный — сегодня",
                            pictogram = PlakatPictogram.Circle,
                            accentLast = true,
                            stretched = true,
                            numberColor = colors.red,
                        )
                    }
                }
            }
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)) {
                RuledLabel("ЧТЕНИЕ")
                SwitchRow("Присылать дайджест", digest, { digest = it })
                SwitchRow("Только непрочитанное", unread, { unread = it })
                SwitchRow("Офлайн-режим · скоро", false, null)
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText("Тема", Modifier.offset(y = (-1).dp), style = body(16.sp))
                    PlakatSegmentedButton(listOf("БУМАГА", "ЧЁРНАЯ"), if (dark) 1 else 0, { onDarkChange(it == 1) })
                }
                PlakatDivider()
                Spacer(Modifier.height(32.dp))
                PlakatDialogAction("ВЫЙТИ ИЗ АККАУНТА", {}, Modifier.fillMaxWidth(), danger = true)
            }
        }
        PlakatNavigationBar(NavItems, nav, { nav = it }, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun SwitchRow(
    text: String,
    checked: Boolean,
    onChange: ((Boolean) -> Unit)?,
) {
    val colors = PlakatTheme.colors
    // The whole row toggles, as the page's `role="switch"` does.
    Column(Modifier.clickable(enabled = onChange != null) { onChange?.invoke(!checked) }) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text,
                Modifier.offset(y = (-1).dp),
                style = body(16.sp).copy(color = if (onChange == null) colors.inkMuted else colors.ink),
            )
            PlakatSwitch(checked, onChange, enabled = onChange != null)
        }
        PlakatDivider()
    }
}
