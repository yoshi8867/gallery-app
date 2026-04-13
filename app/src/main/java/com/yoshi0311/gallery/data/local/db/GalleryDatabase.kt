package com.yoshi0311.gallery.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class, TrashEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun trashDao(): TrashDao
}
