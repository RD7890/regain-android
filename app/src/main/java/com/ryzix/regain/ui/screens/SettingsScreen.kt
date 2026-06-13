package com.ryzix.regain.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryzix.regain.ui.components.BottomNavTab
import com.ryzix.regain.ui.components.RegainBottomNav
import com.ryzix.regain.ui.theme.BackgroundDark
import com.ryzix.regain.ui.theme.CardDark
import com.ryzix.regain.ui.theme.RegainRed
import com.ryzix.regain.ui.theme.TextMuted
import com.ryzix.regain.ui.theme.TextPrimary
import com.ryzix.regain.ui.theme.TextSecondary
import com.ryzix.regain.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateHistory: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            RegainBottomNav(
                selected = BottomNavTab.SETTINGS,
                onHome = onNavigateHome,
                onHistory = onNavigateHistory,
                onSettings = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(24.dp))

            SectionLabel("NOTIFICATIONS")
            SettingsCard {
                ToggleRow(
                    title = "Persistent Notification",
                    subtitle = "Show countdown in notification bar",
                    checked = uiState.showNotification,
                    onCheckedChange = { vm.setShowNotification(it) }
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionLabel("LOCK BEHAVIOUR")
            SettingsCard {
                ToggleRow(
                    title = "Persist Lock on Restart",
                    subtitle = "Resume countdown after device reboot",
                    checked = uiState.persistLock,
                    onCheckedChange = { vm.setPersistLock(it) }
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionLabel("BATTERY & BACKGROUND")
            SettingsCard {
                ActionRow(
                    title = "Battery Optimization",
                    subtitle = "Exempt Regain for reliable background timers",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }
                )
                DividerLine()
                ActionRow(
                    title = "App Settings",
                    subtitle = "Manage permissions and autostart manually",
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionLabel("TROUBLESHOOTING")
            SettingsCard {
                InfoRow(
                    title = "Timer not running in background?",
                    body = "Go to Battery Optimization above and exempt Regain. On some OEM devices (Xiaomi, Huawei, Samsung), also enable Autostart in your system settings."
                )
                DividerLine()
                InfoRow(
                    title = "Lock not resuming after reboot?",
                    body = "Enable 'Persist Lock on Restart' above and grant boot permission if prompted by your device."
                )
            }
            Spacer(Modifier.height(16.dp))

            SectionLabel("ABOUT")
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Version", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    Text(uiState.appVersion, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                }
                DividerLine()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Developer", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    Text("Rohan Dora", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
        color = TextMuted,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardDark,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Column { content() }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = RegainRed,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = TextMuted.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Text("›", style = MaterialTheme.typography.headlineMedium, color = TextSecondary)
    }
}

@Composable
private fun InfoRow(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp), color = TextSecondary)
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(com.ryzix.regain.ui.theme.DividerColor.copy(alpha = 0.4f))
    )
}
