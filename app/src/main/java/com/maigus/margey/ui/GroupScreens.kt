// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - écrans des groupes privés
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.ui.ChatViewModel
import com.maigus.margey.group.Group
import com.maigus.margey.group.GroupInvite
import com.maigus.margey.group.GroupStore
import com.maigus.margey.text.Ay
import java.util.Date

/** Icône carrée des groupes. */
@Composable
fun GroupBadge(size: Int = 52) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(14.dp)).background(M.Green), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.Groups, null, tint = Color.White)
    }
}

/** Liste des groupes en tête de l'onglet Discussions. */
@Composable
fun GroupsSection(go: (Route) -> Unit) {
    val groups by GroupStore.groups.collectAsStateWithLifecycle()
    val messages by GroupStore.messages.collectAsStateWithLifecycle()
    if (groups.isEmpty()) return
    Column {
        AyText(Ay.K01, size = 17.sp, color = M.Muted2, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp))
        groups.forEach { g ->
            val last = messages[g.id]?.lastOrNull()
            Row(
                Modifier.fillMaxWidth().clickable { go(Route.Groupe(g.id)) }.padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GroupBadge()
                Column(Modifier.weight(1f)) {
                    AyText(g.name, size = 22.sp, maxLines = 1)
                    AyText(last?.let { it.sender + " : " + it.text } ?: fill(Ay.K05, n = g.members.size), size = 18.sp, color = M.Muted, maxLines = 1)
                }
                if (last != null) AyText(ayTime(last.timestamp), size = 15.sp, color = M.Muted2)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(M.Line))
        }
    }
}

/** K02-K03 : création d'un groupe (nom écrit avec le clavier AYNEHA). */
@Composable
fun CreateGroupPanel(viewModel: ChatViewModel, onCreated: (Group) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth().background(M.Bar).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AyText(Ay.K02, size = 24.sp, modifier = Modifier.fillMaxWidth())
            AyField(name, Ay.K03, Modifier.fillMaxWidth())
            AyText(Ay.K09, size = 16.sp, color = M.Muted2, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    OrangeButton(Ay.K02) {
                        val n = name.trim()
                        if (n.isNotEmpty()) onCreated(GroupStore.create(n, viewModel.getMyFingerprint(), viewModel.nickname.value))
                    }
                }
                Box(
                    Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(16.dp)).background(M.Card).clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) { AyText(Ay.G08, size = 22.sp, align = TextAlign.Center) }
            }
        }
        AynehaKeyboard(onChar = { name += it }, onBackspace = { name = name.dropLastCodePoint() }, onEnter = {})
    }
}

/** K07 / K06 : confirmation après lecture du QR d'un groupe. Renvoie le groupe rejoint par [onJoined]. */
@Composable
fun JoinGroupPanel(viewModel: ChatViewModel, invite: GroupInvite, onJoined: (String) -> Unit, onCancel: () -> Unit) {
    var full by remember { mutableStateOf(invite.count >= GroupStore.MAX_MEMBERS) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(M.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GroupBadge(44)
            Column(Modifier.weight(1f)) {
                AyText(invite.name, size = 24.sp, color = M.Orange)
                AyText(if (full) Ay.K06 else Ay.K07, size = 21.sp, color = if (full) M.Red else M.Fg)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!full) Box(Modifier.weight(1f)) {
                OrangeButton(Ay.G10) {
                    if (GroupStore.join(invite, viewModel.meshServiceFacade, viewModel.getMyFingerprint(), viewModel.nickname.value)) onJoined(invite.id)
                    else full = true
                }
            }
            Box(
                Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(16.dp)).background(M.Key).clickable { onCancel() },
                contentAlignment = Alignment.Center
            ) { AyText(if (full) Ay.G09 else Ay.G11, size = 22.sp, align = TextAlign.Center) }
        }
    }
}

/** Conversation d'un groupe privé. */
@Composable
fun GroupScreen(viewModel: ChatViewModel, groupId: String, onBack: () -> Unit) {
    val groups by GroupStore.groups.collectAsStateWithLifecycle()
    val all by GroupStore.messages.collectAsStateWithLifecycle()
    val g = groups.firstOrNull { it.id == groupId }
    if (g == null) { LaunchedEffect(Unit) { onBack() }; return }
    val myFp = remember { com.maigus.margey.tata.TataKeys.publicHex }
    val messages = all[g.id].orEmpty()
    var showQr by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }
    var showMembers by remember { mutableStateOf(false) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { GroupStore.attach(viewModel.meshServiceFacade, myFp) }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }

    Column(Modifier.fillMaxSize()) {
        SubHeader(
            title = g.name, onBack = onBack,
            leading = { GroupBadge(44) },
            subtitle = { AyText(fill(Ay.K05, n = g.members.size), size = 15.sp, color = M.Muted, modifier = Modifier.clickable { showMembers = true }) },
            actions = {
                Icon(Icons.Filled.Groups, null, tint = M.Fg, modifier = Modifier.size(44.dp).clickable { showMembers = true }.padding(10.dp))
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = M.Muted, modifier = Modifier.size(44.dp).clickable { confirmLeave = true }.padding(10.dp))
            }
        )
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(M.Card).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.AllInclusive, null, tint = M.Green, modifier = Modifier.size(18.dp))
            AyText(Ay.K09, size = 16.sp, color = M.Muted, modifier = Modifier.weight(1f))
        }
        Row(
            Modifier.padding(horizontal = 14.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(M.Orange)
                .clickable { showQr = !showQr }.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.QrCode2, null, tint = M.Ink, modifier = Modifier.size(22.dp))
            AyText(Ay.K04, size = 17.sp, color = M.Ink, modifier = Modifier.weight(1f))
        }
        if (showQr) {
            Column(
                Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(20.dp)).background(M.Card).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (g.members.size >= GroupStore.MAX_MEMBERS) {
                    AyText(Ay.K06, size = 20.sp, color = M.Red, align = TextAlign.Center)
                } else if (!GroupStore.canInvite(g)) {
                    // De 80 à 100 : places réservées à l'administrateur
                    AyText(Ay.T10, size = 20.sp, color = M.Orange, align = TextAlign.Center)
                } else {
                    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(M.Sand).padding(16.dp)) { QrImage(GroupStore.inviteQr(g, nickname), 220) }
                    AyText(Ay.K10, size = 18.sp, color = M.Muted, align = TextAlign.Center)
                }
                Box(Modifier.fillMaxWidth()) { OrangeButton(Ay.G09) { showQr = false } }
            }
        }
        if (confirmLeave) {
            Column(Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(20.dp)).background(M.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AyText(Ay.K08, size = 21.sp, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp)).background(M.Red).clickable {
                            GroupStore.leave(g, viewModel.meshServiceFacade, myFp, viewModel.nickname.value); onBack()
                        }, contentAlignment = Alignment.Center
                    ) { AyText(Ay.G10, size = 22.sp, align = TextAlign.Center) }
                    Box(
                        Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp)).background(M.Key).clickable { confirmLeave = false },
                        contentAlignment = Alignment.Center
                    ) { AyText(Ay.G11, size = 22.sp, align = TextAlign.Center) }
                }
            }
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 14.dp), state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages, key = { it.id }) { m ->
                if (m.info) InfoLine(m.text)
                else {
                    val outgoing = m.fingerprint == myFp
                    Bubble(
                        BitchatMessage(id = m.id, sender = m.sender, content = m.text, timestamp = Date(m.timestamp),
                            type = when (m.media) { "image" -> com.bitchat.android.model.BitchatMessageType.Image; "audio" -> com.bitchat.android.model.BitchatMessageType.Audio; else -> com.bitchat.android.model.BitchatMessageType.Message }),
                        outgoing = outgoing, showSender = !outgoing,
                        onLongPress = { if (outgoing) deleteId = m.id }
                    )
                }
            }
        }
        when {
            showMembers -> MembersPanel(
                g.ledger, myFp,
                canExclude = { x -> x.active && g.adminFp == myFp },
                canReinstate = { x -> GroupStore.canReinstate(g, x) },
                onExclude = { GroupStore.exclude(g, it.fp) },
                onReinstate = { GroupStore.reinstate(g, it.fp) },
                onClose = { showMembers = false }
            )
            deleteId != null -> DeleteMinePanel(onDelete = { GroupStore.deleteMine(g, deleteId!!); deleteId = null }, onCancel = { deleteId = null })
            else -> Composer(extra = {
                MediaButtons(maxVoiceMs = 60_000L, imageMaxDim = 800,
                    onImage = { GroupStore.sendMedia(g, viewModel.nickname.value, "image", it) },
                    onVoice = { GroupStore.sendMedia(g, viewModel.nickname.value, "audio", it) })
            }) { GroupStore.send(g, viewModel.meshServiceFacade, myFp, viewModel.nickname.value, it) }
        }
    }
}
