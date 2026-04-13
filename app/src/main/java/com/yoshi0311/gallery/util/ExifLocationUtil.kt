package com.yoshi0311.gallery.util

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface

object ExifLocationUtil {
    /**
     * 이미지 URI에서 EXIF GPS 좌표를 읽어 반환한다.
     * ACCESS_MEDIA_LOCATION 권한이 필요하다 (API 29+는 setRequireOriginal 사용).
     * IO 스레드에서 호출해야 한다.
     */
    fun getLatLng(contentResolver: ContentResolver, uri: Uri): Pair<Double, Double>? {
        return try {
            val resolvedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.setRequireOriginal(uri)
            } else {
                uri
            }
            contentResolver.openInputStream(resolvedUri)?.use { stream ->
                val exif = ExifInterface(stream)
                exif.latLong?.let { Pair(it[0], it[1]) }
            }
        } catch (_: Exception) {
            null
        }
    }
}
