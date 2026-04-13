package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.FavoriteRepository
import com.yoshi0311.gallery.data.repository.MediaRepository
import com.yoshi0311.gallery.data.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecentsFilter { Recent30Days, All }

@HiltViewModel
class RecentsViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    private val trashRepository: TrashRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    var filter by mutableStateOf(RecentsFilter.Recent30Days)
        private set

    val items: StateFlow<List<MediaItem>> = combine(
        mediaRepository.getAllMedia(),
        trashRepository.observeTrashIds(),
        snapshotFlow { filter },
    ) { allItems, trashIds, f ->
        val nonTrashed = allItems.filter { it.id !in trashIds }
        if (f == RecentsFilter.Recent30Days) {
            val cutoffMs = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1_000
            nonTrashed.filter { it.dateAdded >= cutoffMs }
        } else {
            nonTrashed
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun updateFilter(f: RecentsFilter) { filter = f }

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

    fun addSelectedToFavorites() {
        val ids = selectedIds
        viewModelScope.launch {
            favoriteRepository.addFavorites(ids)
            exitSelectionMode()
        }
    }

    fun moveSelectedToTrash() {
        val ids = selectedIds
        viewModelScope.launch {
            trashRepository.moveAllToTrash(ids)
            exitSelectionMode()
        }
    }
}
