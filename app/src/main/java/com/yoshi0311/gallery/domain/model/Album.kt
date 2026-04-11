package com.yoshi0311.gallery.domain.model

import android.net.Uri

data class Album(
    val id: Long,
    val name: String,
    val coverUri: Uri,
    val count: Int,
)
