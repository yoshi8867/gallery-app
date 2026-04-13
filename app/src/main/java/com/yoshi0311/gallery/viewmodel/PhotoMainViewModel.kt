package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import kotlinx.coroutines.launch
import com.yoshi0311.gallery.data.repository.FavoriteRepository
import com.yoshi0311.gallery.data.repository.MediaRepository
import com.yoshi0311.gallery.data.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PhotoMainViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    private val trashRepository: TrashRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    data class MediaSection(
        val dateLabel: String,
        val items: List<MediaItem>,
    )

    val sections: StateFlow<List<MediaSection>> = combine(
        mediaRepository.getAllMedia(),
        trashRepository.observeTrashIds(),
        snapshotFlow { columnCount },
    ) { items, trashIds, cols ->
        items.filter { it.id !in trashIds }.groupToSections(cols)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    // ── 컬럼 수 (spec 1.5/3/4/7/11/20 → 정수 근사) ─────────────
    private val columnLevels = listOf(2, 3, 4, 7, 11, 20)
    var columnCount by mutableStateOf(4)
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

    fun selectAll() {
        selectedIds = sections.value.flatMap { s -> s.items.map { it.id } }.toSet()
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

    // ── 날짜 그룹핑 ─────────────────────────────────────────────
    private fun List<MediaItem>.groupToSections(cols: Int): List<MediaSection> {
        if (isEmpty()) return emptyList()
        val fmt = when {
            cols <= 3 -> SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN)
            cols <= 7 -> SimpleDateFormat("yyyy년 M월", Locale.KOREAN)
            else      -> SimpleDateFormat("yyyy년", Locale.KOREAN)
        }
        return groupBy { fmt.format(Date(it.dateTaken)) }
            .map { (label, items) -> MediaSection(dateLabel = label, items = items) }
    }
}
