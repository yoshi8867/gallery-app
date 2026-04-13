package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.FavoriteRepository
import com.yoshi0311.gallery.data.repository.MediaRepository
import com.yoshi0311.gallery.data.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoViewViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val favoriteRepository: FavoriteRepository,
    private val trashRepository: TrashRepository,
) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    private val _initialIndex = MutableStateFlow(0)
    val initialIndex: StateFlow<Int> = _initialIndex.asStateFlow()

    var isMuted by mutableStateOf(false)
        private set

    /**
     * 인덱스가 확정된 mediaId. -1L = 미확정.
     * 화면에서 이 값이 현재 진입 mediaId와 같을 때만 pager 스크롤 수행.
     */
    var readyForMediaId by mutableStateOf(-1L)
        private set

    /** 즐겨찾기된 ID 집합 — 실시간 */
    val favoriteIds = favoriteRepository.observeFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private var collectJob: Job? = null

    fun initialize(mediaId: Long, albumId: Long?, fromTrash: Boolean = false) {
        collectJob?.cancel()
        readyForMediaId = -1L
        _initialIndex.value = 0

        collectJob = viewModelScope.launch {
            val filteredFlow = when {
                // 휴지통 진입: trash 항목만 소스로 사용 (일반 필터링 없음)
                fromTrash -> trashRepository.observeTrashItems()
                albumId != null -> combine(
                    mediaRepository.getMediaByAlbum(albumId),
                    trashRepository.observeTrashIds(),
                ) { items, trashIds -> items.filter { it.id !in trashIds } }
                else -> combine(
                    mediaRepository.getAllMedia(),
                    trashRepository.observeTrashIds(),
                ) { items, trashIds -> items.filter { it.id !in trashIds } }
            }
            filteredFlow.collect { filteredItems ->
                if (readyForMediaId != mediaId) {
                    val idx = filteredItems.indexOfFirst { it.id == mediaId }
                    if (idx >= 0) {
                        _initialIndex.value = idx
                        readyForMediaId = mediaId
                    }
                }
                _mediaItems.value = filteredItems
            }
        }
    }

    fun toggleMute() {
        isMuted = !isMuted
    }

    fun toggleFavorite(mediaId: Long) {
        viewModelScope.launch { favoriteRepository.toggleFavorite(mediaId) }
    }

    fun moveToTrash(mediaId: Long) {
        viewModelScope.launch { trashRepository.moveToTrash(mediaId) }
    }

    fun restoreFromTrash(mediaId: Long) {
        viewModelScope.launch { trashRepository.restoreFromTrash(setOf(mediaId)) }
    }
}
