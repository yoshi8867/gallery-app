package com.yoshi0311.gallery.ui.screen.story

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.yoshi0311.gallery.viewmodel.StoryViewViewModel

private val MUSIC_OPTIONS = listOf(
    null,
    "Autumn Day",
    "봄 바람 (Acoustic)",
    "잔잔한 피아노",
    "신나는 팝",
    "여름 바다 (Lo-fi)",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewScreen(
    storyId: Long,
    onBack: () -> Unit,
    viewModel: StoryViewViewModel = hiltViewModel(),
) {
    LaunchedEffect(storyId) {
        viewModel.initialize(storyId)
    }

    val story by viewModel.story.collectAsStateWithLifecycle()
    val currentIndex = viewModel.currentIndex
    val isPlaying = viewModel.isPlaying
    val speed = viewModel.speed
    val isUiVisible = viewModel.isUiVisible
    val selectedMusic = viewModel.selectedMusic
    val isMuted = viewModel.isMuted
    val totalElapsedSec = viewModel.totalElapsedSec

    var showSpeedMenu by remember { mutableStateOf(false) }
    var showMusicSheet by remember { mutableStateOf(false) }
    val musicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val storyData = story

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (storyData != null && storyData.mediaItems.isNotEmpty()) {
            val totalCount = storyData.mediaItems.size
            val slideIntervalMs = (3_000L / speed).toLong()
            val totalSec = (totalCount.toLong() * slideIntervalMs) / 1000L

            // ── 1. Ambient Mode 배경 (블러 이미지) ───────────────────
            Crossfade(
                targetState = currentIndex,
                animationSpec = tween(800),
                modifier = Modifier.fillMaxSize(),
            ) { idx ->
                val bgItem = storyData.mediaItems.getOrNull(idx)
                if (bgItem != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bgItem.uri)
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(28.dp)
                            .graphicsLayer { alpha = 0.55f },
                    )
                }
            }

            // ── 2. 상하 Ambient→Black 그라디언트 ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.72f),
                            0.22f to Color.Transparent,
                            0.78f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.72f),
                        ),
                    ),
            )

            // ── 3. 메인 슬라이드 이미지 (CrossFade) ──────────────────
            Crossfade(
                targetState = currentIndex,
                animationSpec = tween(600),
                modifier = Modifier.fillMaxSize(),
            ) { idx ->
                val item = storyData.mediaItems.getOrNull(idx)
                if (item != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.uri)
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // ── 4. 탭 영역 (좌: 이전 / 중: UI 토글 / 우: 다음) ──────
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { viewModel.previous() },
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { viewModel.toggleUiVisibility() },
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { viewModel.next() },
                )
            }

            // ── 5. UI 오버레이 ────────────────────────────────────────
            AnimatedVisibility(
                visible = isUiVisible,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // 상단: 제목(큰 글씨) + 공유 + ⋮(속도)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 16.dp, end = 4.dp, top = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = storyData.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                            )
                            // 공유 버튼
                            IconButton(onClick = { /* 공유 */ }) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "공유",
                                    tint = Color.White,
                                )
                            }
                            // 더보기 — 재생속도
                            Box {
                                IconButton(onClick = { showSpeedMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "더보기",
                                        tint = Color.White,
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false },
                                ) {
                                    Text(
                                        text = "재생 속도",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    )
                                    listOf(0.5f, 1f, 1.5f, 2f).forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text("${formatSpeed(s)}x") },
                                            onClick = {
                                                viewModel.updateSpeed(s)
                                                showSpeedMenu = false
                                            },
                                            trailingIcon = {
                                                if (speed == s) Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                        // 음악 제목 (선택된 경우만)
                        if (selectedMusic != null) {
                            Row(
                                modifier = Modifier.padding(bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = " $selectedMusic",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 하단 컨트롤: 🎵 | ▶ 0:10/1:00 | 뮤트
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 🎵 배경음악
                        IconButton(onClick = { showMusicSheet = true }) {
                            Icon(
                                imageVector = if (selectedMusic != null) Icons.Filled.MusicNote
                                else Icons.Filled.MusicOff,
                                contentDescription = "배경음악",
                                tint = Color.White,
                            )
                        }

                        // ▶/⏸ + 경과/전체 시간
                        FilledTonalButton(
                            onClick = { viewModel.togglePlayPause() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${formatTime(totalElapsedSec.toLong())} / ${formatTime(totalSec)}",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                        // 뮤트
                        IconButton(onClick = { viewModel.toggleMute() }) {
                            Icon(
                                imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                contentDescription = if (isMuted) "음소거 해제" else "음소거",
                                tint = if (selectedMusic != null) Color.White
                                else Color.White.copy(alpha = 0.35f),
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "스토리를 불러오는 중...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }

    // 배경음악 선택 바텀시트
    if (showMusicSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMusicSheet = false },
            sheetState = musicSheetState,
        ) {
            MusicSelectionSheet(
                selectedMusic = selectedMusic,
                onSelect = { viewModel.selectMusic(it) },
                onConfirm = { showMusicSheet = false },
            )
        }
    }
}

@Composable
private fun MusicSelectionSheet(
    selectedMusic: String?,
    onSelect: (String?) -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = "배경음악 선택",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        HorizontalDivider()
        MUSIC_OPTIONS.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedMusic == option,
                        onClick = { onSelect(option) },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selectedMusic == option, onClick = null)
                Text(
                    text = option ?: "없음",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Text("선택 완료")
        }
    }
}

private fun formatTime(seconds: Long): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%d:%02d".format(min, sec)
}

private fun formatSpeed(speed: Float): String =
    if (speed == speed.toLong().toFloat()) speed.toLong().toString() else speed.toString()
