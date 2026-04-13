package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    mediaRepository: MediaRepository,
) : ViewModel() {

    /** 동영상 아이템 — 촬영일 내림차순 (최신이 먼저) */
    val items: StateFlow<List<MediaItem>> = mediaRepository.getAllVideos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ── 핀치 줌 (3 → 4 → 7단) ────────────────────────────────
    private val columnLevels = listOf(3, 4, 7)
    var columnCount by mutableStateOf(3)
        private set

    fun zoomIn() {
        val idx = columnLevels.indexOf(columnCount)
        if (idx > 0) columnCount = columnLevels[idx - 1]
    }

    fun zoomOut() {
        val idx = columnLevels.indexOf(columnCount)
        if (idx < columnLevels.lastIndex) columnCount = columnLevels[idx + 1]
    }

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
