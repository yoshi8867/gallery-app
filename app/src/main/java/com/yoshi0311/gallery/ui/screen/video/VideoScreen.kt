package com.yoshi0311.gallery.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.ui.component.MediaGridScreen
import com.yoshi0311.gallery.util.shareMediaItems
import com.yoshi0311.gallery.viewmodel.VideoViewModel

@Composable
fun VideoScreen(
    onBack: () -> Unit,
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    viewModel: VideoViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
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
        onFavorite = { viewModel.addSelectedToFavorites() },
        onShare = { shareMediaItems(context, items.filter { it.id in selectedIds }) },
        onDelete = { viewModel.moveSelectedToTrash() },
        onPinchIn = { viewModel.zoomIn() },
        onPinchOut = { viewModel.zoomOut() },
        columnLevels = listOf(3, 4, 7),
    )
}
