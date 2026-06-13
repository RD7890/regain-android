package com.ryzix.regain.repository

import com.ryzix.regain.data.db.HistoryDao
import com.ryzix.regain.data.db.HistoryEntity
import com.ryzix.regain.model.LockSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepository(private val dao: HistoryDao) {

    val sessions: Flow<List<LockSession>> = dao.getAll().map { list ->
        list.map { e ->
            LockSession(
                id = e.id,
                startTime = e.startTime,
                endTime = e.endTime,
                durationMillis = e.durationMillis,
                completedNaturally = e.completedNaturally
            )
        }
    }

    suspend fun record(startTime: Long, endTime: Long, durationMillis: Long, completedNaturally: Boolean) {
        dao.insert(
            HistoryEntity(
                startTime = startTime,
                endTime = endTime,
                durationMillis = durationMillis,
                completedNaturally = completedNaturally
            )
        )
    }

    suspend fun delete(id: Long) = dao.delete(id)
    suspend fun deleteAll() = dao.deleteAll()
}
