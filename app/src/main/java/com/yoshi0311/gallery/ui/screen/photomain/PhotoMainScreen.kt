package com.yoshi0311.gallery.ui.screen.photomain

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.ui.component.MediaThumbnail
import com.yoshi0311.gallery.ui.component.SelectionActionBar
import com.yoshi0311.gallery.ui.component.SelectionTopBar
import com.yoshi0311.gallery.viewmodel.PhotoMainViewModel
import kotlin.math.sqrt

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

    var scaleAccumulator by remember { mutableStateOf(1f) }
    var debugLog by remember { mutableStateOf("핀치 대기 중...") }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Box(modifier = Modifier.fillMaxSize()) {
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
                        modifier = Modifier.padding(vertical = 40.dp),
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                if (!selectionMode) {
                    PhotoMainHeroHeader(
                        onSearch = onNavigateToSearch,
                        onMore = { /* TODO: 드롭다운 */ },
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
                                            debugLog = "zoom=${String.format("%.4f", zoom)}  acc=${String.format("%.4f", scaleAccumulator)}  cols=${viewModel.columnCount}"

                                            when {
                                                scaleAccumulator > 1.3f -> {
                                                    viewModel.zoomIn()
                                                    debugLog = "[핀치 아웃 → 확대] zoom=${String.format("%.4f", zoom)}  acc=${String.format("%.4f", scaleAccumulator)}  cols=${viewModel.columnCount}"
                                                    scaleAccumulator = 1f
                                                }
                                                scaleAccumulator < 0.77f -> {
                                                    viewModel.zoomOut()
                                                    debugLog = "[핀치 인 → 축소] zoom=${String.format("%.4f", zoom)}  acc=${String.format("%.4f", scaleAccumulator)}  cols=${viewModel.columnCount}"
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

        // ── 디버그 오버레이 ─────────────────────────────────────────
        Text(
            text = debugLog,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .background(Color(0xCC000000))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    } // Box 닫기
}

@Composable
private fun PhotoMainHeroHeader(
    onSearch: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
    ) {
        IconButton(onClick = onSearch) {
            Icon(Icons.Default.Search, contentDescription = "검색")
        }
        IconButton(onClick = onMore) {
            Icon(Icons.Default.MoreVert, contentDescription = "더보기")
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
