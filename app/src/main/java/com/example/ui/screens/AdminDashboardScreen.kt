package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.*
import com.example.ui.components.CollectionStatusBadge
import com.example.ui.components.ReportStatusBadge
import com.example.ui.components.ScheduleStateBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.CleanTrackViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: CleanTrackViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.adminTab.collectAsState()
    val puroks by viewModel.puroks.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showCreateScheduleDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var reviewReportTarget by remember { mutableStateOf<ResidentReport?>(null) }
    var editStateScheduleTarget by remember { mutableStateOf<CollectionSchedule?>(null) }
    var showCsvExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CleanGreenPrimary,
                edgePadding = 12.dp,
                modifier = Modifier.testTag("admin_tab_row")
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { viewModel.setAdminTab(0) },
                    text = { Text("Overview") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { viewModel.setAdminTab(1) },
                    text = { Text("Schedules") },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { viewModel.setAdminTab(2) },
                    text = {
                        val openCount = reports.count { it.status != ReportStatus.RESOLVED && it.status != ReportStatus.REJECTED }
                        Text(if (openCount > 0) "Reports ($openCount)" else "Reports")
                    },
                    icon = { Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { viewModel.setAdminTab(3) },
                    text = { Text("18 Puroks") },
                    icon = { Icon(Icons.Default.LocationCity, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { viewModel.setAdminTab(4) },
                    text = { Text("Analytics") },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 5,
                    onClick = { viewModel.setAdminTab(5) },
                    text = { Text("Audit Trail") },
                    icon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateScheduleDialog = true },
                    containerColor = CleanGreenPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Publish Schedule") },
                    modifier = Modifier.testTag("fab_admin_create_schedule")
                )
            } else if (activeTab == 2 || activeTab == 0) {
                FloatingActionButton(
                    onClick = { showBroadcastDialog = true },
                    containerColor = CleanEmeraldSecondary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_admin_broadcast")
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = "Broadcast Notice")
                }
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
                0 -> AdminOverviewTab(
                    puroks = puroks,
                    schedules = schedules,
                    reports = reports,
                    announcements = announcements,
                    onNavigateToSchedules = { viewModel.setAdminTab(1) },
                    onNavigateToReports = { viewModel.setAdminTab(2) },
                    onReviewReport = { reviewReportTarget = it },
                    onOpenCsv = { showCsvExportDialog = true }
                )
                1 -> AdminSchedulesTab(
                    schedules = schedules,
                    onCreateSchedule = { showCreateScheduleDialog = true },
                    onEditState = { editStateScheduleTarget = it }
                )
                2 -> AdminReportsTab(
                    reports = reports,
                    onReviewReport = { reviewReportTarget = it }
                )
                3 -> AdminPuroksTab(puroks = puroks)
                4 -> AdminAnalyticsTab(
                    puroks = puroks,
                    schedules = schedules,
                    reports = reports,
                    onOpenCsv = { showCsvExportDialog = true }
                )
                5 -> AdminAuditLogsTab(auditLogs = auditLogs)
            }
        }
    }

    // Create Schedule Dialog
    if (showCreateScheduleDialog) {
        CreateScheduleDialog(
            puroks = puroks,
            onDismiss = { showCreateScheduleDialog = false },
            onSubmit = { purokId, purokName, date, start, end, worker, truck, instructions ->
                viewModel.createSchedule(purokId, purokName, date, start, end, worker, truck, instructions)
                showCreateScheduleDialog = false
            }
        )
    }

    // Broadcast Announcement Dialog
    if (showBroadcastDialog) {
        BroadcastAnnouncementDialog(
            puroks = puroks,
            onDismiss = { showBroadcastDialog = false },
            onSubmit = { title, content, targetPurok, isUrgent ->
                viewModel.publishAnnouncement(title, content, targetPurok, isUrgent)
                showBroadcastDialog = false
            }
        )
    }

    // Review & Resolve Report Dialog
    reviewReportTarget?.let { report ->
        ReviewReportDialog(
            report = report,
            onDismiss = { reviewReportTarget = null },
            onSubmit = { newStatus, remarks ->
                viewModel.resolveReport(report.id, newStatus, remarks)
                reviewReportTarget = null
            }
        )
    }

    // Edit Schedule State Dialog (Postpone / Cancel / Publish)
    editStateScheduleTarget?.let { schedule ->
        EditScheduleStateDialog(
            schedule = schedule,
            onDismiss = { editStateScheduleTarget = null },
            onSubmit = { newState, reason ->
                viewModel.updateScheduleState(schedule.id, newState, reason)
                editStateScheduleTarget = null
            }
        )
    }

    // CSV Export Preview Dialog
    if (showCsvExportDialog) {
        CsvExportPreviewDialog(
            csvContent = viewModel.getCsvExport(),
            onDismiss = { showCsvExportDialog = false },
            onCopyOrShare = {
                viewModel.showBanner("CSV Report data copied to clipboard!")
                showCsvExportDialog = false
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    puroks: List<Purok>,
    schedules: List<CollectionSchedule>,
    reports: List<ResidentReport>,
    announcements: List<Announcement>,
    onNavigateToSchedules: () -> Unit,
    onNavigateToReports: () -> Unit,
    onReviewReport: (ResidentReport) -> Unit,
    onOpenCsv: () -> Unit
) {
    val totalHouseholds = remember(puroks) { puroks.sumOf { it.householdCount } }
    val schedulesToday = schedules.size
    val inProgressCount = schedules.count { it.collectionStatus == CollectionStatus.IN_PROGRESS }
    val completedCount = schedules.count { it.collectionStatus == CollectionStatus.COMPLETED }
    val pendingCount = schedules.count { it.collectionStatus == CollectionStatus.PENDING }
    val unresolvedReports = reports.count { it.status != ReportStatus.RESOLVED && it.status != ReportStatus.REJECTED }

    val completionRate = if (schedulesToday > 0) ((completedCount.toFloat() / schedulesToday.toFloat()) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CleanGreenPrimary),
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
                        Column {
                            Text(
                                text = "BARANGAY OPERATIONS COMMAND",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA5D6A7),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "North Poblacion, Maramag",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onOpenCsv,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export CSV",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Puroks Covered", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text("${puroks.size} Designated", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Households", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text("$totalHouseholds Total", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // Operational Metrics Grid
        item {
            Text(
                text = "Today's Collection Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "In Progress",
                    value = "$inProgressCount",
                    subtitle = "Trucks Collecting",
                    color = StatusInProgressColor,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Completed",
                    value = "$completedCount",
                    subtitle = "$completionRate% of Target",
                    color = StatusCompletedColor,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pending",
                    value = "$pendingCount",
                    subtitle = "Scheduled",
                    color = StatusPendingColor,
                    icon = Icons.Default.HourglassEmpty,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Unresolved Complaints",
                    value = "$unresolvedReports",
                    subtitle = "Pending Review",
                    color = if (unresolvedReports > 0) Color(0xFFC62828) else StatusCompletedColor,
                    icon = Icons.Default.ReportProblem,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active Teams",
                    value = "3 Teams",
                    subtitle = "LG-01, LG-02, LG-03",
                    color = CleanGreenPrimary,
                    icon = Icons.Default.LocalShipping,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Schedules Quick List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Collection Schedules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToSchedules) {
                    Text("View All (${schedules.size})", fontSize = 12.sp)
                }
            }
        }

        items(schedules.take(3)) { schedule ->
            AdminScheduleItemCard(schedule = schedule, onEditState = {})
        }

        // Unresolved Resident Reports
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Resident Reports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToReports) {
                    Text("Manage (${reports.size})", fontSize = 12.sp)
                }
            }
        }

        items(reports.take(3)) { report ->
            AdminReportItemCard(report = report, onReview = { onReviewReport(report) })
        }
    }
}

@Composable
fun AdminSchedulesTab(
    schedules: List<CollectionSchedule>,
    onCreateSchedule: () -> Unit,
    onEditState: (CollectionSchedule) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Collection Schedule Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage schedules across all 18 Puroks of North Poblacion.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onCreateSchedule,
                    colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_new_schedule_top")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 12.sp)
                }
            }
        }

        items(schedules, key = { it.id }) { schedule ->
            AdminScheduleItemCard(
                schedule = schedule,
                onEditState = { onEditState(schedule) }
            )
        }
    }
}

@Composable
fun AdminReportsTab(
    reports: List<ResidentReport>,
    onReviewReport: (ResidentReport) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Resident Complaints & Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Review uncollected waste, missed schedules, and provide official barangay remarks.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(reports, key = { it.id }) { report ->
            AdminReportItemCard(
                report = report,
                onReview = { onReviewReport(report) }
            )
        }
    }
}

@Composable
fun AdminPuroksTab(puroks: List<Purok>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text(
                    text = "18 Puroks & Sub-Puroks Directory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Barangay North Poblacion, Maramag, Bukidnon Official Puroks",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(puroks, key = { it.id }) { purok ->
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = purok.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = CleanGreenPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CleanGreenContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "${purok.householdCount} Households",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CleanGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Coordinator: ${purok.coordinatorName} (${purok.contactNumber})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Regular Pickup: ${purok.primaryCollectionDay}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active Purok",
                        tint = StatusCompletedColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab(
    puroks: List<Purok>,
    schedules: List<CollectionSchedule>,
    reports: List<ResidentReport>,
    onOpenCsv: () -> Unit
) {
    val completedSchedules = schedules.count { it.collectionStatus == CollectionStatus.COMPLETED }
    val totalSchedules = schedules.size
    val completionPercentage = if (totalSchedules > 0) (completedSchedules * 100) / totalSchedules else 0

    val resolvedReports = reports.count { it.status == ReportStatus.RESOLVED }
    val totalReports = reports.size
    val resolutionPercentage = if (totalReports > 0) (resolvedReports * 100) / totalReports else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Operational Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Performance, completion percentages, and reports.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenCsv,
                    colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_export_csv_analytics")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV", fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Collection Completion Efficiency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { completionPercentage / 100f },
                        color = CleanGreenPrimary,
                        trackColor = CleanGreenContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$completedSchedules of $totalSchedules scheduled operations completed ($completionPercentage%)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Complaint Resolution Rate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { resolutionPercentage / 100f },
                        color = CleanEmeraldSecondary,
                        trackColor = Color(0xFFD7E8CD),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$resolvedReports of $totalReports resident reports resolved ($resolutionPercentage%)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Purok Coverage & Scheduled Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(puroks) { purok ->
            val purokSchedules = schedules.filter { it.purokName == purok.name }
            val statusSummary = if (purokSchedules.any { it.collectionStatus == CollectionStatus.IN_PROGRESS }) {
                "In Progress" to StatusInProgressColor
            } else if (purokSchedules.any { it.collectionStatus == CollectionStatus.COMPLETED }) {
                "Completed" to StatusCompletedColor
            } else if (purokSchedules.isNotEmpty()) {
                "Scheduled (${purokSchedules.first().plannedStartTime})" to CleanGreenPrimary
            } else {
                "Regular: ${purok.primaryCollectionDay}" to Color.Gray
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(purok.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(statusSummary.first, color = statusSummary.second, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogsTab(auditLogs: List<AuditLog>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text(
                    text = "System Audit Trail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Chronological immutable log of actions, schedule changes, and status transitions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(auditLogs, key = { it.id }) { log ->
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CleanGreenContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = log.action,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = CleanGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = log.timestamp,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = log.details,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Actor: ${log.actor}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
        }
    }
}

@Composable
fun AdminScheduleItemCard(
    schedule: CollectionSchedule,
    onEditState: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(schedule.purokName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    ScheduleStateBadge(state = schedule.scheduleState)
                }
                CollectionStatusBadge(status = schedule.collectionStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Date: ${schedule.collectionDate} • Window: ${schedule.plannedStartTime} - ${schedule.plannedEndTime}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Assigned: ${schedule.assignedWorkerName} (${schedule.assignedTruckPlate})",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!schedule.instructions.isBlank()) {
                Text(
                    text = "Instructions: ${schedule.instructions}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEditState) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change State", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AdminReportItemCard(
    report: ResidentReport,
    onReview: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("#${report.id}", fontWeight = FontWeight.Bold, color = CleanGreenPrimary, fontSize = 13.sp)
                    Text("• ${report.category.label}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                ReportStatusBadge(status = report.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(report.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Resident: ${report.residentName} (${report.contactNumber}) • ${report.purokName} - ${report.addressDetails}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!report.adminRemarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Remarks: ${report.adminRemarks}",
                        fontSize = 11.sp,
                        color = CleanGreenPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(report.submittedAt, fontSize = 10.sp, color = Color.Gray)
                Button(
                    onClick = onReview,
                    colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_review_report_${report.id}")
                ) {
                    Text("Review & Resolve", fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleDialog(
    puroks: List<Purok>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String, String, String) -> Unit
) {
    var selectedPurok by remember { mutableStateOf(puroks.firstOrNull()?.name ?: "Purok 1") }
    var selectedDate by remember { mutableStateOf("2026-10-15") }
    var startTime by remember { mutableStateOf("07:00 AM") }
    var endTime by remember { mutableStateOf("09:30 AM") }
    var workerName by remember { mutableStateOf("Juan Dela Cruz & Team A") }
    var truckPlate by remember { mutableStateOf("LG-MARAMAG-01 (Compactor)") }
    var instructions by remember { mutableStateOf("Biodegradable and residual solid waste pickup.") }

    var purokExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Collection Schedule") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Purok selector
                ExposedDropdownMenuBox(
                    expanded = purokExpanded,
                    onExpandedChange = { purokExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPurok,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Purok (18 Puroks)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = purokExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = purokExpanded,
                        onDismissRequest = { purokExpanded = false }
                    ) {
                        puroks.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} (${p.householdCount} hh)") },
                                onClick = {
                                    selectedPurok = p.name
                                    purokExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = workerName,
                    onValueChange = { workerName = it },
                    label = { Text("Assigned Team / Driver") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = truckPlate,
                    onValueChange = { truckPlate = it },
                    label = { Text("Truck Model / Plate") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Collection Instructions") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = puroks.firstOrNull { it.name == selectedPurok }
                    onSubmit(
                        p?.id ?: "p1",
                        selectedPurok,
                        selectedDate,
                        startTime,
                        endTime,
                        workerName,
                        truckPlate,
                        instructions
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary)
            ) {
                Text("Publish Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReviewReportDialog(
    report: ResidentReport,
    onDismiss: () -> Unit,
    onSubmit: (ReportStatus, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(report.status) }
    var remarks by remember { mutableStateOf(report.adminRemarks ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Complaint #${report.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Report by: ${report.residentName} (${report.purokName})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = report.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Update Resolution Status:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                listOf(
                    ReportStatus.UNDER_REVIEW,
                    ReportStatus.IN_PROGRESS,
                    ReportStatus.RESOLVED,
                    ReportStatus.REJECTED
                ).forEach { st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st }
                        )
                        Text(st.label, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Barangay Official Remarks / Action Taken") },
                    placeholder = { Text("e.g. Special collection dispatched on Oct 14.") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedStatus, remarks) },
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary)
            ) {
                Text("Save Decision")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditScheduleStateDialog(
    schedule: CollectionSchedule,
    onDismiss: () -> Unit,
    onSubmit: (ScheduleState, String) -> Unit
) {
    var selectedState by remember { mutableStateOf(schedule.scheduleState) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Schedule State: ${schedule.purokName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScheduleState.values().forEach { st ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedState == st, onClick = { selectedState = st })
                        Text(st.label, fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for state change (optional)") },
                    placeholder = { Text("e.g. Postponed due to road maintenance.") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedState, reason) },
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary)
            ) {
                Text("Update State")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastAnnouncementDialog(
    puroks: List<Purok>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetPurok by remember { mutableStateOf("All Puroks") }
    var isUrgent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broadcast Barangay Announcement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title") },
                    placeholder = { Text("e.g. Special Holiday Collection Advisory") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Notice Content") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mark as Urgent / Alert", fontSize = 12.sp)
                    Switch(checked = isUrgent, onCheckedChange = { isUrgent = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSubmit(title, content, targetPurok, isUrgent)
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary)
            ) {
                Text("Broadcast Notice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CsvExportPreviewDialog(
    csvContent: String,
    onDismiss: () -> Unit,
    onCopyOrShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = CleanGreenPrimary)
                Text("Operational CSV Export")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Generated tabular data for all 18 Puroks, collection schedules, and resident complaints:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Text(
                        text = csvContent,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                            .padding(10.dp)
                            .horizontalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCopyOrShare,
                colors = ButtonDefaults.buttonColors(containerColor = CleanGreenPrimary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
