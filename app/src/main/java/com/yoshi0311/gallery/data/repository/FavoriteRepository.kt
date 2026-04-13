package com.yoshi0311.gallery.data.repository

import com.yoshi0311.gallery.data.local.db.FavoriteDao
import com.yoshi0311.gallery.data.local.db.FavoriteEntity
import com.yoshi0311.gallery.data.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface FavoriteRepository {
    /** 즐겨찾기된 mediaId 집합 — 실시간 */
    fun observeFavoriteIds(): Flow<Set<Long>>

    /** 즐겨찾기된 MediaItem 목록 (촬영일 내림차순) */
    fun observeFavoriteItems(): Flow<List<MediaItem>>

    /** 즐겨찾기 추가 / 해제 토글 */
    suspend fun toggleFavorite(mediaId: Long)

    /** 여러 항목을 즐겨찾기에 추가 (이미 추가된 항목은 건너뜀) */
    suspend fun addFavorites(mediaIds: Set<Long>)

    /** 여러 항목을 즐겨찾기에서 제거 */
    suspend fun removeFavorites(mediaIds: Set<Long>)
}

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
    private val mediaRepository: MediaRepository,
) : FavoriteRepository {

    override fun observeFavoriteIds(): Flow<Set<Long>> =
        dao.observeAllIds().map { it.toSet() }

    override fun observeFavoriteItems(): Flow<List<MediaItem>> =
        combine(mediaRepository.getAllMedia(), observeFavoriteIds()) { items, ids ->
            items.filter { it.id in ids }
        }

    override suspend fun toggleFavorite(mediaId: Long) {
        if (dao.count(mediaId) > 0) dao.delete(mediaId)
        else dao.insert(FavoriteEntity(mediaId))
    }

    override suspend fun addFavorites(mediaIds: Set<Long>) {
        mediaIds.forEach { id ->
            if (dao.count(id) == 0) dao.insert(FavoriteEntity(id))
        }
    }

    override suspend fun removeFavorites(mediaIds: Set<Long>) {
        mediaIds.forEach { dao.delete(it) }
    }
}
