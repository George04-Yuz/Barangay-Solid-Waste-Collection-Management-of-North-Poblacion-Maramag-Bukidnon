package com.example.data.model

enum class ReportCategory(val label: String) {
    UNCOLLECTED_WASTE("Uncollected Waste"),
    MISSED_SCHEDULE("Missed Schedule"),
    COLLECTION_DELAY("Collection Delay"),
    ILLEGAL_DUMPING("Illegal Dumping"),
    OTHER("Other Concern")
}

enum class ReportStatus(val label: String) {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    REJECTED("Rejected")
}

data class ResidentReport(
    val id: String,
    val residentName: String,
    val contactNumber: String,
    val purokName: String,
    val category: ReportCategory,
    val description: String,
    val addressDetails: String,
    val status: ReportStatus = ReportStatus.SUBMITTED,
    val adminRemarks: String? = null,
    val hasPhoto: Boolean = false,
    val photoDescription: String? = null,
    val submittedAt: String,
    val resolvedAt: String? = null
)
