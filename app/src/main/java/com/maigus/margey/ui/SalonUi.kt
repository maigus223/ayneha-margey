// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - écran des salons : mes salons, salons à portée (un toucher pour entrer),
// création (nom + durée, code unique tiré au hasard) et entrée avec un nom et un code.
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bitchat.android.ui.ChatViewModel
import com.maigus.margey.salon.SalonDirectory
import com.maigus.margey.salon.SalonId
import com.maigus.margey.salon.SalonInfo
import com.maigus.margey.salon.SalonTata
import com.maigus.margey.tata.TataSet
import com.maigus.margey.text.Ay

/** Code du salon (petit « # » orange suivi des 4 chiffres). */
@Composable
fun SalonCodeTag(id: String, size: Float = 15f, color: Color = M.Orange) {
    val code = SalonId.code(id) ?: return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(Icons.Filled.Tag, null, tint = color, modifier = Modifier.size((size + 1).dp))
        AyText(ayNum(code), size = size.sp, color = color, maxLines = 1)
    }
}

/** Entrer dans un salon (et garder sa fiche si on la connaît). */
private fun enterSalon(viewModel: ChatViewModel, id: String, info: SalonInfo?) {
    info?.let { SalonDirectory.remember(it) }
    viewModel.joinChannel(id)
    SalonTata.join(id, viewModel.nickname.value)
    SalonDirectory.announce(force = true)
}

@Composable
private fun SalonIcon() {
    Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(M.Orange), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.Tag, null, tint = M.Ink)
    }
}

@Composable
fun SalonsScreen(viewModel: ChatViewModel, go: (Route) -> Unit) {
    val joined by viewModel.joinedChannels.collectAsStateWithLifecycle()
    SalonsContent(joined, { viewModel.nickname.value }, { id, info -> enterSalon(viewModel, id, info) }, go)
}

/** Contenu de l'écran des salons (séparé du ViewModel pour les captures de test). */
@Composable
fun SalonsContent(joined: Set<String>, nickname: () -> String, enter: (String, SalonInfo?) -> Unit, go: (Route) -> Unit, startPanel: Int = 0) {
    val ledgers by SalonTata.ledgers.collectAsStateWithLifecycle()
    val known by SalonDirectory.known.collectAsStateWithLifecycle()
    val nearbyAll by SalonDirectory.nearby.collectAsStateWithLifecycle()
    var panel by remember { mutableIntStateOf(startPanel) }   // 0 aucun, 1 créer, 2 entrer avec un code
    val now = System.currentTimeMillis()
    val nearby = nearbyAll.values
        .filter { it.info.id !in joined && now - it.lastSeen < SalonDirectory.NEARBY_MS }
        .sortedWith(compareByDescending<com.maigus.margey.salon.NearbySalon> { it.members }.thenBy { it.info.name })

    Column(Modifier.fillMaxSize()) {
        MainHeader(Ay.G04) {
            Icon(Icons.Filled.Dialpad, null, tint = M.Fg, modifier = Modifier.size(44.dp).clickable { panel = 2 }.padding(9.dp))
            Icon(Icons.Filled.Add, null, tint = M.Orange, modifier = Modifier.size(44.dp).clickable { panel = 1 }.padding(8.dp))
        }
        LazyColumn(Modifier.weight(1f)) {
            if (joined.isEmpty() && nearby.isEmpty()) item {
                AyText(Ay.S13, size = 19.sp, color = M.Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp))
            }
            items(joined.toList(), key = { it }) { ch ->
                val days = known[ch]?.days ?: (SalonRetention.hours(androidx.compose.ui.platform.LocalContext.current) / 24)
                Row(
                    Modifier.fillMaxWidth().clickable { go(Route.Salon(ch)) }.padding(horizontal = 18.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SalonIcon()
                    Column(Modifier.weight(1f)) {
                        AyText(known[ch]?.name ?: SalonId.rawName(ch), size = 22.sp, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SalonCodeTag(ch)
                            AyText(fill(Ay.K05, n = TataSet.activeCount(TataSet.view(ledgers[ch].orEmpty(), SalonTata.MAX_MEMBERS))), size = 16.sp, color = M.Muted)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Filled.AccessTime, null, tint = M.Muted2, modifier = Modifier.size(14.dp))
                        AyText(daysLabel(days), size = 15.sp, color = M.Muted2, maxLines = 1)
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(M.Line))
            }
            if (nearby.isNotEmpty()) {
                item { Box(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) { SectionTitle(Ay.S15) } }
                items(nearby, key = { "n" + it.info.id }) { s ->
                    Row(
                        Modifier.fillMaxWidth().clickable { enter(s.info.id, s.info); go(Route.Salon(s.info.id)) }
                            .padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SalonIcon()
                        Column(Modifier.weight(1f)) {
                            AyText(s.info.name, size = 22.sp, maxLines = 1)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SalonCodeTag(s.info.id)
                                AyText(fill(Ay.K05, n = s.members), size = 16.sp, color = M.Muted)
                            }
                            if (s.info.founderNick.isNotBlank()) AyText(fill(Ay.S18, nom = s.info.founderNick), size = 15.sp, color = M.Muted2, maxLines = 1)
                        }
                        Box(Modifier.clip(RoundedCornerShape(12.dp)).background(M.Orange).padding(horizontal = 12.dp, vertical = 8.dp)) {
                            AyText(Ay.S04, size = 17.sp, color = M.Ink, maxLines = 1)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(M.Line))
                }
            }
        }
        when (panel) {
            1 -> CreateSalonPanel(nickname, enter, onDone = { id -> panel = 0; go(Route.Salon(id)) }, onClose = { panel = 0 })
            2 -> JoinSalonPanel(enter, onDone = { id -> panel = 0; go(Route.Salon(id)) }, onClose = { panel = 0 })
        }
    }
}

@Composable
private fun PanelBox(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(M.Bar).padding(top = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) { content() }
}

@Composable
private fun PanelTitle(title: String, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        AyText(title, size = 24.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.Close, null, tint = M.Muted, modifier = Modifier.size(40.dp).clickable { onClose() }.padding(8.dp))
    }
}

@Composable
private fun CreateSalonPanel(nickname: () -> String, enter: (String, SalonInfo?) -> Unit, onDone: (String) -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var days by remember { mutableIntStateOf(SalonInfo.MIN_DAYS) }
    PanelBox {
        PanelTitle(Ay.S02, onClose)
        Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AyField(name, Ay.S03, Modifier.fillMaxWidth())
            AyText(Ay.S12, size = 16.sp, color = M.Muted2)
            AyText(Ay.S08, size = 19.sp, modifier = Modifier.fillMaxWidth())
        }
        val choices = listOf(3, 7, 30, 90)
        ChipRow(choices.map { daysLabel(it) }, choices.indexOf(days).coerceAtLeast(0)) { days = choices[it] }
        Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AyText(Ay.S09, size = 16.sp, color = M.Muted2)
            OrangeButton(Ay.S02) {
                if (SalonId.cleanName(name).isNotEmpty()) {
                    val info = SalonDirectory.create(name, days, nickname())
                    enter(info.id, info)
                    onDone(info.id)
                }
            }
        }
        AynehaKeyboard(onChar = { name += it }, onBackspace = { name = name.dropLastCodePoint() }, onEnter = {})
    }
}

@Composable
private fun JoinSalonPanel(enter: (String, SalonInfo?) -> Unit, onDone: (String) -> Unit, onClose: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var onCode by remember { mutableStateOf(false) }
    PanelBox {
        PanelTitle(Ay.S17, onClose)
        Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AyField(name, Ay.S03, Modifier.fillMaxWidth(), bg = if (!onCode) M.Sand else M.Key, onClick = { onCode = false })
            AyField(ayNum(code), Ay.S16, Modifier.fillMaxWidth(), bg = if (onCode) M.Sand else M.Key, onClick = { onCode = true })
            OrangeButton(Ay.S04) {
                if (SalonId.cleanName(name).isNotEmpty() && code.length == 4) {
                    val id = SalonId.make(name, code)
                    enter(id, SalonDirectory.info(id))
                    onDone(id)
                }
            }
        }
        if (onCode) DigitPad(onDigit = { if (code.length < 4) code += it }, onBackspace = { code = code.dropLast(1) })
        else AynehaKeyboard(onChar = { name += it }, onBackspace = { name = name.dropLastCodePoint() }, onEnter = { onCode = true })
    }
}

/** Pavé de chiffres pour le code du salon (chiffres AYNEHA en AYNEHA, latins sinon). */
@Composable
private fun DigitPad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(M.KeyBoard).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "<")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { k ->
                    Box(
                        Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (k.isEmpty()) Color.Transparent else M.Key)
                            .clickable(enabled = k.isNotEmpty()) { if (k == "<") onBackspace() else onDigit(SalonId.digits(k)) },
                        contentAlignment = Alignment.Center
                    ) {
                        when (k) {
                            "<" -> Icon(Icons.Filled.Backspace, null, tint = M.Orange, modifier = Modifier.size(22.dp))
                            "" -> {}
                            else -> AyText(ayNum(k), size = 24.sp, align = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
