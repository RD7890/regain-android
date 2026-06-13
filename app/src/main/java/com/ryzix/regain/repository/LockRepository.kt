package com.ryzix.regain.repository

import com.ryzix.regain.data.datastore.LockDataStore
import com.ryzix.regain.model.LockState
import kotlinx.coroutines.flow.Flow

class LockRepository(
    private val dataStore: LockDataStore,
    private val historyRepository: HistoryRepository
) {
    val lockState: Flow<LockState> = dataStore.lockState
    val showNotification: Flow<Boolean> = dataStore.showNotification
    val persistLock: Flow<Boolean> = dataStore.persistLock

    suspend fun startLock(durationMillis: Long): Long {
        val endTs = System.currentTimeMillis() + durationMillis
        dataStore.startLock(endTs)
        return endTs
    }

    suspend fun stopLock(startTime: Long, endTimestamp: Long, completedNaturally: Boolean) {
        val now = System.currentTimeMillis()
        val actualEnd = if (completedNaturally) endTimestamp else now
        val duration = actualEnd - startTime
        if (duration > 0) {
            historyRepository.record(
                startTime = startTime,
                endTime = actualEnd,
                durationMillis = duration,
                completedNaturally = completedNaturally
            )
        }
        dataStore.stopLock()
    }

    suspend fun saveTimePicker(hours: Int, minutes: Int) = dataStore.saveTimePicker(hours, minutes)
    suspend fun setShowNotification(value: Boolean) = dataStore.setShowNotification(value)
    suspend fun setPersistLock(value: Boolean) = dataStore.setPersistLock(value)
}
