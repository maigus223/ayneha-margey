// SPDX-License-Identifier: GPL-3.0-or-later
// Mentions de Margey en trois langues : AYNEHA (songhay traduit par Mahamadou Issiaka MAIGA - MAIGUS,
// transcrit en AYNEHA, police Ayneha Pro, plan 16 U+10FF7C-U+10FFDD), français, anglais.
// Fichier généré à partir du tableau des mentions : ne pas modifier le songhay sans validation de l'auteur.
// [n] = nombre, [nom] = pseudo (dans T01, le premier [nom] fait entrer le second).
package com.maigus.margey.text

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Langue de l'interface : 0 = AYNEHA, 1 = français, 2 = anglais. */
object Lang {
    const val AY = 0
    const val FR = 1
    const val EN = 2
    private const val PREF = "margey_lang"
    var current by mutableIntStateOf(AY)
    /** false tant que l'utilisateur n'a pas choisi sa langue au premier lancement. */
    var chosen by mutableStateOf(false)
    val isAyneha: Boolean get() = current == AY

    fun init(ctx: Context) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        current = p.getInt("lang", AY).coerceIn(AY, EN)
        chosen = p.getBoolean("chosen", false)
    }

    fun set(ctx: Context, lang: Int) {
        current = lang.coerceIn(AY, EN)
        chosen = true
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putInt("lang", current).putBoolean("chosen", true).apply()
    }
}

object Ay {
    /** G01 - Margey - Margey - margey */
    private val G01_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9D\uDBFF\uDFCC", "Margey", "Margey")
    val G01: String get() = G01_[Lang.current]
    /** G02 - Discussions - Chats - boyrey */
    private val G02_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC", "Discussions", "Chats")
    val G02: String get() = G02_[Lang.current]
    /** G03 - Autour de moi - Around me - wĩdila */
    private val G03_ = arrayOf("\uDBFF\uDF8A\uDBFF\uDFA7\uDBFF\uDF8B\uDBFF\uDF9E\uDBFF\uDF8E\uDBFF\uDF9C", "Autour de moi", "Around me")
    val G03: String get() = G03_[Lang.current]
    /** G04 - Salons - Rooms - shiifa */
    private val G04_ = arrayOf("\uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C", "Salons", "Rooms")
    val G04: String get() = G04_[Lang.current]
    /** G05 - Réglages - Settings - goykasiney */
    private val G05_ = arrayOf("\uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF99\uDBFF\uDF9C\uDBFF\uDF90\uDBFF\uDF9E\uDBFF\uDF8D\uDBFF\uDF9D\uDBFF\uDFCC", "R\u00E9glages", "Settings")
    val G05: String get() = G05_[Lang.current]
    /** G06 - Retour - Back - yee */
    private val G06_ = arrayOf("\uDBFF\uDF97\uDBFF\uDFA3", "Retour", "Back")
    val G06: String get() = G06_[Lang.current]
    /** G07 - Rechercher - Search - ceeci */
    private val G07_ = arrayOf("\uDBFF\uDF93\uDBFF\uDFA3\uDBFF\uDF93\uDBFF\uDF9E", "Rechercher", "Search")
    val G07: String get() = G07_[Lang.current]
    /** G08 - Annuler - Cancel - naŋ */
    private val G08_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9C\uDBFF\uDFD2", "Annuler", "Cancel")
    val G08: String get() = G08_[Lang.current]
    /** G09 - OK - OK - boori */
    private val G09_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA8\uDBFF\uDF8F\uDBFF\uDF9E", "OK", "OK")
    val G09: String get() = G09_[Lang.current]
    /** G10 - Oui - Yes - ayyo */
    private val G10_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFCB\uDBFF\uDF9F", "Oui", "Yes")
    val G10: String get() = G10_[Lang.current]
    /** G11 - Non - No - kala */
    private val G11_ = arrayOf("\uDBFF\uDF99\uDBFF\uDF9C\uDBFF\uDF8E\uDBFF\uDF9C", "Non", "No")
    val G11: String get() = G11_[Lang.current]
    /** G12 - Enregistrer - Save - gaabu */
    private val G12_ = arrayOf("\uDBFF\uDF98\uDBFF\uDFA1\uDBFF\uDF86\uDBFF\uDFA0", "Enregistrer", "Save")
    val G12: String get() = G12_[Lang.current]
    /** G13 - Supprimer - Delete - tuusu */
    private val G13_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDFA0", "Supprimer", "Delete")
    val G13: String get() = G13_[Lang.current]
    /** G14 - Partager - Share - fey-fey */
    private val G14_ = arrayOf("\uDBFF\uDF89\uDBFF\uDF9D\uDBFF\uDFCC-\uDBFF\uDF89\uDBFF\uDF9D\uDBFF\uDFCC", "Partager", "Share")
    val G14: String get() = G14_[Lang.current]
    /** G15 - Suivant - Next - djine */
    private val G15_ = arrayOf("\uDBFF\uDF95\uDBFF\uDF9E\uDBFF\uDF8D\uDBFF\uDF9D", "Suivant", "Next")
    val G15: String get() = G15_[Lang.current]
    /** G16 - Terminé - Done - a bɛn */
    private val G16_ = arrayOf("\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA5\uDBFF\uDFBC", "Termin\u00E9", "Done")
    val G16: String get() = G16_[Lang.current]
    /** G17 - Modifier - Edit - barmey */
    private val G17_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF88\uDBFF\uDF9D\uDBFF\uDFCC", "Modifier", "Edit")
    val G17: String get() = G17_[Lang.current]
    /** G18 - Aujourd'hui - Today - hũkuna */
    private val G18_ = arrayOf("\uDBFF\uDF9A\uDBFF\uDFAC\uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDF8D\uDBFF\uDF9C", "Aujourd'hui", "Today")
    val G18: String get() = G18_[Lang.current]
    /** G19 - Hier - Yesterday - bi */
    private val G19_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9E", "Hier", "Yesterday")
    val G19: String get() = G19_[Lang.current]
    /** G20 - Aucun résultat - No results - ceeciro si */
    private val G20_ = arrayOf("\uDBFF\uDF93\uDBFF\uDFA3\uDBFF\uDF93\uDBFF\uDF9E\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF90\uDBFF\uDF9E", "Aucun r\u00E9sultat", "No results")
    val G20: String get() = G20_[Lang.current]
    /** B01 - Tout reste sur le téléphone - Everything stays on your phone - talfono ra a kul ga cĩdi */
    private val B01_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDF8D\uDBFF\uDF9F \uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDFBE \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF93\uDBFF\uDFA7\uDBFF\uDF8B\uDBFF\uDF9E", "Tout reste sur le t\u00E9l\u00E9phone", "Everything stays on your phone")
    val B01: String get() = B01_[Lang.current]
    /** B02 - Sans Internet - No Internet - dadaara si */
    private val B02_ = arrayOf("\uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDF8B\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF90\uDBFF\uDF9E", "Sans Internet", "No Internet")
    val B02: String get() = B02_[Lang.current]
    /** B03 - Sans compte - No account - kabu si */
    private val B03_ = arrayOf("\uDBFF\uDF99\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDFA0 \uDBFF\uDF90\uDBFF\uDF9E", "Sans compte", "No account")
    val B03: String get() = B03_[Lang.current]
    /** B05 - Commencer - Start - shĩtin */
    private val B05_ = arrayOf("\uDBFF\uDF92\uDBFF\uDFA7\uDBFF\uDF8C\uDBFF\uDF9E\uDBFF\uDFBC", "Commencer", "Start")
    val B05: String get() = B05_[Lang.current]
    /** B06 - Choisis ton pseudo - Choose your nickname - ni maaɲo suuba */
    private val B06_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF96\uDBFF\uDF9F \uDBFF\uDF90\uDBFF\uDFAB\uDBFF\uDF86\uDBFF\uDF9C", "Choisis ton pseudo", "Choose your nickname")
    val B06: String get() = B06_[Lang.current]
    /** B07 - Ton pseudo - Your nickname - ni maaɲo */
    private val B07_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF96\uDBFF\uDF9F", "Ton pseudo", "Your nickname")
    val B07: String get() = B07_[Lang.current]
    /** B08 - Tu pourras le changer plus tard - You can change it later - ni ga hin ka a barmey */
    private val B08_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF9A\uDBFF\uDF9E\uDBFF\uDFBC \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF88\uDBFF\uDF9D\uDBFF\uDFCC", "Tu pourras le changer plus tard", "You can change it later")
    val B08: String get() = B08_[Lang.current]
    /** B09 - Ajouter une photo - Add a photo - bii daŋ */
    private val B09_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA6 \uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDFD2", "Ajouter une photo", "Add a photo")
    val B09: String get() = B09_[Lang.current]
    /** B10 - Création de ton code unique… - Creating your unique code… - ni kɔdo goo ma te… */
    private val B10_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF98\uDBFF\uDFA8 \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9D\u2026", "Cr\u00E9ation de ton code unique\u2026", "Creating your unique code\u2026")
    val B10: String get() = B10_[Lang.current]
    /** B11 - Ton code unique - Your unique code - ni foo kɔdo */
    private val B11_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF89\uDBFF\uDFA8 \uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F", "Ton code unique", "Your unique code")
    val B11: String get() = B11_[Lang.current]
    /** B12 - Autorisations - Permissions - duɲey */
    private val B12_ = arrayOf("\uDBFF\uDF8B\uDBFF\uDFA0\uDBFF\uDF96\uDBFF\uDF9D\uDBFF\uDFCC", "Autorisations", "Permissions")
    val B12: String get() = B12_[Lang.current]
    /** B13 - Margey a besoin du Bluetooth pour parler aux téléphones proches - Margey needs Bluetooth to talk to nearby phones - margey ga ba bulutuf hal ama tuuru talfon maanãtey se */
    private val B13_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA0\uDBFF\uDF8E\uDBFF\uDFA0\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDFB4 \uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDFBE \uDBFF\uDF9C\uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF8F\uDBFF\uDFA0 \uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDFBC \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF90\uDBFF\uDF9D", "Margey a besoin du Bluetooth pour parler aux t\u00E9l\u00E9phones proches", "Margey needs Bluetooth to talk to nearby phones")
    val B13: String get() = B13_[Lang.current]
    /** B14 - Appareils à proximité - Nearby devices - talfon maanãtey */
    private val B14_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDFBC \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDFCC", "Appareils \u00E0 proximit\u00E9", "Nearby devices")
    val B14: String get() = B14_[Lang.current]
    /** B15 - Caméra (pour scanner un QR) - Camera (to scan a QR code) - bii goy-djina */
    private val B15_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA6 \uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDFCC-\uDBFF\uDF95\uDBFF\uDF9E\uDBFF\uDF8D\uDBFF\uDF9C", "Cam\u00E9ra (pour scanner un QR)", "Camera (to scan a QR code)")
    val B15: String get() = B15_[Lang.current]
    /** B16 - Notifications - Notifications - baaruyã */
    private val B16_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDFA0\uDBFF\uDF97\uDBFF\uDFA2", "Notifications", "Notifications")
    val B16: String get() = B16_[Lang.current]
    /** B17 - Autoriser - Allow - duɲa */
    private val B17_ = arrayOf("\uDBFF\uDF8B\uDBFF\uDFA0\uDBFF\uDF96\uDBFF\uDF9C", "Autoriser", "Allow")
    val B17: String get() = B17_[Lang.current]
    /** B18 - Ton téléphone relaie aussi les messages des autres - Your phone also relays other people's messages - ni talfono ga alhabarey zaa ki fey-fey */
    private val B18_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDF8D\uDBFF\uDF9F \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF91\uDBFF\uDFA1 \uDBFF\uDF99\uDBFF\uDF9E \uDBFF\uDF89\uDBFF\uDF9D\uDBFF\uDFCC-\uDBFF\uDF89\uDBFF\uDF9D\uDBFF\uDFCC", "Ton t\u00E9l\u00E9phone relaie aussi les messages des autres", "Your phone also relays other people's messages")
    val B18: String get() = B18_[Lang.current]
    /** D01 - [n] téléphones à portée - [n] phones in range - talfono maanãte [n] */
    private val D01_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDF8D\uDBFF\uDF9F \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D [n]", "[n] t\u00E9l\u00E9phones \u00E0 port\u00E9e", "[n] phones in range")
    val D01: String get() = D01_[Lang.current]
    /** D02 - Personne à portée pour l'instant - Nobody in range for now - bɔrɔ kul si man ni */
    private val D02_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDFBE \uDBFF\uDF90\uDBFF\uDF9E \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFBC \uDBFF\uDF8D\uDBFF\uDF9E", "Personne \u00E0 port\u00E9e pour l'instant", "Nobody in range for now")
    val D02: String get() = D02_[Lang.current]
    /** D03 - Aucune discussion - No chats yet - boyrey kul si */
    private val D03_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDFBE \uDBFF\uDF90\uDBFF\uDF9E", "Aucune discussion", "No chats yet")
    val D03: String get() = D03_[Lang.current]
    /** D04 - Ajoute un contact pour commencer - Add a contact to start - bɔrɔ tɔntɔn ka shĩtin */
    private val D04_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF8C\uDBFF\uDFAA\uDBFF\uDFBC\uDBFF\uDF8C\uDBFF\uDFAA\uDBFF\uDFBC \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF92\uDBFF\uDFA7\uDBFF\uDF8C\uDBFF\uDF9E\uDBFF\uDFBC", "Ajoute un contact pour commencer", "Add a contact to start")
    val D04: String get() = D04_[Lang.current]
    /** D05 - Nouveau message - New message - alhabar tedji */
    private val D05_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDFC0 \uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", "Nouveau message", "New message")
    val D05: String get() = D05_[Lang.current]
    /** D06 - Nouveau groupe - New group - margari tedji */
    private val D06_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9E \uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", "Nouveau groupe", "New group")
    val D06: String get() = D06_[Lang.current]
    /** D07 - Remis en direct - Delivered directly - sohõ-sohõ yee */
    private val D07_ = arrayOf("\uDBFF\uDF90\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA9-\uDBFF\uDF90\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA9 \uDBFF\uDF97\uDBFF\uDFA3", "Remis en direct", "Delivered directly")
    val D07: String get() = D07_[Lang.current]
    /** D08 - Relayé par [n] téléphones - Relayed by [n] phones - talfon [n] goo ma a fey-fey */
    private val D08_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDFBC [n] \uDBFF\uDF98\uDBFF\uDFA8 \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF9C \uDBFF\uDF89\uDBFF\uDF9D\uDBFF\uDFCC-\uDBFF\uDF89\uDBFF\uDF9D\uDBFF\uDFCC", "Relay\u00E9 par [n] t\u00E9l\u00E9phones", "Relayed by [n] phones")
    val D08: String get() = D08_[Lang.current]
    /** D09 - En attente - Pending - baabuyã */
    private val D09_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA1\uDBFF\uDF86\uDBFF\uDFA0\uDBFF\uDF97\uDBFF\uDFA2", "En attente", "Pending")
    val D09: String get() = D09_[Lang.current]
    /** D10 - Lu - Read - cawãte */
    private val D10_ = arrayOf("\uDBFF\uDF93\uDBFF\uDF9C\uDBFF\uDF8A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D", "Lu", "Read")
    val D10: String get() = D10_[Lang.current]
    /** D11 - Image - Image - bii */
    private val D11_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA6", "Image", "Image")
    val D11: String get() = D11_[Lang.current]
    /** D12 - Message vocal - Voice message - djĩde alhabar */
    private val D12_ = arrayOf("\uDBFF\uDF95\uDBFF\uDFA7\uDBFF\uDF8B\uDBFF\uDF9D \uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDFC0", "Message vocal", "Voice message")
    val D12: String get() = D12_[Lang.current]
    /** D13 - Épinglée - Pinned - kãdjãte */
    private val D13_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFA2\uDBFF\uDF95\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D", "\u00C9pingl\u00E9e", "Pinned")
    val D13: String get() = D13_[Lang.current]
    /** D14 - Épingler - Pin - kãdji */
    private val D14_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFA2\uDBFF\uDF95\uDBFF\uDF9E", "\u00C9pingler", "Pin")
    val D14: String get() = D14_[Lang.current]
    /** D15 - Désépingler - Unpin - zumãdi */
    private val D15_ = arrayOf("\uDBFF\uDF91\uDBFF\uDFA0\uDBFF\uDF88\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "D\u00E9s\u00E9pingler", "Unpin")
    val D15: String get() = D15_[Lang.current]
    /** D16 - Supprimer cette discussion ? - Delete this chat? - boyra wo ma tuusa ? */
    private val D16_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF8A\uDBFF\uDF9F \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDF9C \u061F", "Supprimer cette discussion ?", "Delete this chat?")
    val D16: String get() = D16_[Lang.current]
    /** D17 - Cette action est irréversible. - This cannot be undone. - goyo wo si yeeti koyne */
    private val D17_ = arrayOf("\uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDF97\uDBFF\uDF9F \uDBFF\uDF8A\uDBFF\uDF9F \uDBFF\uDF90\uDBFF\uDF9E \uDBFF\uDF97\uDBFF\uDFA3\uDBFF\uDF8C\uDBFF\uDF9E \uDBFF\uDF99\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8D\uDBFF\uDF9D", "Cette action est irr\u00E9versible.", "This cannot be undone.")
    val D17: String get() = D17_[Lang.current]
    /** C01 - À portée (direct) - In range (direct) - maanãtey (sohõ-sohõ) */
    private val C01_ = arrayOf("\uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDFCC (\uDBFF\uDF90\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA9-\uDBFF\uDF90\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA9)", "\u00C0 port\u00E9e (direct)", "In range (direct)")
    val C01: String get() = C01_[Lang.current]
    /** C02 - Joignable par le réseau - Reachable through the mesh - ga tukãdi */
    private val C02_ = arrayOf("\uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDF99\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "Joignable par le r\u00E9seau", "Reachable through the mesh")
    val C02: String get() = C02_[Lang.current]
    /** C03 - Hors de portée - Out of range - mooru */
    private val C03_ = arrayOf("\uDBFF\uDF88\uDBFF\uDFA8\uDBFF\uDF8F\uDBFF\uDFA0", "Hors de port\u00E9e", "Out of range")
    val C03: String get() = C03_[Lang.current]
    /** C04 - Écris ici… - Type here… - ayneha hãtum ne… */
    private val C04_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFCC\uDBFF\uDF8D\uDBFF\uDF9D\uDBFF\uDF9A\uDBFF\uDF9C \uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDFB2 \uDBFF\uDF8D\uDBFF\uDF9D\u2026", "\u00C9cris ici\u2026", "Type here\u2026")
    val C04: String get() = C04_[Lang.current]
    /** C05 - Joindre - Attach - tuku */
    private val C05_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDF99\uDBFF\uDFA0", "Joindre", "Attach")
    val C05: String get() = C05_[Lang.current]
    /** C06 - Envoyer - Send - sãba */
    private val C06_ = arrayOf("\uDBFF\uDF90\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDF9C", "Envoyer", "Send")
    val C06: String get() = C06_[Lang.current]
    /** C07 - Répondre - Reply - tuuru */
    private val C07_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF8F\uDBFF\uDFA0", "R\u00E9pondre", "Reply")
    val C07: String get() = C07_[Lang.current]
    /** C08 - Copier - Copy - yee a ga */
    private val C08_ = arrayOf("\uDBFF\uDF97\uDBFF\uDFA3 \uDBFF\uDF9C \uDBFF\uDF98\uDBFF\uDF9C", "Copier", "Copy")
    val C08: String get() = C08_[Lang.current]
    /** C09 - Transférer - Forward - bɛrɛ */
    private val C09_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA5\uDBFF\uDF8F\uDBFF\uDFA5", "Transf\u00E9rer", "Forward")
    val C09: String get() = C09_[Lang.current]
    /** C10 - Réagir - React - tuuru */
    private val C10_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF8F\uDBFF\uDFA0", "R\u00E9agir", "React")
    val C10: String get() = C10_[Lang.current]
    /** C11 - Supprimer pour moi - Delete for me - ay wano tuusu */
    private val C11_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFCC \uDBFF\uDF8A\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDFA0", "Supprimer pour moi", "Delete for me")
    val C11: String get() = C11_[Lang.current]
    /** C12 - Maintiens pour enregistrer - Hold to record - naagu ka gaabu */
    private val C12_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDFA1\uDBFF\uDF98\uDBFF\uDFA0 \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF98\uDBFF\uDFA1\uDBFF\uDF86\uDBFF\uDFA0", "Maintiens pour enregistrer", "Hold to record")
    val C12: String get() = C12_[Lang.current]
    /** C13 - Glisse pour annuler - Slide to cancel - muley ka a naŋ */
    private val C13_ = arrayOf("\uDBFF\uDF88\uDBFF\uDFA0\uDBFF\uDF8E\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF9C \uDBFF\uDF8D\uDBFF\uDF9C\uDBFF\uDFD2", "Glisse pour annuler", "Slide to cancel")
    val C13: String get() = C13_[Lang.current]
    /** C14 - Appel de proximité - Nearby call - maanãte caari */
    private val C14_ = arrayOf("\uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D \uDBFF\uDF93\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDF9E", "Appel de proximit\u00E9", "Nearby call")
    val C14: String get() = C14_[Lang.current]
    /** C15 - Appel en cours - Call in progress - caaro goo ma te */
    private val C15_ = arrayOf("\uDBFF\uDF93\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF98\uDBFF\uDFA8 \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9D", "Appel en cours", "Call in progress")
    val C15: String get() = C15_[Lang.current]
    /** C16 - Raccrocher - Hang up - caaro ma key */
    private val C16_ = arrayOf("\uDBFF\uDF93\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDF9D\uDBFF\uDFCC", "Raccrocher", "Hang up")
    val C16: String get() = C16_[Lang.current]
    /** C17 - Haut-parleur - Speaker - djĩde-beero */
    private val C17_ = arrayOf("\uDBFF\uDF95\uDBFF\uDFA7\uDBFF\uDF8B\uDBFF\uDF9D-\uDBFF\uDF86\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9F", "Haut-parleur", "Speaker")
    val C17: String get() = C17_[Lang.current]
    /** C18 - Micro coupé - Muted - mikuro dũba */
    private val C18_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9E\uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8B\uDBFF\uDFAC\uDBFF\uDF86\uDBFF\uDF9C", "Micro coup\u00E9", "Muted")
    val C18: String get() = C18_[Lang.current]
    /** C19 - Appel possible seulement à portée directe - Calls only work in direct range - caari ga te bɔrɔ maanãtey dee se */
    private val C19_ = arrayOf("\uDBFF\uDF93\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDF9E \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9D \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF8B\uDBFF\uDFA3 \uDBFF\uDF90\uDBFF\uDF9D", "Appel possible seulement \u00E0 port\u00E9e directe", "Calls only work in direct range")
    val C19: String get() = C19_[Lang.current]
    /** C20 - Réponses rapides - Quick replies - tuuruyã cahãte */
    private val C20_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF8F\uDBFF\uDFA0\uDBFF\uDF97\uDBFF\uDFA2 \uDBFF\uDF93\uDBFF\uDF9C\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D", "R\u00E9ponses rapides", "Quick replies")
    val C20: String get() = C20_[Lang.current]
    /** C21 - Bonjour - Hello - ni ga saabu */
    private val C21_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF90\uDBFF\uDFA1\uDBFF\uDF86\uDBFF\uDFA0", "Bonjour", "Hello")
    val C21: String get() = C21_[Lang.current]
    /** C22 - Bonne journée - Have a good day - waato ma boori */
    private val C22_ = arrayOf("\uDBFF\uDF8A\uDBFF\uDFA1\uDBFF\uDF8C\uDBFF\uDF9F \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA8\uDBFF\uDF8F\uDBFF\uDF9E", "Bonne journ\u00E9e", "Have a good day")
    val C22: String get() = C22_[Lang.current]
    /** C23 - Merci - Thank you - foofo */
    private val C23_ = arrayOf("\uDBFF\uDF89\uDBFF\uDFA8\uDBFF\uDF89\uDBFF\uDF9F", "Merci", "Thank you")
    val C23: String get() = C23_[Lang.current]
    /** C24 - J'arrive - I'm coming - ay go ka */
    private val C24_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFCC \uDBFF\uDF98\uDBFF\uDF9F \uDBFF\uDF99\uDBFF\uDF9C", "J'arrive", "I'm coming")
    val C24: String get() = C24_[Lang.current]
    /** C25 - Attends un peu - Wait a moment - batu keyna */
    private val C25_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8C\uDBFF\uDFA0 \uDBFF\uDF99\uDBFF\uDF9D\uDBFF\uDFCC\uDBFF\uDF8D\uDBFF\uDF9C", "Attends un peu", "Wait a moment")
    val C25: String get() = C25_[Lang.current]
    /** C26 - Je n'ai pas compris - I didn't understand - ay mana faham */
    private val C26_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFCC \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9C \uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDFB2", "Je n'ai pas compris", "I didn't understand")
    val C26: String get() = C26_[Lang.current]
    /** P03 - Code QR - QR code - biyo tõdo */
    private val P03_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDFA9\uDBFF\uDF8B\uDBFF\uDF9F", "Code QR", "QR code")
    val P03: String get() = P03_[Lang.current]
    /** P04 - Bluetooth - Bluetooth - bulutuf */
    private val P04_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA0\uDBFF\uDF8E\uDBFF\uDFA0\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDFB4", "Bluetooth", "Bluetooth")
    val P04: String get() = P04_[Lang.current]
    /** P05 - Téléphones proches - Nearby phones - talfon maanãtey */
    private val P05_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDFBC \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDFCC", "T\u00E9l\u00E9phones proches", "Nearby phones")
    val P05: String get() = P05_[Lang.current]
    /** P06 - Recherche des téléphones proches… - Looking for nearby phones… - talfon maanãtey ceeciro… */
    private val P06_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDFBC \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF93\uDBFF\uDFA3\uDBFF\uDF93\uDBFF\uDF9E\uDBFF\uDF8F\uDBFF\uDF9F\u2026", "Recherche des t\u00E9l\u00E9phones proches\u2026", "Looking for nearby phones\u2026")
    val P06: String get() = P06_[Lang.current]
    /** P07 - Envoyé - Sent - sãba ka bɛn */
    private val P07_ = arrayOf("\uDBFF\uDF90\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA5\uDBFF\uDFBC", "Envoy\u00E9", "Sent")
    val P07: String get() = P07_[Lang.current]
    /** P08 - Échec de l'envoi - Sending failed - sãbaro mana te */
    private val P08_ = arrayOf("\uDBFF\uDF90\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9D", "\u00C9chec de l'envoi", "Sending failed")
    val P08: String get() = P08_[Lang.current]
    /** P09 - Enregistrer l'image - Save image - biyo gaabu */
    private val P09_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDF9F \uDBFF\uDF98\uDBFF\uDFA1\uDBFF\uDF86\uDBFF\uDFA0", "Enregistrer l'image", "Save image")
    val P09: String get() = P09_[Lang.current]
    /** P10 - Fais scanner ce code pour recevoir le message - Let them scan this code to receive the message - biyo tõdo wo sikane ka alhabaro ta */
    private val P10_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDFA9\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF8A\uDBFF\uDF9F \uDBFF\uDF90\uDBFF\uDF9E\uDBFF\uDF99\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9D \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDF9C", "Fais scanner ce code pour recevoir le message", "Let them scan this code to receive the message")
    val P10: String get() = P10_[Lang.current]
    /** A01 - Nouveau contact - New contact - bɔrɔ tedji */
    private val A01_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", "Nouveau contact", "New contact")
    val A01: String get() = A01_[Lang.current]
    /** A02 - Mon code - My code - ay kɔdo */
    private val A02_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFCC \uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F", "Mon code", "My code")
    val A02: String get() = A02_[Lang.current]
    /** A03 - Scanner - Scan - sikane */
    private val A03_ = arrayOf("\uDBFF\uDF90\uDBFF\uDF9E\uDBFF\uDF99\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9D", "Scanner", "Scan")
    val A03: String get() = A03_[Lang.current]
    /** A04 - Code de sécurité - Security code - baani kɔdo */
    private val A04_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F", "Code de s\u00E9curit\u00E9", "Security code")
    val A04: String get() = A04_[Lang.current]
    /** A05 - Comparez ce code avec celui de votre contact - Compare this code with your contact's - kɔdo wo gaaru ni bɔra wano ga */
    private val A05_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF8A\uDBFF\uDF9F \uDBFF\uDF98\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDFA0 \uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF8A\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9F \uDBFF\uDF98\uDBFF\uDF9C", "Comparez ce code avec celui de votre contact", "Compare this code with your contact's")
    val A05: String get() = A05_[Lang.current]
    /** A06 - Les codes sont identiques : contact vérifié - The codes match: contact verified - kɔdey ga hima : bɔra tabbatãdi */
    private val A06_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF9A\uDBFF\uDF9E\uDBFF\uDF88\uDBFF\uDF9C : \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFAD\uDBFF\uDF9C\uDBFF\uDF8C\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "Les codes sont identiques : contact v\u00E9rifi\u00E9", "The codes match: contact verified")
    val A06: String get() = A06_[Lang.current]
    /** A07 - Les codes sont différents : ne pas ajouter - The codes differ: do not add - kɔdey si hima : ma si a tɔntɔn */
    private val A07_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF90\uDBFF\uDF9E \uDBFF\uDF9A\uDBFF\uDF9E\uDBFF\uDF88\uDBFF\uDF9C : \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF90\uDBFF\uDF9E \uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFAA\uDBFF\uDFBC\uDBFF\uDF8C\uDBFF\uDFAA\uDBFF\uDFBC", "Les codes sont diff\u00E9rents : ne pas ajouter", "The codes differ: do not add")
    val A07: String get() = A07_[Lang.current]
    /** A08 - Ajouter - Add - tɔntɔn */
    private val A08_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFAA\uDBFF\uDFBC\uDBFF\uDF8C\uDBFF\uDFAA\uDBFF\uDFBC", "Ajouter", "Add")
    val A08: String get() = A08_[Lang.current]
    /** A09 - Contact ajouté - Contact added - bɔrɔ tedjo tɔntɔn ka bɛn */
    private val A09_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDFAA\uDBFF\uDFBC\uDBFF\uDF8C\uDBFF\uDFAA\uDBFF\uDFBC \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA5\uDBFF\uDFBC", "Contact ajout\u00E9", "Contact added")
    val A09: String get() = A09_[Lang.current]
    /** A10 - Ce contact existe déjà - This contact already exists - bɔra wo goono ka bɛn */
    private val A10_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF8A\uDBFF\uDF9F \uDBFF\uDF98\uDBFF\uDFA8\uDBFF\uDF8D\uDBFF\uDF9F \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA5\uDBFF\uDFBC", "Ce contact existe d\u00E9j\u00E0", "This contact already exists")
    val A10: String get() = A10_[Lang.current]
    /** A11 - Place le code QR dans le cadre - Place the QR code inside the frame - biyo tõdo daŋ kaadiro ra */
    private val A11_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDFA9\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDFD2 \uDBFF\uDF99\uDBFF\uDFA1\uDBFF\uDF8B\uDBFF\uDF9E\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8F\uDBFF\uDF9C", "Place le code QR dans le cadre", "Place the QR code inside the frame")
    val A11: String get() = A11_[Lang.current]
    /** R02 - Relais actif - Relay active - dõtowey ga too */
    private val R02_ = arrayOf("\uDBFF\uDF8B\uDBFF\uDFA9\uDBFF\uDF8C\uDBFF\uDF9F\uDBFF\uDF8A\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFA8", "Relais actif", "Relay active")
    val R02: String get() = R02_[Lang.current]
    /** R03 - Direct - Direct - sohõ-sohõ */
    private val R03_ = arrayOf("\uDBFF\uDF90\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA9-\uDBFF\uDF90\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA9", "Direct", "Direct")
    val R03: String get() = R03_[Lang.current]
    /** R04 - Relais - Relay - toonãdika */
    private val R04_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFA8\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E\uDBFF\uDF99\uDBFF\uDF9C", "Relais", "Relay")
    val R04: String get() = R04_[Lang.current]
    /** R05 - Téléphones - Phones - talfoney */
    private val R05_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDF8D\uDBFF\uDF9D\uDBFF\uDFCC", "T\u00E9l\u00E9phones", "Phones")
    val R05: String get() = R05_[Lang.current]
    /** R07 - Bluetooth désactivé : tu es indisponible - Bluetooth is off: you are unavailable - bulutufo si huna : bɔrɔ kul si du ni */
    private val R07_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA0\uDBFF\uDF8E\uDBFF\uDFA0\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDF89\uDBFF\uDF9F \uDBFF\uDF90\uDBFF\uDF9E \uDBFF\uDF9A\uDBFF\uDFA0\uDBFF\uDF8D\uDBFF\uDF9C : \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDFBE \uDBFF\uDF90\uDBFF\uDF9E \uDBFF\uDF8B\uDBFF\uDFA0 \uDBFF\uDF8D\uDBFF\uDF9E", "Bluetooth d\u00E9sactiv\u00E9 : tu es indisponible", "Bluetooth is off: you are unavailable")
    val R07: String get() = R07_[Lang.current]
    /** R08 - Activer le Bluetooth - Turn on Bluetooth - bulutufo hunãdi */
    private val R08_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA0\uDBFF\uDF8E\uDBFF\uDFA0\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDF89\uDBFF\uDF9F \uDBFF\uDF9A\uDBFF\uDFA0\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "Activer le Bluetooth", "Turn on Bluetooth")
    val R08: String get() = R08_[Lang.current]
    /** R09 - Aucun téléphone autour de toi - No phone around you - talfon maanãte si */
    private val R09_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDFBC \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D \uDBFF\uDF90\uDBFF\uDF9E", "Aucun t\u00E9l\u00E9phone autour de toi", "No phone around you")
    val R09: String get() = R09_[Lang.current]
    /** S02 - Créer un salon - Create a room - shiifa tedji te */
    private val S02_ = arrayOf("\uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E \uDBFF\uDF8C\uDBFF\uDF9D", "Cr\u00E9er un salon", "Create a room")
    val S02: String get() = S02_[Lang.current]
    /** S03 - Nom du salon - Room name - shiifa maaɲo */
    private val S03_ = arrayOf("\uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF96\uDBFF\uDF9F", "Nom du salon", "Room name")
    val S03: String get() = S03_[Lang.current]
    /** S04 - Rejoindre - Join - ceekɔr */
    private val S04_ = arrayOf("\uDBFF\uDF93\uDBFF\uDFA3\uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDFC0", "Rejoindre", "Join")
    val S04: String get() = S04_[Lang.current]
    /** S05 - Quitter le salon - Leave the room - fatta shiifa ra */
    private val S05_ = arrayOf("\uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDFB9\uDBFF\uDF9C \uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C \uDBFF\uDF8F\uDBFF\uDF9C", "Quitter le salon", "Leave the room")
    val S05: String get() = S05_[Lang.current]
    /** S06 - [n] téléphones dans le salon - [n] phones in the room - talfone [n] bara shiifa ra */
    private val S06_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDF8D\uDBFF\uDF9D [n] \uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C \uDBFF\uDF8F\uDBFF\uDF9C", "[n] t\u00E9l\u00E9phones dans le salon", "[n] phones in the room")
    val S06: String get() = S06_[Lang.current]
    /** S07 - Les messages s'effacent après [n] h - Messages are deleted after [n] h - guuru [n] bãda alhabarey ga tuusu */
    private val S07_ = arrayOf("\uDBFF\uDF98\uDBFF\uDFAB\uDBFF\uDF8F\uDBFF\uDFA0 [n] \uDBFF\uDF86\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9C \uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDFA0", "Les messages s'effacent apr\u00E8s [n] h", "Messages are deleted after [n] h")
    val S07: String get() = S07_[Lang.current]
    /** S08 - Durée de conservation - Keep messages for - boyrey geyro */
    private val S08_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF98\uDBFF\uDF9D\uDBFF\uDFCC\uDBFF\uDF8F\uDBFF\uDF9F", "Dur\u00E9e de conservation", "Keep messages for")
    val S08: String get() = S08_[Lang.current]
    /** S09 - 3 jours (minimum) - 3 days (minimum) - zaari 3 (idjaase) */
    private val S09_ = arrayOf("\uDBFF\uDF91\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDF9E \uDBFF\uDF7F (\uDBFF\uDF9E\uDBFF\uDF95\uDBFF\uDFA1\uDBFF\uDF90\uDBFF\uDF9D)", "3 jours (minimum)", "3 days (minimum)")
    val S09: String get() = S09_[Lang.current]
    /** S10 - Heures - Hours - guuru */
    private val S10_ = arrayOf("\uDBFF\uDF98\uDBFF\uDFAB\uDBFF\uDF8F\uDBFF\uDFA0", "Heures", "Hours")
    val S10: String get() = S10_[Lang.current]
    /** S11 - Jours - Days - zaari */
    private val S11_ = arrayOf("\uDBFF\uDF91\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDF9E", "Jours", "Days")
    val S11: String get() = S11_[Lang.current]
    /** S12 - Exemple de nom : marché de Gao - Example: Gao market - maa himakasine : gaawo hebo */
    private val S12_ = arrayOf("\uDBFF\uDF88\uDBFF\uDFA1 \uDBFF\uDF9A\uDBFF\uDF9E\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF99\uDBFF\uDF9C\uDBFF\uDF90\uDBFF\uDF9E\uDBFF\uDF8D\uDBFF\uDF9D : \uDBFF\uDF98\uDBFF\uDFA1\uDBFF\uDF8A\uDBFF\uDF9F \uDBFF\uDF9A\uDBFF\uDF9D\uDBFF\uDF86\uDBFF\uDF9F", "Exemple de nom : march\u00E9 de Gao", "Example: Gao market")
    val S12: String get() = S12_[Lang.current]
    /** S13 - Aucun salon autour de toi - No room around you - shiifa maanãte kul si */
    private val S13_ = arrayOf("\uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D \uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDFBE \uDBFF\uDF90\uDBFF\uDF9E", "Aucun salon autour de toi", "No room around you")
    val S13: String get() = S13_[Lang.current]
    /** E02 - Modifier mon pseudo - Change my nickname - ay maaɲo barmey */
    private val E02_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFCC \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF96\uDBFF\uDF9F \uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF88\uDBFF\uDF9D\uDBFF\uDFCC", "Modifier mon pseudo", "Change my nickname")
    val E02: String get() = E02_[Lang.current]
    /** E04 - Réseau - Network - fawra */
    private val E04_ = arrayOf("\uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDFB6\uDBFF\uDF8F\uDBFF\uDF9C", "R\u00E9seau", "Network")
    val E04: String get() = E04_[Lang.current]
    /** E05 - Sauts maximum - Maximum hops - saryã beeri */
    private val E05_ = arrayOf("\uDBFF\uDF90\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF97\uDBFF\uDFA2 \uDBFF\uDF86\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9E", "Sauts maximum", "Maximum hops")
    val E05: String get() = E05_[Lang.current]
    /** E07 - Wi-Fi Direct - Wi-Fi Direct - wiifi sohõ-sohõ */
    private val E07_ = arrayOf("\uDBFF\uDF8A\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9E \uDBFF\uDF90\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA9-\uDBFF\uDF90\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA9", "Wi-Fi Direct", "Wi-Fi Direct")
    val E07: String get() = E07_[Lang.current]
    /** E09 - Confidentialité - Privacy - sutura */
    private val E09_ = arrayOf("\uDBFF\uDF90\uDBFF\uDFA0\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDF8F\uDBFF\uDF9C", "Confidentialit\u00E9", "Privacy")
    val E09: String get() = E09_[Lang.current]
    /** E10 - Effacement automatique - Auto-delete - nga bõ se tuusuyã */
    private val E10_ = arrayOf("\uDBFF\uDFBC\uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA9 \uDBFF\uDF90\uDBFF\uDF9D \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDFA0\uDBFF\uDF97\uDBFF\uDFA2", "Effacement automatique", "Auto-delete")
    val E10: String get() = E10_[Lang.current]
    /** E11 - Jamais - Never - abada */
    private val E11_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8B\uDBFF\uDF9C", "Jamais", "Never")
    val E11: String get() = E11_[Lang.current]
    /** E12 - Verrou de l'application - App lock - margey kɔdo */
    private val E12_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F", "Verrou de l'application", "App lock")
    val E12: String get() = E12_[Lang.current]
    /** E13 - Effacement d'urgence - Emergency wipe - tuusuyã cahãte */
    private val E13_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDFA0\uDBFF\uDF97\uDBFF\uDFA2 \uDBFF\uDF93\uDBFF\uDF9C\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D", "Effacement d'urgence", "Emergency wipe")
    val E13: String get() = E13_[Lang.current]
    /** E14 - Thème - Theme - furari */
    private val E14_ = arrayOf("\uDBFF\uDF89\uDBFF\uDFA0\uDBFF\uDF8F\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9E", "Th\u00E8me", "Theme")
    val E14: String get() = E14_[Lang.current]
    /** E15 - Automatique / Clair / Sombre - Automatic / Light / Dark - nga-bõ / kaarey / bibi */
    private val E15_ = arrayOf("\uDBFF\uDFBC\uDBFF\uDF98\uDBFF\uDF9C-\uDBFF\uDF86\uDBFF\uDFA9 / \uDBFF\uDF99\uDBFF\uDFA1\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC / \uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF86\uDBFF\uDF9E", "Automatique / Clair / Sombre", "Automatic / Light / Dark")
    val E15: String get() = E15_[Lang.current]
    /** E16 - Taille du texte - Text size - hãtum bõ */
    private val E16_ = arrayOf("\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDFB2 \uDBFF\uDF86\uDBFF\uDFA9", "Taille du texte", "Text size")
    val E16: String get() = E16_[Lang.current]
    /** E17 - Sauvegarde - Backup - kãbãdo */
    private val E17_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9F", "Sauvegarde", "Backup")
    val E17: String get() = E17_[Lang.current]
    /** E18 - Exporter - Export - fattãdi */
    private val E18_ = arrayOf("\uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDFB9\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "Exporter", "Export")
    val E18: String get() = E18_[Lang.current]
    /** E19 - Importer - Import - daŋãdi */
    private val E19_ = arrayOf("\uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDF9B\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "Importer", "Import")
    val E19: String get() = E19_[Lang.current]
    /** E20 - Réinitialiser - Reset - willãdi */
    private val E20_ = arrayOf("\uDBFF\uDF8A\uDBFF\uDF9E\uDBFF\uDFBD\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "R\u00E9initialiser", "Reset")
    val E20: String get() = E20_[Lang.current]
    /** E21 - À propos - About - ir ga */
    private val E21_ = arrayOf("\uDBFF\uDF9E\uDBFF\uDFC0 \uDBFF\uDF98\uDBFF\uDF9C", "\u00C0 propos", "About")
    val E21: String get() = E21_[Lang.current]
    /** E22 - Logiciel libre - Free software - goydjiney cawta */
    private val E22_ = arrayOf("\uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF95\uDBFF\uDF9E\uDBFF\uDF8D\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF93\uDBFF\uDF9C\uDBFF\uDFB6\uDBFF\uDF8C\uDBFF\uDF9C", "Logiciel libre", "Free software")
    val E22: String get() = E22_[Lang.current]
    /** E23 - Code source - Source code - kɔdo ŋaa */
    private val E23_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF9B\uDBFF\uDFA1", "Code source", "Source code")
    val E23: String get() = E23_[Lang.current]
    /** E24 - Règles de confidentialité - Privacy policy - sutura fõdowey */
    private val E24_ = arrayOf("\uDBFF\uDF90\uDBFF\uDFA0\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF89\uDBFF\uDFA9\uDBFF\uDF8B\uDBFF\uDF9F\uDBFF\uDF8A\uDBFF\uDF9D\uDBFF\uDFCC", "R\u00E8gles de confidentialit\u00E9", "Privacy policy")
    val E24: String get() = E24_[Lang.current]
    /** M01 - Nouveau message de [nom] - New message from [nom] - [nom] alhabar tedji */
    private val M01_ = arrayOf("[nom] \uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDFC0 \uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", "Nouveau message de [nom]", "New message from [nom]")
    val M01: String get() = M01_[Lang.current]
    /** M02 - [n] nouveaux messages - [n] new messages - alhabar tedji [n] */
    private val M02_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDFC0 \uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E [n]", "[n] nouveaux messages", "[n] new messages")
    val M02: String get() = M02_[Lang.current]
    /** M03 - Message remis - Message delivered - alhabaro to */
    private val M03_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDF9F", "Message remis", "Message delivered")
    val M03: String get() = M03_[Lang.current]
    /** M04 - Appel entrant de [nom] - Incoming call from [nom] - [nom] goo ma ni cee */
    private val M04_ = arrayOf("[nom] \uDBFF\uDF98\uDBFF\uDFA8 \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF93\uDBFF\uDFA3", "Appel entrant de [nom]", "Incoming call from [nom]")
    val M04: String get() = M04_[Lang.current]
    /** M06 - Refuser - Decline - wãdji */
    private val M06_ = arrayOf("\uDBFF\uDF8A\uDBFF\uDFA2\uDBFF\uDF95\uDBFF\uDF9E", "Refuser", "Decline")
    val M06: String get() = M06_[Lang.current]
    /** M07 - [nom] est à portée - [nom] is in range - [nom] ga man */
    private val M07_ = arrayOf("[nom] \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFBC", "[nom] est \u00E0 port\u00E9e", "[nom] is in range")
    val M07: String get() = M07_[Lang.current]
    /** M08 - Entre ton code pour ouvrir Margey - Enter your code to open Margey - ni kɔdo daŋ ka margey feeri */
    private val M08_ = arrayOf("\uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDFD2 \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF89\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9E", "Entre ton code pour ouvrir Margey", "Enter your code to open Margey")
    val M08: String get() = M08_[Lang.current]
    /** M09 - Code incorrect - Wrong code - kɔdo si boori */
    private val M09_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF90\uDBFF\uDF9E \uDBFF\uDF86\uDBFF\uDFA8\uDBFF\uDF8F\uDBFF\uDF9E", "Code incorrect", "Wrong code")
    val M09: String get() = M09_[Lang.current]
    /** M10 - Tout effacer maintenant ? - Erase everything now? - kul tuusu mɔreyda ? */
    private val M10_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDFBE \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDFA0 \uDBFF\uDF88\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC\uDBFF\uDF8B\uDBFF\uDF9C \u061F", "Tout effacer maintenant ?", "Erase everything now?")
    val M10: String get() = M10_[Lang.current]
    /** M11 - Toutes les discussions ont été effacées - All chats have been erased - boyrawey kul tuusa */
    private val M11_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8F\uDBFF\uDF9C\uDBFF\uDF8A\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDFBE \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDF9C", "Toutes les discussions ont \u00E9t\u00E9 effac\u00E9es", "All chats have been erased")
    val M11: String get() = M11_[Lang.current]
    /** M12 - Sauvegarde terminée - Backup complete - kãbãdo bɛn */
    private val M12_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF86\uDBFF\uDFA5\uDBFF\uDFBC", "Sauvegarde termin\u00E9e", "Backup complete")
    val M12: String get() = M12_[Lang.current]
    /** M13 - Fichier non reconnu - File not recognised - tiira wo tammaasa mana beyãdi */
    private val M13_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFA6\uDBFF\uDF8F\uDBFF\uDF9C \uDBFF\uDF8A\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFB1\uDBFF\uDFA1\uDBFF\uDF90\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDF9D\uDBFF\uDF97\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "Fichier non reconnu", "File not recognised")
    val M13: String get() = M13_[Lang.current]
    /** M14 - Message trop long - Message too long - alhabaro hãse ka kuu */
    private val M14_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF90\uDBFF\uDF9D \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDFAB", "Message trop long", "Message too long")
    val M14: String get() = M14_[Lang.current]
    /** M15 - Copié - Copied - yee a ga ka bɛn */
    private val M15_ = arrayOf("\uDBFF\uDF97\uDBFF\uDFA3 \uDBFF\uDF9C \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA5\uDBFF\uDFBC", "Copi\u00E9", "Copied")
    val M15: String get() = M15_[Lang.current]
    /** L01 - Localisation - Location - dogo */
    private val L01_ = arrayOf("\uDBFF\uDF8B\uDBFF\uDF9F\uDBFF\uDF98\uDBFF\uDF9F", "Localisation", "Location")
    val L01: String get() = L01_[Lang.current]
    /** L02 - Android demande la localisation pour trouver les téléphones proches en Bluetooth - Android needs location access to find nearby phones over Bluetooth - talfono ga hima ka ni dogo bey hal a ma hin ka du bɔrɔ maanãtey */
    private val L02_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDF8D\uDBFF\uDF9F \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF9A\uDBFF\uDF9E\uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF8D\uDBFF\uDF9E \uDBFF\uDF8B\uDBFF\uDF9F\uDBFF\uDF98\uDBFF\uDF9F \uDBFF\uDF86\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDFBE \uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF9A\uDBFF\uDF9E\uDBFF\uDFBC \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF8B\uDBFF\uDFA0 \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDFCC", "Android demande la localisation pour trouver les t\u00E9l\u00E9phones proches en Bluetooth", "Android needs location access to find nearby phones over Bluetooth")
    val L02: String get() = L02_[Lang.current]
    /** L03 - Pour garder la maille active, laisse Margey fonctionner en arrière-plan - To keep the mesh alive, let Margey run in the background - Hal goyo ma te ka boori, margey ga hima ka goy waati kul */
    private val L03_ = arrayOf("\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDFBE \uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDF97\uDBFF\uDF9F \uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9D \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF86\uDBFF\uDFA8\uDBFF\uDF8F\uDBFF\uDF9E\uDBFF\uDFDC \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF9A\uDBFF\uDF9E\uDBFF\uDF88\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDFCC \uDBFF\uDF8A\uDBFF\uDFA1\uDBFF\uDF8C\uDBFF\uDF9E \uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDFBE", "Pour garder la maille active, laisse Margey fonctionner en arri\u00E8re-plan", "To keep the mesh alive, let Margey run in the background")
    val L03: String get() = L03_[Lang.current]
    /** L04 - Batterie - Battery - baatiri */
    private val L04_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA1\uDBFF\uDF8C\uDBFF\uDF9E\uDBFF\uDF8F\uDBFF\uDF9E", "Batterie", "Battery")
    val L04: String get() = L04_[Lang.current]
    /** K01 - Groupes - Groups - margarey */
    private val K01_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC", "Groupes", "Groups")
    val K01: String get() = K01_[Lang.current]
    /** K02 - Créer un groupe - Create a group - ka margari taka */
    private val K02_ = arrayOf("\uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9E \uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDF99\uDBFF\uDF9C", "Cr\u00E9er un groupe", "Create a group")
    val K02: String get() = K02_[Lang.current]
    /** K03 - Nom du groupe - Group name - margaro maaɲo */
    private val K03_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF96\uDBFF\uDF9F", "Nom du groupe", "Group name")
    val K03: String get() = K03_[Lang.current]
    /** K04 - Inviter (montrer le QR du groupe) - Invite (show the group QR code) - beyrãdi (margaro biyo tõdo cebe) */
    private val K04_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9D\uDBFF\uDFCC\uDBFF\uDF8F\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E (\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDFA9\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF93\uDBFF\uDF9D\uDBFF\uDF86\uDBFF\uDF9D)", "Inviter (montrer le QR du groupe)", "Invite (show the group QR code)")
    val K04: String get() = K04_[Lang.current]
    /** K05 - [n] membres - [n] members - bɔrɔ [n] */
    private val K05_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA [n]", "[n] membres", "[n] members")
    val K05: String get() = K05_[Lang.current]
    /** K06 - Groupe complet : 100 personnes au maximum - Group full: 100 people maximum - margari timmãte : bɔrɔ 100 dee */
    private val K06_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9E \uDBFF\uDF8C\uDBFF\uDF9E\uDBFF\uDFB1\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D : \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF7D\uDBFF\uDF7C\uDBFF\uDF7C \uDBFF\uDF8B\uDBFF\uDFA3", "Groupe complet : 100 personnes au maximum", "Group full: 100 people maximum")
    val K06: String get() = K06_[Lang.current]
    /** K07 - Rejoindre le groupe ? - Join the group? - ka huru margaro ra ? */
    private val K07_ = arrayOf("\uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF9A\uDBFF\uDFA0\uDBFF\uDF8F\uDBFF\uDFA0 \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8F\uDBFF\uDF9C \u061F", "Rejoindre le groupe ?", "Join the group?")
    val K07: String get() = K07_[Lang.current]
    /** K08 - Quitter le groupe - Leave the group - ka fatta margaro ra */
    private val K08_ = arrayOf("\uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDFB9\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8F\uDBFF\uDF9C", "Quitter le groupe", "Leave the group")
    val K08: String get() = K08_[Lang.current]
    /** K09 - Les messages du groupe ne s'effacent jamais - Group messages are never deleted automatically - margaro alhabarey si tuusa */
    private val K09_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF90\uDBFF\uDF9E \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDF9C", "Les messages du groupe ne s'effacent jamais", "Group messages are never deleted automatically")
    val K09: String get() = K09_[Lang.current]
    /** K10 - Fais scanner ce QR pour inviter dans le groupe - Let them scan this QR code to invite them to the group - biyo tõdo wo sikane ka margaro beyrãdi */
    private val K10_ = arrayOf("\uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDFA9\uDBFF\uDF8B\uDBFF\uDF9F \uDBFF\uDF8A\uDBFF\uDF9F \uDBFF\uDF90\uDBFF\uDF9E\uDBFF\uDF99\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9D \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF86\uDBFF\uDF9D\uDBFF\uDFCC\uDBFF\uDF8F\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "Fais scanner ce QR pour inviter dans le groupe", "Let them scan this QR code to invite them to the group")
    val K10: String get() = K10_[Lang.current]
    /** S14 - Salon complet : 500 personnes au maximum - Room full: 500 people maximum - shiifa timmãte : bɔrɔ 500 dee */
    private val S14_ = arrayOf("\uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9E\uDBFF\uDFB1\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D : \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDFAA \uDBFF\uDF81\uDBFF\uDF7C\uDBFF\uDF7C \uDBFF\uDF8B\uDBFF\uDFA3", "Salon complet : 500 personnes au maximum", "Room full: 500 people maximum")
    val S14: String get() = S14_[Lang.current]
    /** T01 - [nom] a fait entrer [nom] - [nom] added [nom] - [nom] na [nom] daŋ */
    private val T01_ = arrayOf("[nom] \uDBFF\uDF8D\uDBFF\uDF9C [nom] \uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDFD2", "[nom] a fait entrer [nom]", "[nom] added [nom]")
    val T01: String get() = T01_[Lang.current]
    /** T02 - [nom] a quitté - [nom] left - [nom] fatta */
    private val T02_ = arrayOf("[nom] \uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDFB9\uDBFF\uDF9C", "[nom] a quitt\u00E9", "[nom] left")
    val T02: String get() = T02_[Lang.current]
    /** T03 - L'administrateur a exclu [nom] - The admin removed [nom] - goyteero na [nom] ka */
    private val T03_ = arrayOf("\uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8C\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8D\uDBFF\uDF9C [nom] \uDBFF\uDF99\uDBFF\uDF9C", "L'administrateur a exclu [nom]", "The admin removed [nom]")
    val T03: String get() = T03_[Lang.current]
    /** T04 - [nom] a été retiré : 45 jours sans être vu - [nom] was removed: not seen for 45 days - [nom] hun (djirdi 45, bɔrey mana a maate) */
    private val T04_ = arrayOf("[nom] \uDBFF\uDF9A\uDBFF\uDFA0\uDBFF\uDFBC (\uDBFF\uDF95\uDBFF\uDF9E\uDBFF\uDFC0\uDBFF\uDF8B\uDBFF\uDF9E \uDBFF\uDF80\uDBFF\uDF81\uDBFF\uDFDC \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9C \uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8C\uDBFF\uDF9D)", "[nom] a \u00E9t\u00E9 retir\u00E9 : 45 jours sans \u00EAtre vu", "[nom] was removed: not seen for 45 days")
    val T04: String get() = T04_[Lang.current]
    /** T05 - [nom] a été retiré : nombre maximum atteint - [nom] was removed: maximum reached - [nom] hun, bɔrey hinna too */
    private val T05_ = arrayOf("[nom] \uDBFF\uDF9A\uDBFF\uDFA0\uDBFF\uDFBC\uDBFF\uDFDC \uDBFF\uDF86\uDBFF\uDFAA\uDBFF\uDF8F\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF9A\uDBFF\uDF9E\uDBFF\uDFBB\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFA8", "[nom] a \u00E9t\u00E9 retir\u00E9 : nombre maximum atteint", "[nom] was removed: maximum reached")
    val T05: String get() = T05_[Lang.current]
    /** T06 - [nom] est le nouvel administrateur - [nom] is the new admin - [nom] ti goyteeri tedjo */
    private val T06_ = arrayOf("[nom] \uDBFF\uDF8C\uDBFF\uDF9E \uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8C\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9E \uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9F", "[nom] est le nouvel administrateur", "[nom] is the new admin")
    val T06: String get() = T06_[Lang.current]
    /** T07 - Exclure - Remove - Kaayã */
    private val T07_ = arrayOf("\uDBFF\uDF99\uDBFF\uDFA1\uDBFF\uDF97\uDBFF\uDFA2", "Exclure", "Remove")
    val T07: String get() = T07_[Lang.current]
    /** T08 - Réintégrer - Add back - yee ka a daŋ */
    private val T08_ = arrayOf("\uDBFF\uDF97\uDBFF\uDFA3 \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF9C \uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDFD2", "R\u00E9int\u00E9grer", "Add back")
    val T08: String get() = T08_[Lang.current]
    /** T09 - Effacer mon message - Delete my message - ay alhabaro tuusu */
    private val T09_ = arrayOf("\uDBFF\uDF9C\uDBFF\uDFCC \uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF86\uDBFF\uDF9C\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8C\uDBFF\uDFAB\uDBFF\uDF90\uDBFF\uDFA0", "Effacer mon message", "Delete my message")
    val T09: String get() = T09_[Lang.current]
    /** T10 - Places réservées à l'administrateur - Places reserved for the admin - goyteero dogo */
    private val T10_ = arrayOf("\uDBFF\uDF98\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF8C\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9F \uDBFF\uDF8B\uDBFF\uDF9F\uDBFF\uDF98\uDBFF\uDF9F", "Places r\u00E9serv\u00E9es \u00E0 l'administrateur", "Places reserved for the admin")
    val T10: String get() = T10_[Lang.current]
    /** T11 - [nom] est entré - [nom] joined - [nom] huru */
    private val T11_ = arrayOf("[nom] \uDBFF\uDF9A\uDBFF\uDFA0\uDBFF\uDF8F\uDBFF\uDFA0", "[nom] est entr\u00E9", "[nom] joined")
    val T11: String get() = T11_[Lang.current]
    /** N01 - Partager Margey - Share Margey - margey fey-fey */
    private val N01_ = arrayOf("\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDFC0\uDBFF\uDF98\uDBFF\uDF9D\uDBFF\uDFCC \uDBFF\uDF89\uDBFF\uDF9D\uDBFF\uDFCC-\uDBFF\uDF89\uDBFF\uDF9D\uDBFF\uDFCC", "Partager Margey", "Share Margey")
    val N01: String get() = N01_[Lang.current]
    /** N02 - Envoie l'application à un téléphone proche, sans Internet - Send the app to a nearby phone, without Internet - a sãba talfon maanãte se */
    private val N02_ = arrayOf("\uDBFF\uDF9C \uDBFF\uDF90\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDFBC \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D \uDBFF\uDF90\uDBFF\uDF9D", "Envoie l'application \u00E0 un t\u00E9l\u00E9phone proche, sans Internet", "Send the app to a nearby phone, without Internet")
    val N02: String get() = N02_[Lang.current]
    /** N03 - Photo - Photo - bii */
    private val N03_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA6", "Photo", "Photo")
    val N03: String get() = N03_[Lang.current]
    /** N05 - Alerte - Alert - taashiyã */
    private val N05_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFA1\uDBFF\uDF92\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDFA2", "Alerte", "Alert")
    val N05: String get() = N05_[Lang.current]
    /** N06 - Envoyer une alerte - Send an alert - taashiyã sãba */
    private val N06_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFA1\uDBFF\uDF92\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDFA2 \uDBFF\uDF90\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDF9C", "Envoyer une alerte", "Send an alert")
    val N06: String get() = N06_[Lang.current]
    /** N07 - Écris ce qui se passe - Describe what is happening - haya kã go mate hãtum */
    private val N07_ = arrayOf("\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDF97\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDFA2 \uDBFF\uDF98\uDBFF\uDF9F \uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF8C\uDBFF\uDF9D \uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDFA0\uDBFF\uDFB2", "\u00C9cris ce qui se passe", "Describe what is happening")
    val N07: String get() = N07_[Lang.current]
    /** N08 - Alerte de [nom] - Alert from [nom] - [nom] taashiyano */
    private val N08_ = arrayOf("[nom] \uDBFF\uDF8C\uDBFF\uDFA1\uDBFF\uDF92\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDF9C\uDBFF\uDF8D\uDBFF\uDF9F", "Alerte de [nom]", "Alert from [nom]")
    val N08: String get() = N08_[Lang.current]
    /** N09 - Envoyer l'alerte aux téléphones proches ? - Send the alert to nearby phones? - taashiyã sãba talfon maanãte se */
    private val N09_ = arrayOf("\uDBFF\uDF8C\uDBFF\uDFA1\uDBFF\uDF92\uDBFF\uDF9E\uDBFF\uDF97\uDBFF\uDFA2 \uDBFF\uDF90\uDBFF\uDFA2\uDBFF\uDF86\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDF9C\uDBFF\uDFBE\uDBFF\uDF89\uDBFF\uDF9F\uDBFF\uDFBC \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D \uDBFF\uDF90\uDBFF\uDF9D", "Envoyer l'alerte aux t\u00E9l\u00E9phones proches ?", "Send the alert to nearby phones?")
    val N09: String get() = N09_[Lang.current]
    /** N10 - Apprendre l'AYNEHA - Learn AYNEHA - A TRADUIRE */
    private val N10_ = arrayOf("Apprendre l'AYNEHA", "Apprendre l'AYNEHA", "Learn AYNEHA")
    val N10: String get() = N10_[Lang.current]
    /** N11 - Alphabet - Alphabet - A TRADUIRE */
    private val N11_ = arrayOf("Alphabet", "Alphabet", "Alphabet")
    val N11: String get() = N11_[Lang.current]
    /** N12 - Lettre du jour - Letter of the day - A TRADUIRE */
    private val N12_ = arrayOf("Lettre du jour", "Lettre du jour", "Letter of the day")
    val N12: String get() = N12_[Lang.current]
    /** N13 - Mot du jour - Word of the day - A TRADUIRE */
    private val N13_ = arrayOf("Mot du jour", "Mot du jour", "Word of the day")
    val N13: String get() = N13_[Lang.current]
    /** N14 - Quelle est cette lettre ? - Which letter is this? - A TRADUIRE */
    private val N14_ = arrayOf("Quelle est cette lettre ?", "Quelle est cette lettre ?", "Which letter is this?")
    val N14: String get() = N14_[Lang.current]
    /** N15 - Bonne réponse ! - Correct! - A TRADUIRE */
    private val N15_ = arrayOf("Bonne r\u00E9ponse !", "Bonne r\u00E9ponse !", "Correct!")
    val N15: String get() = N15_[Lang.current]
    /** N16 - Ce n'est pas ça - Not quite - A TRADUIRE */
    private val N16_ = arrayOf("Ce n'est pas \u00E7a", "Ce n'est pas \u00E7a", "Not quite")
    val N16: String get() = N16_[Lang.current]
    /** N17 - Score - Score - A TRADUIRE */
    private val N17_ = arrayOf("Score", "Score", "Score")
    val N17: String get() = N17_[Lang.current]
    /** N18 - double - double - A TRADUIRE */
    private val N18_ = arrayOf("double", "double", "double")
    val N18: String get() = N18_[Lang.current]
    /** N19 - muette - silent - A TRADUIRE */
    private val N19_ = arrayOf("muette", "muette", "silent")
    val N19: String get() = N19_[Lang.current]
    /** N20 - nasale - nasal - A TRADUIRE */
    private val N20_ = arrayOf("nasale", "nasale", "nasal")
    val N20: String get() = N20_[Lang.current]
    /** N21 - ouverte - open - A TRADUIRE */
    private val N21_ = arrayOf("ouverte", "ouverte", "open")
    val N21: String get() = N21_[Lang.current]
    /** N22 - En attente : remis dès que [nom] sera à portée - Waiting: delivered as soon as [nom] is in range - baabuyã : ga too zaa [nom] maanãdi */
    private val N22_ = arrayOf("\uDBFF\uDF86\uDBFF\uDFA1\uDBFF\uDF86\uDBFF\uDFA0\uDBFF\uDF97\uDBFF\uDFA2 : \uDBFF\uDF98\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFA8 \uDBFF\uDF91\uDBFF\uDFA1 [nom] \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9E", "En attente : remis d\u00E8s que [nom] sera \u00E0 port\u00E9e", "Waiting: delivered as soon as [nom] is in range")
    val N22: String get() = N22_[Lang.current]
    /** N23 - Pris en charge - Help is here - faaba */
    private val N23_ = arrayOf("\uDBFF\uDF89\uDBFF\uDFA1\uDBFF\uDF86\uDBFF\uDF9C", "Pris en charge", "Help is here")
    val N23: String get() = N23_[Lang.current]
    /** N24 - Pris en charge par [nom] - Handled by [nom] - [nom] ne faaba too */
    private val N24_ = arrayOf("[nom] \uDBFF\uDF8D\uDBFF\uDF9D \uDBFF\uDF89\uDBFF\uDFA1\uDBFF\uDF86\uDBFF\uDF9C \uDBFF\uDF8C\uDBFF\uDFA8", "Pris en charge par [nom]", "Handled by [nom]")
    val N24: String get() = N24_[Lang.current]
    /** S15 - Salons à portée - Rooms nearby - shiifa maanãtey */
    private val S15_ = arrayOf("\uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C \uDBFF\uDF88\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDFCC", "Salons \u00E0 port\u00E9e", "Rooms nearby")
    val S15: String get() = S15_[Lang.current]
    /** S16 - Code du salon - Room code - shiifa kɔdo */
    private val S16_ = arrayOf("\uDBFF\uDF92\uDBFF\uDFA6\uDBFF\uDF89\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F", "Code du salon", "Room code")
    val S16: String get() = S16_[Lang.current]
    /** S17 - Rejoindre avec un code - Join with a code - ka ceekɔr nda kɔdo */
    private val S17_ = arrayOf("\uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF93\uDBFF\uDFA3\uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDFC0 \uDBFF\uDFBC\uDBFF\uDF8B\uDBFF\uDF9C \uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDF8B\uDBFF\uDF9F", "Rejoindre avec un code", "Join with a code")
    val S17: String get() = S17_[Lang.current]
    /** S18 - Créé par [nom] - Created by [nom] - [nom] ka feeri */
    private val S18_ = arrayOf("[nom] \uDBFF\uDF99\uDBFF\uDF9C \uDBFF\uDF89\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9E", "Cr\u00E9\u00E9 par [nom]", "Created by [nom]")
    val S18: String get() = S18_[Lang.current]
}
