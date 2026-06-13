package com.ryzix.regain.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

object MiuiUtils {

    fun isMiui(): Boolean {
        return Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
                getSystemProperty("ro.miui.ui.version.name").isNotEmpty() ||
                getSystemProperty("ro.miui.ui.version.code").isNotEmpty()
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java, String::class.java)
            get.invoke(c, key, "") as? String ?: ""
        } catch (_: Exception) { "" }
    }

    /**
     * Opens MIUI AutoStart settings so the user can enable autostart for Regain.
     * Returns false if the intent could not be resolved (non-MIUI device).
     */
    fun openMiuiAutoStart(context: Context): Boolean {
        val intents = listOf(
            Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            },
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
            },
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
            }
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Open generic app details (fallback for non-MIUI or when MIUI intent fails).
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { context.startActivity(intent) } catch (_: Exception) {}
    }
}
