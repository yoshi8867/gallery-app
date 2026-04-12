package com.yoshi0311.gallery.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.yoshi0311.gallery.ui.navigation.AlbumsScreen
import com.yoshi0311.gallery.ui.navigation.PhotosScreen
import com.yoshi0311.gallery.ui.navigation.StoryListScreen

// 0=사진, 1=앨범, 2=스토리, 3=메뉴
@Composable
fun GalleryNavigationBar(
    selectedTabIndex: Int,
    onTabSelected: (NavKey) -> Unit,
    onMenuTap: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTabIndex == 0,
            onClick = { onTabSelected(PhotosScreen) },
            icon = {
                Icon(
                    imageVector = if (selectedTabIndex == 0) Icons.Filled.PhotoLibrary
                    else Icons.Outlined.PhotoLibrary,
                    contentDescription = "사진",
                )
            },
            label = { Text("사진") },
        )
        NavigationBarItem(
            selected = selectedTabIndex == 1,
            onClick = { onTabSelected(AlbumsScreen) },
            icon = {
                Icon(
                    imageVector = if (selectedTabIndex == 1) Icons.Filled.GridView
                    else Icons.Outlined.GridView,
                    contentDescription = "앨범",
                )
            },
            label = { Text("앨범") },
        )
        NavigationBarItem(
            selected = selectedTabIndex == 2,
            onClick = { onTabSelected(StoryListScreen) },
            icon = {
                Icon(
                    imageVector = if (selectedTabIndex == 2) Icons.Filled.AutoAwesome
                    else Icons.Outlined.AutoAwesome,
                    contentDescription = "스토리",
                )
            },
            label = { Text("스토리") },
        )
        NavigationBarItem(
            selected = selectedTabIndex == 3,
            onClick = onMenuTap,
            icon = {
                Icon(
                    imageVector = if (selectedTabIndex == 3) Icons.Filled.Menu
                    else Icons.Outlined.Menu,
                    contentDescription = "메뉴",
                )
            },
            label = { Text("메뉴") },
        )
    }
}
