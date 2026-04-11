package com.yoshi0311.gallery.ui.navigation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.yoshi0311.gallery.ui.MapScreen
import com.yoshi0311.gallery.ui.PermissionScreen
import com.yoshi0311.gallery.ui.PhotoViewScreen
import com.yoshi0311.gallery.ui.PhotosScreen
import com.yoshi0311.gallery.ui.RecentsScreen
import com.yoshi0311.gallery.ui.SearchScreen
import com.yoshi0311.gallery.ui.TrashScreen
import com.yoshi0311.gallery.ui.VideosScreen
import com.yoshi0311.gallery.ui.component.GalleryNavigationBar
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

    val startDestination: NavKey = if (permissionsState.allPermissionsGranted) PhotosScreen else PermissionScreen

    val backStack = rememberNavBackStack(startDestination)

    Scaffold(
        bottomBar = {
            // PermissionScreen에서는 하단 네비게이션 바 숨김
            val current = backStack.lastOrNull()
            if (current != null && current !is PermissionScreen) {
                GalleryNavigationBar(
                    currentEntry = current,
                    onTabSelected = { screen ->
                        if (backStack.lastOrNull() != screen) {
                            backStack.clear()
                            backStack.add(screen)
                        }
                    }
                )
            }
        }
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
                            }
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
                    entry<RecentsScreen> {
                        PlaceholderScreen("최근 항목 (P1-9에서 구현 예정)")
                    }
                    entry<VideosScreen> {
                        PlaceholderScreen("동영상 화면 (P1-9에서 구현 예정)")

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
                    entry<MapScreen> {
                        PlaceholderScreen("지도 화면 (P2-5에서 구현 예정)")
                    }
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label)
    }
}
