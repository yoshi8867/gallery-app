package com.yoshi0311.gallery.data.repository

import com.yoshi0311.gallery.data.local.db.TrashDao
import com.yoshi0311.gallery.data.local.db.TrashEntity
import com.yoshi0311.gallery.data.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface TrashRepository {
    /** 휴지통에 담긴 MediaItem 목록 — 실시간 */
    fun observeTrashItems(): Flow<List<MediaItem>>

    /** 휴지통에 담긴 mediaId 집합 — 실시간 */
    fun observeTrashIds(): Flow<Set<Long>>

    /** mediaId → 삭제 후 남은 일수 (최대 30일, 최소 0일) */
    fun observeDaysRemaining(): Flow<Map<Long, Int>>

    /** 미디어를 휴지통으로 이동 */
    suspend fun moveToTrash(mediaId: Long)

    /** 선택한 항목들을 휴지통에서 복원 */
    suspend fun restoreFromTrash(mediaIds: Set<Long>)
}

@Singleton
class TrashRepositoryImpl @Inject constructor(
    private val dao: TrashDao,
    private val mediaRepository: MediaRepository,
) : TrashRepository {

    override fun observeTrashIds(): Flow<Set<Long>> =
        dao.observeAllIds().map { it.toSet() }

    override fun observeTrashItems(): Flow<List<MediaItem>> =
        combine(mediaRepository.getAllMedia(), observeTrashIds()) { items, ids ->
            items.filter { it.id in ids }
        }

    override fun observeDaysRemaining(): Flow<Map<Long, Int>> =
        dao.observeAll().map { entities ->
            entities.associate { entity ->
                val elapsedDays = ((System.currentTimeMillis() - entity.addedAt) /
                    (1000L * 60 * 60 * 24)).toInt()
                entity.mediaId to (30 - elapsedDays).coerceIn(0, 30)
            }
        }

    override suspend fun moveToTrash(mediaId: Long) {
        dao.insert(TrashEntity(mediaId))
    }

    override suspend fun restoreFromTrash(mediaIds: Set<Long>) {
        mediaIds.forEach { dao.delete(it) }
    }
}
