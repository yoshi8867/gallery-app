package com.yoshi0311.gallery.ui.screen.albumview

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yoshi0311.gallery.data.model.Album
import com.yoshi0311.gallery.ui.component.MediaThumbnail
import com.yoshi0311.gallery.ui.component.SelectionActionBar
import com.yoshi0311.gallery.ui.component.SelectionTopBar
import com.yoshi0311.gallery.util.shareMediaItems
import com.yoshi0311.gallery.viewmodel.AlbumViewViewModel
import kotlin.math.abs
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumViewScreen(
    albumId: Long,
    albumName: String,
    onBack: () -> Unit,
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    onNavigateToAlbum: ((albumId: Long, albumName: String) -> Unit)? = null,
    onSelectionModeChange: (Boolean) -> Unit = {},
    viewModel: AlbumViewViewModel = hiltViewModel(),
) {
    LaunchedEffect(albumId) { viewModel.initialize(albumId, albumName) }

    val context = LocalContext.current
    val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()
    val albums     by viewModel.albums.collectAsStateWithLifecycle()
    val columnCount    = viewModel.columnCount
    val isDrawerOpen   = viewModel.isDrawerOpen
    val selectionMode  = viewModel.selectionMode
    val selectedIds    = viewModel.selectedIds
    val activeAlbumId  = viewModel.currentAlbumId ?: albumId
    val activeAlbumName = viewModel.currentAlbumName.ifEmpty { albumName }

    // 다중 선택 상태를 GalleryNavHost로 올림
    LaunchedEffect(selectionMode) { onSelectionModeChange(selectionMode) }

    // 다중 선택 중 시스템 뒤로가기 → 선택 해제
    BackHandler(enabled = selectionMode) { viewModel.exitSelectionMode() }

    // columnIndex 기반 단계별 썸네일 간격: 0~2→1dp, 3→0.5dp, 4+→0dp
    val thumbnailGap: Dp = when {
        viewModel.columnIndex <= 2 -> 1.dp
        viewModel.columnIndex == 3 -> 0.5.dp
        else                       -> 0.dp
    }

    var scaleAccumulator by remember { mutableStateOf(1f) }
    val drawerListState = rememberLazyListState()   // 드로어 스크롤 위치 유지
    val scrollBehavior  = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (selectionMode) {
                SelectionTopBar(
                    selectedCount = selectedIds.size,
                    onClose = { viewModel.exitSelectionMode() },
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = activeAlbumName,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(vertical = 40.dp),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: 드롭다운 */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "더보기")
                        }
                    },
                    scrollBehavior = scrollBehavior,
//                    modifier = Modifier.padding(vertical = 40.dp),
                )
            }
        },
        bottomBar = {
            if (selectionMode) {
                SelectionActionBar(
                    onFavorite = { /* TODO: AlbumViewViewModel에 즐겨찾기 추가 */ },
                    onShare = {
                        shareMediaItems(context, mediaItems.filter { it.id in selectedIds })
                    },
                    onDelete = { /* TODO: AlbumViewViewModel에 휴지통 추가 */ },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // ── 수평 스와이프: 우측→드로어 열기, 좌측→드로어 닫기 (두 손가락 시 비활성) ──
                .pointerInput(isDrawerOpen) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var totalX = 0f
                        var triggered = false
                        var isMultiTouch = false
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.filter { it.pressed }.size >= 2) {
                                isMultiTouch = true
                            }
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            if (!isMultiTouch) {
                                totalX += change.position.x - change.previousPosition.x
                                if (!triggered && abs(totalX) > 90f) {
                                    triggered = true
                                    if (!isDrawerOpen && totalX > 0f) viewModel.openDrawer()
                                    else if (isDrawerOpen && totalX < 0f) viewModel.closeDrawer()
                                }
                            }
                        }
                    }
                },
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // ── 사이드 드로어 (좌측, 우측 스와이프로 열림) ──────────
                AnimatedVisibility(
                    visible = isDrawerOpen,
                    enter = slideInHorizontally(initialOffsetX = { -it }),
                    exit  = slideOutHorizontally(targetOffsetX = { -it }),
                ) {
                    AlbumDrawerPanel(
                        albums = albums,
                        currentAlbumId = activeAlbumId,
                        listState = drawerListState,
                        onAlbumSelected = { album ->
                            viewModel.switchAlbum(album.id, album.name)
                        },
                    )
                }

                // ── 미디어 그리드 ────────────────────────────────────
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    contentPadding = PaddingValues(thumbnailGap),
                    horizontalArrangement = Arrangement.spacedBy(thumbnailGap),
                    verticalArrangement = Arrangement.spacedBy(thumbnailGap),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                var prevDistance = 0f
                                var isPinching = false
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val active = event.changes.filter { it.pressed }
                                    if (active.size >= 2) {
                                        val p1 = active[0].position
                                        val p2 = active[1].position
                                        val dx = p2.x - p1.x
                                        val dy = p2.y - p1.y
                                        val distance = sqrt(dx * dx + dy * dy)
                                        if (!isPinching) {
                                            prevDistance = distance
                                            isPinching = true
                                        } else if (prevDistance > 0f) {
                                            val zoom = distance / prevDistance
                                            scaleAccumulator *= zoom
                                            when {
                                                scaleAccumulator > 1.3f -> {
                                                    viewModel.zoomIn()
                                                    scaleAccumulator = 1f
                                                }
                                                scaleAccumulator < 0.77f -> {
                                                    viewModel.zoomOut()
                                                    scaleAccumulator = 1f
                                                }
                                            }
                                            prevDistance = distance
                                        }
                                        event.changes.forEach { it.consume() }
                                    } else {
                                        if (isPinching) {
                                            isPinching = false
                                            prevDistance = 0f
                                            scaleAccumulator = 1f
                                        }
                                        if (event.changes.all { !it.pressed }) break
                                    }
                                }
                            }
                        },
                ) {
                    items(mediaItems, key = { it.id }) { item ->
                        MediaThumbnail(
                            item = item,
                            isSelected = item.id in selectedIds,
                            inSelectionMode = selectionMode,
                            thumbnailPadding = 0.dp,
                            onClick = {
                                when {
                                    selectionMode          -> viewModel.toggleSelection(item.id)
                                    viewModel.isMaxZoomedOut -> viewModel.zoomIn()
                                    else                   -> onNavigateToPhoto(item.id)
                                }
                            },
                            onLongClick = {
                                if (!selectionMode) viewModel.enterSelectionMode(item.id)
                            },
                        )
                    }
                }

            }
        }
    }
}

// ── 사이드 드로어 패널 ────────────────────────────────────────────
@Composable
private fun AlbumDrawerPanel(
    albums: List<Album>,
    currentAlbumId: Long,
    listState: LazyListState,
    onAlbumSelected: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .width(120.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(albums, key = { it.id }) { album ->
            val isSelected = album.id == currentAlbumId
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlbumSelected(album) },
            ) {
                // 썸네일 — 정사각형 + 둥근 모서리 + 선택 테두리
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .then(
                            if (isSelected) Modifier.border(
                                width = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp),
                            ) else Modifier
                        )
                        .clip(RoundedCornerShape(10.dp)),
                ) {
                    AsyncImage(
                        model = album.coverUri,
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // 텍스트 — 둥근 사각형 바깥 아래
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, start = 1.dp, end = 1.dp),
                )
                Text(
                    text = "${album.count}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 1.dp, bottom = 2.dp),
                )
            }
        }
    }
}
