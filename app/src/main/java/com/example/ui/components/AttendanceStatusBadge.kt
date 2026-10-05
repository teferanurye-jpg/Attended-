package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.AttendanceStatus
import com.example.ui.theme.EthioEmerald
import com.example.ui.theme.EthioGold
import com.example.ui.theme.StatusError

@Composable
fun AttendanceStatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, dotColor, text) = when (status) {
        AttendanceStatus.CLOCKED_IN -> Triple(
            EthioEmerald.copy(alpha = 0.12f),
            EthioEmerald,
            com.example.util.AppStrings.statusClockedIn
        )
        AttendanceStatus.ON_BREAK -> Triple(
            EthioGold.copy(alpha = 0.15f),
            EthioGold,
            com.example.util.AppStrings.statusOnBreak
        )
        AttendanceStatus.CLOCKED_OUT -> Triple(
            Color.Gray.copy(alpha = 0.12f),
            Color.Gray,
            com.example.util.AppStrings.statusClockedOut
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = dotColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
        )
    }
}
