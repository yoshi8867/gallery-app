package com.yoshi0311.gallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _cityFilter = MutableStateFlow<String?>(null)

    // 도시 필터가 설정되면 해당 도시 항목만, 아니면 전체 GPS 항목
    val displayItems: StateFlow<List<MediaItem>> = combine(
        locationRepository.observeLocationGroups(),
        _cityFilter,
    ) { groups, filter ->
        val allItems = groups.values.flatten()
        if (filter == null) {
            allItems.flatMap { it.items }.distinctBy { it.id }
        } else {
            allItems.find { it.city == filter }?.items ?: emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun setCityFilter(city: String?) {
        _cityFilter.value = city
    }
}
