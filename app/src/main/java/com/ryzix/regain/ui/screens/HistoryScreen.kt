package com.ryzix.regain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryzix.regain.model.LockSession
import com.ryzix.regain.ui.components.BottomNavTab
import com.ryzix.regain.ui.components.RegainBottomNav
import com.ryzix.regain.ui.theme.BackgroundDark
import com.ryzix.regain.ui.theme.CardDark
import com.ryzix.regain.ui.theme.DividerColor
import com.ryzix.regain.ui.theme.GreenActive
import com.ryzix.regain.ui.theme.RegainRed
import com.ryzix.regain.ui.theme.TextMuted
import com.ryzix.regain.ui.theme.TextPrimary
import com.ryzix.regain.ui.theme.TextSecondary
import com.ryzix.regain.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateSettings: () -> Unit,
    vm: HistoryViewModel = viewModel()
) {
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History", color = TextPrimary) },
            text = { Text("Delete all lockdown session history?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAll()
                    showClearDialog = false
                }) { Text("Clear", color = RegainRed) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            titleContentColor = TextPrimary
        )
    }

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            RegainBottomNav(
                selected = BottomNavTab.HISTORY,
                onHome = onNavigateHome,
                onHistory = {},
                onSettings = onNavigateSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.weight(1f))
                if (sessions.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear all", tint = TextSecondary)
                    }
                }
            }

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No lockdown sessions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sessions, key = { it.id }) { session ->
                        SessionRow(session = session)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: LockSession) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
    val startDate = dateFormat.format(Date(session.startTime))
    val h = session.durationMillis / 3_600_000
    val m = (session.durationMillis % 3_600_000) / 60_000
    val s = (session.durationMillis % 60_000) / 1_000
    val durationStr = when {
        h > 0 -> "%dh %02dm %02ds".format(h, m, s)
        m > 0 -> "%dm %02ds".format(m, s)
        else -> "${s}s"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = CardDark,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = startDate,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Duration: $durationStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        if (session.completedNaturally) Color(0xFF1A3A1A) else Color(0xFF2A1A1A),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (session.completedNaturally) "Completed" else "Stopped",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    color = if (session.completedNaturally) GreenActive else RegainRed
                )
            }
        }
    }
}
