package com.yoshi0311.gallery.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash")
data class TrashEntity(
    @PrimaryKey val mediaId: Long,
    val addedAt: Long = System.currentTimeMillis(),
)
