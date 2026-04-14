package com.yoshi0311.gallery.ui.screen.photomain

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.ui.component.MediaThumbnail
import com.yoshi0311.gallery.ui.component.SelectionActionBar
import com.yoshi0311.gallery.ui.component.SelectionTopBar
import com.yoshi0311.gallery.util.shareMediaItems
import com.yoshi0311.gallery.viewmodel.PhotoMainViewModel
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoMainScreen(
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    onSelectionModeChange: (Boolean) -> Unit = {},
    viewModel: PhotoMainViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val columnCount = viewModel.columnCount
    val selectionMode = viewModel.selectionMode
    val selectedIds = viewModel.selectedIds

    // 다중 선택 상태를 GalleryNavHost로 올림 (내비게이션 바 숨김 처리용)
    LaunchedEffect(selectionMode) { onSelectionModeChange(selectionMode) }

    // 다중 선택 중 시스템 뒤로가기 → 선택 해제
    BackHandler(enabled = selectionMode) { viewModel.exitSelectionMode() }

    // columnLevels = [2, 3, 4, 7, 11, 20]: 1~3단계=1dp, 4단계=0.5dp, 5~6단계=0dp
    val thumbnailPadding: Dp = when {
        columnCount <= 4 -> 1.dp
        columnCount <= 7 -> 0.5.dp
        else -> 0.dp
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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
                                text = "사진",
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.padding(vertical = 40.dp),
                            )
                        },
                        scrollBehavior = scrollBehavior,
                    )
                }
            },
            bottomBar = {
                if (selectionMode) {
                    SelectionActionBar(
                        onFavorite = { viewModel.addSelectedToFavorites() },
                        onShare = {
                        val allItems = sections.flatMap { it.items }
                        shareMediaItems(context, allItems.filter { it.id in selectedIds })
                    },
                        onDelete = { viewModel.moveSelectedToTrash() },
                    )
                }
            },
        ) { innerPadding ->
            val listState = rememberLazyListState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                if (!selectionMode) {
                    PhotoMainHeroHeader(
                        onSearch = onNavigateToSearch,
                        onSelect = { viewModel.enterSelectionMode() },
                    )
                }

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        bottom = innerPadding.calculateBottomPadding(),
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                var prevDistance = 0f
                                var isPinching = false
                                var scaleAccumulator = 1f

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
                    sections.forEach { section ->
                        stickyHeader(key = "header_${section.dateLabel}") {
                            DateSectionHeader(
                                label = section.dateLabel,
                                count = section.items.size,
                            )
                        }

                        val rows = section.items.chunked(columnCount)
                        items(
                            count = rows.size,
                            key = { idx -> "${section.dateLabel}_row_$idx" },
                        ) { rowIdx ->
                            val rowItems = rows[rowIdx]
                            Row(modifier = Modifier.fillMaxWidth()) {
                                rowItems.forEach { item ->
                                    MediaThumbnail(
                                        item = item,
                                        isSelected = item.id in selectedIds,
                                        inSelectionMode = selectionMode,
                                        modifier = Modifier.weight(1f),
                                        thumbnailPadding = thumbnailPadding,
                                        onClick = {
                                            when {
                                                selectionMode -> viewModel.toggleSelection(item.id)
                                                // 11단·20단: 핀치 아웃 효과 (PhotoView 미이동)
                                                columnCount >= 11 -> viewModel.zoomIn()
                                                else -> onNavigateToPhoto(item.id)
                                            }
                                        },
                                        onLongClick = {
                                            if (!selectionMode) viewModel.enterSelectionMode(item.id)
                                        },
                                    )
                                }
                                repeat(columnCount - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun PhotoMainHeroHeader(
    onSearch: () -> Unit,
    onSelect: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
    ) {
        IconButton(onClick = onSearch, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Search, contentDescription = "검색")
        }
        Box {
            IconButton(onClick = { expanded = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "더보기")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("선택") },
                    onClick = { expanded = false; onSelect() },
                )
                DropdownMenuItem(
                    text = { Text("만들기") },
                    onClick = { expanded = false },
                )
                DropdownMenuItem(
                    text = { Text("비슷한 이미지 모아보기") },
                    onClick = { expanded = false },
                )
                DropdownMenuItem(
                    text = { Text("슬라이드쇼") },
                    onClick = { expanded = false },
                )
            }
        }
    }
}

@Composable
private fun DateSectionHeader(label: String, count: Int) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${count}장",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
