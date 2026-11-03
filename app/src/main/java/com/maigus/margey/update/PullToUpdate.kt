// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - « tirer vers le bas pour chercher une mise à jour » (comme les autres applications AYNEHA)
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * Quand la liste des discussions est tout en haut, tirer vers le bas fait descendre le rond orange
 * (MiseAJourUi) ; en lâchant assez bas, Margey cherche une nouvelle version sur ayneha.org.
 */
@Composable
fun rememberPullToUpdate(): NestedScrollConnection = remember {
    object : NestedScrollConnection {
        var d = 0f
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (d > 0f && available.y < 0f) {
                val c = maxOf(available.y, -d)
                d += c
                MiseAJourUi.courant?.tirer(d)
                return Offset(0f, c)
            }
            return Offset.Zero
        }
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.UserInput && available.y > 0f) {
                d += available.y
                MiseAJourUi.courant?.tirer(d)
                return Offset(0f, available.y)
            }
            return Offset.Zero
        }
        override suspend fun onPreFling(available: Velocity): Velocity {
            if (d > 0f) { d = 0f; MiseAJourUi.courant?.lacher() }
            return Velocity.Zero
        }
    }
}
