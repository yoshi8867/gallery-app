package com.yoshi0311.gallery.ui.screen.recents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.ui.component.MediaGridScreen
import com.yoshi0311.gallery.viewmodel.RecentsFilter
import com.yoshi0311.gallery.viewmodel.RecentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    onBack: () -> Unit,
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    viewModel: RecentsViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val filter = viewModel.filter
    val selectionMode = viewModel.selectionMode
    val selectedIds = viewModel.selectedIds

    MediaGridScreen(
        title = "최근 항목",
        items = items,
        columnCount = 3,
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
        onShare = { /* P2-3에서 구현 */ },
        onDelete = { viewModel.moveSelectedToTrash() },
        headerContent = if (!selectionMode) {
            {
//                SingleChoiceSegmentedButtonRow(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                ) {
//                    SegmentedButton(
//                        selected = filter == RecentsFilter.Recent30Days,
//                        onClick = { viewModel.updateFilter(RecentsFilter.Recent30Days) },
//                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
//                    ) { Text("최근 30일") }
//                    SegmentedButton(
//                        selected = filter == RecentsFilter.All,
//                        onClick = { viewModel.updateFilter(RecentsFilter.All) },
//                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
//                    ) { Text("전체 기간") }
//                }
            }
        } else null,
    )
}
