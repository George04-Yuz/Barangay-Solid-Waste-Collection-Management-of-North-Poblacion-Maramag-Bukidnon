package com.example.data.model

data class Purok(
    val id: String,
    val name: String,
    val householdCount: Int,
    val coordinatorName: String,
    val contactNumber: String,
    val primaryCollectionDay: String,
    val isActive: Boolean = true
)

object NorthPoblacionPuroks {
    val ALL_18_PUROKS = listOf(
        Purok("p1", "Purok 1", 142, "Kagawad Ramos", "+63 917 123 4501", "Monday & Thursday"),
        Purok("p1a", "Purok 1A", 98, "Leader Alcantara", "+63 917 123 4502", "Monday & Thursday"),
        Purok("p1b", "Purok 1B", 115, "Leader Valdez", "+63 917 123 4503", "Monday & Thursday"),
        Purok("p2", "Purok 2", 180, "Kagawad Bautista", "+63 917 123 4504", "Tuesday & Friday"),
        Purok("p2a", "Purok 2A", 124, "Leader Mendoza", "+63 917 123 4505", "Tuesday & Friday"),
        Purok("p3", "Purok 3", 165, "Kagawad Flores", "+63 917 123 4506", "Wednesday & Saturday"),
        Purok("p4", "Purok 4", 210, "Kagawad Santos", "+63 917 123 4507", "Monday & Thursday"),
        Purok("p5", "Purok 5", 195, "Kagawad Dizon", "+63 917 123 4508", "Tuesday & Friday"),
        Purok("p5a", "Purok 5A", 130, "Leader Castillo", "+63 917 123 4509", "Tuesday & Friday"),
        Purok("p5b", "Purok 5B", 110, "Leader Navarro", "+63 917 123 4510", "Tuesday & Friday"),
        Purok("p6", "Purok 6", 175, "Kagawad Garcia", "+63 917 123 4511", "Wednesday & Saturday"),
        Purok("p6a", "Purok 6A", 140, "Leader Ocampo", "+63 917 123 4512", "Wednesday & Saturday"),
        Purok("p7", "Purok 7", 220, "Kagawad Reyes", "+63 917 123 4513", "Monday & Thursday"),
        Purok("p8", "Purok 8", 190, "Kagawad Cruz", "+63 917 123 4514", "Tuesday & Friday"),
        Purok("p9", "Purok 9", 160, "Kagawad Morales", "+63 917 123 4515", "Wednesday & Saturday"),
        Purok("p9a", "Purok 9A", 105, "Leader Villanueva", "+63 917 123 4516", "Wednesday & Saturday"),
        Purok("p9b", "Purok 9B", 95, "Leader Pineda", "+63 917 123 4517", "Wednesday & Saturday"),
        Purok("p10", "Purok 10", 185, "Kagawad Aquino", "+63 917 123 4518", "Wednesday & Saturday")
    )
}
