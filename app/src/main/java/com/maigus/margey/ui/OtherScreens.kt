// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - Autour de moi, Salons, Réglages, Pseudo
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import android.content.Context
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.ui.ChatViewModel
import com.maigus.margey.text.Ay
import kotlin.math.cos
import kotlin.math.sin

// ---------------------------------------------------------------- Autour de moi (réseau maillé)

@Composable
fun AutourScreen(viewModel: ChatViewModel) {
    val connected by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val direct by viewModel.peerDirect.collectAsStateWithLifecycle()
    val names by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val directPeers = connected.filter { direct[it] == true }
    val relayPeers = connected.filter { direct[it] != true }

    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
        MainHeader(Ay.G03) {
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF1F3B33)).padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(M.Green))
                AyText(Ay.R02, size = 16.sp, color = Color(0xFFCDEBDD))
            }
        }
        MeshMap(directPeers.map { names[it] ?: "" }, relayPeers.map { names[it] ?: "" })
        Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Stat(directPeers.size, Ay.R03, M.Orange, Modifier.weight(1f))
            Stat(relayPeers.size, Ay.R04, M.Fg, Modifier.weight(1f))
            Stat(connected.size, Ay.R05, M.Fg, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        if (connected.isEmpty()) {
            AyText(Ay.R09, size = 19.sp, color = M.Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp))
        }
        Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            connected.forEach { id ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(M.Card).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Avatar(names[id] ?: "", 40)
                    AyText(names[id] ?: "", size = 20.sp, modifier = Modifier.weight(1f))
                    AyText(if (direct[id] == true) Ay.R03 else Ay.R04, size = 15.sp, color = if (direct[id] == true) M.Orange else M.Muted2)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
    // Alerte d'urgence : envoyée à tous les téléphones de la maille
    AlertSender(nickname)
    }
}

@Composable
private fun Stat(value: Int, label: String, color: Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(M.Card).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        AyText(ayNum(value.toString()), size = 30.sp, color = color, align = TextAlign.Center)
        AyText(label, size = 16.sp, color = M.Muted, align = TextAlign.Center)
    }
}

/** Carte du maillage : moi au centre, téléphones en direct sur le premier cercle, relayés sur le second. */
@Composable
private fun MeshMap(directNames: List<String>, relayNames: List<String>) {
    Box(Modifier.fillMaxWidth().height(300.dp).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(290.dp)) {
            val c = Offset(size.width / 2, size.height / 2)
            drawCircle(M.Key, radius = size.minDimension * 0.25f, center = c, style = Stroke(3f))
            drawCircle(M.Line, radius = size.minDimension * 0.47f, center = c, style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f))))
            directNames.forEachIndexed { i, _ ->
                val a = Math.toRadians(-90.0 + i * 360.0 / directNames.size.coerceAtLeast(1))
                val p = Offset(c.x + (size.minDimension * 0.25f * cos(a)).toFloat(), c.y + (size.minDimension * 0.25f * sin(a)).toFloat())
                drawLine(M.Orange, c, p, strokeWidth = 4f)
                drawCircle(M.Sand, radius = 30f, center = p)
            }
            relayNames.forEachIndexed { i, _ ->
                val a = Math.toRadians(-60.0 + i * 360.0 / relayNames.size.coerceAtLeast(1))
                val p = Offset(c.x + (size.minDimension * 0.47f * cos(a)).toFloat(), c.y + (size.minDimension * 0.47f * sin(a)).toFloat())
                drawCircle(M.Key, radius = 22f, center = p)
                drawCircle(Color(0xFF4A6390), radius = 22f, center = p, style = Stroke(3f))
            }
            drawCircle(M.Orange.copy(alpha = 0.2f), radius = 58f, center = c)
            drawCircle(M.Orange, radius = 42f, center = c)
        }
        AyText(String(Character.toChars(0x10FF88)), size = 30.sp, color = M.Ink, align = TextAlign.Center)
    }
}

// ---------------------------------------------------------------- Salons de proximité

/** Durée de conservation des messages des salons : 72 h minimum (règle de l'auteur), ou plus. */
/** Nombre de jours dans la langue de l'interface (AYNEHA : « zaari 7 », comme « zaari 3 » de S09). */
fun daysLabel(n: Int): String = when (com.maigus.margey.text.Lang.current) {
    com.maigus.margey.text.Lang.FR -> if (n == 1) "1 jour" else "$n jours"
    com.maigus.margey.text.Lang.EN -> if (n == 1) "1 day" else "$n days"
    else -> Ay.S11 + " " + ayNum(n.toString())
}

/** Lettre A de base (glyphe AYNEHA), pour illustrer la taille du texte. */
val A_GLYPH: String = String(Character.toChars(0x10FF9D))

/** Rangée de choix (thème, taille, effacement automatique). */
@Composable
fun ChipRow(labels: List<String>, selected: Int, sizes: List<Float>? = null, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (on) M.Orange else M.Key)
                    .clickable { onSelect(i) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AyText(label, size = (sizes?.get(i) ?: 17f).sp, color = if (on) M.Ink else M.Fg, align = TextAlign.Center, maxLines = 1)
            }
        }
    }
}

object SalonRetention {
    const val MIN_HOURS = com.maigus.margey.salon.SalonStore.MIN_HOURS
    fun hours(ctx: Context): Int = com.maigus.margey.salon.SalonStore.hours(ctx)
    fun setHours(ctx: Context, h: Int) = com.maigus.margey.salon.SalonStore.setHours(ctx, h)
    fun isAlive(timestampMs: Long, hours: Int): Boolean = com.maigus.margey.salon.SalonStore.isAlive(timestampMs, hours)
}

// ---------------------------------------------------------------- Réglages

@Composable
fun ReglagesScreen(viewModel: ChatViewModel, go: (Route) -> Unit) {
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val wifiAware by com.bitchat.android.wifiaware.WifiAwareController.enabled.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var confirmWipe by remember { mutableStateOf(false) }
    var pinSetup by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val code = remember { MyCode.of(viewModel) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MainHeaderInline(Ay.G05)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(M.Card).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Avatar(nickname, 60, M.Orange, M.Ink)
                Column(Modifier.weight(1f)) {
                    AyText(nickname, size = 26.sp)
                    AyText(Ay.B11, size = 15.sp, color = M.Muted2)
                    AyText(ayNum(code), size = 17.sp, color = M.Orange)
                }
                Column(Modifier.clickable { draft = nickname; editing = true }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Edit, null, tint = M.Orange, modifier = Modifier.size(22.dp))
                    AyText(Ay.E02, size = 13.sp, color = M.Orange, align = TextAlign.Center)
                }
            }
            SectionTitle(Ay.E04)
            SettingGroup {
                SettingRow(Icons.Filled.Hub, Ay.E05) { AyText(ayNum(com.bitchat.android.util.AppConstants.MESSAGE_TTL_HOPS.toString()), size = 20.sp, color = M.Orange) }
                SettingRow(Icons.Filled.Bluetooth, Ay.P04) { Box(Modifier.size(12.dp).clip(CircleShape).background(M.Green)) }
                SettingRow(Icons.Filled.Wifi, Ay.E07) {
                    Switch(checked = wifiAware, onCheckedChange = { com.bitchat.android.wifiaware.WifiAwareController.setEnabled(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = M.Orange))
                }
            }
            SettingGroup {
                SettingRow(Icons.Filled.Language, LANG_NAMES[com.maigus.margey.text.Lang.current]) {}
                LangChips()
            }
            SettingGroup {
                SettingRow(Icons.Filled.Palette, Ay.E14) {}
                ChipRow(Ay.E15.split(" / "), M.themeMode) { Display.setTheme(context, it) }
                SettingRow(Icons.Filled.FormatSize, Ay.E16) {}
                ChipRow(listOf(0.85f, 1f, 1.2f).map { A_GLYPH }, listOf(0.85f, 1f, 1.2f).indexOf(M.textScale).coerceAtLeast(0),
                    sizes = listOf(15f, 20f, 26f)) { Display.setScale(context, listOf(0.85f, 1f, 1.2f)[it]) }
            }
            SectionTitle(Ay.E09)
            SettingGroup {
                SettingRow(Icons.Filled.Lock, Ay.E12) {
                    Switch(checked = com.maigus.margey.security.AppLock.enabled,
                        onCheckedChange = { on -> if (on) pinSetup = true else com.maigus.margey.security.AppLock.disable() },
                        colors = SwitchDefaults.colors(checkedTrackColor = M.Orange))
                }
                SettingRow(Icons.Filled.AutoDelete, Ay.E10) {}
                ChipRow(
                    com.maigus.margey.security.AutoErase.CHOICES.map { d -> if (d == 0) Ay.E11 else daysLabel(d) },
                    com.maigus.margey.security.AutoErase.CHOICES.indexOf(com.maigus.margey.security.AutoErase.days).coerceAtLeast(0)
                ) { com.maigus.margey.security.AutoErase.set(com.maigus.margey.security.AutoErase.CHOICES[it]) }
                SettingRow(Icons.Filled.Warning, Ay.E13, color = Color(0xFFF2A08E), onClick = { confirmWipe = true })
                SettingRow(Icons.Filled.Shield, Ay.E24, onClick = { go(Route.Confidentialite) })
            }
            SettingGroup {
                SettingRow(Icons.Filled.School, LEARN_TITLE, onClick = { go(Route.Learn) })
                SettingRow(Icons.Filled.Share, Ay.N01, onClick = { com.maigus.margey.features.ShareApp.share(context) })
                SettingRow(Icons.Filled.Info, Ay.E21, onClick = { go(Route.Confidentialite) })
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
                LatinText("By MAIGUS  ")
                AyText(ayNum(MargeyVersion.NAME), size = 14.sp, color = M.Muted2)
            }
        }
        if (editing) {
            Column(Modifier.fillMaxWidth().background(M.Bar).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AyText(Ay.E02, size = 22.sp)
                    AyField(draft, Ay.B07, Modifier.fillMaxWidth())
                    OrangeButton(Ay.G12) { if (draft.isNotBlank()) viewModel.setNickname(draft.trim()); editing = false }
                }
                AynehaKeyboard(onChar = { draft += it }, onBackspace = { draft = draft.dropLastCodePoint() }, onEnter = {})
            }
        }
        if (pinSetup) {
            PinSetupPanel(onDone = { pinSetup = false }, onCancel = { pinSetup = false })
        }
        if (confirmWipe) {
            Column(Modifier.fillMaxWidth().background(M.Bar).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AyText(Ay.M10, size = 22.sp)
                AyText(Ay.D17, size = 18.sp, color = M.Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp)).background(M.Red).clickable { viewModel.panicClearAllData(); confirmWipe = false }, contentAlignment = Alignment.Center) {
                        AyText(Ay.G13, size = 22.sp, align = TextAlign.Center)
                    }
                    Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp)).background(M.Card).clickable { confirmWipe = false }, contentAlignment = Alignment.Center) {
                        AyText(Ay.G08, size = 22.sp, align = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainHeaderInline(title: String) {
    Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        AyText(title, size = 30.sp, modifier = Modifier.weight(1f))
        LangButton()
    }
}

/** Code unique : 12 chiffres tirés de l'empreinte de la clé publique du téléphone (créée sur l'appareil). */
object MyCode {
    fun of(viewModel: ChatViewModel): String = fromFingerprint(runCatching { viewModel.getMyFingerprint() }.getOrDefault(""))
    fun fromFingerprint(fp: String): String {
        val hex = fp.filter { it.isLetterOrDigit() }.take(12)
        if (hex.isEmpty()) return ""
        val n = java.math.BigInteger(hex, 16).mod(java.math.BigInteger.TEN.pow(12)).toString().padStart(12, '0')
        return n.chunked(4).joinToString(" ")
    }
}

object MargeyVersion { const val NAME = "1.0.0" }

// ---------------------------------------------------------------- Premier lancement : pseudo

object PseudoStore {
    private const val PREF = "margey_setup"
    fun needsSetup(viewModel: ChatViewModel): Boolean =
        !viewModel.getApplication<android.app.Application>().getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean("done", false)
    fun markDone(viewModel: ChatViewModel) =
        viewModel.getApplication<android.app.Application>().getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean("done", true).apply()
}

@Composable
fun PseudoScreen(viewModel: ChatViewModel, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(M.Night).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { LangButton() }
        if (step == 0) {
            Column(Modifier.weight(1f).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Spacer(Modifier.height(40.dp))
                Box(Modifier.size(140.dp).clip(CircleShape).background(M.Orange).padding(8.dp).clip(CircleShape).background(M.Card), contentAlignment = Alignment.Center) {
                    AyText(String(Character.toChars(0x10FF88)), size = 64.sp, align = TextAlign.Center)
                }
                AyText(Ay.G01, size = 44.sp, align = TextAlign.Center)
                AyText(Ay.B01, size = 22.sp, color = M.Muted, align = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Pill(Icons.Filled.Wifi, Ay.B02)
                Pill(Icons.Filled.Shield, Ay.B03)
                Spacer(Modifier.weight(1f))
                OrangeButton(Ay.B05) { step = 1 }
                LatinText("By MAIGUS")
            }
        } else {
            Column(Modifier.weight(1f).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AyText(Ay.B06, size = 28.sp, modifier = Modifier.fillMaxWidth())
                AyField(name, Ay.B07, Modifier.fillMaxWidth())
                AyText(Ay.B08, size = 18.sp, color = M.Muted, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f))
                OrangeButton(Ay.G15) {
                    if (name.isNotBlank()) { viewModel.setNickname(name.trim()); onDone() }
                }
            }
            AynehaKeyboard(onChar = { name += it }, onBackspace = { name = name.dropLastCodePoint() }, onEnter = {})
        }
    }
}

@Composable
private fun Pill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(M.Card).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = M.Orange, modifier = Modifier.size(26.dp))
        AyText(text, size = 24.sp, modifier = Modifier.weight(1f))
    }
}
