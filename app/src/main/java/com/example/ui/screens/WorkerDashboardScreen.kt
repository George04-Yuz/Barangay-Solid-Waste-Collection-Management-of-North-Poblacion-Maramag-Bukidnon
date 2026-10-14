package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.CollectionSchedule
import com.example.data.model.CollectionStatus
import com.example.data.model.UserPersona
import com.example.ui.components.CollectionStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.CleanTrackViewModel

@Composable
fun WorkerDashboardScreen(
    viewModel: CleanTrackViewModel,
    modifier: Modifier = Modifier
) {
    val currentPersona by viewModel.currentPersona.collectAsState()
    val allSchedules by viewModel.schedules.collectAsState()
    val activeTab by viewModel.workerTab.collectAsState()

    // Dialog state
    var completeScheduleTarget by remember { mutableStateOf<CollectionSchedule?>(null) }
    var delayScheduleTarget by remember { mutableStateOf<CollectionSchedule?>(null) }

    // Schedules assigned to worker or driver
    val todaySchedules = allSchedules.filter {
        it.collectionStatus != CollectionStatus.COMPLETED
    }
    val completedSchedules = allSchedules.filter {
        it.collectionStatus == CollectionStatus.COMPLETED
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.setWorkerTab(0) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (todaySchedules.isNotEmpty()) {
                                    Badge(
                                        containerColor = CleanGreenPrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text("${todaySchedules.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Route, contentDescription = "Assigned Route")
                        }
                    },
                    label = { Text("Active Route") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanGreenPrimary,
                        selectedTextColor = CleanGreenPrimary,
                        indicatorColor = CleanGreenContainer
                    ),
                    modifier = Modifier.testTag("worker_nav_route")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.setWorkerTab(1) },
                    icon = { Icon(Icons.Default.History, contentDescription = "Completed Tasks") },
                    label = { Text("Completed Log") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanGreenPrimary,
                        selectedTextColor = CleanGreenPrimary,
                        indicatorColor = CleanGreenContainer
                    ),
                    modifier = Modifier.testTag("worker_nav_history")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CleanBackgroundLight)
        ) {
            // Worker Identification Banner
            WorkerHeaderCard(persona = currentPersona)

            when (activeTab) {
                0 -> ActiveRouteTab(
                    schedules = todaySchedules,
                    onStartCollection = { viewModel.startCollection(it.id) },
                    onCompleteClicked = { completeScheduleTarget = it },
                    onReportDelayClicked = { delayScheduleTarget = it }
                )
                1 -> CompletedHistoryTab(schedules = completedSchedules)
            }
        }
    }

    // Complete Collection Confirmation Dialog
    completeScheduleTarget?.let { schedule ->
        CompleteCollectionDialog(
            schedule = schedule,
            onDismiss = { completeScheduleTarget = null },
            onConfirm = { notes ->
                viewModel.completeCollection(schedule.id, notes)
                completeScheduleTarget = null
            }
        )
    }

    // Report Delay / Problem Dialog
    delayScheduleTarget?.let { schedule ->
        ReportDelayDialog(
            schedule = schedule,
            onDismiss = { delayScheduleTarget = null },
            onSubmit = { reason ->
                viewModel.reportDelay(schedule.id, reason)
                delayScheduleTarget = null
            }
        )
    }
}

@Composable
fun WorkerHeaderCard(persona: UserPersona) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CleanGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = CleanGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = persona.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Assigned Truck: ${persona.vehiclePlate.ifBlank { "LG-MARAMAG-01" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CleanGreenPrimary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusCompletedColor)
                    )
                    Text(
                        text = "On-Duty",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleanGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveRouteTab(
    schedules: List<CollectionSchedule>,
    onStartCollection: (CollectionSchedule) -> Unit,
    onCompleteClicked: (CollectionSchedule) -> Unit,
    onReportDelayClicked: (CollectionSchedule) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Collection Route",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${schedules.size} Assigned",
                    fontSize = 12.sp,
                    color = CleanGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (schedules.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = StatusCompletedColor,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "All Collections Completed!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Great job! All assigned purok routes have been serviced.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(schedules, key = { it.id }) { schedule ->
                WorkerScheduleTaskCard(
                    schedule = schedule,
                    onStart = { onStartCollection(schedule) },
                    onComplete = { onCompleteClicked(schedule) },
                    onReportDelay = { onReportDelayClicked(schedule) }
                )
            }
        }
    }
}

@Composable
fun CompletedHistoryTab(schedules: List<CollectionSchedule>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Completed Route Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (schedules.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No completed collections logged yet today.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(schedules, key = { it.id }) { schedule ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = schedule.purokName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = CleanGreenPrimary
                            )
                            CollectionStatusBadge(status = schedule.collectionStatus)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Planned Window: ${schedule.plannedStartTime} - ${schedule.plannedEndTime}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Actual Timing: Started ${schedule.startedAt ?: "N/A"} • Completed ${schedule.completedAt ?: "N/A"}",
                            fontSize = 12.sp,
                            color = StatusCompletedColor,
                            fontWeight = FontWeight.Medium
                        )

                        if (!schedule.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notes: ${schedule.notes}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerScheduleTaskCard(
    schedule: CollectionSchedule,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onReportDelay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = schedule.purokName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Schedule #${schedule.id}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                CollectionStatusBadge(status = schedule.collectionStatus)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CleanGreenContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = CleanGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${schedule.plannedStartTime} - ${schedule.plannedEndTime}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CleanGreenPrimary
                        )
                    }

                    Text(
                        text = schedule.collectionDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Instructions: ${schedule.instructions}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
            )

            if (schedule.startedAt != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = StatusInProgressColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Started at ${schedule.startedAt}",
                        fontSize = 12.sp,
                        color = StatusInProgressColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!schedule.delayReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Delay Reported: ${schedule.delayReason}",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (schedule.collectionStatus == CollectionStatus.PENDING) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_start_collection_${schedule.id}")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Collection", fontWeight = FontWeight.Bold)
                    }
                } else if (schedule.collectionStatus == CollectionStatus.IN_PROGRESS) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = CleanEmeraldSecondary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_complete_collection_${schedule.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Completed", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onReportDelay,
                    modifier = Modifier.testTag("btn_report_delay_${schedule.id}")
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Report Issue", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CompleteCollectionDialog(
    schedule: CollectionSchedule,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusCompletedColor
                )
                Text("Confirm Collection Completion")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Are you sure you want to mark ${schedule.purokName} collection as COMPLETED?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "This will notify residents of ${schedule.purokName} and record the official completion timestamp.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Completion Notes / Tonnage") },
                    placeholder = { Text("e.g. All streets cleared. Approx 2.1 tons collected.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_completion_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(if (notes.isNotBlank()) notes else "Route completed successfully.") },
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                modifier = Modifier.testTag("btn_confirm_complete")
            ) {
                Text("Confirm Completion")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReportDelayDialog(
    schedule: CollectionSchedule,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Heavy road traffic / road work") }
    var additionalNotes by remember { mutableStateOf("") }

    val presetReasons = listOf(
        "Truck mechanical breakdown / flat tire",
        "Road blockage / impassable street",
        "Inclement weather / heavy rain flooding",
        "High waste volume causing delay",
        "Disposal facility queue / MRF delay"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFE65100)
                )
                Text("Report Collection Delay / Problem")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Report operational impediment for ${schedule.purokName}:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                presetReasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Text(
                            text = reason,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = { Text("Additional Remarks") },
                    placeholder = { Text("e.g. Waiting for backup truck near highway junction...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_delay_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fullReason = if (additionalNotes.isNotBlank()) "$selectedReason: $additionalNotes" else selectedReason
                    onSubmit(fullReason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                modifier = Modifier.testTag("btn_confirm_delay")
            ) {
                Text("Log Delay")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
