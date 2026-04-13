package com.yoshi0311.gallery.data.repository

import com.yoshi0311.gallery.data.local.mediastore.MediaStoreDataSource
import com.yoshi0311.gallery.data.model.Album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

interface AlbumRepository {
    /** 기기 내 모든 앨범(버킷) — 휴지통 항목 제외, 이름 오름차순 */
    fun getAllAlbums(): Flow<List<Album>>
}

@Singleton
class AlbumRepositoryImpl @Inject constructor(
    private val dataSource: MediaStoreDataSource,
    private val trashRepository: TrashRepository,
) : AlbumRepository {

    override fun getAllAlbums(): Flow<List<Album>> =
        combine(dataSource.observeAllMedia(), trashRepository.observeTrashIds()) { items, trashIds ->
            items
                .filter { it.id !in trashIds }
                .groupBy { it.bucketId }
                .map { (bucketId, bucketItems) ->
                    Album(
                        id = bucketId,
                        name = bucketItems.first().bucketName,
                        coverUri = bucketItems.first().uri,
                        count = bucketItems.size,
                        latestDate = bucketItems.maxOf { it.dateTaken },
                    )
                }
                .sortedBy { it.name }
        }
}
