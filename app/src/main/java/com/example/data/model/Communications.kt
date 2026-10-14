package com.example.data.model

enum class NotificationType(val label: String) {
    SCHEDULE_UPDATE("Schedule Update"),
    COLLECTION_STARTED("Collection In Progress"),
    COLLECTION_COMPLETED("Collection Completed"),
    COMPLAINT_UPDATE("Report Status Update"),
    ANNOUNCEMENT("Barangay Announcement"),
    ALERT("Operational Alert")
}

data class InAppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val targetPurok: String, // "All" or specific purok
    val timestamp: String,
    val isRead: Boolean = false
)

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val targetPurok: String = "All Puroks",
    val isUrgent: Boolean = false,
    val postedBy: String = "Barangay Solid Waste Committee",
    val datePosted: String
)

data class AuditLog(
    val id: String,
    val actor: String,
    val action: String,
    val details: String,
    val timestamp: String
)
