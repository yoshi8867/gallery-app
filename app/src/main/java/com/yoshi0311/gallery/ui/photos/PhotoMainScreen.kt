package com.yoshi0311.gallery.ui.photos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoMainScreen(
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: PhotoMainViewModel = hiltViewModel(),
) {
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val columnCount = viewModel.columnCount
    val selectionMode = viewModel.selectionMode
    val selectedIds = viewModel.selectedIds

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var scaleAccumulator by remember { mutableStateOf(1f) }

    Scaffold(
        topBar = {
            if (selectionMode) {
                SelectionTopBar(
                    selectedCount = selectedIds.size,
                    onClose = { viewModel.exitSelectionMode() },
                )
            } else {
                LargeTopAppBar(
                    title = { Text("갤러리") },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Default.Search, contentDescription = "검색")
                        }
                        IconButton(onClick = { /* TODO: 드롭다운 */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "더보기")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = {
            if (selectionMode) {
                SelectionActionBar(
                    onFavorite = { /* P2-1에서 구현 */ },
                    onShare = { /* P2-3에서 구현 */ },
                    onDelete = { /* TODO */ },
                )
            }
        },
    ) { innerPadding ->
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
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
                    }
                },
        ) {
            // 검색 바 (TopAppBar 하단, 스크롤 시 사라짐)
            item(key = "search_bar") {
                SearchBarRow(onClick = onNavigateToSearch)
            }

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

@Composable
private fun SearchBarRow(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "사진 검색...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
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
