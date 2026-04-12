package com.yoshi0311.gallery.data.model

import android.net.Uri

data class Album(
    val id: Long,
    val name: String,
    val coverUri: Uri,
    val count: Int,
    val latestDate: Long,   // epoch ms — 앨범 내 가장 최근 미디어의 dateTaken
)
