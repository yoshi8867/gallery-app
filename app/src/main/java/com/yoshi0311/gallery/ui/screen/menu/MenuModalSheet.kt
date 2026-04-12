package com.yoshi0311.gallery.ui.screen.menu

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.yoshi0311.gallery.ui.navigation.FavoritesScreen
import com.yoshi0311.gallery.ui.navigation.LocationScreen
import com.yoshi0311.gallery.ui.navigation.RecentsScreen
import com.yoshi0311.gallery.ui.navigation.SettingsScreen
import com.yoshi0311.gallery.ui.navigation.TrashScreen
import com.yoshi0311.gallery.ui.navigation.VideosScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuModalSheet(
    onDismiss: () -> Unit,
    onNavigate: (NavKey) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            MenuItemRow(
                icon = Icons.Outlined.Videocam,
                label = "동영상",
                onClick = { onDismiss(); onNavigate(VideosScreen) },
            )
            HorizontalDivider()
            MenuItemRow(
                icon = Icons.Outlined.FavoriteBorder,
                label = "즐겨찾기",
                onClick = { onDismiss(); onNavigate(FavoritesScreen) },
            )
            HorizontalDivider()
            MenuItemRow(
                icon = Icons.Outlined.AccessTime,
                label = "최근 항목",
                onClick = { onDismiss(); onNavigate(RecentsScreen) },
            )
            HorizontalDivider()
            MenuItemRow(
                icon = Icons.Outlined.LocationOn,
                label = "위치",
                onClick = { onDismiss(); onNavigate(LocationScreen) },
            )
            HorizontalDivider()
            MenuItemRow(
                icon = Icons.Outlined.PeopleOutline,
                label = "공유 앨범",
                onClick = {
                    Toast.makeText(context, "추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
                },
            )
            HorizontalDivider()
            MenuItemRow(
                icon = Icons.Outlined.AutoFixHigh,
                label = "사진첩 정리",
                onClick = {
                    Toast.makeText(context, "추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
                },
            )
            HorizontalDivider()
            MenuItemRow(
                icon = Icons.Outlined.DeleteOutline,
                label = "휴지통",
                onClick = { onDismiss(); onNavigate(TrashScreen) },
            )
            HorizontalDivider()
            MenuItemRow(
                icon = Icons.Outlined.Settings,
                label = "설정",
                onClick = { onDismiss(); onNavigate(SettingsScreen) },
            )
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
