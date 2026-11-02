// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - apprendre l'AYNEHA : alphabet et variantes, lettre du jour, mot du jour, jeu des lettres
// Concepteur et créateur de l'AYNEHA : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maigus.margey.text.Ay
import com.maigus.margey.text.Lang
import com.maigus.margey.text.Words

/** L'apprentissage se fait en français ou en anglais (l'AYNEHA n'est pas encore connu de tous). */
private fun t(fr: String, en: String) = if (Lang.current == Lang.EN) en else fr
private val L_TITLE get() = t("Apprendre l'AYNEHA", "Learn AYNEHA")
private val L_ALPHA get() = t("Alphabet", "Alphabet")
private val L_DAY get() = t("Lettre du jour", "Letter of the day")
private val L_WORD get() = t("Mot du jour", "Word of the day")
private val L_QUIZ get() = t("Quelle est cette lettre ?", "Which letter is this?")
private val L_OK get() = t("Bonne réponse !", "Correct!")
private val L_NO get() = t("Ce n'est pas ça", "Not quite")
private val L_SCORE get() = t("Score", "Score")
private val L_NEXT get() = t("Suivant", "Next")
/** Titre de l'entrée « Apprendre » (toujours en français ou en anglais). */
val LEARN_TITLE: String get() = L_TITLE

/** Une lettre de l'alphabet AYNEHA : glyphe, valeur latine, variantes (glyphe, sorte). */
data class AyLetter(val glyph: String, val latin: String, val variants: List<Pair<String, Int>>)

/** Alphabet AYNEHA (plan 16) : 23 consonnes puis 5 voyelles, et leurs formes précombinées. */
object Alphabet {
    private fun cp(c: Int) = String(Character.toChars(c))
    private val NAMES = listOf("B", "P", "M", "F", "W", "D", "T", "N", "L", "R", "S", "Z", "SH", "TS", "J", "DJ", "GN", "Y", "G", "K", "H", "NG", "A", "E", "I", "O", "U")
    private val LATIN = listOf("b", "p", "m", "f", "w", "d", "t", "n", "l", "r", "s", "z", "sh", "c", "j", "dj", "ɲ", "y", "g", "k", "h", "ŋ", "a", "e", "i", "o", "u")
    private val COMBOS = listOf("A_D", "A_N", "E_D", "E_N", "E_O", "I_D", "I_N", "O_D", "O_N", "O_O", "U_D", "U_N",
        "B_D", "B_M", "P_D", "P_M", "M_D", "M_M", "F_D", "F_M", "W_D", "W_M", "D_D", "D_M", "T_D", "T_M", "N_D", "N_M", "L_D", "L_M", "R_D", "R_M", "S_D", "S_M", "Z_D", "Z_M",
        "SH_D", "TS_D", "J_D", "DJ_D", "DJ_M", "GN_D", "Y_D", "Y_M", "G_D", "G_M", "K_D", "K_M", "NG_D", "NG_M")
    /** 0 = double, 1 = muette, 2 = nasale, 3 = ouverte */
    private fun kind(k: String) = when (k) { "D" -> 0; "M" -> 1; "N" -> 2; else -> 3 }

    val LETTERS: List<AyLetter> = NAMES.mapIndexed { i, n ->
        val vars = COMBOS.withIndex().filter { it.value.substringBefore('_') == n }
            .map { cp(0x10FFA1 + it.index) to kind(it.value.substringAfter('_')) }
        AyLetter(cp(0x10FF86 + i), LATIN[i], vars)
    }
    val DIGITS: List<Pair<String, String>> = (0..9).map { cp(0x10FF7C + it) to it.toString() }

    fun kindName(k: Int) = when (k) { 0 -> t("double", "double"); 1 -> t("muette", "silent"); 2 -> t("nasale", "nasal"); else -> t("ouverte", "open") }

    private fun day() = (System.currentTimeMillis() / 86_400_000L).toInt()
    fun letterOfDay() = LETTERS[Math.floorMod(day(), LETTERS.size)]
    fun wordOfDay(): Array<String> = Words.LIST[Math.floorMod(day() * 7 + 3, Words.LIST.size)]
}

@Composable
private fun Glyph(s: String, size: Float, color: androidx.compose.ui.graphics.Color = M.Fg) {
    Text(rtl(s), style = TextStyle(fontFamily = AynehaFont, fontSize = (size * AY_SCALE).sp, color = color))
}

@Composable
private fun LearnCard(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(M.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AyText(title, size = 20.sp, color = M.Orange)
        content()
    }
}

@Composable
fun LearnScreen(onBack: () -> Unit) {
    // Toujours de gauche à droite (français / anglais), même si l'interface est en AYNEHA
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        LearnContent(onBack)
    }
}

@Composable
private fun LearnContent(onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<AyLetter?>(null) }
    Column(Modifier.fillMaxSize()) {
        SubHeader(title = L_TITLE, onBack = onBack)
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(L_DAY, L_ALPHA, L_QUIZ).forEachIndexed { i, label ->
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (tab == i) M.Orange else M.Key)
                        .clickable { tab = i }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) { AyText(label, size = 15.sp, color = if (tab == i) M.Ink else M.Fg, align = TextAlign.Center, maxLines = 2) }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (tab) {
                0 -> TodayTab()
                1 -> AlphabetTab(selected) { selected = it }
                else -> QuizTab()
            }
            Box(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun LetterDetail(l: AyLetter) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Glyph(l.glyph, 54f, M.Orange)
        Text(l.latin, style = TextStyle(fontSize = 34.sp, color = M.Fg))
    }
    l.variants.forEach { (g, k) ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Glyph(g, 34f)
            AyText(Alphabet.kindName(k), size = 17.sp, color = M.Muted)
        }
    }
}

@Composable
private fun TodayTab() {
    val l = Alphabet.letterOfDay()
    LearnCard(L_DAY) { LetterDetail(l) }
    val w = Alphabet.wordOfDay()
    LearnCard(L_WORD) {
        Glyph(w[0], 34f, M.Orange)
        Text(w[1], style = TextStyle(fontSize = 22.sp, color = M.Fg))
        Text(if (Lang.current == Lang.EN) w[3] else w[2], style = TextStyle(fontSize = 16.sp, color = M.Muted))
    }
}

@Composable
private fun AlphabetTab(selected: AyLetter?, onSelect: (AyLetter?) -> Unit) {
    if (selected != null) {
        LearnCard(L_ALPHA) { LetterDetail(selected) }
    }
    // Grille : 5 lettres par ligne, toujours de droite à gauche comme l'écriture AYNEHA
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (Alphabet.LETTERS.map { it.glyph to it.latin } + Alphabet.DIGITS).chunked(5).forEachIndexed { row, chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunk.forEachIndexed { col, (g, latin) ->
                        val letter = Alphabet.LETTERS.getOrNull(row * 5 + col)
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (letter != null && letter == selected) M.Orange else M.Card)
                                .clickable { onSelect(letter) }.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Glyph(g, 26f)
                            Text(latin, style = TextStyle(fontSize = 14.sp, color = M.Muted))
                        }
                    }
                    repeat(5 - chunk.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun QuizTab() {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var answered by remember { mutableStateOf<Int?>(null) }
    val q = remember(round) {
        val letters = Alphabet.LETTERS.shuffled()
        val target = letters[0]
        Triple(target, (letters.take(4)).shuffled(), round)
    }
    val target = q.first
    LearnCard(L_QUIZ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Glyph(target.glyph, 72f, M.Orange) }
        q.second.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { opt ->
                    val idx = q.second.indexOf(opt)
                    val bg = when {
                        answered == null -> M.Key
                        opt == target -> M.Green
                        idx == answered -> M.Red
                        else -> M.Key
                    }
                    Box(
                        Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(14.dp)).background(bg).clickable(enabled = answered == null) {
                            answered = idx; total++; if (opt == target) score++
                        },
                        contentAlignment = Alignment.Center
                    ) { Text(opt.latin, style = TextStyle(fontSize = 24.sp, color = M.Fg)) }
                }
            }
        }
        answered?.let { a ->
            AyText(if (q.second[a] == target) L_OK else L_NO, size = 20.sp, color = if (q.second[a] == target) M.Green else M.Red)
            OrangeButton(L_NEXT) { answered = null; round++ }
        }
        AyText(L_SCORE + " : $score/$total", size = 17.sp, color = M.Muted)
    }
}
