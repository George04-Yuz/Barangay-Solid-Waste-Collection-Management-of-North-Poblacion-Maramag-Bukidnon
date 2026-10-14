package com.example

import com.example.data.model.*
import com.example.data.repository.CleanTrackRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CleanTrackRepositoryTest {

    private lateinit var repository: CleanTrackRepository

    @Before
    fun setUp() {
        repository = CleanTrackRepository()
    }

    @Test
    fun test18PuroksPresent() {
        val puroks = repository.puroks.value
        assertEquals("Must support exactly 18 puroks and sub-puroks", 18, puroks.size)

        val expectedPurokNames = listOf(
            "Purok 1", "Purok 1A", "Purok 1B", "Purok 2", "Purok 2A",
            "Purok 3", "Purok 4", "Purok 5", "Purok 5A", "Purok 5B",
            "Purok 6", "Purok 6A", "Purok 7", "Purok 8", "Purok 9",
            "Purok 9A", "Purok 9B", "Purok 10"
        )

        val actualNames = puroks.map { it.name }
        for (expected in expectedPurokNames) {
            assertTrue("Expected $expected to be present", actualNames.contains(expected))
        }
    }

    @Test
    fun testScheduleCreationAndPublication() {
        val initialCount = repository.schedules.value.size
        repository.createSchedule(
            purokId = "p5",
            purokName = "Purok 5",
            date = "2026-10-16",
            startTime = "08:00 AM",
            endTime = "10:30 AM",
            workerName = "Juan Dela Cruz",
            truckPlate = "LG-MARAMAG-01",
            instructions = "Collect segregated biodegradable waste.",
            actorName = "Admin Ricardo"
        )

        val updated = repository.schedules.value
        assertEquals(initialCount + 1, updated.size)
        val created = updated.first()
        assertEquals("Purok 5", created.purokName)
        assertEquals(ScheduleState.PUBLISHED, created.scheduleState)
        assertEquals(CollectionStatus.PENDING, created.collectionStatus)

        // Verify notification dispatched
        val notifications = repository.notifications.value
        assertTrue(notifications.any { it.title.contains("Purok 5") })
    }

    @Test
    fun testCollectionStatusWorkflow() {
        // Find or create a schedule
        val schedules = repository.schedules.value
        val pendingSchedule = schedules.firstOrNull { it.collectionStatus == CollectionStatus.PENDING }
            ?: run {
                repository.createSchedule(
                    purokId = "p6",
                    purokName = "Purok 6",
                    date = "2026-10-15",
                    startTime = "07:00 AM",
                    endTime = "09:00 AM",
                    workerName = "Driver Juan",
                    truckPlate = "LG-01",
                    instructions = "Test"
                )
                repository.schedules.value.first()
            }

        // Step 1: Start Collection (Pending -> In Progress)
        repository.updateScheduleStatus(
            scheduleId = pendingSchedule.id,
            newStatus = CollectionStatus.IN_PROGRESS,
            workerName = "Driver Juan",
            notes = "Started route"
        )

        var current = repository.schedules.value.first { it.id == pendingSchedule.id }
        assertEquals(CollectionStatus.IN_PROGRESS, current.collectionStatus)
        assertNotNull("startedAt timestamp must be recorded", current.startedAt)

        // Step 2: Complete Collection (In Progress -> Completed)
        repository.updateScheduleStatus(
            scheduleId = pendingSchedule.id,
            newStatus = CollectionStatus.COMPLETED,
            workerName = "Driver Juan",
            notes = "Cleared all streets. 2.5 tons collected."
        )

        current = repository.schedules.value.first { it.id == pendingSchedule.id }
        assertEquals(CollectionStatus.COMPLETED, current.collectionStatus)
        assertNotNull("completedAt timestamp must be recorded", current.completedAt)
        assertEquals("Cleared all streets. 2.5 tons collected.", current.notes)
    }

    @Test
    fun testResidentReportLifecycle() {
        // Step 1: Submit Report
        val report = repository.submitResidentReport(
            residentName = "Maria Santos",
            contactNumber = "+63 917 555 1204",
            purokName = "Purok 1",
            category = ReportCategory.UNCOLLECTED_WASTE,
            description = "Bins outside Daisy St. skipped.",
            addressDetails = "Daisy St. near chapel",
            hasPhoto = true,
            photoDescription = "Overflowing bins photo"
        )

        assertNotNull(report.id)
        assertEquals(ReportStatus.SUBMITTED, report.status)
        assertTrue(report.hasPhoto)

        // Step 2: Admin reviews and sets In Progress
        repository.updateReportStatus(
            reportId = report.id,
            newStatus = ReportStatus.IN_PROGRESS,
            adminRemarks = "Special truck dispatched for pickup.",
            adminName = "Barangay Official"
        )

        var updatedReport = repository.reports.value.first { it.id == report.id }
        assertEquals(ReportStatus.IN_PROGRESS, updatedReport.status)
        assertEquals("Special truck dispatched for pickup.", updatedReport.adminRemarks)

        // Step 3: Admin marks as Resolved
        repository.updateReportStatus(
            reportId = report.id,
            newStatus = ReportStatus.RESOLVED,
            adminRemarks = "Waste collected and verified on site.",
            adminName = "Barangay Official"
        )

        updatedReport = repository.reports.value.first { it.id == report.id }
        assertEquals(ReportStatus.RESOLVED, updatedReport.status)
        assertNotNull("resolvedAt timestamp must be set", updatedReport.resolvedAt)
    }

    @Test
    fun testNotificationInboxReadStatus() {
        val notifs = repository.notifications.value
        assertTrue("Notifications should be populated", notifs.isNotEmpty())

        val unreadBefore = notifs.count { !it.isRead }
        assertTrue("At least some notifications should be unread initially", unreadBefore > 0)

        // Mark all as read
        repository.markAllNotificationsRead()
        val unreadAfter = repository.notifications.value.count { !it.isRead }
        assertEquals(0, unreadAfter)
    }

    @Test
    fun testCsvExportFormat() {
        val csv = repository.generateCsvExport()
        assertTrue("CSV must contain schedule headers", csv.contains("Schedule ID,Purok,Date"))
        assertTrue("CSV must contain resident report section", csv.contains("Resident Reports Export"))
        assertTrue("CSV must include Purok 1", csv.contains("Purok 1"))
    }
}
