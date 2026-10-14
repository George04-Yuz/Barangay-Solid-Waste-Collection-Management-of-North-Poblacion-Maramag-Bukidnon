package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CleanTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CleanTrackViewModel(
    val repository: CleanTrackRepository = CleanTrackRepository()
) : ViewModel() {

    // Available Personas for Demo Switcher
    val demoPersonas = listOf(
        UserPersona(
            id = "adm-01",
            name = "Hon. Ricardo Dizon (Admin)",
            role = UserRole.ADMIN,
            contactNumber = "+63 917 888 1000",
            purokName = "Barangay Hall"
        ),
        UserPersona(
            id = "wrk-01",
            name = "Juan Dela Cruz (Driver)",
            role = UserRole.WORKER,
            contactNumber = "+63 928 111 2233",
            purokName = "Purok 1",
            vehiclePlate = "LG-MARAMAG-01"
        ),
        UserPersona(
            id = "res-01",
            name = "Maria Santos (Resident)",
            role = UserRole.RESIDENT,
            contactNumber = "+63 917 555 1204",
            purokName = "Purok 1",
            householdAddress = "142 Daisy St., Purok 1"
        )
    )

    private val _currentPersona = MutableStateFlow(demoPersonas[2]) // Default to Resident for first impression
    val currentPersona: StateFlow<UserPersona> = _currentPersona.asStateFlow()

    // Resident Selected Purok (can test any of the 18 puroks)
    private val _residentPurok = MutableStateFlow("Purok 1")
    val residentPurok: StateFlow<String> = _residentPurok.asStateFlow()

    // UI Tab Selection per role
    private val _adminTab = MutableStateFlow(0) // 0: Dashboard, 1: Schedules, 2: Reports, 3: Announcements, 4: Analytics, 5: Audit
    val adminTab: StateFlow<Int> = _adminTab.asStateFlow()

    private val _workerTab = MutableStateFlow(0) // 0: Route, 1: History
    val workerTab: StateFlow<Int> = _workerTab.asStateFlow()

    private val _residentTab = MutableStateFlow(0) // 0: Home/Schedule, 1: Reports/File, 2: Announcements, 3: Info
    val residentTab: StateFlow<Int> = _residentTab.asStateFlow()

    // Notification Center Visibility
    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter: StateFlow<Boolean> = _showNotificationCenter.asStateFlow()

    // Backend Server Configuration
    private val _isProductionBackend = MutableStateFlow(false)
    val isProductionBackend: StateFlow<Boolean> = _isProductionBackend.asStateFlow()

    private val _backendUrl = MutableStateFlow("https://api.northpoblacion.gov.ph/api/v1")
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    fun toggleBackendMode(useProduction: Boolean, customUrl: String? = null) {
        _isProductionBackend.value = useProduction
        if (!customUrl.isNullOrBlank()) {
            _backendUrl.value = customUrl
        }
        val mode = if (useProduction) "Production HTTPS (${_backendUrl.value})" else "Embedded Live Demo (18 Puroks Offline)"
        showBanner("Backend mode set to: $mode")
    }

    // Transient Message / Banner (e.g. "Collection marked as Completed!")
    private val _transientBanner = MutableStateFlow<String?>(null)
    val transientBanner: StateFlow<String?> = _transientBanner.asStateFlow()

    // Flows from repository
    val puroks: StateFlow<List<Purok>> = repository.puroks
    val schedules: StateFlow<List<CollectionSchedule>> = repository.schedules
    val reports: StateFlow<List<ResidentReport>> = repository.reports
    val notifications: StateFlow<List<InAppNotification>> = repository.notifications
    val announcements: StateFlow<List<Announcement>> = repository.announcements
    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs

    // Unread notification count
    val unreadNotificationCount: StateFlow<Int> = notifications.combine(_residentPurok) { notifs, _ ->
        notifs.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered Schedules for Resident's active Purok
    val residentSchedules: StateFlow<List<CollectionSchedule>> = combine(schedules, _residentPurok) { list, purok ->
        list.filter { it.purokName.equals(purok, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worker's assigned schedules
    val workerSchedules: StateFlow<List<CollectionSchedule>> = combine(schedules, _currentPersona) { list, persona ->
        list.filter { it.assignedWorkerName.contains(persona.name.split(" ")[0], ignoreCase = true) || it.assignedTruckPlate == persona.vehiclePlate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setPersona(persona: UserPersona) {
        _currentPersona.value = persona
        if (persona.role == UserRole.RESIDENT) {
            _residentPurok.value = persona.purokName
        }
        showBanner("Switched view to ${persona.role.displayName}: ${persona.name}")
    }

    fun setResidentPurok(purokName: String) {
        _residentPurok.value = purokName
        val current = _currentPersona.value
        if (current.role == UserRole.RESIDENT) {
            _currentPersona.value = current.copy(purokName = purokName, householdAddress = "House #24, $purokName")
        }
        showBanner("Viewing data for $purokName")
    }

    fun setAdminTab(index: Int) { _adminTab.value = index }
    fun setWorkerTab(index: Int) { _workerTab.value = index }
    fun setResidentTab(index: Int) { _residentTab.value = index }

    fun toggleNotificationCenter(visible: Boolean) {
        _showNotificationCenter.value = visible
    }

    fun showBanner(message: String) {
        _transientBanner.value = message
    }

    fun clearBanner() {
        _transientBanner.value = null
    }

    // --- Actions delegated to Repository ---

    fun createSchedule(
        purokId: String,
        purokName: String,
        date: String,
        startTime: String,
        endTime: String,
        workerName: String,
        truckPlate: String,
        instructions: String
    ) {
        repository.createSchedule(
            purokId = purokId,
            purokName = purokName,
            date = date,
            startTime = startTime,
            endTime = endTime,
            workerName = workerName,
            truckPlate = truckPlate,
            instructions = instructions,
            actorName = _currentPersona.value.name
        )
        showBanner("Schedule for $purokName successfully published!")
    }

    fun startCollection(scheduleId: String) {
        repository.updateScheduleStatus(
            scheduleId = scheduleId,
            newStatus = CollectionStatus.IN_PROGRESS,
            workerName = _currentPersona.value.name,
            notes = "Collection started on time."
        )
        showBanner("Collection is now IN PROGRESS!")
    }

    fun completeCollection(scheduleId: String, notes: String?) {
        repository.updateScheduleStatus(
            scheduleId = scheduleId,
            newStatus = CollectionStatus.COMPLETED,
            workerName = _currentPersona.value.name,
            notes = notes
        )
        showBanner("Collection successfully marked as COMPLETED!")
    }

    fun reportDelay(scheduleId: String, reason: String) {
        repository.updateScheduleStatus(
            scheduleId = scheduleId,
            newStatus = CollectionStatus.IN_PROGRESS,
            workerName = _currentPersona.value.name,
            notes = "Delay reported: $reason",
            delayReason = reason
        )
        showBanner("Delay report logged for collection truck.")
    }

    fun updateScheduleState(scheduleId: String, newState: ScheduleState, reason: String) {
        repository.updateScheduleState(
            scheduleId = scheduleId,
            newState = newState,
            reason = reason,
            actorName = _currentPersona.value.name
        )
        showBanner("Schedule updated to ${newState.label}")
    }

    fun submitReport(
        category: ReportCategory,
        description: String,
        addressDetails: String,
        hasPhoto: Boolean,
        photoDescription: String?
    ) {
        val resident = _currentPersona.value
        val report = repository.submitResidentReport(
            residentName = resident.name,
            contactNumber = resident.contactNumber,
            purokName = _residentPurok.value,
            category = category,
            description = description,
            addressDetails = addressDetails,
            hasPhoto = hasPhoto,
            photoDescription = photoDescription
        )
        showBanner("Report #${report.id} submitted to Barangay North Poblacion!")
    }

    fun resolveReport(reportId: String, newStatus: ReportStatus, remarks: String) {
        repository.updateReportStatus(
            reportId = reportId,
            newStatus = newStatus,
            adminRemarks = remarks,
            adminName = _currentPersona.value.name
        )
        showBanner("Report #$reportId updated to ${newStatus.label}")
    }

    fun publishAnnouncement(title: String, content: String, targetPurok: String, isUrgent: Boolean) {
        repository.postAnnouncement(
            title = title,
            content = content,
            targetPurok = targetPurok,
            isUrgent = isUrgent,
            author = _currentPersona.value.name
        )
        showBanner("Announcement broadcasted successfully!")
    }

    fun markNotificationRead(id: String) {
        repository.markNotificationRead(id)
    }

    fun markAllNotificationsRead() {
        repository.markAllNotificationsRead()
        showBanner("All notifications marked as read")
    }

    fun getCsvExport(): String {
        return repository.generateCsvExport()
    }
}
