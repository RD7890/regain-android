package com.ryzix.regain.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ryzix.regain.RegainApp
import com.ryzix.regain.model.LockState
import com.ryzix.regain.service.LockForegroundService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val lockState: LockState = LockState(),
    val remainingMillis: Long = 0L,
    val lockStartTime: Long = 0L
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as RegainApp).container.lockRepository

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.lockState.collect { state ->
                val remaining = if (state.isLocked) {
                    (state.lockEndTimestamp - System.currentTimeMillis()).coerceAtLeast(0)
                } else 0L
                _uiState.value = _uiState.value.copy(
                    lockState = state,
                    remainingMillis = remaining
                )
            }
        }
        startCountdownTick()
    }

    private fun startCountdownTick() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                val state = _uiState.value
                if (state.lockState.isLocked) {
                    val remaining = (state.lockState.lockEndTimestamp - System.currentTimeMillis())
                        .coerceAtLeast(0)
                    _uiState.value = state.copy(remainingMillis = remaining)
                    if (remaining <= 0) {
                        repo.stopLock(
                            startTime = state.lockStartTime,
                            endTimestamp = state.lockState.lockEndTimestamp,
                            completedNaturally = true
                        )
                    }
                }
            }
        }
    }

    fun incrementHours() {
        val current = _uiState.value.lockState
        val newHours = (current.selectedHours + 1).coerceAtMost(99)
        viewModelScope.launch { repo.saveTimePicker(newHours, current.selectedMinutes) }
    }

    fun decrementHours() {
        val current = _uiState.value.lockState
        val newHours = (current.selectedHours - 1).coerceAtLeast(0)
        viewModelScope.launch { repo.saveTimePicker(newHours, current.selectedMinutes) }
    }

    fun incrementMinutes() {
        val current = _uiState.value.lockState
        val newMins = (current.selectedMinutes + 1).coerceAtMost(59)
        viewModelScope.launch { repo.saveTimePicker(current.selectedHours, newMins) }
    }

    fun decrementMinutes() {
        val current = _uiState.value.lockState
        val newMins = (current.selectedMinutes - 1).coerceAtLeast(0)
        viewModelScope.launch { repo.saveTimePicker(current.selectedHours, newMins) }
    }

    fun startLockdown() {
        val state = _uiState.value.lockState
        val totalMs = (state.selectedHours * 3_600_000L) + (state.selectedMinutes * 60_000L)
        if (totalMs <= 0) return
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            val endTs = repo.startLock(totalMs)
            _uiState.value = _uiState.value.copy(lockStartTime = now)
            val ctx = getApplication<RegainApp>()
            val intent = Intent(ctx, LockForegroundService::class.java).apply {
                putExtra(LockForegroundService.EXTRA_START_TIME, now)
            }
            ContextCompat.startForegroundService(ctx, intent)
        }
    }

    fun stopLockdown() {
        viewModelScope.launch {
            val state = _uiState.value
            repo.stopLock(
                startTime = state.lockStartTime,
                endTimestamp = state.lockState.lockEndTimestamp,
                completedNaturally = false
            )
            val ctx = getApplication<RegainApp>()
            ctx.stopService(Intent(ctx, LockForegroundService::class.java))
        }
    }
}
