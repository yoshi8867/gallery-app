package com.yoshi0311.gallery.data.local.mediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.yoshi0311.gallery.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreDataSource @Inject constructor(
    private val contentResolver: ContentResolver,
) {
    // API 29+는 VOLUME_EXTERNAL, 그 이하는 "external" 문자열 사용
    private val mediaUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Files.getContentUri("external")
    }

    // ── 공용 projection / cursor 파서 ────────────────────────────

    private fun buildProjection() = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_TAKEN,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.WIDTH,
        MediaStore.Files.FileColumns.HEIGHT,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Images.ImageColumns.BUCKET_ID,
        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
        MediaStore.Video.VideoColumns.DURATION,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
    )

    private fun parseCursor(cursor: Cursor): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val dateTakenCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
        val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
        val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
        val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
        val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
        val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_ID)
        val bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
        val mediaTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val mediaType = cursor.getInt(mediaTypeCol)
            val contentUri = ContentUris.withAppendedId(
                if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                else
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                id,
            )
            val dateTakenMs = cursor.getLong(dateTakenCol)
            val dateAddedMs = cursor.getLong(dateAddedCol) * 1_000L
            val durationMs = cursor.getLong(durationCol).takeIf { it > 0 }

            items += MediaItem(
                id = id,
                uri = contentUri,
                name = cursor.getString(nameCol) ?: "",
                dateTaken = if (dateTakenMs > 0) dateTakenMs else dateAddedMs,
                dateAdded = dateAddedMs,
                size = cursor.getLong(sizeCol),
                width = cursor.getInt(widthCol),
                height = cursor.getInt(heightCol),
                mimeType = cursor.getString(mimeTypeCol) ?: "",
                bucketId = cursor.getLong(bucketIdCol),
                bucketName = cursor.getString(bucketNameCol) ?: "",
                duration = durationMs,
            )
        }
        return items
    }

    // ── 일반 미디어 쿼리 ─────────────────────────────────────────

    /**
     * 기기 내 모든 이미지·동영상을 촬영일 내림차순으로 반환한다.
     * ContentObserver로 미디어 변경을 감지하며 자동 재발행한다.
     */
    fun observeAllMedia(): Flow<List<MediaItem>> = callbackFlow {
        fun reload() { launch { send(queryAllMedia()) } }
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) = reload()
        }
        contentResolver.registerContentObserver(mediaUri, true, observer)
        reload()
        awaitClose { contentResolver.unregisterContentObserver(observer) }
    }.flowOn(Dispatchers.IO)

    private fun queryAllMedia(): List<MediaItem> {
        val selection = "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) AND " +
            "${MediaStore.Files.FileColumns.SIZE} > 0"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC, " +
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        return contentResolver.query(mediaUri, buildProjection(), selection, selectionArgs, sortOrder)
            ?.use { parseCursor(it) } ?: emptyList()
    }

    // ── 휴지통 쿼리 (API 30+) ────────────────────────────────────

    @RequiresApi(Build.VERSION_CODES.R)
    fun observeTrashMedia(): Flow<List<MediaItem>> = callbackFlow {
        fun reload() { launch { send(queryTrashMedia()) } }
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) = reload()
        }
        contentResolver.registerContentObserver(mediaUri, true, observer)
        reload()
        awaitClose { contentResolver.unregisterContentObserver(observer) }
    }.flowOn(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.R)
    private fun queryTrashMedia(): List<MediaItem> {
        val queryArgs = Bundle().apply {
            putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
            putString(
                ContentResolver.QUERY_ARG_SQL_SELECTION,
                "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) AND " +
                    "${MediaStore.Files.FileColumns.SIZE} > 0",
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                ),
            )
            putString(
                ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC, " +
                    "${MediaStore.Files.FileColumns.DATE_ADDED} DESC",
            )
        }
        return contentResolver.query(mediaUri, buildProjection(), queryArgs, null)
            ?.use { parseCursor(it) } ?: emptyList()
    }
}
