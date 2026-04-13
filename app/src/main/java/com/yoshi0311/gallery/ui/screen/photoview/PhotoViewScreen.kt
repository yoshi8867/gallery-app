package com.yoshi0311.gallery.ui.screen.photoview

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.ui.component.VideoOverlay
import com.yoshi0311.gallery.viewmodel.PhotoViewViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.gestures.awaitEachGesture
import kotlin.math.abs
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewScreen(
    mediaId: Long,
    albumId: Long?,
    onBack: () -> Unit,
    fromTrash: Boolean = false,
    viewModel: PhotoViewViewModel = hiltViewModel(),
) {
    LaunchedEffect(mediaId, albumId, fromTrash) { viewModel.initialize(mediaId, albumId, fromTrash) }

    val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()
    val initialIndex by viewModel.initialIndex.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val readyForMediaId = viewModel.readyForMediaId
    val isMuted = viewModel.isMuted
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = 0) { mediaItems.size }

    // readyForMediaId가 현재 진입 mediaId와 같아졌을 때만 스크롤.
    // ViewModel이 재사용되더라도 이전 mediaId의 플래그가 현재 mediaId와 달라
    // 오동작하지 않는다.
    LaunchedEffect(readyForMediaId, mediaItems.size) {
        if (readyForMediaId == mediaId && mediaItems.isNotEmpty()) {
            pagerState.scrollToPage(initialIndex)
        }
    }

    val currentMedia = mediaItems.getOrNull(pagerState.currentPage)

    var isUiVisible by remember { mutableStateOf(true) }
    var showInfoPanel by remember { mutableStateOf(false) }
    var currentPageScale by remember { mutableFloatStateOf(1f) }

    // 페이지 전환 시 줌 상태 초기화
    LaunchedEffect(pagerState.currentPage) { currentPageScale = 1f }

    // UI 표시 여부에 따른 배경 색상 전환 (흰 배경 ↔ 검정 배경)
    val bgColor by animateColorAsState(
        targetValue = if (isUiVisible) MaterialTheme.colorScheme.background else Color.Black,
        label = "bgColor",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
    ) {
        if (mediaItems.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = currentPageScale == 1f,
            ) { page ->
                val media = mediaItems[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { isUiVisible = !isUiVisible },
                ) {
                    if (media.isVideo) {
                        VideoOverlay(
                            uri = media.uri,
                            isMuted = isMuted,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        ZoomableImage(
                            model = media.uri,
                            onScaleChanged = { scale ->
                                if (page == pagerState.currentPage) currentPageScale = scale
                            },
                            onSwipeUp = { showInfoPanel = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                        )
                    }
                }
            }
        }

        // ── 상단 TopAppBar 오버레이 ────────────────────────────────
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            TopAppBar(
                title = { /* 제목 없음 */ },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
                actions = {
                    if (currentMedia?.isVideo == true) {
                        IconButton(onClick = { viewModel.toggleMute() }) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "음소거 해제" else "음소거",
                            )
                        }
                    } else {
                        // 화면 회전 버튼 (추후 구현)
                        IconButton(onClick = { /* 추후 구현 */ }) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "화면 회전",
                            )
                        }
                        IconButton(onClick = { /* 추후 구현 */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "더보기")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier.statusBarsPadding(),
            )
        }

        // ── 하단 썸네일 + 액션 바 오버레이 ───────────────────────
        AnimatedVisibility(
            visible = isUiVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
//                ThumbnailStrip(
//                    items = mediaItems,
//                    currentIndex = pagerState.currentPage,
//                    onPageSelected = { idx ->
//                        scope.launch { pagerState.scrollToPage(idx) }
//                    },
//                )
                BottomActionBar(
                    isFavorite = currentMedia?.id?.let { it in favoriteIds } ?: false,
                    onFavorite = { viewModel.toggleFavorite(currentMedia?.id ?: 0L) },
                    onEdit = {
                        Toast.makeText(context, "추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
                    },
                    onAI = {
                        Toast.makeText(context, "추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        currentMedia?.let { media ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = media.mimeType.ifEmpty { "*/*" }
                                putExtra(Intent.EXTRA_STREAM, media.uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, null))
                        }
                    },
                    onDelete = {
                        currentMedia?.id?.let { id ->
                            viewModel.moveToTrash(id)
                            onBack()
                        }
                    },
                )
            }
        }
    }

    // ── 상세정보 바텀시트 ──────────────────────────────────────────
    if (showInfoPanel && currentMedia != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showInfoPanel = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            InfoPanelContent(
                media = currentMedia,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            )
        }
    }
}

// ── ZoomableImage (커스텀 제스처: 핀치 줌 + 팬 + 위로 스와이프) ──
@Composable
private fun ZoomableImage(
    model: Any,
    onScaleChanged: (Float) -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    AsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    var prevDistance = 0f
                    var isPinching = false
                    var prevCentroid = Offset.Zero
                    var totalDragX = 0f
                    var totalDragY = 0f
                    var isVertical: Boolean? = null // null=방향 미결정

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val active = event.changes.filter { it.pressed }

                        when {
                            active.size >= 2 -> {
                                // ── 핀치 줌 ──────────────────────────
                                val p1 = active[0].position
                                val p2 = active[1].position
                                val centroid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                                val dx = p2.x - p1.x
                                val dy = p2.y - p1.y
                                val dist = sqrt(dx * dx + dy * dy)

                                if (!isPinching) {
                                    prevDistance = dist
                                    prevCentroid = centroid
                                    isPinching = true
                                } else if (prevDistance > 0f) {
                                    val zoomChange = dist / prevDistance
                                    val panChange = centroid - prevCentroid
                                    val newScale = (scale * zoomChange).coerceIn(1f, 8f)
                                    scale = newScale
                                    offset = if (newScale > 1f) offset + panChange else Offset.Zero
                                    onScaleChanged(newScale)
                                    prevDistance = dist
                                    prevCentroid = centroid
                                }
                                event.changes.forEach { it.consume() }
                            }

                            active.size == 1 && !isPinching -> {
                                // ── 단일 손가락 ─────────────────────
                                val change = active[0]
                                val dx = change.position.x - change.previousPosition.x
                                val dy = change.position.y - change.previousPosition.y
                                totalDragX += dx
                                totalDragY += dy

                                if (scale > 1f) {
                                    // 확대 상태: 팬 (항상 소비)
                                    offset += Offset(dx, dy)
                                    change.consume()
                                } else {
                                    // 1× 상태: 방향 결정 (15px 임계값)
                                    if (isVertical == null &&
                                        (abs(totalDragX) > 15f || abs(totalDragY) > 15f)
                                    ) {
                                        isVertical = abs(totalDragY) > abs(totalDragX)
                                    }
                                    // 위로 스와이프로 판명된 경우만 소비 (가로 스와이프는 페이저가 처리)
                                    if (isVertical == true && totalDragY < 0f) {
                                        change.consume()
                                    }
                                }
                            }

                            else -> {
                                // ── 모든 손가락 뗌 ──────────────────
                                if (!isPinching && isVertical == true && totalDragY < -60f) {
                                    onSwipeUp()
                                }
                                if (event.changes.all { !it.pressed }) break
                            }
                        }
                    }
                }
            },
    )
}

// ── ThumbnailStrip (릴 방식: 스트립 중앙 = 선택된 사진) ────────
@Composable
private fun ThumbnailStrip(
    items: List<MediaItem>,
    currentIndex: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val thumbnailWidth = 30.dp
    val spacing = 3.dp

    // 사용자가 직접 드래그 중인지 추적 (프로그래밍 스크롤과 구분하여 루프 방지)
    var userDragging by remember { mutableStateOf(false) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) userDragging = true
                return Offset.Zero
            }
        }
    }

    // 뷰포트 너비 – 레이아웃 완료 후 자동 갱신
    val viewportWidth by remember { derivedStateOf { listState.layoutInfo.viewportSize.width } }

    // pager currentIndex 변경 또는 뷰포트 준비 시 해당 아이템을 중앙으로 스크롤 (프로그래밍)
    LaunchedEffect(currentIndex, viewportWidth) {
        if (items.isNotEmpty() && viewportWidth > 0) {
            val itemPx = with(density) { thumbnailWidth.toPx() }
            val spacePx = with(density) { spacing.toPx() }
            // 아이템 중앙이 viewport 중앙에 오도록 pixel offset 계산
            val offset = (currentIndex * (itemPx + spacePx) - (viewportWidth - itemPx) / 2f)
                .coerceAtLeast(0f).toInt()
            listState.animateScrollToItem(index = 0, scrollOffset = offset)
        }
    }

    // 스크롤 정지 시 → 사용자 드래그인 경우만 pager 동기화 (프로그래밍 스크롤은 무시)
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && userDragging && items.isNotEmpty()) {
            userDragging = false
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportSize.width / 2
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - center)
            }
            centeredItem?.let { onPageSelected(it.index) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.Black.copy(alpha = 0.6f)),
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .width(thumbnailWidth)
                        .fillMaxHeight()
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = Color(0xFF6750A4),
                                    shape = RoundedCornerShape(3.dp),
                                )
                            } else Modifier,
                        )
                        .clip(RoundedCornerShape(3.dp))
                        .clickable {
                            scope.launch {
                                val vw = listState.layoutInfo.viewportSize.width.toFloat()
                                val itemPx = with(density) { thumbnailWidth.toPx() }
                                val spacePx = with(density) { spacing.toPx() }
                                val offset = (index * (itemPx + spacePx) - (vw - itemPx) / 2f)
                                    .coerceAtLeast(0f).toInt()
                                listState.animateScrollToItem(index = 0, scrollOffset = offset)
                                onPageSelected(index)
                            }
                        },
                ) {
                    AsyncImage(
                        model = item.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

// ── BottomActionBar ─────────────────────────────────────────────
@Composable
private fun BottomActionBar(
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onEdit: () -> Unit,
    onAI: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기",
                tint = if (isFavorite) androidx.compose.ui.graphics.Color(0xFFE91E63) else androidx.compose.material3.LocalContentColor.current,
            )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Outlined.Edit, contentDescription = "편집")
        }
        IconButton(onClick = onAI) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = "AI")
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Default.Share, contentDescription = "공유")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "삭제")
        }
    }
}

// ── InfoPanelContent ────────────────────────────────────────────
@Composable
private fun InfoPanelContent(
    media: MediaItem,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        InfoRow(label = "파일명", value = media.name)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        InfoRow(label = "날짜", value = media.dateTaken.toDateString())
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        InfoRow(
            label = "크기 및 해상도",
            value = "${media.size.toMbString()}  /  ${media.width} × ${media.height}",
        )
        if (media.latitude != null && media.longitude != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            InfoRow(
                label = "위치",
                value = "%.4f, %.4f".format(media.latitude, media.longitude),
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── 유틸 ────────────────────────────────────────────────────────
private fun Long.toDateString(): String =
    SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN).format(Date(this))

private fun Long.toMbString(): String =
    if (this >= 1_048_576L) "%.1f MB".format(this / 1_048_576.0)
    else "%.0f KB".format(this / 1024.0)
