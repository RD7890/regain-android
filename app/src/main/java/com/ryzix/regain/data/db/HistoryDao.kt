package com.ryzix.regain.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM lock_history ORDER BY startTime DESC")
    fun getAll(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity): Long

    @Query("DELETE FROM lock_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM lock_history")
    suspend fun deleteAll()
}
