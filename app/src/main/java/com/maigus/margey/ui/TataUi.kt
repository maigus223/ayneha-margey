// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - interface Tata-Set : liste des membres, avis, effacement de son message
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maigus.margey.tata.Member
import com.maigus.margey.tata.TataEvent
import com.maigus.margey.tata.TataSet
import com.maigus.margey.text.Ay

/** Texte d'un petit message d'information Tata-Set, dans la langue de l'interface. */
fun tataInfo(ev: TataEvent): String = when (ev) {
    // Entrée libre (salon) ou sans personne connue qui fait entrer : « [nom] huru »
    is TataEvent.Joined -> if (ev.member.by.isBlank() || ev.member.by == ev.member.fp || ev.byNick.isBlank()) fill(Ay.T11, nom = ev.member.nick)
        else fill(Ay.T01, nom = ev.byNick, nom2 = ev.member.nick)
    is TataEvent.Left -> fill(Ay.T02, nom = ev.member.nick)
    is TataEvent.Excluded -> fill(Ay.T03, nom = ev.member.nick)
    is TataEvent.Offline -> fill(Ay.T04, nom = ev.member.nick)
    is TataEvent.Surplus -> fill(Ay.T05, nom = ev.member.nick)
    is TataEvent.NewAdmin -> fill(Ay.T06, nom = ev.member.nick)
}

/** Petit message d'information, centré. */
@Composable
fun InfoLine(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.clip(RoundedCornerShape(12.dp)).background(M.Card).padding(horizontal = 12.dp, vertical = 6.dp)) {
            AyText(text, size = 15.sp, color = M.Muted, align = TextAlign.Center)
        }
    }
}

/** Panneau « effacer mon message ». */
@Composable
fun DeleteMinePanel(onDelete: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(M.Bar).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(M.Red).clickable { onDelete() }.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Delete, null, tint = Color.White)
            AyText(Ay.T09, size = 20.sp, color = Color.White)
        }
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(M.Card).clickable { onCancel() }.padding(14.dp),
            contentAlignment = Alignment.Center
        ) { AyText(Ay.G08, size = 20.sp, align = TextAlign.Center) }
    }
}

/** Liste des membres (actifs puis inactifs), avec les boutons Exclure / Réintégrer selon les règles. */
@Composable
fun MembersPanel(
    ledger: Map<String, Member>,
    myFp: String,
    canExclude: (Member) -> Boolean,
    canReinstate: (Member) -> Boolean,
    onExclude: (Member) -> Unit,
    onReinstate: (Member) -> Unit,
    onClose: () -> Unit,
) {
    val adminFp = TataSet.admin(ledger)
    val list = ledger.values.sortedWith(compareBy<Member>({ !it.active }, { it.rank }, { it.fp }))
    Column(Modifier.fillMaxWidth().heightIn(max = 460.dp).background(M.Bar).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AyText(fill(Ay.K05, n = TataSet.activeCount(ledger)), size = 20.sp, modifier = Modifier.weight(1f))
            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(M.Orange).clickable { onClose() }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                AyText(Ay.G09, size = 18.sp, color = M.Ink)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(list, key = { it.fp }) { x ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(M.Card).padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Avatar(x.nick, 36, if (x.active) M.Sand else M.Key)
                    AyText(x.nick, size = 19.sp, color = if (x.active) M.Fg else M.Muted2, modifier = Modifier.weight(1f), maxLines = 1)
                    if (x.fp == adminFp) Icon(Icons.Filled.Star, null, tint = M.Orange, modifier = Modifier.size(20.dp))
                    if (x.fp != myFp && canExclude(x)) SmallAction(Ay.T07, M.Red) { onExclude(x) }
                    if (canReinstate(x)) SmallAction(Ay.T08, M.Green) { onReinstate(x) }
                }
            }
        }
    }
}

@Composable
private fun SmallAction(label: String, bg: Color, onClick: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(bg).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp)) {
        AyText(label, size = 15.sp, color = Color.White)
    }
}
