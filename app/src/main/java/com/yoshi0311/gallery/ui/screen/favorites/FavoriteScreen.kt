package com.yoshi0311.gallery.ui.screen.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.ui.component.MediaGridScreen
import com.yoshi0311.gallery.viewmodel.FavoriteViewModel

@Composable
fun FavoriteScreen(
    onBack: () -> Unit,
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    MediaGridScreen(
        title = "즐겨찾기",
        items = items,
        columnCount = 3,
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
        onShare = { /* P2-3에서 구현 */ },
        onDelete = { viewModel.moveSelectedToTrash() },
    )
}
