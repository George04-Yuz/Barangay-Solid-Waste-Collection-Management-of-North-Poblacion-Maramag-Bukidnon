package com.example.data.model

enum class ScheduleState(val label: String) {
    PUBLISHED("Published"),
    DRAFT("Draft"),
    POSTPONED("Postponed"),
    CANCELLED("Cancelled")
}

enum class CollectionStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

data class CollectionSchedule(
    val id: String,
    val purokId: String,
    val purokName: String,
    val collectionDate: String, // YYYY-MM-DD
    val plannedStartTime: String,
    val plannedEndTime: String,
    val scheduleState: ScheduleState = ScheduleState.PUBLISHED,
    val collectionStatus: CollectionStatus = CollectionStatus.PENDING,
    val assignedWorkerName: String,
    val assignedTruckPlate: String,
    val instructions: String,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val delayReason: String? = null,
    val notes: String? = null
)
