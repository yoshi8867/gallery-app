package com.yoshi0311.gallery.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashDao {

    @Query("SELECT * FROM trash")
    fun observeAll(): Flow<List<TrashEntity>>

    @Query("SELECT mediaId FROM trash")
    fun observeAllIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TrashEntity)

    @Query("DELETE FROM trash WHERE mediaId = :mediaId")
    suspend fun delete(mediaId: Long)
}
