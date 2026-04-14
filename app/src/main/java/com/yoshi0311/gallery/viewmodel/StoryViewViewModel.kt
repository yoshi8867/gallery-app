package com.yoshi0311.gallery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.data.model.Story
import com.yoshi0311.gallery.data.repository.MediaRepository
import com.yoshi0311.gallery.data.repository.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val trashRepository: TrashRepository,
) : ViewModel() {

    private val _story = MutableStateFlow<Story?>(null)
    val story: StateFlow<Story?> = _story.asStateFlow()

    var currentIndex by mutableStateOf(0)
        private set
    var isPlaying by mutableStateOf(true)
        private set
    var speed by mutableStateOf(1f)
        private set
    var isUiVisible by mutableStateOf(true)
        private set
    var selectedMusic by mutableStateOf<String?>(null)
        private set
    var isMuted by mutableStateOf(false)
        private set

    /** 현재까지 전체 경과 시간 (초) — 매 200ms마다 갱신 */
    var totalElapsedSec by mutableStateOf(0)
        private set

    private var collectJob: Job? = null
    private var autoAdvanceJob: Job? = null

    /** 현재 슬라이드 내에서 경과한 ms — 일시정지 시에도 보존됨 */
    private var elapsedInCurrentSlideMs = 0L

    val slideIntervalMs: Long get() = (3_000L / speed).toLong()

    // ── 초기화 ─────────────────────────────────────────────────

    fun initialize(storyId: Long) {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            combine(
                mediaRepository.getAllMedia(),
                trashRepository.observeTrashIds(),
            ) { allItems, trashIds ->
                buildStory(storyId, allItems.filter { it.id !in trashIds })
            }.collect { story ->
                _story.value = story
                if (story != null && autoAdvanceJob?.isActive != true && isPlaying) {
                    startAutoAdvance()
                }
            }
        }
    }

    private fun buildStory(storyId: Long, items: List<MediaItem>): Story? {
        val storyItems = items
            .filter { it.bucketId == storyId }
            .sortedByDescending { it.dateTaken }
        return if (storyItems.size < 3) null else Story(
            id = storyId,
            title = storyItems.first().bucketName,
            coverUri = storyItems.first().uri,
            dateStart = storyItems.last().dateTaken,
            dateEnd = storyItems.first().dateTaken,
            mediaItems = storyItems,
        )
    }

    // ── 재생 제어 ───────────────────────────────────────────────

    fun togglePlayPause() {
        isPlaying = !isPlaying
        if (isPlaying) launchAdvanceLoop() else autoAdvanceJob?.cancel()
    }

    fun toggleUiVisibility() { isUiVisible = !isUiVisible }

    fun next() {
        val total = _story.value?.mediaItems?.size ?: return
        currentIndex = (currentIndex + 1) % total
        startAutoAdvance()
    }

    fun previous() {
        val total = _story.value?.mediaItems?.size ?: return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else total - 1
        startAutoAdvance()
    }

    fun updateSpeed(s: Float) {
        speed = s
        elapsedInCurrentSlideMs = 0L
        if (isPlaying) launchAdvanceLoop()
    }

    fun selectMusic(name: String?) { selectedMusic = name }
    fun toggleMute() { isMuted = !isMuted }

    // ── 내부 타이머 ─────────────────────────────────────────────

    /** 슬라이드 교체 시 호출 — elapsed 리셋 후 루프 재시작 */
    private fun startAutoAdvance() {
        elapsedInCurrentSlideMs = 0L
        totalElapsedSec = (currentIndex.toLong() * slideIntervalMs / 1000L).toInt()
        if (isPlaying) launchAdvanceLoop()
    }

    /**
     * 200ms 단위로 타이머를 갱신하고 slideIntervalMs 도달 시 다음 슬라이드로 이동.
     * 일시정지 시에는 elapsedInCurrentSlideMs를 보존하여 재개 시 이어서 동작.
     */
    private fun launchAdvanceLoop() {
        autoAdvanceJob?.cancel()
        if (!isPlaying) return

        val interval = slideIntervalMs   // 캡처 (속도 변경 전까지 고정)

        autoAdvanceJob = viewModelScope.launch {
            var elapsed = elapsedInCurrentSlideMs

            while (elapsed < interval) {
                delay(200L)
                elapsed = (elapsed + 200L).coerceAtMost(interval)
                elapsedInCurrentSlideMs = elapsed
                totalElapsedSec = ((currentIndex.toLong() * interval + elapsed) / 1000L).toInt()
            }

            // 슬라이드 완료 → 다음으로
            val total = _story.value?.mediaItems?.size ?: return@launch
            currentIndex = (currentIndex + 1) % total
            elapsedInCurrentSlideMs = 0L
            if (isPlaying) launchAdvanceLoop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        collectJob?.cancel()
        autoAdvanceJob?.cancel()
    }
}
