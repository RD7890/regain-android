package com.ryzix.regain.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.ryzix.regain.RegainApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val repo = (context.applicationContext as RegainApp).container.lockRepository
        CoroutineScope(Dispatchers.IO).launch {
            val state = repo.lockState.first()
            if (state.isLocked && state.lockEndTimestamp > System.currentTimeMillis()) {
                val svcIntent = Intent(context, LockForegroundService::class.java).apply {
                    putExtra(LockForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                }
                ContextCompat.startForegroundService(context, svcIntent)
            } else if (state.isLocked) {
                repo.stopLock(0L, state.lockEndTimestamp, completedNaturally = true)
            }
        }
    }
}
