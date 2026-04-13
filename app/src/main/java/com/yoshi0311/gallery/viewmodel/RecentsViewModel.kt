package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class RecentsFilter { Recent30Days, All }

@HiltViewModel
class RecentsViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    var filter by mutableStateOf(RecentsFilter.Recent30Days)
        private set

    /** 필터 적용된 미디어 아이템 — dateAdded 내림차순 (최신이 먼저) */
    val items: StateFlow<List<MediaItem>> = combine(
        mediaRepository.getAllMedia(),
        snapshotFlow { filter },
    ) { allItems, f ->
        if (f == RecentsFilter.Recent30Days) {
            val cutoffMs = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1_000
            allItems.filter { it.dateAdded >= cutoffMs }
        } else {
            allItems
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun updateFilter(f: RecentsFilter) { filter = f }

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
