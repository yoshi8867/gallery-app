package com.yoshi0311.gallery.util

import android.content.Context
import android.content.Intent
import com.yoshi0311.gallery.data.model.MediaItem

fun shareMediaItems(context: Context, items: List<MediaItem>) {
    if (items.isEmpty()) return
    val uris = ArrayList(items.map { it.uri })
    val intent = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = items.first().mimeType.ifEmpty { "*/*" }
            putExtra(Intent.EXTRA_STREAM, uris.first())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    } else {
        val types = items.map { it.mimeType }.toSet()
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = if (types.size == 1) types.first().ifEmpty { "*/*" } else "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(Intent.createChooser(intent, null))
}
