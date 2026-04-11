package com.yoshi0311.gallery.data.repository

import com.yoshi0311.gallery.data.source.MediaStoreDataSource
import com.yoshi0311.gallery.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface MediaRepository {
    /** 모든 미디어(이미지+동영상) — 촬영일 내림차순 */
    fun getAllMedia(): Flow<List<MediaItem>>

    /** 특정 앨범(버킷) 내 미디어 */
    fun getMediaByAlbum(albumId: Long): Flow<List<MediaItem>>
}

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val dataSource: MediaStoreDataSource,
) : MediaRepository {

    override fun getAllMedia(): Flow<List<MediaItem>> =
        dataSource.observeAllMedia()

    override fun getMediaByAlbum(albumId: Long): Flow<List<MediaItem>> =
        dataSource.observeAllMedia().map { items ->
            items.filter { it.bucketId == albumId }
        }
}
