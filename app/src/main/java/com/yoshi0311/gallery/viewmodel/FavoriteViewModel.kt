package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.FavoriteRepository
import com.yoshi0311.gallery.data.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val trashRepository: TrashRepository,
) : ViewModel() {

    val items: StateFlow<List<MediaItem>> = combine(
        favoriteRepository.observeFavoriteItems(),
        trashRepository.observeTrashIds(),
    ) { items, trashIds ->
        items.filter { it.id !in trashIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

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

    /** 선택된 항목들을 즐겨찾기에서 해제 */
    fun removeSelectedFromFavorites() {
        val ids = selectedIds
        viewModelScope.launch {
            favoriteRepository.removeFavorites(ids)
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
