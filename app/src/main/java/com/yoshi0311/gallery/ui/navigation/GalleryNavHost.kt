package com.yoshi0311.gallery.ui.navigation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yoshi0311.gallery.ui.component.GalleryNavigationBar
import com.yoshi0311.gallery.ui.screen.albumlist.AlbumListScreen
import com.yoshi0311.gallery.ui.screen.albumview.AlbumViewScreen
import com.yoshi0311.gallery.ui.screen.favorites.FavoriteScreen as FavoriteScreenUI
import com.yoshi0311.gallery.ui.screen.menu.MenuModalSheet
import com.yoshi0311.gallery.ui.screen.permission.PermissionScreen as PermissionScreenUI
import com.yoshi0311.gallery.ui.screen.photomain.PhotoMainScreen
import com.yoshi0311.gallery.ui.screen.photoview.PhotoViewScreen as PhotoViewScreenUI
import com.yoshi0311.gallery.ui.screen.recents.RecentsScreen as RecentsScreenUI
import com.yoshi0311.gallery.ui.screen.location.LocationScreen as LocationScreenUI
import com.yoshi0311.gallery.ui.screen.map.MapScreen as MapScreenUI
import com.yoshi0311.gallery.ui.screen.trash.TrashScreen as TrashScreenUI
import com.yoshi0311.gallery.ui.screen.video.VideoScreen as VideoScreenUI

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalleryNavHost() {
    val mediaPermissions = remember {
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }
    val permissionsState = rememberMultiplePermissionsState(permissions = mediaPermissions)

    val startDestination: NavKey =
        if (permissionsState.allPermissionsGranted) PhotosScreen else PermissionScreen

    val backStack = rememberNavBackStack(startDestination)
    var showMenuSheet by remember { mutableStateOf(false) }

    // ── NavigationBar 표시 여부 (Scaffold 밖에서 계산하여 bottomBar·content 공유) ──
    val current = backStack.lastOrNull()
    val showBottomBar = current != null &&
        current !is PermissionScreen &&
        current !is PhotoViewScreen &&
        current !is VideosScreen &&
        current !is RecentsScreen &&
        current !is FavoritesScreen &&
        current !is TrashScreen &&
        current !is LocationScreen &&
        current !is MapScreen &&
        current !is SettingsScreen

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val selectedTabIndex = when {
                    showMenuSheet -> 3
                    current is AlbumsScreen || current is AlbumViewScreen -> 1
                    current is StoryListScreen -> 2
                    else -> 0
                }
                GalleryNavigationBar(
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { screen ->
                        showMenuSheet = false
                        if (backStack.lastOrNull() != screen) {
                            backStack.clear()
                            backStack.add(screen)
                        }
                    },
                    onMenuTap = { showMenuSheet = true },
                )
            }
        },
    ) { innerPadding ->
        // NavigationBar가 없는 화면은 innerPadding 미적용 (자체 inset 처리)
        Box(
            if (!showBottomBar) Modifier.fillMaxSize()
            else Modifier.padding(innerPadding)) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<PermissionScreen> {
                        PermissionScreenUI(
                            onPermissionsGranted = {
                                backStack.clear()
                                backStack.add(PhotosScreen)
                            },
                        )
                    }
                    entry<PhotosScreen> {
                        PhotoMainScreen(
                            onNavigateToPhoto = { mediaId ->
                                backStack.add(PhotoViewScreen(mediaId = mediaId))
                            },
                            onNavigateToSearch = {
                                backStack.add(SearchScreen)
                            },
                        )
                    }
                    entry<AlbumsScreen> {
                        AlbumListScreen(
                            onNavigateToAlbum = { albumId, albumName ->
                                backStack.add(AlbumViewScreen(albumId = albumId, albumName = albumName))
                            },
                            onNavigateToSearch = {
                                backStack.add(SearchScreen)
                            },
                        )
                    }
                    entry<StoryListScreen> {
                        PlaceholderScreen("스토리 리스트 (P3에서 구현 예정)")
                    }
                    entry<AlbumViewScreen> {
                        AlbumViewScreen(
                            albumId = it.albumId,
                            albumName = it.albumName,
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToPhoto = { mediaId ->
                                backStack.add(PhotoViewScreen(mediaId = mediaId, albumId = it.albumId))
                            },
                            onNavigateToAlbum = { albumId, albumName ->
                                backStack.removeLastOrNull()
                                backStack.add(AlbumViewScreen(albumId = albumId, albumName = albumName))
                            },
                        )
                    }
                    entry<PhotoViewScreen> {
                        PhotoViewScreenUI(
                            mediaId = it.mediaId,
                            albumId = it.albumId,
                            onBack = { backStack.removeLastOrNull() },
                            fromTrash = it.fromTrash,
                        )
                    }
                    entry<SearchScreen> {
                        PlaceholderScreen("검색 화면 (P2-4에서 구현 예정)")
                    }
                    entry<FavoritesScreen> {
                        FavoriteScreenUI(
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToPhoto = { mediaId ->
                                backStack.add(PhotoViewScreen(mediaId = mediaId))
                            },
                        )
                    }
                    entry<TrashScreen> {
                        TrashScreenUI(
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToPhoto = { mediaId ->
                                backStack.add(PhotoViewScreen(mediaId = mediaId, fromTrash = true))
                            },
                        )
                    }
                    entry<LocationScreen> {
                        LocationScreenUI(
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToMap = { cityFilter ->
                                backStack.add(MapScreen(cityFilter = cityFilter))
                            },
                        )
                    }
                    entry<MapScreen> {
                        MapScreenUI(
                            cityFilter = it.cityFilter,
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToPhoto = { mediaId ->
                                backStack.add(PhotoViewScreen(mediaId = mediaId))
                            },
                        )
                    }
                    entry<VideosScreen> {
                        VideoScreenUI(
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToPhoto = { mediaId ->
                                backStack.add(PhotoViewScreen(mediaId = mediaId))
                            },
                        )
                    }
                    entry<RecentsScreen> {
                        RecentsScreenUI(
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToPhoto = { mediaId ->
                                backStack.add(PhotoViewScreen(mediaId = mediaId))
                            },
                        )
                    }
                    entry<SettingsScreen> {
                        PlaceholderScreen("설정 (미구현)")
                    }
                },
            )
        }
    }

    // 메뉴 바텀시트 — Scaffold 외부에 배치하여 전체 화면 오버레이
    if (showMenuSheet) {
        MenuModalSheet(
            onDismiss = { showMenuSheet = false },
            onNavigate = { key ->
                showMenuSheet = false
                backStack.add(key)
            },
        )
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label)
    }
}
