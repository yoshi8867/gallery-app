package com.yoshi0311.gallery.data.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateTaken: Long,       // epoch ms (DATE_TAKEN이 없으면 DATE_ADDED 기반)
    val dateAdded: Long,       // epoch ms
    val size: Long,            // bytes
    val width: Int,
    val height: Int,
    val mimeType: String,
    val bucketId: Long,
    val bucketName: String,
    val duration: Long? = null,    // null: 이미지, >0: 동영상(ms)
    val latitude: Double? = null,  // EXIF 읽기 시 채움 (지도 화면 전용)
    val longitude: Double? = null,
) {
    val isVideo: Boolean get() = duration != null
}
