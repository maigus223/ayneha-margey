// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - application de messagerie AYNEHA sans Internet (Bluetooth maillé)
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
// Basée sur bitchat-android (GPL v3) : https://github.com/permissionlesstech/bitchat-android
package com.maigus.margey.ui

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.setValue

import androidx.compose.runtime.mutableStateOf

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.ui.ChatViewModel
import com.maigus.margey.text.Ay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Écrans de l'application. */
sealed class Route {
    object Discussions : Route()
    object Autour : Route()
    object Salons : Route()
    object Reglages : Route()
    object Contact : Route()
    object Confidentialite : Route()
    data class Chat(val peerID: String, val title: String) : Route()
    data class Salon(val channel: String) : Route()
    data class Groupe(val id: String) : Route()
    object Learn : Route()
}

private val TABS = listOf(Route.Discussions, Route.Autour, Route.Salons, Route.Reglages)

/** Point d'entrée de l'interface AYNEHA Margey (remplace l'interface d'origine de bitchat). */
@Composable
fun MargeyApp(viewModel: ChatViewModel) {
    MargeyTheme {
        val stack = remember { mutableStateListOf<Route>(Route.Discussions) }
        val current = stack.last()
        fun go(r: Route) { if (r in TABS) { stack.clear(); stack.add(r) } else stack.add(r) }
        fun back() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
        BackHandler(enabled = stack.size > 1) { back() }

        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
            val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
                when (e) {
                    androidx.lifecycle.Lifecycle.Event.ON_STOP -> com.maigus.margey.security.AppLock.onStop()
                    androidx.lifecycle.Lifecycle.Event.ON_START -> com.maigus.margey.security.AppLock.onStart()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(obs)
            onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
        }
        if (com.maigus.margey.security.AppLock.locked) {
            LockScreen()
            return@MargeyTheme
        }

        LaunchedEffect(Unit) {
            // Tata-Set : brancher les groupes et les salons sur le maillage, textes des avis
            val fp = runCatching { viewModel.getMyFingerprint() }.getOrDefault("")
            com.maigus.margey.group.GroupStore.attach(viewModel.meshServiceFacade, fp)
            com.maigus.margey.salon.SalonTata.attach(viewModel.meshServiceFacade, fp, viewModel.myPeerID)
            com.maigus.margey.salon.SalonDirectory.attach(viewModel.meshServiceFacade)
            com.maigus.margey.group.GroupStore.infoText = ::tataInfo
            com.maigus.margey.salon.SalonTata.infoText = ::tataInfo
            com.maigus.margey.features.Alerts.attach(viewModel.meshServiceFacade)
            com.maigus.margey.features.Courier.attach(viewModel.meshServiceFacade)
        }
        // Messages différés et alertes : à chaque téléphone qui apparaît à portée
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val peersNow by viewModel.connectedPeers.collectAsStateWithLifecycle()
        LaunchedEffect(peersNow) {
            com.maigus.margey.features.PendingOutbox.onPeers(ctx, viewModel.meshServiceFacade, peersNow, viewModel.nickname.value)
            if (peersNow.isNotEmpty()) {
                com.maigus.margey.features.Alerts.onPeersChanged()
                com.maigus.margey.salon.SalonDirectory.onPeersChanged()
                com.maigus.margey.features.Courier.onPeersChanged(viewModel.nickname.value)
            }
        }
        // Facteurs : chaque message en attente pour un absent est confié aux autres téléphones
        val chatsNow by viewModel.privateChats.collectAsStateWithLifecycle()
        LaunchedEffect(chatsNow, peersNow) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.maigus.margey.features.PendingOutbox.courierize(peersNow, viewModel.nickname.value)
            }
        }
        val nickname by viewModel.nickname.collectAsStateWithLifecycle()
        if (nickname.isBlank() || PseudoStore.needsSetup(viewModel)) {
            PseudoScreen(viewModel) { PseudoStore.markDone(viewModel) ; go(Route.Discussions) }
            return@MargeyTheme
        }

        Column(Modifier.fillMaxSize().background(M.Night).statusBarsPadding().navigationBarsPadding()) {
            Box(Modifier.weight(1f)) {
                when (current) {
                    Route.Discussions -> DiscussionsScreen(viewModel, ::go)
                    Route.Autour -> AutourScreen(viewModel)
                    Route.Salons -> SalonsScreen(viewModel, ::go)
                    Route.Reglages -> ReglagesScreen(viewModel, ::go)
                    Route.Contact -> ContactScreen(viewModel, ::back, openChat = { id, name -> back(); go(Route.Chat(id, name)) }, openGroup = { gid -> back(); go(Route.Groupe(gid)) })
                    Route.Confidentialite -> ConfidentialiteScreen(::back)
                    Route.Learn -> LearnScreen(::back)
                    is Route.Chat -> ChatScreen(viewModel, current.peerID, current.title, ::back)
                    is Route.Salon -> SalonScreen(viewModel, current.channel, ::back)
                    is Route.Groupe -> GroupScreen(viewModel, current.id, ::back)
                }
            }
            if (current in TABS) BottomNav(current, ::go)
        }
    }
}

@Composable
private fun BottomNav(current: Route, go: (Route) -> Unit) {
    val items: List<Triple<Route, ImageVector, String>> = listOf(
        Triple(Route.Discussions, Icons.AutoMirrored.Filled.Chat, Ay.G02),
        Triple(Route.Autour, Icons.Filled.Radar, Ay.G03),
        Triple(Route.Salons, Icons.Filled.Tag, Ay.G04),
        Triple(Route.Reglages, Icons.Filled.Settings, Ay.G05),
    )
    Row(Modifier.fillMaxWidth().background(M.Bar).padding(top = 10.dp, bottom = 12.dp)) {
        items.forEach { (route, icon, label) ->
            val color = if (route == current) M.Orange else M.Muted2
            Column(
                Modifier.weight(1f).clickable { go(route) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                AyText(label, size = 15.sp, color = color, align = TextAlign.Center, maxLines = 1)
            }
        }
    }
}

/** En-tête des écrans principaux : titre AYNEHA à droite, actions à gauche. */
@Composable
fun MainHeader(title: String, actions: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AyText(title, size = 30.sp, modifier = Modifier.weight(1f))
        actions()
        LangButton()
    }
}

/** Avatar : première lettre du pseudo, sur fond sable. */
@Composable
fun Avatar(name: String, size: Int = 52, bg: Color = M.Sand, fg: Color = M.Ink) {
    val first = if (name.isEmpty()) "" else name.substring(0, name.offsetByCodePoints(0, 1))
    Box(Modifier.size(size.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        AyText(first, size = (size / 2).sp, color = fg, align = TextAlign.Center)
    }
}

/** Heure en chiffres AYNEHA, ou « bi » (hier). */
fun ayTime(ms: Long): String {
    if (ms <= 0) return ""
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = ms }
    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    return if (sameDay) ayNum(SimpleDateFormat("HH:mm", Locale.ROOT).format(then.time))
    else if (now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) == 1) Ay.G19
    else ayNum(SimpleDateFormat("dd.MM", Locale.ROOT).format(then.time))
}

@Composable
private fun DiscussionsScreen(viewModel: ChatViewModel, go: (Route) -> Unit) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val connected by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val groups by com.maigus.margey.group.GroupStore.groups.collectAsStateWithLifecycle()
    var creatingGroup by remember { mutableStateOf(false) }
    if (creatingGroup) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            CreateGroupPanel(viewModel, onCreated = { creatingGroup = false; go(Route.Groupe(it.id)) }, onCancel = { creatingGroup = false })
        }
        return
    }
    Column(Modifier.fillMaxSize()) {
        MainHeader(Ay.G01) {
            Icon(Icons.Filled.GroupAdd, null, tint = M.Fg, modifier = Modifier.size(44.dp).clickable { creatingGroup = true }.padding(10.dp))
            Icon(Icons.Filled.QrCode2, null, tint = M.Fg, modifier = Modifier.size(44.dp).clickable { go(Route.Contact) }.padding(10.dp))
        }
        Row(
            Modifier.padding(horizontal = 18.dp).clip(RoundedCornerShape(50)).background(M.Card).padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Hub, null, tint = M.Orange, modifier = Modifier.size(18.dp))
            AyText(fill(Ay.D01, n = connected.size), size = 18.sp)
            Box(Modifier.size(9.dp).clip(CircleShape).background(if (connected.isEmpty()) M.Muted2 else M.Green))
        }
        Spacer(Modifier.height(8.dp))
        AlertsBanner(viewModel.nickname.collectAsStateWithLifecycle().value)
        GroupsSection(go)
        val pull = com.maigus.margey.update.rememberPullToUpdate()
        if (conversations.isEmpty() && groups.isEmpty()) {
            LazyColumn(Modifier.fillMaxSize().nestedScroll(pull)) { item {
                Column(Modifier.fillParentMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    AyText(Ay.D03, size = 22.sp, align = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    AyText(Ay.D04, size = 18.sp, color = M.Muted, align = TextAlign.Center)
                }
            } }
        } else {
            LazyColumn(Modifier.fillMaxSize().nestedScroll(pull)) {
                items(conversations, key = { it.conversationID }) { c ->
                    Row(
                        Modifier.fillMaxWidth().clickable { go(Route.Chat(c.connectedPeerID ?: c.conversationID, c.displayName)) }
                            .padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Avatar(c.displayName)
                        Column(Modifier.weight(1f)) {
                            AyText(c.displayName, size = 22.sp, maxLines = 1)
                            AyText(c.latestMessagePreview, size = 18.sp, color = M.Muted, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            AyText(ayTime(c.latestMessageAt), size = 15.sp, color = if (c.unreadCount > 0) M.Orange else M.Muted2)
                            if (c.unreadCount > 0) {
                                Box(Modifier.size(24.dp).clip(CircleShape).background(M.Orange), contentAlignment = Alignment.Center) {
                                    AyText(ayNum(c.unreadCount.toString()), size = 15.sp, color = M.Ink, align = TextAlign.Center)
                                }
                            } else if (c.isConnected) {
                                Box(Modifier.padding(top = 6.dp).size(9.dp).clip(CircleShape).background(M.Green))
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(M.Line))
                }
            }
        }
    }
}

/** Bouton orange plein, texte AYNEHA. */
@Composable
fun OrangeButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)).background(M.Orange).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { AyText(label, size = 26.sp, color = M.Ink, align = TextAlign.Center) }
}

/** Ligne de réglage : icône, libellé AYNEHA, contenu à gauche. */
@Composable
fun SettingRow(icon: ImageVector, label: String, color: Color = M.Fg, onClick: (() -> Unit)? = null, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().let { if (onClick != null) it.clickable { onClick() } else it }.padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = if (color == M.Fg) M.Muted else color, modifier = Modifier.size(22.dp))
        AyText(label, size = 20.sp, color = color, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun SettingGroup(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(M.Card)) { content() }
}

@Composable
fun SectionTitle(text: String) {
    AyText(text, size = 17.sp, color = M.Orange, modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 8.dp))
}

@Composable
fun HSpace(w: Int) = Spacer(Modifier.width(w.dp))
