// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - interface des alertes d'urgence
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maigus.margey.features.Alerts
import com.maigus.margey.text.Ay

private val AlertRed = Color(0xFFB3261E)

/**
 * Alertes des 72 dernières heures, en tête des Discussions : auteur, date et heure, texte, vocal.
 * « Pris en charge » : l'auteur ou toute personne arrivée sur place met fin à l'alerte (son pseudo est affiché).
 */
@Composable
fun AlertsBanner(nickname: String = "") {
    val alerts by Alerts.alerts.collectAsStateWithLifecycle()
    val recent = alerts.filter { System.currentTimeMillis() - it.ts < 72L * 3600 * 1000 }
    Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        recent.forEach { a ->
            val bg = if (a.closed) M.Card else AlertRed
            val fg = if (a.closed) M.Muted else Color.White
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.Warning, null, tint = fg, modifier = Modifier.size(26.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AyText(fill(Ay.N08, nom = a.nick), size = 18.sp, color = fg)
                        AyText(ayNum(Alerts.timeLabel(a.ts)), size = 15.sp, color = if (a.closed) M.Muted2 else Color(0xFFFFDAD4))
                        if (a.text.isNotBlank()) AyText(a.text, size = 20.sp, color = fg)
                    }
                    Icon(Icons.Filled.Close, null, tint = fg, modifier = Modifier.size(32.dp).clickable { Alerts.dismiss(a.id) }.padding(4.dp))
                }
                if (a.voicePath.isNotBlank()) {
                    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(M.Sand).padding(8.dp)) { AudioContent(a.voicePath) }
                }
                if (a.closed) {
                    AyText(fill(Ay.N24, nom = a.closedBy) + " - " + ayNum(Alerts.timeLabel(a.closedAt)), size = 15.sp, color = M.Green)
                } else if (nickname.isNotBlank()) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).clickable { Alerts.close(a.id, nickname) }.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) { AyText(Ay.N23, size = 18.sp, color = AlertRed, align = TextAlign.Center) }
                }
            }
        }
    }
}

/** Bouton et panneau d'envoi d'une alerte (écran Autour de moi). */
@Composable
fun AlertSender(nickname: String) {
    var open by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var voice by remember { mutableStateOf<String?>(null) }
    if (!open) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp).height(60.dp).clip(RoundedCornerShape(18.dp)).background(AlertRed).clickable { open = true },
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Warning, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Box(Modifier.padding(start = 10.dp)) { AyText(Ay.N06, size = 22.sp, color = Color.White) }
        }
        return
    }
    Column(Modifier.fillMaxWidth().background(M.Bar).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AyText(if (confirm) Ay.N09 else Ay.N06, size = 22.sp, color = if (confirm) AlertRed else M.Fg)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AyField(text, Ay.N07, Modifier.weight(1f))
                // Alerte vocale : maintenir le micro pour parler (60 secondes au plus)
                MediaButtonsVoiceOnly(Alerts.VOICE_MAX_MS) { voice = it; confirm = false }
            }
            VoiceRecordingBar()
            voice?.let { v -> Box(Modifier.clip(RoundedCornerShape(12.dp)).background(M.Sand).padding(8.dp)) { AudioContent(v) } }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(16.dp)).background(AlertRed).clickable {
                        val t = text.trim()
                        if (t.isEmpty() && voice == null) return@clickable
                        if (!confirm) { confirm = true; return@clickable }
                        Alerts.send(nickname, t, voice)
                        text = ""; voice = null; confirm = false; open = false
                    },
                    contentAlignment = Alignment.Center
                ) { AyText(if (confirm) Ay.G10 else Ay.N06, size = 20.sp, color = Color.White, align = TextAlign.Center) }
                Box(
                    Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(16.dp)).background(M.Card).clickable { open = false; confirm = false; voice = null },
                    contentAlignment = Alignment.Center
                ) { AyText(Ay.G08, size = 20.sp, align = TextAlign.Center) }
            }
        }
        AynehaKeyboard(onChar = { text += it; confirm = false }, onBackspace = { text = text.dropLastCodePoint() }, onEnter = {})
    }
}
