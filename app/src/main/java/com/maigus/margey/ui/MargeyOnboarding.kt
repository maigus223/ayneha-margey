// SPDX-License-Identifier: GPL-3.0-or-later
// AYNEHA Margey - écrans de démarrage (autorisations, Bluetooth) en AYNEHA
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maigus.margey.text.Ay

/** B12-B18 : explication des autorisations avant la demande d'Android. */
@Composable
fun MargeyPermissionScreen(onContinue: () -> Unit) {
    MargeyTheme {
        Column(
            Modifier.fillMaxSize().background(M.Night).statusBarsPadding().navigationBarsPadding().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AyText(Ay.B12, size = 28.sp, modifier = Modifier.weight(1f).padding(vertical = 8.dp))
                LangButton()
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(M.Card).padding(16.dp)) {
                AyText(Ay.B13, size = 21.sp)
            }
            SettingGroup {
                PermRow(Icons.Filled.Bluetooth, Ay.P04)
                PermRow(Icons.Filled.Radar, Ay.B14)
                PermRow(Icons.Filled.CameraAlt, Ay.B15)
                PermRow(Icons.Filled.Notifications, Ay.B16)
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF1F3B33)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Hub, null, tint = Color(0xFFCDEBDD), modifier = Modifier.size(24.dp))
                AyText(Ay.B18, size = 19.sp, color = Color(0xFFCDEBDD), modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.weight(1f))
            OrangeButton(Ay.B17, onClick = onContinue)
        }
    }
}

@Composable
private fun PermRow(icon: ImageVector, label: String) {
    SettingRow(icon, label) {}
}

/** R07-R08 : le Bluetooth est éteint, l'utilisateur est indisponible tant qu'il ne l'allume pas. */
@Composable
fun MargeyBluetoothScreen(onEnable: () -> Unit) {
    MargeyTheme {
        Column(
            Modifier.fillMaxSize().background(M.Night).statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { LangButton() }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.Bluetooth, null, tint = M.Orange, modifier = Modifier.size(96.dp))
            AyText(Ay.R07, size = 24.sp, align = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.weight(1f))
            OrangeButton(Ay.R08, onClick = onEnable)
        }
    }
}

/** Écran de démarrage simple : icône, titre facultatif, texte, bouton principal, bouton secondaire facultatif. */
@Composable
fun MargeyInfoScreen(
    icon: ImageVector,
    title: String?,
    text: String,
    button: String,
    onClick: () -> Unit,
    secondary: String? = null,
    onSecondary: () -> Unit = {}
) {
    MargeyTheme {
        Column(
            Modifier.fillMaxSize().background(M.Night).statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { LangButton() }
            Spacer(Modifier.weight(1f))
            Icon(icon, null, tint = M.Orange, modifier = Modifier.size(96.dp))
            if (title != null) AyText(title, size = 30.sp, color = M.Orange, align = androidx.compose.ui.text.style.TextAlign.Center)
            AyText(text, size = 22.sp, align = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.weight(1f))
            OrangeButton(button, onClick = onClick)
            if (secondary != null) {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(M.Card)
                        .androidxClickable(onSecondary).padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { AyText(secondary, size = 20.sp, align = androidx.compose.ui.text.style.TextAlign.Center) }
            }
        }
    }
}

private fun Modifier.androidxClickable(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

object MargeyIcons {
    val location: ImageVector get() = Icons.Filled.LocationOn
    val battery: ImageVector get() = Icons.Filled.BatteryChargingFull
}
