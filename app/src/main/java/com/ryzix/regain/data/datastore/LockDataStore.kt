package com.ryzix.regain.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ryzix.regain.model.LockState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "regain_prefs")

class LockDataStore(private val context: Context) {

    companion object {
        private val KEY_IS_LOCKED = booleanPreferencesKey("is_locked")
        private val KEY_LOCK_END_TS = longPreferencesKey("lock_end_timestamp")
        private val KEY_SELECTED_HOURS = intPreferencesKey("selected_hours")
        private val KEY_SELECTED_MINUTES = intPreferencesKey("selected_minutes")
        private val KEY_SHOW_NOTIFICATION = booleanPreferencesKey("show_notification")
        private val KEY_PERSIST_LOCK = booleanPreferencesKey("persist_lock")
    }

    val lockState: Flow<LockState> = context.dataStore.data.map { prefs ->
        LockState(
            isLocked = prefs[KEY_IS_LOCKED] ?: false,
            lockEndTimestamp = prefs[KEY_LOCK_END_TS] ?: 0L,
            selectedHours = prefs[KEY_SELECTED_HOURS] ?: 0,
            selectedMinutes = prefs[KEY_SELECTED_MINUTES] ?: 1
        )
    }

    val showNotification: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_NOTIFICATION] ?: true
    }

    val persistLock: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PERSIST_LOCK] ?: true
    }

    suspend fun startLock(endTimestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOCKED] = true
            prefs[KEY_LOCK_END_TS] = endTimestamp
        }
    }

    suspend fun stopLock() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOCKED] = false
            prefs[KEY_LOCK_END_TS] = 0L
        }
    }

    suspend fun saveTimePicker(hours: Int, minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_HOURS] = hours
            prefs[KEY_SELECTED_MINUTES] = minutes
        }
    }

    suspend fun setShowNotification(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SHOW_NOTIFICATION] = value }
    }

    suspend fun setPersistLock(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_PERSIST_LOCK] = value }
    }
}
