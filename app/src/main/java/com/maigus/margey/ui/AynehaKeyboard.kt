// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - Clavier AYNEHA intégré (disposition 10-10-7 du clavier AYNEHA Keyboard)
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Backspace
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

/** Convertit un code de l'ancienne zone U+E000 vers la nouvelle zone AYNEHA du plan 16. */
private fun g(e: Int): String = String(Character.toChars(0x10FF7C + (e - 0xE000)))

private data class AyKey(val char: String, val variants: List<String> = emptyList())

private fun k(base: Int, vararg v: Int) = AyKey(g(base), v.map { g(it) })

// Ordre logique : l'élément 0 est la touche la plus à droite (la mise en page est RTL).
private val ROW1 = listOf(
    k(0xE020, 0xE025, 0xE026), k(0xE00D, 0xE037, 0xE038), k(0xE021, 0xE027, 0xE028, 0xE029),
    k(0xE023, 0xE02C, 0xE02D, 0xE02E), k(0xE00B, 0xE033, 0xE034), k(0xE022, 0xE02A, 0xE02B),
    k(0xE014, 0xE045, 0xE046), k(0xE017, 0xE04A), k(0xE012, 0xE041, 0xE042), k(0xE00E, 0xE039, 0xE03A),
)
private val ROW2 = listOf(
    k(0xE01E), k(0xE00C, 0xE035, 0xE036), k(0xE011, 0xE03F, 0xE040), k(0xE00A, 0xE031, 0xE032),
    k(0xE010, 0xE03D, 0xE03E), k(0xE01D, 0xE053, 0xE054), k(0xE013, 0xE043, 0xE044),
    k(0xE024, 0xE02F, 0xE030), k(0xE00F, 0xE03B, 0xE03C), k(0xE016, 0xE049),
)
private val ROW3 = listOf(
    k(0xE015, 0xE047, 0xE048), k(0xE018, 0xE04B), k(0xE019, 0xE04C, 0xE04D), k(0xE01A, 0xE04E),
    k(0xE01B, 0xE04F, 0xE050), k(0xE01C, 0xE051, 0xE052), k(0xE01F, 0xE055, 0xE056),
)
private val DIGITS = (1..9).map { AyKey(g(0xE000 + it)) } + AyKey(g(0xE000))
private val SYMBOLS = listOf("…", "!", "؟", ":", "»", "«", "(", ")", "-", "/").map { AyKey(it) }
private val SYMBOLS2 = listOf("@", "#", "%", "+", "=", "&", "_").map { AyKey(it) }
private val COMMA = g(0xE060)
private val SEMICOLON = g(0xE061)

/** Mot « AYNEHA » écrit en AYNEHA, affiché sur la barre d'espace. */
private val SPACE_LABEL = listOf(0xE020, 0xE050, 0xE011, 0xE021, 0xE020, 0xE01E).joinToString("") { g(it) }

/**
 * Clavier AYNEHA, seul moyen de saisie de l'application : aucun clavier système ne s'ouvre.
 * Appui long sur une touche marquée d'un point : variantes (double, muette, nasalisée, ouverte).
 */
@Composable
fun AynehaKeyboard(
    onChar: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by remember { mutableStateOf(0) }
    if (!Kb.ayneha) {
        // Clavier latin intégré (français / anglais) : le clavier du téléphone n'est jamais utilisé
        LatinKeyboard(onChar, onBackspace, onEnter, modifier)
        return
    }
    // Le clavier AYNEHA garde toujours sa disposition de droite à gauche
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
    Column(
        modifier.fillMaxWidth().background(M.KeyBoard).padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (page == 0) {
            KeyRow(ROW1, onChar)
            KeyRow(ROW2, onChar)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FuncKey(1.25f, onBackspace) { Icon(Icons.Filled.Backspace, null, tint = M.Orange, modifier = Modifier.size(22.dp).scale(-1f, 1f)) }
                ROW3.forEach { CharKey(it, onChar) }
                FuncKey(1.25f, onEnter) { Icon(Icons.AutoMirrored.Filled.KeyboardReturn, null, tint = M.Orange) }
            }
        } else {
            KeyRow(DIGITS, onChar)
            KeyRow(SYMBOLS, onChar, ayneha = false)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FuncKey(1.25f, onBackspace) { Icon(Icons.Filled.Backspace, null, tint = M.Orange, modifier = Modifier.size(22.dp).scale(-1f, 1f)) }
                CharKey(AyKey(SEMICOLON), onChar)
                SYMBOLS2.forEach { CharKey(it, onChar, ayneha = false) }
                FuncKey(1.25f, onEnter) { Icon(Icons.AutoMirrored.Filled.KeyboardReturn, null, tint = M.Orange) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FuncKey(1.5f, { page = 1 - page }) {
                if (page == 0) AyGlyph(ayDigits("123") + "؟", 16f, M.Orange) else AyGlyph(g(0xE020) + g(0xE021) + g(0xE022), 16f, M.Orange)
            }
            CharKey(AyKey(COMMA), onChar)
            Box(Modifier.weight(4.5f)) { CharKeyBox(SPACE_LABEL, { onChar(" ") }, M.Muted, 17f) }
            CharKey(AyKey("."), onChar, ayneha = false)
            // Bascule vers le clavier latin intégré (français / anglais)
            FuncKey(1.5f, { Kb.ayneha = false }, bg = M.Orange) {
                Text("abc", style = TextStyle(fontSize = 17.sp, color = M.Ink))
            }
        }
    }
    }
}

@Composable
private fun KeyRow(keys: List<AyKey>, onChar: (String) -> Unit, ayneha: Boolean = true) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { keys.forEach { CharKey(it, onChar, ayneha) } }
}

@Composable
private fun AyGlyph(s: String, size: Float, color: Color) {
    Text(rtl(s), style = TextStyle(fontFamily = AynehaFont, fontSize = (size * AY_SCALE).sp, color = color))
}

@Composable
private fun RowScope.CharKey(key: AyKey, onChar: (String) -> Unit, ayneha: Boolean = true) {
    // Appui court = caractère de base. Appui long = bulle des variantes : on glisse le pouce
    // vers la variante voulue (elle s'allume) et on relâche pour l'écrire, sans recliquer.
    var showVariants by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(-1) }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val cellPx = with(density) { VARIANT_CELL.toPx() }
    val gapPx = with(density) { VARIANT_GAP.toPx() }
    val padPx = with(density) { VARIANT_PAD.toPx() }
    val n = key.variants.size

    fun indexAt(x: Float, keyWidth: Int): Int {
        if (n == 0) return -1
        val rowWidth = n * cellPx + (n - 1) * gapPx + 2 * padPx
        val fromLeft = x - (keyWidth / 2f - rowWidth / 2f) - padPx
        val visual = (fromLeft / (cellPx + gapPx)).toInt().coerceIn(0, n - 1)
        return if (rtl) n - 1 - visual else visual
    }

    Box(
        Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(9.dp)).background(M.Key)
            .pointerInput(key) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val longPressed = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        waitForUpOrCancellation()
                    }
                    if (longPressed != null) {
                        // Relâché avant l'appui long : caractère de base
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onChar(key.char)
                        return@awaitEachGesture
                    }
                    if (n == 0) {
                        // Pas de variante : on écrit le caractère de base au relâchement
                        waitForUpOrCancellation()
                        onChar(key.char)
                        return@awaitEachGesture
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showVariants = true
                    selected = indexAt(down.position.x, size.width)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            val idx = indexAt(change.position.x, size.width)
                            if (idx != selected) {
                                selected = idx
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            change.consume()
                        } else {
                            if (selected in 0 until n) onChar(key.variants[selected])
                            break
                        }
                    }
                    showVariants = false
                    selected = -1
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(if (ayneha) key.char else key.char, style = TextStyle(fontFamily = if (ayneha) AynehaFont else null, fontSize = (22 * AY_SCALE).sp, color = M.Fg))
        if (n > 0) {
            Box(Modifier.align(Alignment.TopEnd).padding(4.dp).size(4.dp).clip(RoundedCornerShape(2.dp)).background(M.Muted2))
        }
        if (showVariants) {
            Popup(alignment = Alignment.TopCenter, offset = androidx.compose.ui.unit.IntOffset(0, -170)) {
                Row(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(M.Card).padding(VARIANT_PAD),
                    horizontalArrangement = Arrangement.spacedBy(VARIANT_GAP)
                ) {
                    key.variants.forEachIndexed { i, v ->
                        val on = i == selected
                        Box(
                            Modifier.size(VARIANT_CELL).clip(RoundedCornerShape(9.dp)).background(if (on) M.Orange else M.Key),
                            contentAlignment = Alignment.Center
                        ) { Text(v, style = TextStyle(fontFamily = AynehaFont, fontSize = (24 * AY_SCALE).sp, color = if (on) M.Ink else M.Fg)) }
                    }
                }
            }
        }
    }
}

private val VARIANT_CELL = 56.dp
private val VARIANT_GAP = 6.dp
private val VARIANT_PAD = 6.dp

@Composable
private fun CharKeyBox(label: String, onTap: () -> Unit, color: Color, size: Float) {
    Box(
        Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(9.dp)).background(M.Key)
            .pointerInput(label) { detectTapGestures(onTap = { onTap() }) },
        contentAlignment = Alignment.Center
    ) { AyGlyph(label, size, color) }
}

@Composable
private fun RowScope.FuncKey(weight: Float, onTap: () -> Unit, bg: Color = M.Card, content: @Composable () -> Unit) {
    Box(
        Modifier.weight(weight).height(46.dp).clip(RoundedCornerShape(9.dp)).background(bg)
            .pointerInput(onTap) { detectTapGestures(onTap = { onTap() }) },
        contentAlignment = Alignment.Center
    ) { content() }
}

// ---------- Clavier latin intégré ----------

private fun lk(c: String, vararg v: String) = AyKey(c, v.toList())

/** Variantes par appui long, avec les lettres du songhay écrit en latin (ɛ, ɔ, ŋ, ɲ, voyelles nasales). */
private val LATIN_VARIANTS = mapOf(
    "a" to listOf("à", "â", "ä", "ã", "æ"), "e" to listOf("é", "è", "ê", "ë", "ɛ", "ẽ"),
    "i" to listOf("î", "ï", "ĩ"), "o" to listOf("ô", "ö", "ɔ", "õ", "œ"), "u" to listOf("ù", "û", "ü", "ũ"),
    "c" to listOf("ç"), "n" to listOf("ñ", "ŋ", "ɲ"), "y" to listOf("ÿ"), "s" to listOf("ʃ"),
)
private fun latinRow(letters: String) = letters.map { c -> lk(c.toString(), *(LATIN_VARIANTS[c.toString()] ?: emptyList()).toTypedArray()) }

/** AZERTY pour le français (et l'AYNEHA), QWERTY pour l'anglais. */
private fun latinRows(): List<List<AyKey>> = if (com.maigus.margey.text.Lang.current == com.maigus.margey.text.Lang.EN)
    listOf(latinRow("qwertyuiop"), latinRow("asdfghjkl"), latinRow("zxcvbnm"))
else listOf(latinRow("azertyuiop"), latinRow("qsdfghjklm"), latinRow("wxcvbn"))

private val LATIN_SYM1 = "1234567890".map { AyKey(it.toString()) }
private val LATIN_SYM2 = listOf("@", "#", "&", "-", "+", "(", ")", "/", ":", ";").map { AyKey(it) }
private val LATIN_SYM3 = listOf("!", "?", "'", "\"", "%", "*", "=").map { AyKey(it) }

@Composable
fun LatinKeyboard(onChar: (String) -> Unit, onBackspace: () -> Unit, onEnter: () -> Unit, modifier: Modifier = Modifier) {
    var page by remember { mutableStateOf(0) }
    var shift by remember { mutableStateOf(false) }
    // Majuscule pour la prochaine lettre seulement
    val type: (String) -> Unit = { c -> onChar(if (shift) c.uppercase() else c); shift = false }
    val rows = latinRows()
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(
            modifier.fillMaxWidth().background(M.KeyBoard).padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (page == 0) {
                val show: (AyKey) -> AyKey = { k -> if (shift) AyKey(k.char.uppercase(), k.variants.map { it.uppercase() }) else k }
                KeyRow(rows[0].map(show), { onChar(it); shift = false }, ayneha = false)
                KeyRow(rows[1].map(show), { onChar(it); shift = false }, ayneha = false)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FuncKey(1.4f, { shift = !shift }, bg = if (shift) M.Orange else M.Card) {
                        Text("⇧", style = TextStyle(fontSize = 22.sp, color = if (shift) M.Ink else M.Orange))
                    }
                    rows[2].map(show).forEach { CharKey(it, { c -> onChar(c); shift = false }, ayneha = false) }
                    FuncKey(1.4f, onBackspace) { Icon(Icons.Filled.Backspace, null, tint = M.Orange, modifier = Modifier.size(22.dp)) }
                }
            } else {
                KeyRow(LATIN_SYM1, type, ayneha = false)
                KeyRow(LATIN_SYM2, type, ayneha = false)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LATIN_SYM3.forEach { CharKey(it, type, ayneha = false) }
                    FuncKey(1.4f, onBackspace) { Icon(Icons.Filled.Backspace, null, tint = M.Orange, modifier = Modifier.size(22.dp)) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Bascule vers le clavier AYNEHA (même place que la bascule du clavier AYNEHA)
                FuncKey(1.5f, { Kb.ayneha = true }, bg = M.Orange) { AyGlyph(SPACE_LABEL, 14f, M.Ink) }
                FuncKey(1.3f, { page = 1 - page }) { Text(if (page == 0) "123" else "abc", style = TextStyle(fontSize = 15.sp, color = M.Orange)) }
                CharKey(AyKey(","), type, ayneha = false)
                FuncKey(4f, { onChar(" ") }) { Text(if (com.maigus.margey.text.Lang.current == com.maigus.margey.text.Lang.EN) "English" else "Français", style = TextStyle(fontSize = 14.sp, color = M.Muted)) }
                CharKey(AyKey("."), type, ayneha = false)
                FuncKey(1.5f, onEnter) { Icon(Icons.AutoMirrored.Filled.KeyboardReturn, null, tint = M.Orange) }
            }
        }
    }
}

/** Efface le dernier caractère (un glyphe du plan 16 occupe deux unités UTF-16). */
fun String.dropLastCodePoint(): String {
    if (isEmpty()) return this
    val cut = offsetByCodePoints(length, -1)
    return substring(0, cut)
}
