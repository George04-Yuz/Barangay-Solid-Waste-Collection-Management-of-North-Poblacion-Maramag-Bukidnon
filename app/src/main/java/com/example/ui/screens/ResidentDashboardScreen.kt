package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.CollectionStatusBadge
import com.example.ui.components.ReportStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.CleanTrackViewModel

@Composable
fun ResidentDashboardScreen(
    viewModel: CleanTrackViewModel,
    modifier: Modifier = Modifier
) {
    val currentPersona by viewModel.currentPersona.collectAsState()
    val residentPurok by viewModel.residentPurok.collectAsState()
    val residentSchedules by viewModel.residentSchedules.collectAsState()
    val allReports by viewModel.reports.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val activeTab by viewModel.residentTab.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }

    // Filter reports for this resident / purok
    val residentReports = allReports.filter { it.purokName.equals(residentPurok, ignoreCase = true) }

    // Next scheduled pickup (find the active or earliest upcoming)
    val nextSchedule = residentSchedules.firstOrNull { it.scheduleState == ScheduleState.PUBLISHED }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.setResidentTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home & Pickup") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanGreenPrimary,
                        selectedTextColor = CleanGreenPrimary,
                        indicatorColor = CleanGreenContainer
                    ),
                    modifier = Modifier.testTag("resident_nav_home")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.setResidentTab(1) },
                    icon = { Icon(Icons.Default.ReportProblem, contentDescription = "My Reports") },
                    label = { Text("My Reports") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanGreenPrimary,
                        selectedTextColor = CleanGreenPrimary,
                        indicatorColor = CleanGreenContainer
                    ),
                    modifier = Modifier.testTag("resident_nav_reports")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.setResidentTab(2) },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Notices") },
                    label = { Text("Barangay News") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanGreenPrimary,
                        selectedTextColor = CleanGreenPrimary,
                        indicatorColor = CleanGreenContainer
                    ),
                    modifier = Modifier.testTag("resident_nav_news")
                )
            }
        },
        floatingActionButton = {
            if (activeTab != 1) {
                ExtendedFloatingActionButton(
                    onClick = { showReportDialog = true },
                    containerColor = Color(0xFFC62828),
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.WarningAmber, contentDescription = null) },
                    text = { Text("Report Missed Waste", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_report_uncollected")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CleanBackgroundLight)
        ) {
            when (activeTab) {
                0 -> ResidentHomeTab(
                    persona = currentPersona,
                    purok = residentPurok,
                    nextSchedule = nextSchedule,
                    allPurokSchedules = residentSchedules,
                    recentAnnouncements = announcements.take(2),
                    onOpenReportForm = { showReportDialog = true }
                )
                1 -> ResidentReportsTab(
                    reports = residentReports,
                    onOpenReportForm = { showReportDialog = true }
                )
                2 -> ResidentAnnouncementsTab(
                    announcements = announcements,
                    purok = residentPurok
                )
            }
        }
    }

    if (showReportDialog) {
        SubmitReportDialog(
            residentPurok = residentPurok,
            residentName = currentPersona.name,
            onDismiss = { showReportDialog = false },
            onSubmit = { category, description, address, hasPhoto, photoDesc ->
                viewModel.submitReport(
                    category = category,
                    description = description,
                    addressDetails = address,
                    hasPhoto = hasPhoto,
                    photoDescription = photoDesc
                )
                showReportDialog = false
            }
        )
    }
}

@Composable
fun ResidentHomeTab(
    persona: UserPersona,
    purok: String,
    nextSchedule: CollectionSchedule?,
    allPurokSchedules: List<CollectionSchedule>,
    recentAnnouncements: List<Announcement>,
    onOpenReportForm: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome & Household Profile Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CleanGreenPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MABUHAY, ${persona.name.split(" ")[0].uppercase()}",
                                color = Color(0xFFA5D6A7),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = purok,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (persona.householdAddress.isNotBlank()) persona.householdAddress else "Household Member, $purok",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = persona.contactNumber,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Live Next Collection Status Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = CleanGreenPrimary
                            )
                            Text(
                                text = "Next Scheduled Pickup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        if (nextSchedule != null) {
                            CollectionStatusBadge(status = nextSchedule.collectionStatus)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (nextSchedule != null) {
                        // Date & Time Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CleanGreenContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Collection Date",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = nextSchedule.collectionDate,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CleanGreenPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Planned Window",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${nextSchedule.plannedStartTime} - ${nextSchedule.plannedEndTime}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Description
                        val statusNote = when (nextSchedule.collectionStatus) {
                            CollectionStatus.PENDING -> "⏳ The collection truck is scheduled and will arrive in $purok within the planned window."
                            CollectionStatus.IN_PROGRESS -> "🚛 The collection truck (${nextSchedule.assignedTruckPlate}) is currently collecting garbage in $purok! Please bring out your segregated bins."
                            CollectionStatus.COMPLETED -> "✅ Today's collection in $purok has been completed. Completed at ${nextSchedule.completedAt ?: "scheduled time"}."
                        }

                        Text(
                            text = statusNote,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                            fontWeight = if (nextSchedule.collectionStatus == CollectionStatus.IN_PROGRESS) FontWeight.SemiBold else FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Assigned Team: ${nextSchedule.assignedWorkerName} (${nextSchedule.assignedTruckPlate})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!nextSchedule.instructions.isBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instructions: ${nextSchedule.instructions}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No active collection scheduled for $purok today.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Check upcoming collection schedules below or contact the Barangay Hall.",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Waste Segregation Reminder Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CleanEmeraldSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Recycling,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RA 9003 Waste Segregation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CleanGreenPrimary
                        )
                        Text(
                            text = "Separate Biodegradable (Green bin), Residual (Black bin), and Recyclables. Non-segregated waste will not be picked up.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Recent Barangay Announcements
        if (recentAnnouncements.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Barangay Announcements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(recentAnnouncements) { ann ->
                AnnouncementItemCard(announcement = ann)
            }
        }

        // All Schedules for this Purok
        item {
            Text(
                text = "Collection History & Schedule for $purok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (allPurokSchedules.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No collection history logged yet for $purok.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(allPurokSchedules) { sch ->
                PurokScheduleCard(schedule = sch)
            }
        }
    }
}

@Composable
fun ResidentReportsTab(
    reports: List<ResidentReport>,
    onOpenReportForm: () -> Unit
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
                Column {
                    Text(
                        text = "My Reports & Complaints",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track status of missed collections and waste concerns.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenReportForm,
                    colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_file_new_report")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File Report", fontSize = 12.sp)
                }
            }
        }

        if (reports.isEmpty()) {
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
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = StatusCompletedColor,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Active Complaints or Reports",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your household has not filed any uncollected waste reports. Tap 'File Report' if your bins were missed.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(reports, key = { it.id }) { report ->
                ResidentReportItemCard(report = report)
            }
        }
    }
}

@Composable
fun ResidentAnnouncementsTab(
    announcements: List<Announcement>,
    purok: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Barangay Public Notices",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Official updates from North Poblacion Solid Waste Management Committee",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(announcements) { ann ->
            AnnouncementItemCard(announcement = ann)
        }
    }
}

@Composable
fun ResidentReportItemCard(report: ResidentReport) {
    Card(
        shape = RoundedCornerShape(14.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${report.id}",
                        fontWeight = FontWeight.Bold,
                        color = CleanGreenPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• ${report.category.label}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                ReportStatusBadge(status = report.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${report.addressDetails} (${report.purokName})",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            if (report.hasPhoto) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = CleanGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Photo Evidence Attached: ${report.photoDescription ?: "Waste pile photo"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Official Administrator Remarks
            if (!report.adminRemarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = CleanGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Barangay Official Response",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CleanGreenPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = report.adminRemarks,
                            fontSize = 12.sp,
                            color = Color(0xFF1B5E20),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Submitted: ${report.submittedAt}${if (report.resolvedAt != null) " • Resolved: ${report.resolvedAt}" else ""}",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PurokScheduleCard(schedule: CollectionSchedule) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${schedule.collectionDate} (${schedule.plannedStartTime} - ${schedule.plannedEndTime})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Truck: ${schedule.assignedTruckPlate} • Team: ${schedule.assignedWorkerName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (schedule.completedAt != null) {
                    Text(
                        text = "Completed at: ${schedule.completedAt}",
                        fontSize = 11.sp,
                        color = StatusCompletedColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            CollectionStatusBadge(status = schedule.collectionStatus)
        }
    }
}

@Composable
fun AnnouncementItemCard(announcement: Announcement) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (announcement.isUrgent) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (announcement.isUrgent) Icons.Default.Warning else Icons.Default.Campaign,
                        contentDescription = null,
                        tint = if (announcement.isUrgent) Color(0xFFE65100) else CleanGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = announcement.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (announcement.isUrgent) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CleanGreenPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = announcement.targetPurok,
                        fontSize = 10.sp,
                        color = CleanGreenPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Posted by: ${announcement.postedBy}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    text = announcement.datePosted,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitReportDialog(
    residentPurok: String,
    residentName: String,
    onDismiss: () -> Unit,
    onSubmit: (ReportCategory, String, String, Boolean, String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ReportCategory.UNCOLLECTED_WASTE) }
    var description by remember { mutableStateOf("") }
    var addressDetails by remember { mutableStateOf("") }
    var attachPhoto by remember { mutableStateOf(false) }
    var photoDescription by remember { mutableStateOf("Photo of overflowing bin attached") }

    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = Color(0xFFC62828)
                )
                Text("Report Uncollected Waste")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Submitting for: $residentPurok (Reporting Resident: $residentName)",
                    fontSize = 12.sp,
                    color = CleanGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        ReportCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.label) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = addressDetails,
                    onValueChange = { addressDetails = it },
                    label = { Text("Exact Location / Street Address") },
                    placeholder = { Text("e.g. Near Basketball court, Daisy St.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_report_address")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description of Issue") },
                    placeholder = { Text("e.g. 4 garbage bags not picked up during morning schedule...") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_report_desc")
                )

                // Photo Evidence Attachment Simulation
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (attachPhoto) CleanGreenPrimary else Color.Gray
                            )
                            Column {
                                Text(
                                    text = if (attachPhoto) "Photo attached" else "Attach Photo Evidence",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (attachPhoto) "waste_evidence_photo.jpg" else "Optional photo upload",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Switch(
                            checked = attachPhoto,
                            onCheckedChange = { attachPhoto = it },
                            modifier = Modifier.testTag("switch_attach_photo")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        onSubmit(
                            selectedCategory,
                            description,
                            if (addressDetails.isNotBlank()) addressDetails else "Household in $residentPurok",
                            attachPhoto,
                            if (attachPhoto) photoDescription else null
                        )
                    }
                },
                enabled = description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                modifier = Modifier.testTag("btn_submit_report_confirm")
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
