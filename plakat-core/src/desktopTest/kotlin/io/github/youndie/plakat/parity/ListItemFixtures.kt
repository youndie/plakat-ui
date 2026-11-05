package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatListItemFrame
import io.github.youndie.plakat.components.PlakatSwitch
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-23: the MVP page's list rows, 390 wide, each cropped out of its one block; the chevron is the page's
// Back stencil mirrored. The Divider's cells are the floor's, in FloorFixtures.kt.

@Composable
private fun Row(
    colors: PlakatColors,
    height: Int,
    content: @Composable () -> Unit,
) = PlakatTheme(colors = colors, reduceMotion = true) {
    Box(Modifier.size(390.dp, height.dp).background(PlakatTheme.colors.paper)) { content() }
}

@Composable
private fun Chevron() = PlakatIcon(PlakatIcons.Back, null, Modifier.graphicsLayer { scaleX = -1f })

@Composable
private fun Default(colors: PlakatColors) =
    Row(colors, 73) {
        PlakatListItemFrame(
            "Город из фанеры",
            Modifier,
            "Анна Ветрова · 8 мин",
            "01",
            { Chevron() },
            {},
            enabled = true,
            divider = true,
            interactionSource = null,
        )
    }

@Composable
private fun Pressed(colors: PlakatColors) =
    Row(colors, 72) {
        PlakatListItemFrame(
            "Буквы для площади",
            Modifier,
            "Илья Осин · 5 мин",
            "02",
            { Chevron() },
            {},
            enabled = true,
            divider = false,
            interactionSource = null,
            forcePressed = true,
        )
    }

@Composable
private fun WithSwitch(colors: PlakatColors) =
    Row(colors, 57) {
        PlakatListItemFrame(
            "Присылать дайджест",
            Modifier,
            null,
            null,
            { PlakatSwitch(true, {}) },
            null,
            enabled = true,
            divider = true,
            interactionSource = null,
        )
    }

@Composable
private fun Disabled(colors: PlakatColors) =
    Row(colors, 57) {
        PlakatListItemFrame(
            "Офлайн-режим · скоро",
            Modifier,
            null,
            null,
            null,
            {},
            enabled = false,
            divider = true,
            interactionSource = null,
        )
    }

private val light = PlakatPalettes.Paper
private val dark = PlakatPalettes.BlackPaper

@ViddikScreenshot(group = "ListItem", name = "Default_Light", width = 390, height = 73)
@Composable
fun ListItemDefaultLight() = Default(light)

@ViddikScreenshot(group = "ListItem", name = "Default_Dark", width = 390, height = 73)
@Composable
fun ListItemDefaultDark() = Default(dark)

@ViddikScreenshot(group = "ListItem", name = "Pressed_Light", width = 390, height = 72)
@Composable
fun ListItemPressedLight() = Pressed(light)

@ViddikScreenshot(group = "ListItem", name = "Pressed_Dark", width = 390, height = 72)
@Composable
fun ListItemPressedDark() = Pressed(dark)

@ViddikScreenshot(group = "ListItem", name = "Switch_Light", width = 390, height = 57)
@Composable
fun ListItemSwitchLight() = WithSwitch(light)

@ViddikScreenshot(group = "ListItem", name = "Switch_Dark", width = 390, height = 57)
@Composable
fun ListItemSwitchDark() = WithSwitch(dark)

@ViddikScreenshot(group = "ListItem", name = "Disabled_Light", width = 390, height = 57)
@Composable
fun ListItemDisabledLight() = Disabled(light)

@ViddikScreenshot(group = "ListItem", name = "Disabled_Dark", width = 390, height = 57)
@Composable
fun ListItemDisabledDark() = Disabled(dark)
