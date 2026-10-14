package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NorthPoblacionPuroks
import com.example.data.model.UserPersona
import com.example.data.model.UserRole
import com.example.ui.theme.CleanGreenPrimary
import com.example.ui.theme.CleanGreenOnPrimary

@Composable
fun PersonaSwitcherBar(
    currentPersona: UserPersona,
    selectedPurok: String,
    unreadNotificationCount: Int,
    onPersonaSelected: (UserPersona) -> Unit,
    onPurokSelected: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAbout: () -> Unit,
    availablePersonas: List<UserPersona>,
    modifier: Modifier = Modifier
) {
    var showPurokMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CleanGreenPrimary,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Top Row: App Title, Role Pills, Notification & Info Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CleanTrack",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "North Poblacion, Maramag",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Action Icons (Notifications & About)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Notification Button with Badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("notification_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadNotificationCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications Inbox",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // About Project Button
                    IconButton(
                        onClick = onOpenAbout,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Capstone Project",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Persona Switcher Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                availablePersonas.forEach { persona ->
                    val isSelected = currentPersona.role == persona.role
                    val roleLabel = when (persona.role) {
                        UserRole.ADMIN -> "Admin"
                        UserRole.WORKER -> "Worker"
                        UserRole.RESIDENT -> "Resident"
                    }
                    val icon = when (persona.role) {
                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                        UserRole.WORKER -> Icons.Default.LocalShipping
                        UserRole.RESIDENT -> Icons.Default.Home
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { onPersonaSelected(persona) }
                            .padding(vertical = 6.dp, horizontal = 6.dp)
                            .testTag("role_pill_${roleLabel.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) CleanGreenPrimary else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = roleLabel,
                                color = if (isSelected) CleanGreenPrimary else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // If Resident role is active, display horizontal quick Purok switcher for all 18 puroks
            AnimatedVisibility(visible = currentPersona.role == UserRole.RESIDENT) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Assigned Purok (${NorthPoblacionPuroks.ALL_18_PUROKS.size} Puroks):",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Active: $selectedPurok",
                            color = Color(0xFFA5D6A7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NorthPoblacionPuroks.ALL_18_PUROKS.forEach { purok ->
                            val isPurokActive = selectedPurok == purok.name
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isPurokActive) Color(0xFFA5D6A7) else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .clickable { onPurokSelected(purok.name) }
                                    .testTag("purok_chip_${purok.id}")
                            ) {
                                Text(
                                    text = purok.name,
                                    color = if (isPurokActive) CleanGreenPrimary else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isPurokActive) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
