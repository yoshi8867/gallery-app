package com.yoshi0311.gallery.data.repository

import com.yoshi0311.gallery.data.local.mediastore.MediaStoreDataSource
import com.yoshi0311.gallery.data.model.Album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface AlbumRepository {
    /** 기기 내 모든 앨범(버킷) — 이름 오름차순 */
    fun getAllAlbums(): Flow<List<Album>>
}

@Singleton
class AlbumRepositoryImpl @Inject constructor(
    private val dataSource: MediaStoreDataSource,
) : AlbumRepository {

    override fun getAllAlbums(): Flow<List<Album>> =
        dataSource.observeAllMedia().map { items ->
            items
                .groupBy { it.bucketId }
                .map { (bucketId, bucketItems) ->
                    Album(
                        id = bucketId,
                        name = bucketItems.first().bucketName,
                        coverUri = bucketItems.first().uri,
                        count = bucketItems.size,
                    )
                }
                .sortedBy { it.name }
        }
}
