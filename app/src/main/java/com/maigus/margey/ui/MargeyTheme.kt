// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.bitchat.android.R
import com.maigus.margey.text.Lang

/** Palette bleu nuit / orange (thème « Nuit du fleuve » des applications AYNEHA). */
object M {
    /** 0 = automatique (suit le téléphone), 1 = clair, 2 = sombre (E14 / E15). */
    var themeMode by mutableIntStateOf(0)
    /** Résolu par MargeyTheme selon themeMode et le réglage du téléphone. */
    var light by mutableStateOf(false)
    /** Taille du texte AYNEHA (E16). */
    var textScale by mutableFloatStateOf(1f)

    private fun pick(dark: Long, clear: Long) = Color(if (light) clear else dark)
    val Night get() = pick(0xFF0E1A30, 0xFFEEF2F8)
    val Card get() = pick(0xFF182B4D, 0xFFFFFFFF)
    val Bar get() = pick(0xFF13223E, 0xFFE2E8F2)
    val Line get() = pick(0xFF1C3054, 0xFFD3DBE8)
    val Key get() = pick(0xFF22385F, 0xFFFFFFFF)
    val KeyBoard get() = pick(0xFF0B1526, 0xFFD5DCE7)
    /** Couleur du texte principal. */
    val Fg get() = pick(0xFFF5EDDF, 0xFF18213A)
    val Body get() = pick(0xFFD6DEEC, 0xFF2E3B57)
    val Muted get() = pick(0xFFB9C6DD, 0xFF4A5B7A)
    val Muted2 get() = pick(0xFF8FA2C2, 0xFF6B7C99)
    val Orange = Color(0xFFF07C22)
    /** Couleur « papier » : bulles reçues, champs de saisie, QR. */
    val Sand = Color(0xFFF5EDDF)
    val Ink = Color(0xFF18213A)
    val Green = Color(0xFF3E9A72)
    val Red = Color(0xFFC9472F)
    val Share = Color(0xFFFF6600)
}

/** Réglages d'affichage enregistrés (thème, taille du texte). */
object Display {
    private const val PREF = "margey_display"
    fun load(ctx: android.content.Context) {
        val p = ctx.getSharedPreferences(PREF, android.content.Context.MODE_PRIVATE)
        M.themeMode = p.getInt("theme", 0)
        M.textScale = p.getFloat("scale", 1f)
    }
    fun setTheme(ctx: android.content.Context, mode: Int) {
        M.themeMode = mode
        ctx.getSharedPreferences(PREF, android.content.Context.MODE_PRIVATE).edit().putInt("theme", mode).apply()
    }
    fun setScale(ctx: android.content.Context, scale: Float) {
        M.textScale = scale
        ctx.getSharedPreferences(PREF, android.content.Context.MODE_PRIVATE).edit().putFloat("scale", scale).apply()
    }
}

/**
 * Taille des caractères AYNEHA.
 * Règle de l'auteur : Cambria 1 = AYNEHA 1,1 (les tailles de base du code suivent déjà ce rapport
 * face au texte latin). En plus, tant que l'écriture est encore peu connue, toutes les tailles AYNEHA
 * sont multipliées par 1,3 pour être plus visibles et mieux lisibles.
 */
const val AY_SCALE = 1.3f

/** Police AYNEHA Pro (plan 16), embarquée dans l'application. Jamais de gras sur les glyphes. */
val AynehaFont = FontFamily(Font(R.font.ayneha, FontWeight.Normal))

private const val RLO = '‮'
private const val PDF = '‬'

/**
 * Les glyphes AYNEHA (zone privée du plan 16) sont classés « gauche à droite » par Android.
 * On encadre donc chaque texte AYNEHA par RLO…PDF pour forcer l'affichage de droite à gauche,
 * y compris quand le texte passe à la ligne.
 */
fun rtl(s: String): String =
    // Texte provisoire en français (mention pas encore traduite) : pas d'inversion
    if (s.codePoints().noneMatch { it >= 0x10FF7C }) s else "$RLO$s$PDF"

/** Chiffres AYNEHA (plan 16, U+10FF7C = 0), quelle que soit la langue (clavier AYNEHA). */
fun ayDigits(value: String): String = buildString {
    value.forEach { c ->
        if (c in '0'..'9') appendCodePoint(0x10FF7C + (c - '0')) else append(c)
    }
}

/** Nombre dans la langue de l'interface : chiffres AYNEHA en AYNEHA, chiffres latins en français et en anglais. */
fun ayNum(value: String): String = if (Lang.isAyneha) ayDigits(value) else value

/** true si le texte contient des caractères AYNEHA. */
fun hasAyneha(s: String): Boolean = s.codePoints().anyMatch { it >= 0x10FF7C }

/** Remplace [n] par un nombre et [nom] par un pseudo (s'il y a deux [nom], [nom2] remplace le second). */
fun fill(template: String, n: Int? = null, nom: String? = null, nom2: String? = null): String {
    var s = template
    if (n != null) s = s.replace("[n]", ayNum(n.toString()))
    if (nom != null) { val i = s.indexOf("[nom]"); if (i >= 0) s = s.substring(0, i) + nom + s.substring(i + 5) }
    if (nom2 != null) { val i = s.indexOf("[nom]"); if (i >= 0) s = s.substring(0, i) + nom2 + s.substring(i + 5) }
    return s
}

/** Clavier utilisé pour écrire : clavier AYNEHA intégré ou clavier habituel du téléphone. */
object Kb {
    var ayneha by mutableStateOf(true)
}

/**
 * Texte de l'interface ou d'un message. S'il contient de l'AYNEHA : police AYNEHA, droite à gauche,
 * taille x1,3. Sinon (français, anglais, pseudo latin) : police du téléphone, taille normale.
 */
@Composable
fun AyText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 20.sp,
    color: Color = M.Fg,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
) {
    val ay = hasAyneha(text)
    val scale = if (ay) M.textScale * AY_SCALE else M.textScale / 1.1f
    Text(
        text = if (ay) rtl(text) else text,
        modifier = modifier,
        style = TextStyle(fontFamily = if (ay) AynehaFont else null, fontSize = size * scale, color = color, lineHeight = size * (scale * 1.35f),
            // Texte latin : sens de lecture d'après son contenu (gauche à droite), même dans l'interface AYNEHA
            textDirection = if (ay) androidx.compose.ui.text.style.TextDirection.Unspecified else androidx.compose.ui.text.style.TextDirection.Content),
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Texte latin (seulement « By MAIGUS » et la page Confidentialité). */
@Composable
fun LatinText(text: String, modifier: Modifier = Modifier, size: TextUnit = 13.sp, color: Color = M.Muted2) {
    Text(text, modifier = modifier, color = color, fontSize = size)
}

@Composable
fun MargeyTheme(content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    M.light = when (M.themeMode) { 1 -> true; 2 -> false; else -> !systemDark }
    MaterialTheme(
        colorScheme = if (M.light) lightColorScheme(
            primary = M.Orange, onPrimary = M.Ink,
            background = M.Night, onBackground = M.Fg,
            surface = M.Card, onSurface = M.Fg,
        ) else darkColorScheme(
            primary = M.Orange, onPrimary = M.Ink,
            background = M.Night, onBackground = M.Fg,
            surface = M.Card, onSurface = M.Fg,
        )
    ) {
        // AYNEHA : de droite à gauche ; français et anglais : de gauche à droite
        CompositionLocalProvider(LocalLayoutDirection provides if (Lang.isAyneha) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            Box { content() }
        }
    }
}
