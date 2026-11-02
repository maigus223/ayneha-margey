package com.bitchat.android

import android.app.Application
import com.bitchat.android.ui.theme.ThemePreferenceManager

/**
 * Main application class for bitchat Android
 */
class BitchatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Start the single process-wide power policy before transport components are constructed.
        com.bitchat.android.mesh.PowerManager.getInstance(this).start()

        // AYNEHA Margey : pas de Tor, pas de relais Nostr, pas de notes de lieu (aucune liaison Internet).

        // Initialize favorites persistence early so MessageRouter/NostrTransport can use it on startup
        try {
            com.bitchat.android.favorites.FavoritesPersistenceService.initialize(this)
        } catch (_: Exception) { }

        // Restore private conversations before background transports can deliver new messages.
        // AppStateStore merges any in-flight arrivals by message ID, so startup cannot replace
        // newer transport state with an older database snapshot.
        try {
            com.bitchat.android.services.AppStateStore.initializeConversationPersistence(this)
        } catch (_: Exception) { }


        // Restore the spam filter toggle before transports start admitting public messages
        com.bitchat.android.spam.SpamFilterPreferences.init(this)

        // Initialize theme preference
        ThemePreferenceManager.init(this)

        // Initialize chat UI mode (matrix transcript vs bubbles)
        com.bitchat.android.ui.theme.ChatUiModeManager.init(this)

        // Initialize debug preference manager (persists debug toggles)
        try { com.bitchat.android.ui.debug.DebugPreferenceManager.init(this) } catch (_: Exception) { }

        // Initialize Wi‑Fi Aware controller with persisted default
        try {
            val enabled = com.bitchat.android.ui.debug.DebugPreferenceManager.getWifiAwareEnabled(true) // Margey : Wi-Fi direct activé par défaut
            com.bitchat.android.wifiaware.WifiAwareController.initialize(this, enabled)
        } catch (_: Exception) { }

        // Initialize Geohash Registries for persistence
        try {
            com.bitchat.android.nostr.GeohashAliasRegistry.initialize(this)
            com.bitchat.android.nostr.GeohashConversationRegistry.initialize(this)
        } catch (_: Exception) { }


        // AYNEHA Margey : messages des salons conservés 72 h minimum
        try { com.maigus.margey.salon.SalonDirectory.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.salon.SalonStore.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.tata.TataKeys.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.group.GroupStore.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.salon.SalonTata.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.features.Alerts.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.features.MediaFiles.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.features.Courier.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.ui.Display.load(this) } catch (_: Exception) { }
        try {
            com.maigus.margey.text.Lang.init(this)
            com.maigus.margey.ui.Kb.ayneha = com.maigus.margey.text.Lang.isAyneha
        } catch (_: Exception) { }
        try { com.maigus.margey.security.AppLock.init(this) } catch (_: Exception) { }
        try { com.maigus.margey.security.AutoErase.init(this) } catch (_: Exception) { }

        // Initialize mesh service preferences
        try { com.bitchat.android.service.MeshServicePreferences.init(this) } catch (_: Exception) { }

        // Proactively start the foreground service to keep mesh alive
        try { com.bitchat.android.service.MeshForegroundService.start(this) } catch (_: Exception) { }

        // TorManager already initialized above
    }
}
