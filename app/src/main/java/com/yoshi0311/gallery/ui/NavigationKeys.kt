package com.yoshi0311.gallery.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ── 권한 스크린 ─────────────────────────────────────────────
@Serializable data object PermissionScreen : NavKey

// ── 바텀 탭 스크린 ──────────────────────────────────────────
@Serializable data object PhotosScreen : NavKey
@Serializable data object AlbumsScreen : NavKey
@Serializable data object RecentsScreen : NavKey
@Serializable data object VideosScreen : NavKey

// ── 상세 스크린 ─────────────────────────────────────────────
@Serializable data class AlbumViewScreen(
    val albumId: Long,
    val albumName: String
) : NavKey

@Serializable data class PhotoViewScreen(
    val mediaId: Long,
    val albumId: Long? = null
) : NavKey

// ── 기능 스크린 ─────────────────────────────────────────────
@Serializable data object SearchScreen : NavKey
@Serializable data object FavoritesScreen : NavKey
@Serializable data object TrashScreen : NavKey
@Serializable data object MapScreen : NavKey
@Serializable data object StoryListScreen : NavKey
@Serializable data class StoryViewScreen(val storyId: Long) : NavKey
