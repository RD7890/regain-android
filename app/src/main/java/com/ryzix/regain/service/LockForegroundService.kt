package com.ryzix.regain.service

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ryzix.regain.MainActivity
import com.ryzix.regain.R
import com.ryzix.regain.RegainApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LockForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "regain_lock_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.ryzix.regain.ACTION_STOP_LOCK"
        const val EXTRA_START_TIME = "extra_start_time"
    }

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var startTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            handleStopFromNotification()
            return START_NOT_STICKY
        }
        startTime = intent?.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
            ?: System.currentTimeMillis()

        startForeground(NOTIFICATION_ID, buildNotification("Lockdown active", "Calculating..."))
        startTickLoop()
        return START_STICKY
    }

    private fun startTickLoop() {
        val repo = (application as RegainApp).container.lockRepository
        scope.launch {
            while (true) {
                val state = repo.lockState.first()
                if (!state.isLocked) { stopSelf(); break }

                val remaining = state.lockEndTimestamp - System.currentTimeMillis()
                if (remaining <= 0) {
                    repo.stopLock(startTime, state.lockEndTimestamp, completedNaturally = true)
                    stopSelf(); break
                }

                val h = remaining / 3_600_000
                val m = (remaining % 3_600_000) / 60_000
                val s = (remaining % 60_000) / 1_000
                updateNotification("🔒 Regain Lockdown Active", "%02d:%02d:%02d remaining".format(h, m, s))

                // Every 2 seconds bring app back to foreground if it was pushed away
                if ((remaining / 1000) % 2 == 0L) {
                    ensureAppInForeground()
                }

                delay(1_000)
            }
        }
    }

    private fun ensureAppInForeground() {
        if (!MainActivity.isInForeground) {
            val am = getSystemService(ActivityManager::class.java)
            val isPinned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
            } else false

            if (!isPinned) {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }
                if (launchIntent != null) {
                    try { startActivity(launchIntent) } catch (_: Exception) {}
                }
            }
        }
    }

    private fun handleStopFromNotification() {
        scope.launch {
            val repo = (application as RegainApp).container.lockRepository
            val state = repo.lockState.first()
            if (state.isLocked) {
                repo.stopLock(startTime, state.lockEndTimestamp, completedNaturally = false)
            }
            stopSelf()
        }
    }

    private fun buildNotification(title: String, text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(
                0, "Stop Lock",
                PendingIntent.getService(
                    this, 1,
                    Intent(this, LockForegroundService::class.java).apply { action = ACTION_STOP },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()

    private fun updateNotification(title: String, text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(title, text))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Regain Lock Service", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the active lockdown countdown timer"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
