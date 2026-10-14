package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class CleanTrackRepository {

    private val _puroks = MutableStateFlow<List<Purok>>(NorthPoblacionPuroks.ALL_18_PUROKS)
    val puroks: StateFlow<List<Purok>> = _puroks.asStateFlow()

    private val _schedules = MutableStateFlow<List<CollectionSchedule>>(getInitialSchedules())
    val schedules: StateFlow<List<CollectionSchedule>> = _schedules.asStateFlow()

    private val _reports = MutableStateFlow<List<ResidentReport>>(getInitialReports())
    val reports: StateFlow<List<ResidentReport>> = _reports.asStateFlow()

    private val _notifications = MutableStateFlow<List<InAppNotification>>(getInitialNotifications())
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(getInitialAnnouncements())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(getInitialAuditLogs())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Manila")
        return sdf.format(Date())
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Manila")
        return sdf.format(Date())
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Manila")
        return sdf.format(Date())
    }

    // --- Schedule Operations ---

    fun createSchedule(
        purokId: String,
        purokName: String,
        date: String,
        startTime: String,
        endTime: String,
        workerName: String,
        truckPlate: String,
        instructions: String,
        actorName: String = "Barangay Admin"
    ) {
        val newSchedule = CollectionSchedule(
            id = "SCH-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
            purokId = purokId,
            purokName = purokName,
            collectionDate = date,
            plannedStartTime = startTime,
            plannedEndTime = endTime,
            scheduleState = ScheduleState.PUBLISHED,
            collectionStatus = CollectionStatus.PENDING,
            assignedWorkerName = workerName,
            assignedTruckPlate = truckPlate,
            instructions = instructions
        )

        _schedules.value = listOf(newSchedule) + _schedules.value

        // Add Notification
        addNotification(
            title = "New Schedule Published: $purokName",
            message = "Garbage collection scheduled on $date from $startTime to $endTime by $workerName ($truckPlate).",
            type = NotificationType.SCHEDULE_UPDATE,
            targetPurok = purokName
        )

        // Log Audit
        addAuditLog(
            actor = actorName,
            action = "CREATE_SCHEDULE",
            details = "Published collection schedule for $purokName on $date ($startTime-$endTime)"
        )
    }

    fun updateScheduleStatus(
        scheduleId: String,
        newStatus: CollectionStatus,
        workerName: String,
        notes: String? = null,
        delayReason: String? = null
    ) {
        val currentList = _schedules.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == scheduleId }
        if (index != -1) {
            val item = currentList[index]
            val nowTime = getCurrentTime()
            val startedAt = if (newStatus == CollectionStatus.IN_PROGRESS && item.startedAt == null) nowTime else item.startedAt
            val completedAt = if (newStatus == CollectionStatus.COMPLETED) nowTime else item.completedAt

            val updated = item.copy(
                collectionStatus = newStatus,
                startedAt = startedAt,
                completedAt = completedAt,
                notes = if (!notes.isNullOrBlank()) notes else item.notes,
                delayReason = if (!delayReason.isNullOrBlank()) delayReason else item.delayReason
            )
            currentList[index] = updated
            _schedules.value = currentList

            // Trigger notification
            when (newStatus) {
                CollectionStatus.IN_PROGRESS -> {
                    addNotification(
                        title = "Collection Started: ${item.purokName}",
                        message = "Truck ${item.assignedTruckPlate} has started solid waste collection in ${item.purokName}.",
                        type = NotificationType.COLLECTION_STARTED,
                        targetPurok = item.purokName
                    )
                }
                CollectionStatus.COMPLETED -> {
                    addNotification(
                        title = "Collection Completed: ${item.purokName}",
                        message = "Solid waste collection in ${item.purokName} was successfully completed at $nowTime.",
                        type = NotificationType.COLLECTION_COMPLETED,
                        targetPurok = item.purokName
                    )
                }
                else -> {}
            }

            addAuditLog(
                actor = workerName,
                action = "UPDATE_STATUS_${newStatus.name}",
                details = "Schedule ${item.id} (${item.purokName}) status set to ${newStatus.label} by $workerName. Notes: ${notes ?: "None"}"
            )
        }
    }

    fun updateScheduleState(
        scheduleId: String,
        newState: ScheduleState,
        reason: String? = null,
        actorName: String = "Barangay Admin"
    ) {
        val currentList = _schedules.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == scheduleId }
        if (index != -1) {
            val item = currentList[index]
            val updated = item.copy(
                scheduleState = newState,
                notes = if (!reason.isNullOrBlank()) "State changed to ${newState.label}: $reason" else item.notes
            )
            currentList[index] = updated
            _schedules.value = currentList

            addNotification(
                title = "Schedule ${newState.label}: ${item.purokName}",
                message = "Collection schedule for ${item.purokName} has been ${newState.label.lowercase()}. ${reason ?: ""}",
                type = NotificationType.SCHEDULE_UPDATE,
                targetPurok = item.purokName
            )

            addAuditLog(
                actor = actorName,
                action = "UPDATE_STATE_${newState.name}",
                details = "Schedule ${item.id} (${item.purokName}) changed to ${newState.label}. Reason: ${reason ?: "N/A"}"
            )
        }
    }

    // --- Resident Report Operations ---

    fun submitResidentReport(
        residentName: String,
        contactNumber: String,
        purokName: String,
        category: ReportCategory,
        description: String,
        addressDetails: String,
        hasPhoto: Boolean = false,
        photoDescription: String? = null
    ): ResidentReport {
        val report = ResidentReport(
            id = "REP-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
            residentName = residentName,
            contactNumber = contactNumber,
            purokName = purokName,
            category = category,
            description = description,
            addressDetails = addressDetails,
            status = ReportStatus.SUBMITTED,
            hasPhoto = hasPhoto,
            photoDescription = photoDescription,
            submittedAt = getCurrentTimestamp()
        )

        _reports.value = listOf(report) + _reports.value

        addNotification(
            title = "Report Submitted: ${category.label}",
            message = "Your report for $purokName has been received by Barangay North Poblacion and is queued for review.",
            type = NotificationType.COMPLAINT_UPDATE,
            targetPurok = purokName
        )

        addAuditLog(
            actor = residentName,
            action = "SUBMIT_REPORT",
            details = "Submitted ${category.label} report (${report.id}) for $purokName"
        )

        return report
    }

    fun updateReportStatus(
        reportId: String,
        newStatus: ReportStatus,
        adminRemarks: String,
        adminName: String = "Barangay Official"
    ) {
        val currentList = _reports.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == reportId }
        if (index != -1) {
            val item = currentList[index]
            val resolvedAt = if (newStatus == ReportStatus.RESOLVED) getCurrentTimestamp() else item.resolvedAt
            val updated = item.copy(
                status = newStatus,
                adminRemarks = adminRemarks,
                resolvedAt = resolvedAt
            )
            currentList[index] = updated
            _reports.value = currentList

            addNotification(
                title = "Report ${newStatus.label}: ${item.category.label}",
                message = "Report #${item.id} is now ${newStatus.label}. Official remarks: $adminRemarks",
                type = NotificationType.COMPLAINT_UPDATE,
                targetPurok = item.purokName
            )

            addAuditLog(
                actor = adminName,
                action = "UPDATE_REPORT_STATUS",
                details = "Report ${item.id} status updated to ${newStatus.label} by $adminName. Remarks: $adminRemarks"
            )
        }
    }

    // --- Announcements & Communications ---

    fun postAnnouncement(
        title: String,
        content: String,
        targetPurok: String = "All Puroks",
        isUrgent: Boolean = false,
        author: String = "Barangay Solid Waste Committee"
    ) {
        val announcement = Announcement(
            id = "ANN-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
            title = title,
            content = content,
            targetPurok = targetPurok,
            isUrgent = isUrgent,
            postedBy = author,
            datePosted = getCurrentTimestamp()
        )

        _announcements.value = listOf(announcement) + _announcements.value

        addNotification(
            title = if (isUrgent) "🚨 URGENT: $title" else "📢 $title",
            message = content,
            type = NotificationType.ANNOUNCEMENT,
            targetPurok = targetPurok
        )

        addAuditLog(
            actor = author,
            action = "POST_ANNOUNCEMENT",
            details = "Posted announcement '$title' targeting $targetPurok (Urgent: $isUrgent)"
        )
    }

    fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        targetPurok: String = "All"
    ) {
        val notification = InAppNotification(
            id = "NOTIF-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
            title = title,
            message = message,
            type = type,
            targetPurok = targetPurok,
            timestamp = getCurrentTimestamp(),
            isRead = false
        )
        _notifications.value = listOf(notification) + _notifications.value
    }

    fun markNotificationRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    private fun addAuditLog(actor: String, action: String, details: String) {
        val log = AuditLog(
            id = "AUD-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
            actor = actor,
            action = action,
            details = details,
            timestamp = getCurrentTimestamp()
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    // --- Analytics & Export Helper ---

    fun generateCsvExport(): String {
        val sb = StringBuilder()
        sb.append("Schedule ID,Purok,Date,Planned Window,State,Status,Assigned Worker,Truck Plate,Started At,Completed At,Notes\n")
        _schedules.value.forEach { s ->
            sb.append("\"${s.id}\",\"${s.purokName}\",\"${s.collectionDate}\",\"${s.plannedStartTime} - ${s.plannedEndTime}\",\"${s.scheduleState.label}\",\"${s.collectionStatus.label}\",\"${s.assignedWorkerName}\",\"${s.assignedTruckPlate}\",\"${s.startedAt ?: ""}\",\"${s.completedAt ?: ""}\",\"${s.notes ?: ""}\"\n")
        }
        sb.append("\n\nResident Reports Export\n")
        sb.append("Report ID,Resident Name,Purok,Category,Status,Submitted At,Resolved At,Admin Remarks\n")
        _reports.value.forEach { r ->
            sb.append("\"${r.id}\",\"${r.residentName}\",\"${r.purokName}\",\"${r.category.label}\",\"${r.status.label}\",\"${r.submittedAt}\",\"${r.resolvedAt ?: ""}\",\"${r.adminRemarks ?: ""}\"\n")
        }
        return sb.toString()
    }

    // --- Seed Data Builders ---

    private fun getInitialSchedules(): List<CollectionSchedule> {
        val today = getCurrentDate()
        return listOf(
            CollectionSchedule(
                id = "SCH-00101",
                purokId = "p1",
                purokName = "Purok 1",
                collectionDate = today,
                plannedStartTime = "07:00 AM",
                plannedEndTime = "09:30 AM",
                scheduleState = ScheduleState.PUBLISHED,
                collectionStatus = CollectionStatus.IN_PROGRESS,
                assignedWorkerName = "Juan Dela Cruz (Driver) & Team A",
                assignedTruckPlate = "LG-MARAMAG-01 (Dump Truck)",
                instructions = "Biodegradable & Residual Waste collection. Priority coverage on Main Highway & Plaza perimeter.",
                startedAt = "07:12 AM"
            ),
            CollectionSchedule(
                id = "SCH-00102",
                purokId = "p1a",
                purokName = "Purok 1A",
                collectionDate = today,
                plannedStartTime = "09:45 AM",
                plannedEndTime = "11:30 AM",
                scheduleState = ScheduleState.PUBLISHED,
                collectionStatus = CollectionStatus.PENDING,
                assignedWorkerName = "Juan Dela Cruz (Driver) & Team A",
                assignedTruckPlate = "LG-MARAMAG-01 (Dump Truck)",
                instructions = "Collection along residential sector and school perimeter."
            ),
            CollectionSchedule(
                id = "SCH-00103",
                purokId = "p2",
                purokName = "Purok 2",
                collectionDate = today,
                plannedStartTime = "06:30 AM",
                plannedEndTime = "08:30 AM",
                scheduleState = ScheduleState.PUBLISHED,
                collectionStatus = CollectionStatus.COMPLETED,
                assignedWorkerName = "Roberto Gomez (Driver) & Team B",
                assignedTruckPlate = "LG-MARAMAG-02 (Compactor)",
                instructions = "Commercial and market sector solid waste pickup.",
                startedAt = "06:35 AM",
                completedAt = "08:25 AM",
                notes = "All 180 households covered. 2.4 tons collected."
            ),
            CollectionSchedule(
                id = "SCH-00104",
                purokId = "p3",
                purokName = "Purok 3",
                collectionDate = today,
                plannedStartTime = "01:00 PM",
                plannedEndTime = "03:30 PM",
                scheduleState = ScheduleState.PUBLISHED,
                collectionStatus = CollectionStatus.PENDING,
                assignedWorkerName = "Danilo Santos & Team C",
                assignedTruckPlate = "LG-MARAMAG-03",
                instructions = "Segregated collection: Recyclable plastics, bottles, and cardboards."
            ),
            CollectionSchedule(
                id = "SCH-00105",
                purokId = "p4",
                purokName = "Purok 4",
                collectionDate = today,
                plannedStartTime = "03:45 PM",
                plannedEndTime = "06:00 PM",
                scheduleState = ScheduleState.PUBLISHED,
                collectionStatus = CollectionStatus.PENDING,
                assignedWorkerName = "Roberto Gomez & Team B",
                assignedTruckPlate = "LG-MARAMAG-02 (Compactor)",
                instructions = "Purok 4 residential zone pickup."
            )
        )
    }

    private fun getInitialReports(): List<ResidentReport> {
        return listOf(
            ResidentReport(
                id = "REP-70821",
                residentName = "Maria Santos",
                contactNumber = "+63 917 555 1204",
                purokName = "Purok 1",
                category = ReportCategory.UNCOLLECTED_WASTE,
                description = "Garbage bins at the corner of Daisy St. were skipped during yesterday's rounds. Several stray animals are scattering the waste bags.",
                addressDetails = "Daisy St. near North Poblacion Basketball Court",
                status = ReportStatus.IN_PROGRESS,
                adminRemarks = "Forwarded to Team A. Truck LG-01 dispatched to collect during morning sweep.",
                hasPhoto = true,
                photoDescription = "Overflowing waste bins with torn sacks",
                submittedAt = "Oct 13, 2026, 4:15 PM"
            ),
            ResidentReport(
                id = "REP-70822",
                residentName = "Edgar Ramirez",
                contactNumber = "+63 928 444 8821",
                purokName = "Purok 4",
                category = ReportCategory.MISSED_SCHEDULE,
                description = "Our whole street wasn't reached last Thursday. Please schedule a makeup collection before weekend.",
                addressDetails = "Block 3 Lot 12, Purok 4 Sub-division",
                status = ReportStatus.UNDER_REVIEW,
                adminRemarks = "Verification in progress with collection supervisor.",
                hasPhoto = false,
                submittedAt = "Oct 14, 2026, 6:40 AM"
            ),
            ResidentReport(
                id = "REP-70823",
                residentName = "Elena Cruz",
                contactNumber = "+63 909 333 9911",
                purokName = "Purok 2",
                category = ReportCategory.ILLEGAL_DUMPING,
                description = "Unknown truck dumped construction debris and garden clippings on the vacant lot adjacent to the creek.",
                addressDetails = "Riverside boundary, Purok 2",
                status = ReportStatus.RESOLVED,
                adminRemarks = "Barangay Tanods inspected site; cleanup operation conducted Oct 13. Lot owner notified.",
                hasPhoto = true,
                photoDescription = "Debris pile on vacant lot",
                submittedAt = "Oct 12, 2026, 9:20 AM",
                resolvedAt = "Oct 13, 2026, 11:30 AM"
            )
        )
    }

    private fun getInitialAnnouncements(): List<Announcement> {
        return listOf(
            Announcement(
                id = "ANN-101",
                title = "Strict Waste Segregation Policy (RA 9003)",
                content = "Barangay North Poblacion reminds all households in all 18 Puroks to practice strict at-source segregation: Biodegradable (Nabubulok), Residual (Di-nabubulok), and Recyclable. Unsegregated garbage will NOT be accepted by collection teams.",
                targetPurok = "All Puroks",
                isUrgent = false,
                postedBy = "Hon. Barangay Captain & SWMC",
                datePosted = "Oct 12, 2026, 8:00 AM"
            ),
            Announcement(
                id = "ANN-102",
                title = "Upcoming Hazardous & E-Waste Drop-off Day",
                content = "Residents may bring old electronics, batteries, fluorescent bulbs, and paint cans to the Barangay Hall Material Recovery Facility (MRF) this coming Saturday.",
                targetPurok = "All Puroks",
                isUrgent = false,
                postedBy = "Barangay Environment Officer",
                datePosted = "Oct 13, 2026, 10:30 AM"
            )
        )
    }

    private fun getInitialNotifications(): List<InAppNotification> {
        return listOf(
            InAppNotification(
                id = "NOTIF-001",
                title = "Collection in Progress: Purok 1",
                message = "Truck LG-MARAMAG-01 has started waste collection in Purok 1. Please place your bins outside.",
                type = NotificationType.COLLECTION_STARTED,
                targetPurok = "Purok 1",
                timestamp = "Oct 14, 2026, 7:12 AM",
                isRead = false
            ),
            InAppNotification(
                id = "NOTIF-002",
                title = "Collection Completed: Purok 2",
                message = "Solid waste pickup for Purok 2 has been completed by Team B.",
                type = NotificationType.COLLECTION_COMPLETED,
                targetPurok = "Purok 2",
                timestamp = "Oct 14, 2026, 8:25 AM",
                isRead = true
            ),
            InAppNotification(
                id = "NOTIF-003",
                title = "Complaint Ticket In Progress: REP-70821",
                message = "Your uncollected waste report has been assigned to Team A for priority pickup today.",
                type = NotificationType.COMPLAINT_UPDATE,
                targetPurok = "Purok 1",
                timestamp = "Oct 14, 2026, 7:30 AM",
                isRead = false
            )
        )
    }

    private fun getInitialAuditLogs(): List<AuditLog> {
        return listOf(
            AuditLog("AUD-01", "Admin Ramos", "PUBLISH_SCHEDULE", "Published collection schedule for Purok 1 & Purok 1A", "Oct 14, 2026, 6:00 AM"),
            AuditLog("AUD-02", "Juan Dela Cruz (Driver)", "START_COLLECTION", "Started collection in Purok 1 with Truck LG-MARAMAG-01", "Oct 14, 2026, 7:12 AM"),
            AuditLog("AUD-03", "Roberto Gomez (Driver)", "COMPLETE_COLLECTION", "Completed Purok 2 collection: 2.4 tons recorded", "Oct 14, 2026, 8:25 AM"),
            AuditLog("AUD-04", "Maria Santos (Resident)", "SUBMIT_REPORT", "Reported uncollected waste at Daisy St., Purok 1", "Oct 13, 2026, 4:15 PM")
        )
    }
}
