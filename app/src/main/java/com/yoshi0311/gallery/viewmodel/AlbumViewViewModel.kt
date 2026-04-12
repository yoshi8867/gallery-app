package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.Album
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.AlbumRepository
import com.yoshi0311.gallery.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumViewViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val albumRepository: AlbumRepository,
) : ViewModel() {

    // ── 앨범 ID / 이름 ──────────────────────────────────────────
    private val albumIdFlow = MutableStateFlow<Long?>(null)

    var currentAlbumId: Long? by mutableStateOf(null)
        private set

    var currentAlbumName: String by mutableStateOf("")
        private set

    /** 최초 1회 초기화 */
    fun initialize(albumId: Long, albumName: String) {
        if (albumIdFlow.value == null) {
            albumIdFlow.value = albumId
            currentAlbumId = albumId
            currentAlbumName = albumName
        }
    }

    /** 드로어에서 다른 앨범 선택 시 호출 — 그리드만 교체, 드로어 유지 */
    fun switchAlbum(albumId: Long, albumName: String) {
        albumIdFlow.value = albumId
        currentAlbumId = albumId
        currentAlbumName = albumName
    }

    val mediaItems: StateFlow<List<MediaItem>> = albumIdFlow
        .filterNotNull()
        .flatMapLatest { id ->
            mediaRepository.getAllMedia()
                .map { items -> items.filter { it.bucketId == id } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    /** 서랍에 표시할 전체 앨범 목록 (이름순) */
    val albums: StateFlow<List<Album>> = albumRepository.getAllAlbums()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ── 컬럼 수 (서랍 닫힘: 5단계 / 열림: 4단계) ────────────────
    private val closedLevels = listOf(2, 3, 4, 7, 12)
    private val openLevels   = listOf(2, 3, 5, 9)

    var columnIndex by mutableStateOf(1) // 기본: 3단
        private set

    var isDrawerOpen by mutableStateOf(false)
        private set

    val columnCount: Int
        get() = if (isDrawerOpen) {
            openLevels[columnIndex.coerceAtMost(openLevels.lastIndex)]
        } else {
            closedLevels[columnIndex]
        }

    /** 최대 축소 단계 여부 — 탭 시 PhotoView 이동 대신 핀치 아웃 효과만 적용 */
    val isMaxZoomedOut: Boolean
        get() = columnIndex == closedLevels.lastIndex

    /** 핀치 아웃 — 컬럼 수 줄임 */
    fun zoomIn() { if (columnIndex > 0) columnIndex-- }

    /** 핀치 인 — 컬럼 수 늘림 */
    fun zoomOut() { if (columnIndex < closedLevels.lastIndex) columnIndex++ }

    // ── 서랍 ────────────────────────────────────────────────────
    fun openDrawer()  { isDrawerOpen = true }
    fun closeDrawer() { isDrawerOpen = false }

    // ── 다중 선택 ────────────────────────────────────────────────
    var selectionMode by mutableStateOf(false)
        private set

    var selectedIds by mutableStateOf(emptySet<Long>())
        private set

    fun enterSelectionMode(id: Long) {
        selectionMode = true
        selectedIds = setOf(id)
    }

    fun toggleSelection(id: Long) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun exitSelectionMode() {
        selectionMode = false
        selectedIds = emptySet()
    }
}
