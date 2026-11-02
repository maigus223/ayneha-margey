// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - choix de la langue : AYNEHA, français, anglais
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maigus.margey.text.Lang

/** Le mot « ayneha » en glyphes AYNEHA (toujours affiché ainsi, quelle que soit la langue). */
val AYNEHA_WORD: String = intArrayOf(0x10FF9C, 0x10FFCC, 0x10FF8D, 0x10FF9D, 0x10FF9A, 0x10FF9C)
    .joinToString("") { String(Character.toChars(it)) }

/** Noms des trois langues, chacun écrit dans sa propre langue. */
val LANG_NAMES: List<String> get() = listOf(AYNEHA_WORD, "Français", "English")

/** Change la langue de l'interface (et le clavier par défaut qui va avec). */
fun setLanguage(ctx: Context, lang: Int) {
    Lang.set(ctx, lang)
    Kb.ayneha = lang == Lang.AY
}

/** Nom d'une langue, avec la bonne police. */
@Composable
fun LangName(i: Int, size: Float, color: androidx.compose.ui.graphics.Color) {
    if (i == Lang.AY) Text(rtl(AYNEHA_WORD), style = TextStyle(fontFamily = AynehaFont, fontSize = (size * AY_SCALE).sp, color = color))
    else Text(LANG_NAMES[i], style = TextStyle(fontSize = size.sp, color = color))
}

/** Icône globe, en haut de chaque écran : un toucher ouvre le choix de la langue. */
@Composable
fun LangButton(tint: androidx.compose.ui.graphics.Color = M.Fg) {
    val ctx = LocalContext.current
    var open by remember { mutableStateOf(false) }
    Box {
        Icon(Icons.Filled.Language, null, tint = tint, modifier = Modifier.size(44.dp).clickable { open = true }.padding(10.dp))
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            (0..2).forEach { i ->
                DropdownMenuItem(
                    text = { LangName(i, 18f, if (i == Lang.current) M.Orange else androidx.compose.ui.graphics.Color.Unspecified) },
                    onClick = { setLanguage(ctx, i); open = false }
                )
            }
        }
    }
}

/** Rangée de choix de la langue (Réglages). */
@Composable
fun LangChips() {
    val ctx = LocalContext.current
    Row(
        Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        (0..2).forEach { i ->
            val on = i == Lang.current
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (on) M.Orange else M.Key)
                    .clickable { setLanguage(ctx, i) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) { LangName(i, 16f, if (on) M.Ink else M.Fg) }
        }
    }
}

/** Tout premier écran : choisir la langue. */
@Composable
fun LanguageScreen(onChosen: () -> Unit) {
    val ctx = LocalContext.current
    MargeyTheme {
        Column(
            Modifier.fillMaxSize().background(M.Night).statusBarsPadding().navigationBarsPadding().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.weight(1f))
            MargeyLogo(112.dp)
            Text("Margey", style = TextStyle(fontSize = 34.sp, color = M.Orange), textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            (0..2).forEach { i ->
                Box(
                    Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(18.dp))
                        .background(if (i == Lang.AY) M.Orange else M.Card)
                        .clickable { setLanguage(ctx, i); onChosen() },
                    contentAlignment = Alignment.Center
                ) { LangName(i, 24f, if (i == Lang.AY) M.Ink else M.Fg) }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

/** Logo de Margey (création de MAIGUS). */
@Composable
fun MargeyLogo(size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(com.bitchat.android.R.drawable.margey_logo),
        contentDescription = "Margey",
        modifier = modifier.size(size)
    )
}
