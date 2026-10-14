package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InAppNotification
import com.example.data.model.NotificationType
import com.example.ui.theme.CleanGreenPrimary
import com.example.ui.theme.StatusCompletedColor
import com.example.ui.theme.StatusInProgressColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSheet(
    notifications: List<InAppNotification>,
    onClose: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        modifier = modifier.testTag("notification_sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = CleanGreenPrimary
                    )
                    Text(
                        text = "Notifications Inbox",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (notifications.any { !it.isRead }) {
                    TextButton(
                        onClick = onMarkAllAsRead,
                        modifier = Modifier.testTag("mark_all_read_btn")
                    ) {
                        Text("Mark all read", fontSize = 12.sp)
                    }
                }
            }

            Text(
                text = "Real-time household notices, collection reminders, and complaint updates.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider()

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No notifications yet", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationItemCard(
                            notification = notif,
                            onClick = { onMarkAsRead(notif.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: InAppNotification,
    onClick: () -> Unit
) {
    val (icon, iconTint, iconBg) = when (notification.type) {
        NotificationType.COLLECTION_STARTED -> Triple(
            Icons.Default.PlayArrow,
            StatusInProgressColor,
            StatusInProgressColor.copy(alpha = 0.15f)
        )
        NotificationType.COLLECTION_COMPLETED -> Triple(
            Icons.Default.CheckCircle,
            StatusCompletedColor,
            StatusCompletedColor.copy(alpha = 0.15f)
        )
        NotificationType.SCHEDULE_UPDATE -> Triple(
            Icons.Default.CalendarMonth,
            CleanGreenPrimary,
            CleanGreenPrimary.copy(alpha = 0.15f)
        )
        NotificationType.COMPLAINT_UPDATE -> Triple(
            Icons.Default.Feedback,
            Color(0xFF512DA8),
            Color(0xFFEDE7F6)
        )
        NotificationType.ANNOUNCEMENT -> Triple(
            Icons.Default.Campaign,
            Color(0xFFE65100),
            Color(0xFFFFF3E0)
        )
        NotificationType.ALERT -> Triple(
            Icons.Default.Warning,
            Color(0xFFC62828),
            Color(0xFFFFEBEE)
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (notification.isRead) 0.dp else 2.dp,
        shadowElevation = if (notification.isRead) 0.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CleanGreenPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Target: ${notification.targetPurok}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = notification.timestamp,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
