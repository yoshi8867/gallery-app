package com.yoshi0311.gallery.ui.screen.trash

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.ui.component.MediaGridScreen
import com.yoshi0311.gallery.viewmodel.TrashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit,
    onNavigateToPhoto: (mediaId: Long) -> Unit,
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val daysRemainingMap by viewModel.daysRemainingMap.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    MediaGridScreen(
        title = "휴지통",
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
        getItemBadge = { item -> daysRemainingMap[item.id]?.let { "${it}일" } },
        selectionBottomBar = {
            TrashSelectionBar(
                onRestore = { viewModel.restoreSelected() },
                onDelete = { showDeleteDialog = true },
            )
        },
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("정말 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.exitSelectionMode()
                        Toast.makeText(
                            context,
                            "안전을 위해서, 실제 삭제 기능은 생략♡",
                            Toast.LENGTH_LONG,
                        ).show()
                    },
                ) { Text("예") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("아니오") }
            },
        )
    }
}

@Composable
private fun TrashSelectionBar(
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TrashActionItem(icon = Icons.Default.Restore, label = "복원", onClick = onRestore)
            TrashActionItem(icon = Icons.Default.Delete, label = "삭제", onClick = onDelete)
        }
    }
}

@Composable
private fun TrashActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
