package com.yoshi0311.gallery.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.model.Story
import com.yoshi0311.gallery.data.repository.MediaRepository
import com.yoshi0311.gallery.data.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    trashRepository: TrashRepository,
) : ViewModel() {

    val stories: StateFlow<List<Story>> = combine(
        mediaRepository.getAllMedia(),
        trashRepository.observeTrashIds(),
    ) { allItems, trashIds ->
        generateStories(allItems.filter { it.id !in trashIds })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private fun generateStories(items: List<MediaItem>): List<Story> =
        items.groupBy { it.bucketId }
            .mapNotNull { (bucketId, groupItems) ->
                if (groupItems.size < 3) return@mapNotNull null
                val sorted = groupItems.sortedByDescending { it.dateTaken }
                Story(
                    id = bucketId,
                    title = groupItems.first().bucketName,
                    coverUri = sorted.first().uri,
                    dateStart = sorted.last().dateTaken,
                    dateEnd = sorted.first().dateTaken,
                    mediaItems = sorted,
                )
            }
            .sortedByDescending { it.dateEnd }
}
