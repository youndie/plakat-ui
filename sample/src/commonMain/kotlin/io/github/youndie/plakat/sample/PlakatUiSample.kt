package io.github.youndie.plakat.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatChip
import io.github.youndie.plakat.print.plakatPaper
import io.github.youndie.plakat.theme.PlakatEventListener
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlinx.coroutines.delay

/** What the sample shows, grouped as the design's pages are. */
enum class SampleSection(
    val title: String,
) {
    Core("ЯДРО"),
    Components("КОМПОНЕНТЫ"),
    Agit("АГИТ"),
    Onboarding("ОНБОРДИНГ"),
    Feed("ЛЕНТА"),
    Settings("НАСТРОЙКИ"),
}

/** How long a beat of the sample's own motion lasts: long enough to see each change finish. */
private const val BEAT_MS = 2000L

/**
 * The sample's root, shared by the desktop window and the browser, through the kit's public API only. The theme and
 * reduce motion are parameters and switches in its header, so they reach every screen it shows (cmp-ui-library §7).
 * [events] hears the kit's [PlakatEventListener]. With [autoplay], numbers and pages move by themselves, so an animation is
 * seen without a click.
 */
@Composable
fun PlakatUiSample(
    dark: Boolean = false,
    reduceMotion: Boolean? = null,
    texture: Boolean = true,
    section: SampleSection = SampleSection.Core,
    autoplay: Boolean = true,
    events: PlakatEventListener = PlakatEventListener.Silent,
) {
    var isDark by remember { mutableStateOf(dark) }
    var reduce by remember { mutableStateOf(reduceMotion) }
    var textureOn by remember { mutableStateOf(texture) }
    var shown by remember { mutableStateOf(section) }
    var beat by remember { mutableIntStateOf(0) }
    LaunchedEffect(autoplay) {
        while (autoplay) {
            delay(BEAT_MS)
            beat++
        }
    }
    PlakatTheme(
        colors = if (isDark) PlakatPalettes.BlackPaper else PlakatPalettes.Paper,
        reduceMotion = reduce,
        print = PlakatPrintSwitches(texture = textureOn),
        events = events,
    ) {
        Column(Modifier.fillMaxSize().plakatPaper().testTag("sample-root")) {
            Header(
                shown,
                { shown = it },
                isDark,
                { isDark = !isDark },
                reduce == true,
                { reduce = reduce != true },
                textureOn,
                { textureOn = !textureOn },
            )
            when (shown) {
                SampleSection.Core -> Page { KitCore() }
                SampleSection.Components -> Page { KitComponents(beat) }
                SampleSection.Agit -> Page { KitAgit() }
                SampleSection.Onboarding -> Phone { DemoOnboarding() }
                SampleSection.Feed -> Phone { DemoFeed() }
                SampleSection.Settings -> Phone { DemoSettings(isDark, { isDark = it }) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(
    shown: SampleSection,
    onShow: (SampleSection) -> Unit,
    dark: Boolean,
    onDark: () -> Unit,
    reduce: Boolean,
    onReduce: () -> Unit,
    texture: Boolean,
    onTexture: () -> Unit,
) {
    val label = PlakatTheme.typography.label
    FlowRow(
        Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (s in SampleSection.entries) {
            PlakatChip(
                s.title,
                s == shown,
                { onShow(s) },
                Modifier.testTag("section-${s.name}"),
            )
        }
        PlakatChip("ЧЁРНАЯ БУМАГА", dark, onDark, Modifier.testTag("sample-dark"))
        PlakatChip("БЕЗ ДВИЖЕНИЯ", reduce, onReduce, Modifier.testTag("sample-reduce"))
        BasicText(
            label.text(if (texture) "Текстура: вкл" else "Текстура: выкл"),
            Modifier
                .clickable(onClick = onTexture)
                .padding(16.dp)
                .testTag("sample-texture"),
            style = label.style.copy(color = PlakatTheme.colors.ink),
        )
    }
}

@Composable
private fun Page(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) { content() }
}

/** A screen of the Demo page, at its size. */
@Composable
private fun Phone(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Box(Modifier.size(390.dp, 844.dp)) { content() }
    }
}
