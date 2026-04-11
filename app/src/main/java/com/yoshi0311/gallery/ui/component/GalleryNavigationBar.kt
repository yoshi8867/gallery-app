package com.yoshi0311.gallery.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.yoshi0311.gallery.ui.AlbumsScreen
import com.yoshi0311.gallery.ui.PhotosScreen
import com.yoshi0311.gallery.ui.RecentsScreen

private enum class GalleryTab(val label: String) {
    Photos("사진"),
    Albums("앨범"),
    Recents("최근")
}

@Composable
fun GalleryNavigationBar(
    currentEntry: NavKey?,
    onTabSelected: (NavKey) -> Unit
) {
    val selectedTab = when (currentEntry) {
        is AlbumsScreen -> GalleryTab.Albums
        is RecentsScreen -> GalleryTab.Recents
        else -> GalleryTab.Photos
    }

    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == GalleryTab.Photos,
            onClick = { onTabSelected(PhotosScreen) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == GalleryTab.Photos) Icons.Filled.PhotoLibrary
                    else Icons.Outlined.PhotoLibrary,
                    contentDescription = "사진"
                )
            },
            label = { Text("사진") }
        )
        NavigationBarItem(
            selected = selectedTab == GalleryTab.Albums,
            onClick = { onTabSelected(AlbumsScreen) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == GalleryTab.Albums) Icons.Filled.GridView
                    else Icons.Outlined.GridView,
                    contentDescription = "앨범"
                )
            },
            label = { Text("앨범") }
        )
        NavigationBarItem(
            selected = selectedTab == GalleryTab.Recents,
            onClick = { onTabSelected(RecentsScreen) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == GalleryTab.Recents) Icons.Filled.AccessTime
                    else Icons.Outlined.AccessTime,
                    contentDescription = "최근"
                )
            },
            label = { Text("최근") }
        )
    }
}
