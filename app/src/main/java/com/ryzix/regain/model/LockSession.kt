package com.ryzix.regain.model

data class LockSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
    val completedNaturally: Boolean = false
) {
    val durationSeconds: Long get() = durationMillis / 1000
    val durationMinutes: Long get() = durationSeconds / 60
    val durationHours: Long get() = durationMinutes / 60
}
