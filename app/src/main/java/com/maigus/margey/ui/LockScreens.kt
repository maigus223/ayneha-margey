// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - écran de verrouillage et pavé de chiffres AYNEHA
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import com.maigus.margey.security.AppLock
import com.maigus.margey.text.Ay

const val PIN_LENGTH = 4

/** Points de saisie du code. */
@Composable
fun PinDots(count: Int, error: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(PIN_LENGTH) { i ->
            Box(
                Modifier.size(18.dp).clip(CircleShape)
                    .background(if (error) M.Red else if (i < count) M.Orange else M.Key)
            )
        }
    }
}

/** Pavé 3 x 4 en chiffres AYNEHA (1 2 3 / 4 5 6 / 7 8 9 / effacer 0). */
@Composable
fun AyPinPad(onDigit: (Char) -> Unit, onDelete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    // Disposition fixe de gauche à droite, comme tous les pavés de chiffres
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            listOf("123", "456", "789", "x0").forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    if (row.length == 2) Spacer(Modifier.size(72.dp))
                    row.forEach { c ->
                        Box(
                            Modifier.size(72.dp).clip(CircleShape).background(M.Key).clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (c == 'x') onDelete() else onDigit(c)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            if (c == 'x') Icon(Icons.AutoMirrored.Filled.Backspace, null, tint = M.Muted)
                            else AyText(ayNum(c.toString()), size = 30.sp, align = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

/** M08 / M09 : saisie du code pour ouvrir Margey. */
@Composable
fun LockScreen() {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().background(M.Night).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { LangButton() }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.Lock, null, tint = M.Orange, modifier = Modifier.size(56.dp))
        AyText(Ay.G01, size = 30.sp, color = M.Orange, align = TextAlign.Center)
        AyText(if (error) Ay.M09 else Ay.M08, size = 21.sp, color = if (error) M.Red else M.Fg, align = TextAlign.Center)
        PinDots(code.length, error)
        Spacer(Modifier.height(6.dp))
        AyPinPad(
            onDigit = { d ->
                if (code.length < PIN_LENGTH) {
                    error = false
                    code += d
                    if (code.length == PIN_LENGTH) {
                        if (!AppLock.check(code)) { error = true; code = "" }
                    }
                }
            },
            onDelete = { code = code.dropLast(1); error = false }
        )
        Spacer(Modifier.weight(1f))
    }
}

/** Panneau de création du code (E12). */
@Composable
fun PinSetupPanel(onDone: () -> Unit, onCancel: () -> Unit) {
    var code by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxWidth().background(M.Bar).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AyText(Ay.E12, size = 22.sp, modifier = Modifier.fillMaxWidth())
        PinDots(code.length)
        AyPinPad(onDigit = { if (code.length < PIN_LENGTH) code += it }, onDelete = { code = code.dropLast(1) })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (code.length == PIN_LENGTH) M.Orange else M.Key)
                    .clickable(enabled = code.length == PIN_LENGTH) { AppLock.setCode(code); onDone() },
                contentAlignment = Alignment.Center
            ) { AyText(Ay.G12, size = 22.sp, color = if (code.length == PIN_LENGTH) M.Ink else M.Muted, align = TextAlign.Center) }
            Box(
                Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp)).background(M.Card).clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) { AyText(Ay.G08, size = 22.sp, align = TextAlign.Center) }
        }
    }
}
