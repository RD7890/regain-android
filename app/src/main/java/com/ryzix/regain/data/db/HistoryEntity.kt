package com.ryzix.regain.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lock_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
    val completedNaturally: Boolean
)
