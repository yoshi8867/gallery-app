package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.Album
import com.yoshi0311.gallery.data.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumListViewModel @Inject constructor(
    albumRepository: AlbumRepository,
) : ViewModel() {

    enum class SortOrder { BY_NAME, BY_DATE, BY_COUNT }

    var sortOrder by mutableStateOf(SortOrder.BY_NAME)
        private set

    // 컬럼 수 1~3단, 기본 2단
    var columnCount by mutableStateOf(2)
        private set

    val albums: StateFlow<List<Album>> = combine(
        albumRepository.getAllAlbums(),
        snapshotFlow { sortOrder },
    ) { albums, sort ->
        when (sort) {
            SortOrder.BY_NAME  -> albums.sortedBy { it.name.lowercase() }
            SortOrder.BY_DATE  -> albums.sortedByDescending { it.latestDate }
            SortOrder.BY_COUNT -> albums.sortedByDescending { it.count }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun updateSortOrder(order: SortOrder) { sortOrder = order }

    /** 핀치 아웃 — 컬럼 수 줄임(카드 확대) */
    fun zoomIn() { if (columnCount > 1) columnCount-- }

    /** 핀치 인 — 컬럼 수 늘림(카드 축소) */
    fun zoomOut() { if (columnCount < 3) columnCount++ }
}
