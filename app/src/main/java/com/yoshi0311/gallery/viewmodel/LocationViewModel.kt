package com.yoshi0311.gallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.CityGroup
import com.yoshi0311.gallery.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
) : ViewModel() {

    sealed class UiState {
        data object Loading : UiState()
        data object Empty : UiState()
        data class Success(
            val groups: Map<String, List<CityGroup>>,
            val hasPhotosWithoutGps: Boolean,
        ) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            locationRepository.observeLocationGroups().collect { groups ->
                _uiState.value = if (groups.isEmpty()) {
                    UiState.Empty
                } else {
                    // GPS 없는 사진이 있는지 여부는 단순히 InfoBanner 표시용으로 항상 true 처리
                    // (location screen에 도달했다는 것은 사진 중 일부에 GPS 없는 것이 존재할 수 있음)
                    UiState.Success(groups = groups, hasPhotosWithoutGps = true)
                }
            }
        }
    }
}
