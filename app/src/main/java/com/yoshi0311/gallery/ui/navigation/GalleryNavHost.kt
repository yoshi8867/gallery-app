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
import com.yoshi0311.gallery.ui.AlbumViewScreen
import com.yoshi0311.gallery.ui.AlbumsScreen
import com.yoshi0311.gallery.ui.FavoritesScreen
import com.yoshi0311.gallery.ui.LocationScreen
import com.yoshi0311.gallery.ui.MapScreen
import com.yoshi0311.gallery.ui.PermissionScreen
import com.yoshi0311.gallery.ui.PhotoViewScreen
import com.yoshi0311.gallery.ui.PhotosScreen
import com.yoshi0311.gallery.ui.RecentsScreen
import com.yoshi0311.gallery.ui.SearchScreen
import com.yoshi0311.gallery.ui.SettingsScreen
import com.yoshi0311.gallery.ui.StoryListScreen
import com.yoshi0311.gallery.ui.TrashScreen
import com.yoshi0311.gallery.ui.VideosScreen
import com.yoshi0311.gallery.ui.component.GalleryNavigationBar
import com.yoshi0311.gallery.ui.menu.MenuModalSheet
import com.yoshi0311.gallery.ui.permission.PermissionScreen as PermissionScreenUI
import com.yoshi0311.gallery.ui.photos.PhotoMainScreen

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

    Scaffold(
        bottomBar = {
            val current = backStack.lastOrNull()
            if (current != null && current !is PermissionScreen) {
                // 현재 화면 기반으로 활성 탭 결정
                val selectedTabIndex = when {
                    showMenuSheet -> 3
                    current is AlbumsScreen || current is AlbumViewScreen -> 1
                    current is StoryListScreen -> 2
                    // 메뉴에서 진입하는 2차 화면들 → 메뉴 탭 활성
                    current is VideosScreen || current is FavoritesScreen ||
                        current is RecentsScreen || current is TrashScreen ||
                        current is LocationScreen || current is MapScreen ||
                        current is SettingsScreen -> 3
                    else -> 0 // PhotosScreen, SearchScreen, PhotoViewScreen
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
        Box(Modifier.padding(innerPadding)) {
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
                        PlaceholderScreen("앨범 화면 (P1-5에서 구현 예정)")
                    }
                    entry<StoryListScreen> {
                        PlaceholderScreen("스토리 리스트 (P3에서 구현 예정)")
                    }
                    entry<AlbumViewScreen> {
                        PlaceholderScreen("앨범 상세 (P1-6에서 구현 예정)")
                    }
                    entry<PhotoViewScreen> {
                        PlaceholderScreen("사진 뷰어 (P1-7에서 구현 예정)")
                    }
                    entry<SearchScreen> {
                        PlaceholderScreen("검색 화면 (P2-4에서 구현 예정)")
                    }
                    entry<FavoritesScreen> {
                        PlaceholderScreen("즐겨찾기 (P2-1에서 구현 예정)")
                    }
                    entry<TrashScreen> {
                        PlaceholderScreen("휴지통 (P2-2에서 구현 예정)")
                    }
                    entry<LocationScreen> {
                        PlaceholderScreen("위치 화면 (P2-5에서 구현 예정)")
                    }
                    entry<MapScreen> {
                        PlaceholderScreen("지도 화면 (P2-5에서 구현 예정)")
                    }
                    entry<VideosScreen> {
                        PlaceholderScreen("동영상 화면 (P1-9에서 구현 예정)")
                    }
                    entry<RecentsScreen> {
                        PlaceholderScreen("최근 항목 (P1-9에서 구현 예정)")
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
