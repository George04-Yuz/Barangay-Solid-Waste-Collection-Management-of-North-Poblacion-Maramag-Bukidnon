package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import com.example.data.model.CollectionStatus
import com.example.data.model.ReportStatus
import com.example.data.model.ScheduleState
import com.example.ui.theme.*

@Composable
fun CollectionStatusBadge(
    status: CollectionStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        CollectionStatus.PENDING -> Triple(
            StatusPendingColor.copy(alpha = 0.15f),
            StatusPendingColor,
            Icons.Default.HourglassEmpty
        )
        CollectionStatus.IN_PROGRESS -> Triple(
            StatusInProgressColor.copy(alpha = 0.15f),
            StatusInProgressColor,
            Icons.Default.PlayArrow
        )
        CollectionStatus.COMPLETED -> Triple(
            StatusCompletedColor.copy(alpha = 0.15f),
            StatusCompletedColor,
            Icons.Default.CheckCircle
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = status.label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScheduleStateBadge(
    state: ScheduleState,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (state) {
        ScheduleState.PUBLISHED -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        ScheduleState.DRAFT -> Color(0xFFEEEEEE) to Color(0xFF616161)
        ScheduleState.POSTPONED -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        ScheduleState.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = state.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ReportStatusBadge(
    status: ReportStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        ReportStatus.SUBMITTED -> Color(0xFFEDE7F6) to Color(0xFF512DA8)
        ReportStatus.UNDER_REVIEW -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        ReportStatus.IN_PROGRESS -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        ReportStatus.RESOLVED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        ReportStatus.REJECTED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
