package com.ryzix.regain.model

data class LockState(
    val isLocked: Boolean = false,
    val lockEndTimestamp: Long = 0L,
    val selectedHours: Int = 0,
    val selectedMinutes: Int = 1
)
