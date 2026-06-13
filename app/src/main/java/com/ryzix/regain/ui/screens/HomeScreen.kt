package com.ryzix.regain.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryzix.regain.ui.components.RegainBottomNav
import com.ryzix.regain.ui.components.BottomNavTab
import com.ryzix.regain.ui.theme.BackgroundDark
import com.ryzix.regain.ui.theme.CardDark
import com.ryzix.regain.ui.theme.GreenActive
import com.ryzix.regain.ui.theme.RegainRed
import com.ryzix.regain.ui.theme.RegainRedContainer
import com.ryzix.regain.ui.theme.TextMuted
import com.ryzix.regain.ui.theme.TextPrimary
import com.ryzix.regain.ui.theme.TextSecondary
import com.ryzix.regain.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenDialer: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateSettings: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isLocked = uiState.lockState.isLocked

    var notifPermissionGranted by remember { mutableStateOf(true) }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notifPermissionGranted = it }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            RegainBottomNav(
                selected = BottomNavTab.HOME,
                onHome = {},
                onHistory = onNavigateHistory,
                onSettings = onNavigateSettings
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Regain.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    ),
                    color = TextPrimary
                )
                StatusPill(isLocked = isLocked)
            }

            Spacer(Modifier.height(20.dp))

            AnimatedContent(targetState = isLocked, label = "lock_content") { locked ->
                if (locked) {
                    LockedContent(
                        remainingMillis = uiState.remainingMillis,
                        onOpenDialer = onOpenDialer,
                        onStopLock = { vm.stopLockdown() }
                    )
                } else {
                    UnlockedContent(
                        hours = uiState.lockState.selectedHours,
                        minutes = uiState.lockState.selectedMinutes,
                        onIncrementHours = { vm.incrementHours() },
                        onDecrementHours = { vm.decrementHours() },
                        onIncrementMinutes = { vm.incrementMinutes() },
                        onDecrementMinutes = { vm.decrementMinutes() },
                        onOpenDialer = onOpenDialer,
                        onStartLock = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                (context as? Activity)?.startLockTask()
                            }
                            vm.startLockdown()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(isLocked: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = if (isLocked) RegainRed else Color(0xFF1E3A1E),
        animationSpec = tween(300),
        label = "pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isLocked) TextPrimary else GreenActive,
        animationSpec = tween(300),
        label = "pill_text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isLocked) RegainRed else GreenActive,
        animationSpec = tween(300),
        label = "pill_border"
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isLocked) "DEVICE LOCKED" else "Unlocked",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UnlockedContent(
    hours: Int,
    minutes: Int,
    onIncrementHours: () -> Unit,
    onDecrementHours: () -> Unit,
    onIncrementMinutes: () -> Unit,
    onDecrementMinutes: () -> Unit,
    onOpenDialer: () -> Unit,
    onStartLock: () -> Unit
) {
    Column {
        DialerCard(onOpenDialer = onOpenDialer, isLocked = false)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "SET LOCK TIME",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
            color = TextMuted
        )
        Spacer(Modifier.height(10.dp))
        TimePickerCard(
            hours = hours,
            minutes = minutes,
            onIncrementHours = onIncrementHours,
            onDecrementHours = onDecrementHours,
            onIncrementMinutes = onIncrementMinutes,
            onDecrementMinutes = onDecrementMinutes
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Developed By Rohan Dora",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onStartLock,
            enabled = hours > 0 || minutes > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RegainRed,
                disabledContainerColor = RegainRed.copy(alpha = 0.4f)
            )
        ) {
            Text(
                text = "START LOCK DOWN",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    fontSize = 15.sp
                ),
                color = TextPrimary
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LockedContent(
    remainingMillis: Long,
    onOpenDialer: () -> Unit,
    onStopLock: () -> Unit
) {
    Column {
        CountdownCard(remainingMillis = remainingMillis)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "ALLOWED ACTIONS",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
            color = TextMuted
        )
        Spacer(Modifier.height(10.dp))
        DialerCard(onOpenDialer = onOpenDialer, isLocked = true)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun CountdownCard(remainingMillis: Long) {
    val h = remainingMillis / 3_600_000
    val m = (remainingMillis % 3_600_000) / 60_000
    val s = (remainingMillis % 60_000) / 1_000
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF2A0A0E))
            .padding(vertical = 28.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TIME REMAINING",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp,
                    fontSize = 11.sp
                ),
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "%02d:%02d:%02d".format(h, m, s),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 52.sp,
                    letterSpacing = 2.sp
                ),
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun DialerCard(onOpenDialer: () -> Unit, isLocked: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenDialer() },
        color = CardDark,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Regain Dialer",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isLocked) "In-App Dialer & Contacts (Tap here)"
                else "Access Contacts & Calls anytime",
                style = MaterialTheme.typography.bodyMedium,
                color = GreenActive
            )
        }
    }
}

@Composable
private fun TimePickerCard(
    hours: Int,
    minutes: Int,
    onIncrementHours: () -> Unit,
    onDecrementHours: () -> Unit,
    onIncrementMinutes: () -> Unit,
    onDecrementMinutes: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardDark,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeUnit(
                value = hours,
                label = "HRS",
                onIncrement = onIncrementHours,
                onDecrement = onDecrementHours,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            TimeUnit(
                value = minutes,
                label = "MINS",
                onIncrement = onIncrementMinutes,
                onDecrement = onDecrementMinutes,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimeUnit(
    value: Int,
    label: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepButton(label = "+", onClick = onIncrement)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 52.sp
            ),
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        StepButton(label = "-", onClick = onDecrement)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
            color = TextSecondary
        )
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Light),
            color = TextPrimary
        )
    }
}
