package com.ryzix.regain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ryzix.regain.ui.theme.BackgroundDark
import com.ryzix.regain.ui.theme.DividerColor
import com.ryzix.regain.ui.theme.RegainRed
import com.ryzix.regain.ui.theme.TextMuted
import com.ryzix.regain.ui.theme.TextSecondary

enum class BottomNavTab { HOME, HISTORY, SETTINGS }

@Composable
fun RegainBottomNav(
    selected: BottomNavTab,
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor.copy(alpha = 0.5f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavItem(
                label = "HOME",
                selected = selected == BottomNavTab.HOME,
                onClick = onHome
            )
            NavItem(
                label = "HISTORY",
                selected = selected == BottomNavTab.HISTORY,
                onClick = onHistory
            )
            NavItem(
                label = "SETTINGS",
                selected = selected == BottomNavTab.SETTINGS,
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun NavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            ),
            color = if (selected) RegainRed else TextSecondary
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(20.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(RegainRed)
            )
        }
    }
}
