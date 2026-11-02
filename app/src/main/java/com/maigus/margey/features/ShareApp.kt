// SPDX-License-Identifier: GPL-3.0-or-later
// Margey - partager l'application elle-même, sans Internet (Bluetooth, partage rapide d'Android...)
// Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.margey.features

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareApp {
    /** Copie l'APK installé et ouvre le choix d'Android (Bluetooth, Quick Share...). */
    fun share(ctx: Context) {
        try {
            val src = File(ctx.applicationInfo.sourceDir)
            val dir = File(ctx.cacheDir, "apk_share").apply { mkdirs() }
            val out = File(dir, "Margey.apk")
            src.copyTo(out, overwrite = true)
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", out)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ctx.startActivity(Intent.createChooser(send, "Margey").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) { }
    }
}
