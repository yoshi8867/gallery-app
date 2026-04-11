package com.yoshi0311.gallery.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.yoshi0311.gallery.ui.AlbumViewScreen
import com.yoshi0311.gallery.ui.AlbumsScreen
import com.yoshi0311.gallery.ui.FavoritesScreen
import com.yoshi0311.gallery.ui.MapScreen
import com.yoshi0311.gallery.ui.PhotoViewScreen
import com.yoshi0311.gallery.ui.PhotosScreen
import com.yoshi0311.gallery.ui.RecentsScreen
import com.yoshi0311.gallery.ui.SearchScreen
import com.yoshi0311.gallery.ui.TrashScreen
import com.yoshi0311.gallery.ui.VideosScreen
import com.yoshi0311.gallery.ui.component.GalleryNavigationBar

@Composable
fun GalleryNavHost() {
    val backStack = rememberNavBackStack(PhotosScreen)

    Scaffold(
        bottomBar = {
            GalleryNavigationBar(
                currentEntry = backStack.lastOrNull(),
                onTabSelected = { screen ->
                    if (backStack.lastOrNull() != screen) {
                        backStack.clear()
                        backStack.add(screen)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<PhotosScreen> {
                        PlaceholderScreen("사진 화면 (P1-4에서 구현 예정)")
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
