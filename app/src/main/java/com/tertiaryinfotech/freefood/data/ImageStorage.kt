package com.tertiaryinfotech.freefood.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/** Copies picked photos into app-internal storage so they survive the original Uri's lifetime. */
object ImageStorage {
    fun copyToInternal(context: Context, uri: Uri): String? = runCatching {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val target = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.absolutePath
    }.getOrNull()
}
