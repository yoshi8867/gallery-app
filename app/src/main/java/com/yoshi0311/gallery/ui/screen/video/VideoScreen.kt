package com.yoshi0311.gallery.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.ui.component.MediaGridScreen
import com.yoshi0311.gallery.viewmodel.VideoViewModel

@Composable
fun VideoScreen(
    onBack: () -> Unit,
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    viewModel: VideoViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val columnCount = viewModel.columnCount
    val selectionMode = viewModel.selectionMode
    val selectedIds = viewModel.selectedIds

    MediaGridScreen(
        title = "동영상",
        items = items,
        columnCount = columnCount,
        selectionMode = selectionMode,
        selectedIds = selectedIds,
        onBack = onBack,
        onItemClick = { item ->
            if (selectionMode) viewModel.toggleSelection(item.id)
            else onNavigateToPhoto(item.id)
        },
        onItemLongClick = { item ->
            if (!selectionMode) viewModel.enterSelectionMode(item.id)
        },
        onExitSelection = { viewModel.exitSelectionMode() },
        onFavorite = { /* P2-1에서 구현 */ },
        onShare = { /* P2-3에서 구현 */ },
        onDelete = { /* TODO */ },
        onPinchIn = { viewModel.zoomIn() },
        onPinchOut = { viewModel.zoomOut() },
    )
}
