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
import androidx.compose.foundation.gestures.detectTapGestures
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
    Image(bmp.asImageBitmap(), null, contentScale = ContentScale.Fit,
        modifier = Modifier.widthIn(max = 240.dp).heightIn(max = 300.dp).clip(RoundedCornerShape(12.dp)))
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
 * Boutons photo et micro de la zone de rédaction (discussions privées).
 * Micro : maintenir pour enregistrer, relâcher pour envoyer (moins d'une demi-seconde = annulé).
 */
@Composable
fun MediaButtons(maxVoiceMs: Long = 60_000L, imageMaxDim: Int = 1024, onImage: (String) -> Unit, onVoice: (String) -> Unit) {
    val ctx = LocalContext.current
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) ImageUtils.downscaleAndSaveToAppFiles(ctx, uri, imageMaxDim, 75)?.let(onImage)
    }
    var recording by remember { mutableStateOf(false) }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Icon(Icons.Filled.Image, null, tint = M.Muted, modifier = Modifier.size(40.dp).clickable { pick.launch("image/*") }.padding8())
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(if (recording) M.Red else M.Card)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        askMic.launch(Manifest.permission.RECORD_AUDIO); return@detectTapGestures
                    }
                    val rec = VoiceRecorder(ctx)
                    val started = System.currentTimeMillis()
                    if (rec.start() == null) return@detectTapGestures
                    recording = true
                    // Durée maximale : l'enregistrement s'arrête et part tout seul
                    val released = kotlinx.coroutines.withTimeoutOrNull(maxVoiceMs) { tryAwaitRelease() } ?: true
                    recording = false
                    val long = System.currentTimeMillis() - started > 500
                    val file = rec.stop(canceled = !released || !long)
                    if (released && long && file != null) onVoice(file.absolutePath)
                })
            },
        contentAlignment = Alignment.Center
    ) { Icon(Icons.Filled.Mic, null, tint = if (recording) M.Sand else M.Orange) }
}

private fun Modifier.padding8() = this.then(Modifier.padding(8.dp))

/** Seulement le micro (alertes vocales). */
@Composable
fun MediaButtonsVoiceOnly(maxVoiceMs: Long, onVoice: (String) -> Unit) {
    val ctx = LocalContext.current
    var recording by remember { mutableStateOf(false) }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(if (recording) M.Red else M.Card)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        askMic.launch(Manifest.permission.RECORD_AUDIO); return@detectTapGestures
                    }
                    val rec = VoiceRecorder(ctx)
                    val started = System.currentTimeMillis()
                    if (rec.start() == null) return@detectTapGestures
                    recording = true
                    val released = kotlinx.coroutines.withTimeoutOrNull(maxVoiceMs) { tryAwaitRelease() } ?: true
                    recording = false
                    val long = System.currentTimeMillis() - started > 500
                    val file = rec.stop(canceled = !released || !long)
                    if (released && long && file != null) onVoice(file.absolutePath)
                })
            },
        contentAlignment = Alignment.Center
    ) { Icon(Icons.Filled.Mic, null, tint = if (recording) M.Sand else M.Orange) }
}
