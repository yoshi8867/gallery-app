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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PhotoMainViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    data class MediaSection(
        val dateLabel: String,
        val items: List<MediaItem>,
    )

    val sections: StateFlow<List<MediaSection>> = mediaRepository.getAllMedia()
        .map { it.groupToSections() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // ── 컬럼 수 (spec 1.5/3/4/7/11/20 → 정수 근사) ─────────────
    private val columnLevels = listOf(2, 3, 4, 7, 11, 20)
    var columnCount by mutableStateOf(4)
        private set

    /** 핀치 아웃 — 컬럼 수 줄임(아이템 확대) */
    fun zoomIn() {
        val idx = columnLevels.indexOf(columnCount)
        if (idx > 0) columnCount = columnLevels[idx - 1]
    }

    /** 핀치 인 — 컬럼 수 늘림(아이템 축소) */
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

    // ── 날짜 그룹핑 (일별) ───────────────────────────────────────
    private fun List<MediaItem>.groupToSections(): List<MediaSection> {
        if (isEmpty()) return emptyList()
        val fmt = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN)
        return groupBy { fmt.format(Date(it.dateTaken)) }
            .map { (label, items) -> MediaSection(dateLabel = label, items = items) }
    }
}
