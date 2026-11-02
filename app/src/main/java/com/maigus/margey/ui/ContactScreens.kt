// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - ajout de contact par QR, partage en image, page Confidentialité
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
// Lecteur QR repris de bitchat-android (GPL v3), VerificationSheet.kt
package com.maigus.margey.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitchat.android.R
import com.bitchat.android.services.VerificationService
import com.bitchat.android.ui.ChatViewModel
import com.bitchat.android.ui.QRCodeAnalyzer
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.maigus.margey.text.Ay
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun ContactScreen(viewModel: ChatViewModel, onBack: () -> Unit, openChat: (String, String) -> Unit = { _, _ -> }, openGroup: (String) -> Unit = {}) {
    var tab by remember { mutableIntStateOf(0) }
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val connected by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val names by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val qr = remember(nickname) { viewModel.buildMyQRString(nickname, null) }
    val code = remember { MyCode.of(viewModel) }
    var done by remember { mutableStateOf(false) }
    var groupInvite by remember { mutableStateOf<com.maigus.margey.group.GroupInvite?>(null) }

    Column(Modifier.fillMaxSize()) {
        SubHeader(Ay.A01, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(M.Card).padding(4.dp)) {
                listOf(Ay.A02, Ay.A03).forEachIndexed { i, label ->
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(11.dp)).background(if (tab == i) M.Orange else Color.Transparent).clickable { tab = i },
                        contentAlignment = Alignment.Center
                    ) { AyText(label, size = 21.sp, color = if (tab == i) M.Ink else M.Muted, align = TextAlign.Center) }
                }
            }
            if (tab == 0) {
                Column(
                    Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(26.dp)).background(M.Sand).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QrImage(qr, 236)
                    AyText(nickname, size = 28.sp, color = M.Ink, align = TextAlign.Center)
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(M.Card).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Shield, null, tint = M.Green, modifier = Modifier.size(18.dp))
                        AyText(Ay.A04, size = 18.sp, color = M.Muted)
                    }
                    AyText(ayNum(code), size = 26.sp, align = TextAlign.Center)
                    AyText(Ay.A05, size = 17.sp, color = M.Muted2, align = TextAlign.Center)
                }
            } else {
                val inv = groupInvite
                if (inv != null) {
                    JoinGroupPanel(viewModel, inv, onJoined = { groupInvite = null; openGroup(it) }, onCancel = { groupInvite = null })
                } else Scanner { text ->
                    val g = com.maigus.margey.group.GroupStore.parseInvite(text)
                    if (g != null) { groupInvite = g; return@Scanner }
                    val parsed = VerificationService.verifyScannedQR(text)
                    if (parsed != null && viewModel.beginQRVerification(parsed)) done = true
                }
                if (done) androidx.compose.runtime.LaunchedEffect(Unit) { kotlinx.coroutines.delay(1500); onBack() }
                if (done) AyText(Ay.A09, size = 22.sp, color = M.Green, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                else AyText(Ay.A11, size = 19.sp, color = M.Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Bluetooth, null, tint = M.Orange, modifier = Modifier.size(18.dp))
                AyText(Ay.P05, size = 19.sp, color = M.Muted)
            }
            connected.forEach { id ->
                val name = names[id] ?: ""
                // Toucher un téléphone proche ouvre directement la discussion avec lui
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(M.Card)
                        .clickable { openChat(id, name) }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Avatar(name, 42)
                    AyText(name, size = 20.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = M.Orange, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

@Composable
fun QrImage(data: String, sizeDp: Int) {
    val bmp = remember(data) {
        runCatching {
            val m = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 600, 600)
            Bitmap.createBitmap(m.width, m.height, Bitmap.Config.RGB_565).also { b ->
                for (x in 0 until m.width) for (y in 0 until m.height) b.setPixel(x, y, if (m[x, y]) 0xFF0E1A30.toInt() else 0xFFF5EDDF.toInt())
            }
        }.getOrNull()
    }
    if (bmp != null) Image(bmp.asImageBitmap(), null, Modifier.size(sizeDp.dp))
}

@Composable
private fun Scanner(onScan: (String) -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    Box(Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(24.dp)).background(Color.Black), contentAlignment = Alignment.Center) {
        if (granted) ScannerView(onScan)
        else Box(Modifier.clip(RoundedCornerShape(16.dp)).background(M.Orange).clickable { launcher.launch(Manifest.permission.CAMERA) }.padding(16.dp)) {
            AyText(Ay.B17, size = 22.sp, color = M.Ink, align = TextAlign.Center)
        }
    }
}

@Composable
private fun ScannerView(onScan: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val providerFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val requests = remember { MutableStateFlow<SurfaceRequest?>(null) }
    val request by requests.collectAsState(initial = null)
    val handler = remember { Handler(Looper.getMainLooper()) }
    val onCode = rememberUpdatedState(onScan)
    val analyzer = remember { QRCodeAnalyzer { t -> handler.post { onCode.value(t) } } }
    DisposableEffect(Unit) {
        var provider: ProcessCameraProvider? = null
        providerFuture.addListener({
            val p = providerFuture.get(); provider = p
            val preview = Preview.Builder().build().also { it.setSurfaceProvider { r -> requests.value = r } }
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(executor, analyzer) }
            runCatching { p.unbindAll(); p.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis) }
        }, ContextCompat.getMainExecutor(context))
        onDispose { requests.value = null; runCatching { provider?.unbindAll() }; executor.shutdown() }
    }
    request?.let { CameraXViewfinder(surfaceRequest = it, implementationMode = ImplementationMode.EMBEDDED, modifier = Modifier.fillMaxSize()) }
}

/** Partage en image : fond blanc, « AYNEHA Margey » en orange, texte AYNEHA, pseudo et horodatage. */
object ShareImage {
    fun share(context: Context, text: String, pseudo: String) {
        val font: Typeface = ResourcesCompat.getFont(context, R.font.ayneha) ?: Typeface.DEFAULT
        val w = 1080
        // Texte AYNEHA : police AYNEHA, droite à gauche, x1,3 ; texte latin : police normale
        val ay = hasAyneha(text)
        val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = if (ay) font else Typeface.DEFAULT
            textSize = if (ay) 84f * AY_SCALE else 84f / 1.1f
            color = android.graphics.Color.BLACK
        }
        val shown = if (ay) rtl(text) else text
        val layout = StaticLayout.Builder.obtain(shown, 0, shown.length, body, w - 120)
            .setAlignment(if (ay) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL).build()
        val h = layout.height + 520
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(android.graphics.Color.WHITE)
        val head = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = if (hasAyneha(Ay.G01)) font else Typeface.DEFAULT; textSize = if (hasAyneha(Ay.G01)) 54f * AY_SCALE else 54f; color = 0xFFFF6600.toInt(); textAlign = Paint.Align.RIGHT }
        c.drawText(rtl(Ay.G01), (w - 60).toFloat(), 150f, head)
        c.save(); c.translate(60f, 230f); layout.draw(c); c.restore()
        val foot = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = font; textSize = 40f * AY_SCALE; color = 0xFF444444.toInt(); textAlign = Paint.Align.RIGHT }
        c.drawText(rtl(pseudo), (w - 60).toFloat(), (h - 70).toFloat(), foot)
        foot.textAlign = Paint.Align.LEFT
        c.drawText(rtl(ayNum(SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT).format(Date()))), 60f, (h - 70).toFloat(), foot)
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "margey.png")
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/** Page Confidentialité (en français, comme pour AYNEHA Carnet). */
@Composable
fun ConfidentialiteScreen(onBack: () -> Unit) {
    val sectionsFr = listOf(
        "Margey ne collecte aucune donnée." to "Pas de compte, pas de numéro de téléphone, pas de serveur, pas de publicité, pas de traceur. L'application n'a même pas l'autorisation d'accéder à Internet : tout reste sur ton téléphone.",
        "Ton identité" to "Au premier lancement, ton téléphone crée lui-même ton code unique, sans connexion. Seuls ton pseudo et ce code sont visibles par les personnes à qui tu les montres (code QR). Tu peux changer ton pseudo à tout moment.",
        "Tes messages" to "Les messages sont enregistrés uniquement sur ton téléphone et sur celui de ton correspondant. Les messages privés sont chiffrés de bout en bout : seul le destinataire peut les lire.",
        "Le relais (réseau maillé)" to "Chaque téléphone Margey relaie automatiquement les messages des autres pour faire vivre le réseau. Un message privé relayé reste chiffré : ton téléphone le transporte sans pouvoir le lire. Si tu désactives le Bluetooth, tu deviens indisponible jusqu'à sa réactivation.",
        "Les salons" to "Les messages d'un salon sont publics : tous les téléphones à portée du salon peuvent les lire. Ils sont effacés après la durée choisie, de 3 jours au minimum à 90 jours au maximum. Un salon accueille 500 personnes au maximum.",
        "Les groupes privés" to "On entre dans un groupe uniquement en scannant son code QR d'invitation : ne le montre qu'aux personnes invitées. Les messages du groupe sont chiffrés : seuls les membres peuvent les lire, les autres téléphones les relaient sans pouvoir les lire. Un groupe accueille 100 personnes au maximum. Les messages du groupe ne s'effacent jamais d'eux-mêmes ; seul l'effacement d'urgence ou la sortie du groupe les supprime de ton téléphone.",
        "Les autorisations" to "Bluetooth et appareils à proximité : trouver les téléphones proches, envoyer et relayer les messages. Wi-Fi (sans Internet) : liaison directe entre téléphones compatibles. Localisation : exigée par Android sur certains téléphones pour la recherche Bluetooth ; Margey n'enregistre ni n'envoie jamais ta position. Caméra : scanner un code QR, uniquement quand tu le demandes. Micro : messages vocaux, uniquement pendant l'enregistrement. Notifications : t'avertir d'un nouveau message.",
        "Partage en image" to "L'image d'un message est créée sur ton téléphone, seulement pour l'application que tu choisis. Margey ne la conserve pas et ne l'envoie nulle part ailleurs.",
        "Effacer tes données" to "Tu peux tout effacer d'un coup avec l'effacement d'urgence dans les Réglages. Tu peux aussi choisir l'effacement automatique des discussions privées. Désinstaller Margey supprime tout ce qu'elle contient.",
        "Logiciel libre" to "Margey est un logiciel libre développé par MAIGUS avec l'aide d'outils d'IA (Claude, Anthropic), à partir du projet libre bitchat (GPL v3). Code sous licence GNU GPL v3 ou ultérieure, police AYNEHA sous SIL Open Font License 1.1, logos, images et textes sous CC BY-SA 4.0. Code source : github.com/maigus223/ayneha-margey",
        "Contact" to "Développé par MAIGUS : gwokmt2q@duck.com",
    )
    val sectionsEn = listOf(
        "Margey collects no data." to "No account, no phone number, no server, no ads, no trackers. The app does not even have permission to access the Internet: everything stays on your phone.",
        "Your identity" to "On first launch, your phone creates your unique code by itself, offline. Only your nickname and this code are visible to the people you show them to (QR code). You can change your nickname at any time.",
        "Your messages" to "Messages are stored only on your phone and on your contact's phone. Private messages are end-to-end encrypted: only the recipient can read them.",
        "The relay (mesh network)" to "Every Margey phone automatically relays other people's messages to keep the network alive. A relayed private message stays encrypted: your phone carries it without being able to read it. If you turn Bluetooth off, you are unavailable until you turn it back on.",
        "Rooms" to "Room messages are public: every phone in the room can read them. They are deleted after the chosen period, from 3 days minimum to 90 days maximum, or earlier by their author. A room holds 500 people maximum.",
        "Private groups" to "You join a group only by scanning its invitation QR code: show it only to the people you invite. Group messages are encrypted: only members can read them; other phones relay them without being able to read them. A group holds 100 people maximum. Group messages are never deleted automatically; only their author, the emergency wipe or leaving the group removes them from your phone.",
        "Permissions" to "Bluetooth and nearby devices: find nearby phones, send and relay messages. Wi-Fi (no Internet): direct link between compatible phones. Location: required by Android on some phones for Bluetooth scanning; Margey never records or sends your position. Camera: scan a QR code, only when you ask. Microphone: voice messages, only while recording. Notifications: tell you about a new message.",
        "Sharing as an image" to "The image of a message is created on your phone, only for the app you choose. Margey does not keep it or send it anywhere else.",
        "Erasing your data" to "You can erase everything at once with the emergency wipe in Settings. You can also choose automatic deletion of private chats. Uninstalling Margey removes everything it contains.",
        "Free software" to "Margey is free software developed by MAIGUS with the help of AI tools (Claude, Anthropic), based on the free project bitchat (GPL v3). Code under GNU GPL v3 or later, AYNEHA font under SIL Open Font License 1.1, logos, images and texts under CC BY-SA 4.0. Source code: github.com/maigus223/ayneha-margey",
        "Contact" to "Developed by MAIGUS: gwokmt2q@duck.com",
    )
    val en = com.maigus.margey.text.Lang.current == com.maigus.margey.text.Lang.EN
    val sections = if (en) sectionsEn else sectionsFr
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().background(M.Bar).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("←", color = M.Fg, fontSize = 24.sp, modifier = Modifier.clickable { onBack() }.padding(end = 12.dp))
                Text(if (en) "Privacy" else "Confidentialité", color = M.Fg, fontSize = 22.sp, modifier = Modifier.weight(1f))
                LangButton()
            }
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MargeyLogo(88.dp, Modifier.align(Alignment.CenterHorizontally))
                sections.forEachIndexed { i, (t, b) ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(t, color = if (i == 0) M.Orange else M.Fg, fontSize = 17.sp)
                        Text(b, color = M.Body, fontSize = 15.sp, lineHeight = 22.sp)
                    }
                }
                Text(if (en) "Last updated: 1 November 2026" else "Dernière mise à jour : 1er novembre 2026", color = M.Muted, fontSize = 14.sp)
            }
        }
    }
}
