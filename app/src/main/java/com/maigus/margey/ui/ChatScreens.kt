// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - conversation privée et salon de proximité
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.model.BitchatMessage
import com.bitchat.android.model.DeliveryStatus
import com.bitchat.android.ui.ChatViewModel
import com.maigus.margey.text.Ay

/** Barre du haut des écrans secondaires : flèche retour (à droite en RTL), titre, actions. */
@Composable
fun SubHeader(title: String, onBack: () -> Unit, subtitle: (@Composable () -> Unit)? = null, leading: (@Composable () -> Unit)? = null, actions: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().background(M.Bar).padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = M.Fg, modifier = Modifier.size(44.dp).clickable { onBack() }.padding(10.dp))
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            AyText(title, size = 22.sp, maxLines = 1)
            subtitle?.invoke()
        }
        actions()
        LangButton()
    }
}

/** Champ de saisie AYNEHA (affichage seul : la saisie passe par le clavier AYNEHA intégré). */
@Composable
fun AyField(text: String, placeholder: String, modifier: Modifier = Modifier, bg: Color = M.Sand, onKeyboard: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    // Champ d'affichage : la saisie passe toujours par les claviers intégrés (AYNEHA ou latin),
    // le clavier du téléphone ne s'ouvre jamais dans Margey.
    Row(
        modifier.heightIn(min = 46.dp).clip(RoundedCornerShape(23.dp)).background(bg).clickable { onClick() }
            .padding(start = if (onKeyboard != null) 4.dp else 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône clavier : du côté opposé au bouton d'envoi, sert à cacher / montrer le clavier
        if (onKeyboard != null) Icon(Icons.Filled.Keyboard, null, tint = Color(0xFF5A6478), modifier = Modifier.size(40.dp).clickable { onKeyboard() }.padding(8.dp))
        Box(Modifier.weight(1f).padding(vertical = 4.dp)) {
            if (text.isEmpty()) AyText(placeholder, size = 20.sp, color = Color(0xFF7A8090), modifier = Modifier.fillMaxWidth())
            else AyText(text + "│", size = 22.sp, color = M.Ink, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Zone de rédaction + clavier AYNEHA, partagée par les conversations et les salons. */
@Composable
fun Composer(extra: @Composable () -> Unit = {}, maxVoiceMs: Long = 0L, onVoice: ((String) -> Unit)? = null, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var showKeyboard by remember { mutableStateOf(true) }
    Column {
        VoiceRecordingBar()
        Row(
            Modifier.fillMaxWidth().background(M.Bar).padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AyField(text, Ay.C04, Modifier.weight(1f), onKeyboard = { showKeyboard = !showKeyboard }) { showKeyboard = true }
            if (VoiceUi.session == null) extra()
            // Comme WhatsApp : micro tant que le champ est vide, flèche d'envoi dès qu'on écrit
            if (onVoice != null && text.isBlank()) MicButton(maxVoiceMs, 48, onVoice)
            else Box(
                Modifier.size(48.dp).clip(CircleShape).background(M.Orange).clickable {
                    val t = text.trim()
                    if (t.isNotEmpty()) { onSend(t); text = "" }
                },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = M.Ink) }
        }
        if (showKeyboard) {
            AynehaKeyboard(
                onChar = { text += it },
                onBackspace = { text = text.dropLastCodePoint() },
                onEnter = { val t = text.trim(); if (t.isNotEmpty()) { onSend(t); text = "" } },
            )
        }
    }
}

/** Bulle de message. Reçu : sable à droite. Envoyé : orange à gauche (mise en page RTL). */
@Composable
fun Bubble(msg: BitchatMessage, outgoing: Boolean, showSender: Boolean = false, onLongPress: () -> Unit = {}) {
    val bg = if (outgoing) M.Orange else M.Sand
    // L'interface est en RTL : « Start » = à droite. Mes messages à droite, ceux de l'autre à gauche.
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (outgoing) Arrangement.Absolute.Right else Arrangement.Absolute.Left) {
        Column(
            Modifier.widthIn(max = 300.dp)
                .clip(androidx.compose.foundation.shape.AbsoluteRoundedCornerShape(topLeft = if (outgoing) 18.dp else 4.dp, topRight = if (outgoing) 4.dp else 18.dp, bottomLeft = 18.dp, bottomRight = 18.dp))
                .background(bg).clickable { onLongPress() }.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (showSender && !outgoing) AyText(msg.sender, size = 15.sp, color = Color(0xFFB85A12))
            when (msg.type) {
                com.bitchat.android.model.BitchatMessageType.Image -> ImageContent(msg.content)
                com.bitchat.android.model.BitchatMessageType.Audio -> AudioContent(msg.content)
                else -> AyText(msg.content, size = 23.sp, color = M.Ink)
            }
            Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (outgoing) DeliveryIcon(msg.deliveryStatus)
                AyText(ayTime(msg.timestamp.time), size = 13.sp, color = Color(0xFF5A4A30))
            }
        }
    }
}

@Composable
private fun DeliveryIcon(status: DeliveryStatus?) {
    val tint = M.Ink
    when (status) {
        is DeliveryStatus.Delivered, is DeliveryStatus.Read -> Icon(Icons.Filled.DoneAll, null, tint = tint, modifier = Modifier.size(15.dp))
        is DeliveryStatus.Sent -> Icon(Icons.Filled.Bluetooth, null, tint = tint, modifier = Modifier.size(14.dp))
        else -> Icon(Icons.Filled.AccessTime, null, tint = tint, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel, peerID: String, title: String, onBack: () -> Unit) {
    val chats by viewModel.privateChats.collectAsStateWithLifecycle()
    val connected by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val direct by viewModel.peerDirect.collectAsStateWithLifecycle()
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var shareMsg by remember { mutableStateOf<BitchatMessage?>(null) }

    LaunchedEffect(peerID) { viewModel.startPrivateChat(peerID) }
    DisposableEffect(peerID) { onDispose { viewModel.endPrivateChat() } }

    // Les discussions privées sont rangées sous l'identifiant canonique de la conversation,
    // pas sous l'identifiant de maillage du téléphone.
    val selected by viewModel.selectedPrivateChatPeer.collectAsStateWithLifecycle()
    val canonical = remember(peerID) { com.bitchat.android.services.ContactDirectory.canonicalConversationId(peerID) }
    // Une même personne peut avoir plusieurs identifiants (en ligne / hors ligne) :
    // on réunit les messages de tous ses identifiants pour qu'aucun ne disparaisse.
    val keys = remember(peerID, selected) {
        (listOfNotNull(selected, canonical, peerID) +
            runCatching { com.bitchat.android.services.ContactDirectory.aliasesForConversation(peerID) }.getOrDefault(emptySet()))
            .map { it.lowercase() }.toSet()
    }
    val messages = chats.entries.filter { it.key.lowercase() in keys }.flatMap { it.value }
        .filter { it.sender != "system" }
        .distinctBy { it.id }
        .sortedBy { it.timestamp.time }
    val isHere = peerID in connected
    val isDirect = direct[peerID] == true
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }

    Column(Modifier.fillMaxSize()) {
        SubHeader(
            title = title, onBack = onBack,
            leading = { Avatar(title, 42) },
            subtitle = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(if (isHere) M.Green else M.Muted2))
                    AyText(if (!isHere) Ay.C03 else if (isDirect) Ay.C01 else Ay.C02, size = 16.sp, color = M.Muted)
                }
            },
        )
        LazyColumn(Modifier.weight(1f).padding(horizontal = 14.dp), state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages, key = { it.id }) { m ->
                val out = m.sender == nickname
                Bubble(m, out, onLongPress = { if (m.type == com.bitchat.android.model.BitchatMessageType.Message) shareMsg = m })
            }
        }
        // Messages différés : en attente tant que le destinataire ne les a pas reçus
        if (!isHere && messages.any { com.maigus.margey.features.PendingOutbox.isPending(it, nickname) }) {
            AyText(fill(Ay.N22, nom = title), size = 15.sp, color = M.Muted2, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))
        }
        Composer(
            extra = { PhotoButton(1024) { path -> viewModel.sendImageNote(peerID, null, path) } },
            maxVoiceMs = 60_000L, onVoice = { path -> viewModel.sendVoiceNote(peerID, null, path) }
        ) { viewModel.sendMessage(it) }
    }
    shareMsg?.let { m ->
        ShareSheet(message = m.content, pseudo = nickname, onDismiss = { shareMsg = null }, onShareImage = {
            ShareImage.share(context, m.content, nickname); shareMsg = null
        })
    }
}

@Composable
fun SalonScreen(viewModel: ChatViewModel, channel: String, onBack: () -> Unit) {
    val channelMessages by viewModel.channelMessages.collectAsStateWithLifecycle()
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val ledgers by com.maigus.margey.salon.SalonTata.ledgers.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val salonInfos by com.maigus.margey.salon.SalonDirectory.known.collectAsStateWithLifecycle()
    val hours = salonInfos[channel]?.days?.let { it * 24 } ?: SalonRetention.hours(context)
    val tata = com.maigus.margey.salon.SalonTata
    val myFp = remember { com.maigus.margey.tata.TataKeys.publicHex }
    var joinResult by remember { mutableStateOf(com.maigus.margey.salon.SalonTata.JoinResult.OK) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    var showMembers by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }

    LaunchedEffect(channel) {
        viewModel.endPrivateChat(); viewModel.switchToChannel(channel)
        joinResult = tata.join(channel, nickname)
    }
    DisposableEffect(channel) { onDispose { viewModel.switchToChannel(null) } }

    val ledger = ledgers[channel].orEmpty()
    val messages = channelMessages[channel].orEmpty().filter { SalonRetention.isAlive(it.timestamp.time, hours) }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }

    Column(Modifier.fillMaxSize()) {
        SubHeader(
            title = salonInfos[channel]?.name ?: com.maigus.margey.salon.SalonId.rawName(channel), onBack = onBack,
            leading = {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(M.Orange), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Tag, null, tint = M.Ink)
                }
            },
            subtitle = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SalonCodeTag(channel)
                    AyText(fill(Ay.K05, n = com.maigus.margey.tata.TataSet.activeCount(com.maigus.margey.tata.TataSet.view(ledger, com.maigus.margey.salon.SalonTata.MAX_MEMBERS))), size = 15.sp, color = M.Muted, modifier = Modifier.clickable { showMembers = true })
                }
            },
            actions = {
                Icon(Icons.Filled.Groups, null, tint = M.Fg, modifier = Modifier.size(44.dp).clickable { showMembers = true }.padding(10.dp))
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = M.Muted, modifier = Modifier.size(44.dp).clickable { confirmLeave = true }.padding(10.dp))
            }
        )
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(M.Card).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.AccessTime, null, tint = M.Orange, modifier = Modifier.size(18.dp))
            AyText(fill(Ay.S07, n = hours), size = 16.sp, color = M.Muted, modifier = Modifier.weight(1f))
        }
        if (confirmLeave) {
            Column(Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(20.dp)).background(M.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AyText(Ay.S05, size = 21.sp, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(16.dp)).background(M.Red).clickable {
                            tata.leave(channel); viewModel.leaveChannel(channel); onBack()
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
                if (m.sender == com.maigus.margey.salon.SalonTata.INFO_SENDER) InfoLine(m.content)
                else {
                    val mine = m.sender == nickname
                    Bubble(m, mine, showSender = true, onLongPress = { if (tata.isMine(m.id)) deleteId = m.id })
                }
            }
        }
        val blocked = joinResult != com.maigus.margey.salon.SalonTata.JoinResult.OK || tata.isExcluded(channel, myFp)
        when {
            showMembers -> MembersPanel(
                ledger, myFp,
                canExclude = { x -> x.active && tata.admin(channel) == myFp },
                canReinstate = { x -> tata.canReinstate(channel, x) },
                onExclude = { tata.exclude(channel, it.fp) },
                onReinstate = { tata.reinstate(channel, it.fp) },
                onClose = { showMembers = false }
            )
            deleteId != null -> DeleteMinePanel(onDelete = { tata.deleteMine(channel, deleteId!!); deleteId = null }, onCancel = { deleteId = null })
            blocked -> Row(
                Modifier.fillMaxWidth().background(M.Bar).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Block, null, tint = M.Red, modifier = Modifier.size(22.dp))
                AyText(if (tata.isExcluded(channel, myFp)) fill(Ay.T03, nom = nickname) else Ay.S14, size = 19.sp, color = M.Muted, modifier = Modifier.weight(1f))
            }
            else -> Composer(
                extra = { PhotoButton(720) { tata.sendMedia(channel, nickname, "image", it) } },
                maxVoiceMs = 30_000L, onVoice = { tata.sendMedia(channel, nickname, "audio", it) }
            ) { tata.send(channel, nickname, it) }
        }
    }
}

/** Feuille de partage : image, QR, Bluetooth (P01-P10). */
@Composable
fun ShareSheet(message: String, pseudo: String, onDismiss: () -> Unit, onShareImage: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xB3040914)).clickable { onDismiss() }, contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(M.Bar)
                .clickable(enabled = false) {}.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AyText(Ay.G14, size = 28.sp)
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(14.dp)) {
                AyText(Ay.G01, size = 18.sp, color = M.Share)
                AyText(message, size = 32.sp, color = Color.Black, modifier = Modifier.padding(vertical = 6.dp))
                AyText(pseudo, size = 15.sp, color = Color(0xFF444444))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShareTile(Icons.Filled.Share, Ay.D11, Modifier.weight(1f), onShareImage)
            }
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp)).background(M.Card).clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) { AyText(Ay.G08, size = 22.sp, align = TextAlign.Center) }
        }
    }
}

@Composable
private fun ShareTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.height(92.dp).clip(RoundedCornerShape(18.dp)).background(M.Key).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = M.Orange, modifier = Modifier.size(28.dp))
        AyText(label, size = 20.sp, align = TextAlign.Center)
    }
}

@Composable
fun MeshBadge(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(Icons.Filled.Hub, null, tint = M.Ink, modifier = Modifier.size(13.dp))
        AyText(ayNum(count.toString()), size = 13.sp, color = M.Ink)
    }
}
