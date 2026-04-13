package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val trashRepository: TrashRepository,
) : ViewModel() {

    val items = trashRepository.observeTrashItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** mediaId → 남은 일수 (0~30) */
    val daysRemainingMap = trashRepository.observeDaysRemaining()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

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
        if (selectedIds.isEmpty()) exitSelectionMode()
    }

    fun exitSelectionMode() {
        selectionMode = false
        selectedIds = emptySet()
    }

    /** 선택한 항목들을 휴지통에서 복원 (토스트 없음, 실제 복원) */
    fun restoreSelected() {
        val ids = selectedIds
        viewModelScope.launch {
            trashRepository.restoreFromTrash(ids)
            exitSelectionMode()
        }
    }
}
