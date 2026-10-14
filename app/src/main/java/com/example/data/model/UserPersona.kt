package com.example.data.model

enum class UserRole(val displayName: String) {
    ADMIN("Barangay Administrator"),
    WORKER("Collection Worker / Driver"),
    RESIDENT("Resident / Household")
}

data class UserPersona(
    val id: String,
    val name: String,
    val role: UserRole,
    val contactNumber: String,
    val purokName: String,
    val householdAddress: String = "",
    val vehiclePlate: String = ""
)
