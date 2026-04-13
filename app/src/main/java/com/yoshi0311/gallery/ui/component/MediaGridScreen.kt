package com.yoshi0311.gallery.ui.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.yoshi0311.gallery.data.model.MediaItem
import kotlin.math.sqrt

/**
 * 동영상·최근 항목·즐겨찾기·휴지통 화면이 공유하는 공용 그리드 레이아웃.
 * - 스티키 헤더 없음, 단순 그리드
 * - 핀치 줌: onPinchIn/onPinchOut이 null이 아닌 경우에만 활성화
 * - headerContent 슬롯: RecentsScreen 필터 버튼 등 화면별 서브헤더 삽입
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGridScreen(
    title: String,
    items: List<MediaItem>,
    columnCount: Int,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onBack: () -> Unit,
    onItemClick: (MediaItem) -> Unit,
    onItemLongClick: (MediaItem) -> Unit,
    onExitSelection: () -> Unit,
    onFavorite: () -> Unit = {},
    onShare: () -> Unit = {},
    onDelete: () -> Unit = {},
    onPinchIn: (() -> Unit)? = null,
    onPinchOut: (() -> Unit)? = null,
    headerContent: (@Composable () -> Unit)? = null,
    /** null이면 기본 SelectionActionBar(4버튼) 사용, 비null이면 해당 Composable로 대체 */
    selectionBottomBar: (@Composable () -> Unit)? = null,
    /** 썸네일 하단 우측에 표시할 텍스트 배지 (예: 휴지통 남은 일수). null이면 미표시 */
    getItemBadge: ((MediaItem) -> String?)? = null,
) {
    var scaleAccumulator by remember { mutableStateOf(1f) }

    Scaffold(
        topBar = {
            if (selectionMode) {
                SelectionTopBar(
                    selectedCount = selectedIds.size,
                    onClose = onExitSelection,
                )
            } else {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "뒤로가기",
                            )
                        }
                    },
                    title = { Text(title) },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, contentDescription = "더보기")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (selectionMode) {
                selectionBottomBar?.invoke() ?: SelectionActionBar(
                    onFavorite = onFavorite,
                    onShare = onShare,
                    onDelete = onDelete,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            // 화면별 서브헤더 (예: 필터 칩)
            headerContent?.invoke()

            val hasPinch = onPinchIn != null || onPinchOut != null
            val pinchModifier = if (hasPinch) {
                Modifier.pointerInput(Unit) {
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
                                            onPinchIn?.invoke()
                                            scaleAccumulator = 1f
                                        }
                                        scaleAccumulator < 0.77f -> {
                                            onPinchOut?.invoke()
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
                }
            } else Modifier

            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                modifier = Modifier
                    .weight(1f)
                    .then(pinchModifier),
            ) {
                val rows = items.chunked(columnCount)
                items(
                    count = rows.size,
                    key = { idx -> "row_$idx" },
                ) { rowIdx ->
                    val rowItems = rows[rowIdx]
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { item ->
                            MediaThumbnail(
                                item = item,
                                isSelected = item.id in selectedIds,
                                inSelectionMode = selectionMode,
                                modifier = Modifier.weight(1f),
                                bottomBadge = getItemBadge?.invoke(item),
                                onClick = { onItemClick(item) },
                                onLongClick = { onItemLongClick(item) },
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
