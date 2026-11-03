// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - photos et messages vocaux dans les discussions privées
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bitchat.android.features.media.ImageUtils
import com.bitchat.android.features.voice.VoiceRecorder
import com.maigus.margey.text.Ay

/** Photo d'un message (chemin du fichier reçu ou envoyé). */
@Composable
fun ImageContent(path: String) {
    val bmp = remember(path) { runCatching { ImageUtils.loadBitmapWithExifOrientation(path) }.getOrNull() }
    if (bmp == null) { AyText(Ay.D11, size = 18.sp, color = M.Ink); return }
    var open by remember { mutableStateOf(false) }
    Image(bmp.asImageBitmap(), null, contentScale = ContentScale.Fit,
        modifier = Modifier.widthIn(max = 240.dp).heightIn(max = 300.dp).clip(RoundedCornerShape(12.dp)).clickable { open = true })
    if (open) ImageViewer(bmp) { open = false }
}

/** Photo en plein écran : pincer pour agrandir, glisser pour se déplacer, double toucher pour revenir. */
@Composable
fun ImageViewer(bmp: android.graphics.Bitmap, onClose: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by remember { mutableFloatStateOf(1f) }
        var pan by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {
            Image(bmp.asImageBitmap(), null, contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, p, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 6f)
                            pan = if (scale == 1f) androidx.compose.ui.geometry.Offset.Zero else pan + p
                        }
                    }
                    .pointerInput(Unit) { detectTapGestures(onDoubleTap = { scale = 1f; pan = androidx.compose.ui.geometry.Offset.Zero }) }
                    .graphicsLayer { scaleX = scale; scaleY = scale; translationX = pan.x; translationY = pan.y })
            Box(Modifier.align(Alignment.TopEnd).padding(16.dp).size(48.dp).clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color(0x88000000)).clickable { onClose() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Close, null, tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    }
}

/** Message vocal : bouton lecture / pause. */
@Composable
fun AudioContent(path: String) {
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember { mutableStateOf(false) }
    DisposableEffect(path) { onDispose { runCatching { player?.release() }; player = null } }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(M.Ink).clickable {
                if (playing) { player?.pause(); playing = false }
                else {
                    val p = player ?: runCatching { MediaPlayer().apply { setDataSource(path); prepare(); setOnCompletionListener { playing = false } } }.getOrNull()
                    player = p
                    p?.start(); playing = p != null
                }
            },
            contentAlignment = Alignment.Center
        ) { Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = M.Sand) }
        AyText(Ay.D12, size = 17.sp, color = M.Ink)
    }
}

/**
 * Enregistrement vocal en cours (comme WhatsApp) : maintenir le micro pour parler, relâcher pour
 * envoyer ; glisser vers le haut pour verrouiller et parler les mains libres. La barre de suivi
 * montre la durée en direct et s'arrête net à la durée maximale (le vocal part alors tout seul).
 */
object VoiceUi {
    class Session(val rec: VoiceRecorder, val maxMs: Long, val onVoice: (String) -> Unit) {
        val started = android.os.SystemClock.elapsedRealtime()
        var locked by mutableStateOf(false)
        var elapsed by mutableLongStateOf(0L)
        /** Avancée du glissement vers le cadenas (0 à 1). */
        var drag by mutableFloatStateOf(0f)
    }

    var session by mutableStateOf<Session?>(null)
        private set

    /** Captures de test uniquement. */
    fun setForTest(s: Session?) { session = s }
    private val scope = kotlinx.coroutines.MainScope()
    private var ticker: kotlinx.coroutines.Job? = null

    fun start(ctx: android.content.Context, maxMs: Long, onVoice: (String) -> Unit): Boolean {
        if (session != null) return false
        val rec = VoiceRecorder(ctx)
        if (rec.start() == null) return false
        val s = Session(rec, maxMs, onVoice)
        session = s
        ticker = scope.launch {
            while (isActive) {
                s.elapsed = (android.os.SystemClock.elapsedRealtime() - s.started).coerceAtMost(maxMs)
                if (s.elapsed >= maxMs) { finish(send = true); break }
                delay(100)
            }
        }
        return true
    }

    /** Arrêter : [send] = envoyer (si plus d'une demi-seconde), sinon annuler. */
    fun finish(send: Boolean) {
        val s = session ?: return
        session = null
        ticker?.cancel(); ticker = null
        val long = android.os.SystemClock.elapsedRealtime() - s.started > 500
        val file = runCatching { s.rec.stop(canceled = !send || !long) }.getOrNull()
        if (send && long && file != null) s.onVoice(file.absolutePath)
    }
}

private fun clock(ms: Long): String { val t = ms / 1000; return "%d:%02d".format(t / 60, t % 60) }

/** Barre de suivi de l'enregistrement (au-dessus de la zone de rédaction). */
@Composable
fun VoiceRecordingBar() {
    val s = VoiceUi.session ?: return
    DisposableEffect(s) { onDispose { if (VoiceUi.session === s) VoiceUi.finish(send = false) } }
    val blink = androidx.compose.animation.core.rememberInfiniteTransition(label = "rec")
    val alpha by blink.animateFloat(1f, 0.2f, androidx.compose.animation.core.infiniteRepeatable(
        androidx.compose.animation.core.tween(600), androidx.compose.animation.core.RepeatMode.Reverse), label = "dot")
    val frac = (s.elapsed.toFloat() / s.maxMs).coerceIn(0f, 1f)
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxWidth().background(M.Bar).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(M.Red.copy(alpha = alpha)))
            AyText(ayNum(clock(s.elapsed)) + " / " + ayNum(clock(s.maxMs)), size = 20.sp, color = M.Fg,
                modifier = Modifier.weight(1f))
            if (s.locked) {
                // Mains libres : la corbeille annule, la touche d'envoi (à la place du micro) envoie
                Box(Modifier.size(46.dp).clip(CircleShape).background(M.Card).clickable { VoiceUi.finish(send = false) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Delete, null, tint = M.Red)
                }
            } else {
                // Glisser vers le haut pour verrouiller
                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 4.dp).androidx_offsetUp(s.drag)) {
                    Icon(Icons.Filled.KeyboardArrowUp, null, tint = M.Orange, modifier = Modifier.size(22.dp))
                    Icon(Icons.Filled.Lock, null, tint = if (s.drag > 0.5f) M.Orange else M.Muted, modifier = Modifier.size(22.dp))
                }
            }
        }
        // Barre de durée : se remplit en direct et s'arrête net à la durée maximale
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(M.Key)) {
            Box(Modifier.fillMaxWidth(frac).height(6.dp).clip(RoundedCornerShape(3.dp)).background(if (frac > 0.9f) M.Red else M.Orange))
        }
    }
}

private fun Modifier.androidx_offsetUp(drag: Float): Modifier =
    this.then(Modifier.offset(y = (-14 * drag).dp))

/** Micro : maintenir pour parler, relâcher pour envoyer, glisser vers le haut pour les mains libres. */
@Composable
fun MicButton(maxVoiceMs: Long, size: Int = 44, onVoice: (String) -> Unit) {
    val ctx = LocalContext.current
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val send by rememberUpdatedState(onVoice)
    val s = VoiceUi.session
    val active = s != null
    Box(
        Modifier.size(size.dp).graphicsLayer { val k = if (active && s?.locked == false) 1.35f else 1f; scaleX = k; scaleY = k }
            .clip(CircleShape).background(if (active && s?.locked != true) M.Red else M.Orange)
            .pointerInput(maxVoiceMs) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Enregistrement verrouillé déjà en cours : toucher le micro l'envoie
                    if (VoiceUi.session != null) { waitForUpOrCancellation(); VoiceUi.finish(send = true); return@awaitEachGesture }
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        askMic.launch(Manifest.permission.RECORD_AUDIO); return@awaitEachGesture
                    }
                    if (!VoiceUi.start(ctx, maxVoiceMs) { send(it) }) return@awaitEachGesture
                    val session = VoiceUi.session ?: return@awaitEachGesture
                    val lockPx = 80.dp.toPx()
                    while (true) {
                        val ev = awaitPointerEvent()
                        if (VoiceUi.session !== session) break            // durée maximale atteinte : déjà envoyé
                        val ch = ev.changes.firstOrNull { it.id == down.id }
                        if (ch == null || !ch.pressed) { VoiceUi.finish(send = true); break }
                        val dy = down.position.y - ch.position.y
                        session.drag = (dy / lockPx).coerceIn(0f, 1f)
                        ch.consume()
                        if (dy >= lockPx) { session.locked = true; session.drag = 0f; break }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(if (s?.locked == true) Icons.AutoMirrored.Filled.Send else Icons.Filled.Mic, null, tint = if (active && s?.locked != true) M.Sand else M.Ink)
    }
}

/** Bouton photo de la zone de rédaction. */
@Composable
fun PhotoButton(imageMaxDim: Int = 1024, onImage: (String) -> Unit) {
    val ctx = LocalContext.current
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) ImageUtils.downscaleAndSaveToAppFiles(ctx, uri, imageMaxDim, 75)?.let(onImage)
    }
    Icon(Icons.Filled.Image, null, tint = M.Muted, modifier = Modifier.size(40.dp).clickable { pick.launch("image/*") }.padding(8.dp))
}

/** Seulement le micro (alertes vocales). */
@Composable
fun MediaButtonsVoiceOnly(maxVoiceMs: Long, onVoice: (String) -> Unit) = MicButton(maxVoiceMs, 48, onVoice)
