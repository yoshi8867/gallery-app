package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.Album
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.AlbumRepository
import com.yoshi0311.gallery.data.repository.FavoriteRepository
import com.yoshi0311.gallery.data.repository.MediaRepository
import com.yoshi0311.gallery.data.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val albumRepository: AlbumRepository,
    private val favoriteRepository: FavoriteRepository,
    private val trashRepository: TrashRepository,
) : ViewModel() {

    // ── 앨범 ID / 이름 ──────────────────────────────────────────
    private val albumIdFlow = MutableStateFlow<Long?>(null)

    var currentAlbumId: Long? by mutableStateOf(null)
        private set

    var currentAlbumName: String by mutableStateOf("")
        private set

    fun initialize(albumId: Long, albumName: String) {
        if (albumIdFlow.value != albumId) {
            albumIdFlow.value = albumId
            currentAlbumId = albumId
            currentAlbumName = albumName
        }
    }

    fun switchAlbum(albumId: Long, albumName: String) {
        albumIdFlow.value = albumId
        currentAlbumId = albumId
        currentAlbumName = albumName
    }

    val mediaItems: StateFlow<List<MediaItem>> = albumIdFlow
        .filterNotNull()
        .flatMapLatest { id ->
            combine(
                mediaRepository.getAllMedia(),
                trashRepository.observeTrashIds(),
            ) { items, trashIds ->
                items.filter { it.bucketId == id && it.id !in trashIds }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val albums: StateFlow<List<Album>> = albumRepository.getAllAlbums()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ── 컬럼 수 ──────────────────────────────────────────────────
    private val closedLevels = listOf(2, 3, 4, 7, 12)
    private val openLevels   = listOf(2, 3, 5, 9)

    var columnIndex by mutableStateOf(1)
        private set

    var isDrawerOpen by mutableStateOf(false)
        private set

    val columnCount: Int
        get() = if (isDrawerOpen) {
            openLevels[columnIndex.coerceAtMost(openLevels.lastIndex)]
        } else {
            closedLevels[columnIndex]
        }

    val isMaxZoomedOut: Boolean
        get() = columnIndex == closedLevels.lastIndex

    fun zoomIn() { if (columnIndex > 0) columnIndex-- }
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

    fun addSelectedToFavorites() {
        viewModelScope.launch {
            favoriteRepository.addFavorites(selectedIds)
            exitSelectionMode()
        }
    }

    fun moveSelectedToTrash() {
        viewModelScope.launch {
            trashRepository.moveAllToTrash(selectedIds)
            exitSelectionMode()
        }
    }
}
