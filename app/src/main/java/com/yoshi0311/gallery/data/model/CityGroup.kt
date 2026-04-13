package com.yoshi0311.gallery.data.model

import android.net.Uri

data class CityGroup(
    val country: String,
    val city: String,
    val coverUri: Uri,
    val count: Int,
    val items: List<MediaItem>,
)
