package com.yoshi0311.gallery.data.model

import android.net.Uri

data class Story(
    val id: Long,           // bucketId 기반 안정적 ID
    val title: String,
    val coverUri: Uri,
    val dateStart: Long,    // 가장 오래된 사진 epoch ms
    val dateEnd: Long,      // 가장 최근 사진 epoch ms
    val mediaItems: List<MediaItem>,
) {
    val count: Int get() = mediaItems.size
}
