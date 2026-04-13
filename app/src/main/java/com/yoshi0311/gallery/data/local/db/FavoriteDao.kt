package com.yoshi0311.gallery.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT mediaId FROM favorites")
    fun observeAllIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE mediaId = :mediaId")
    suspend fun delete(mediaId: Long)

    @Query("SELECT COUNT(*) FROM favorites WHERE mediaId = :mediaId")
    suspend fun count(mediaId: Long): Int
}
