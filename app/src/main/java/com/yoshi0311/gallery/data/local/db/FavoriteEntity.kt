package com.yoshi0311.gallery.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val mediaId: Long,
    val addedAt: Long = System.currentTimeMillis(),
)
