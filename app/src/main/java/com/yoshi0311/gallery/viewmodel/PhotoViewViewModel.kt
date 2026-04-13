package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoViewViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
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
     * → ViewModel 재사용 시 이전 mediaId 값이 남아 오동작하는 문제를 방지.
     */
    var readyForMediaId by mutableStateOf(-1L)
        private set

    private var collectJob: Job? = null

    /**
     * mediaId·albumId 변경 시 언제든 재호출 가능.
     * 이전 수집 Job을 취소하고 새 mediaId 기준으로 재시작.
     */
    fun initialize(mediaId: Long, albumId: Long?) {
        collectJob?.cancel()
        readyForMediaId = -1L          // 이전 mediaId 플래그 초기화
        _initialIndex.value = 0

        collectJob = viewModelScope.launch {
            val flow = if (albumId != null) {
                mediaRepository.getMediaByAlbum(albumId)
            } else {
                mediaRepository.getAllMedia()
            }
            flow.collect { items ->
                // 아직 이 mediaId에 대한 인덱스가 확정되지 않은 경우만 탐색
                if (readyForMediaId != mediaId) {
                    val idx = items.indexOfFirst { it.id == mediaId }
                    if (idx >= 0) {
                        _initialIndex.value = idx
                        readyForMediaId = mediaId   // 인덱스 확정 — mediaId 단위로 기록
                    }
                }
                _mediaItems.value = items
            }
        }
    }

    fun toggleMute() {
        isMuted = !isMuted
    }

    /** P2-1에서 Room DB 연동 구현 예정 */
    fun toggleFavorite(mediaId: Long) {}
}
