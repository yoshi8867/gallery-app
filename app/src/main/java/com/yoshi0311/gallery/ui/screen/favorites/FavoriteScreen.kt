package com.yoshi0311.gallery.ui.screen.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.ui.component.MediaGridScreen
import com.yoshi0311.gallery.util.shareMediaItems
import com.yoshi0311.gallery.viewmodel.FavoriteViewModel

@Composable
fun FavoriteScreen(
    onBack: () -> Unit,
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsStateWithLifecycle()

    MediaGridScreen(
        title = "즐겨찾기",
        items = items,
        columnCount = viewModel.columnCount,
        selectionMode = viewModel.selectionMode,
        selectedIds = viewModel.selectedIds,
        onBack = onBack,
        onItemClick = { item ->
            if (viewModel.selectionMode) viewModel.toggleSelection(item.id)
            else onNavigateToPhoto(item.id)
        },
        onItemLongClick = { item ->
            if (!viewModel.selectionMode) viewModel.enterSelectionMode(item.id)
        },
        onExitSelection = { viewModel.exitSelectionMode() },
        onFavorite = { viewModel.removeSelectedFromFavorites() },
        onShare = { shareMediaItems(context, items.filter { it.id in viewModel.selectedIds }) },
        onDelete = { viewModel.moveSelectedToTrash() },
        onPinchIn = { viewModel.zoomIn() },
        onPinchOut = { viewModel.zoomOut() },
        columnLevels = listOf(3, 4, 7),
        showFavoriteHeart = true,
    )
}
