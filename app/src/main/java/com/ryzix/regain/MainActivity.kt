package com.ryzix.regain

import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.ryzix.regain.navigation.RegainNavGraph
import com.ryzix.regain.ui.theme.RegainTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        @Volatile var isInForeground = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegainTheme {
                RegainNavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
        repinIfLocked()
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) repinIfLocked()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // When user presses Home while locked, immediately relaunch to foreground
        lifecycleScope.launch {
            val state = (application as RegainApp).container.lockRepository.lockState.first()
            if (state.isLocked && state.lockEndTimestamp > System.currentTimeMillis()) {
                val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                            android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                if (intent != null) startActivity(intent)
            }
        }
    }

    private fun repinIfLocked() {
        lifecycleScope.launch {
            val state = (application as RegainApp).container.lockRepository.lockState.first()
            if (state.isLocked && state.lockEndTimestamp > System.currentTimeMillis()) {
                val am = getSystemService(ActivityManager::class.java)
                val notPinned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE
                } else false
                if (notPinned) {
                    try { startLockTask() } catch (_: Exception) {}
                }
            }
        }
    }
}
