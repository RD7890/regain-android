package com.ryzix.regain.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ryzix.regain.BuildConfig
import com.ryzix.regain.RegainApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val showNotification: Boolean = true,
    val persistLock: Boolean = true,
    val appVersion: String = BuildConfig.VERSION_NAME
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as RegainApp).container.lockRepository

    val uiState: StateFlow<SettingsUiState> = combine(
        repo.showNotification,
        repo.persistLock
    ) { notify, persist ->
        SettingsUiState(
            showNotification = notify,
            persistLock = persist,
            appVersion = BuildConfig.VERSION_NAME
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setShowNotification(value: Boolean) {
        viewModelScope.launch { repo.setShowNotification(value) }
    }

    fun setPersistLock(value: Boolean) {
        viewModelScope.launch { repo.setPersistLock(value) }
    }
}
