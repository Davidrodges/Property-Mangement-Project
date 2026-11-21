package com.example.propertymanagementsystem.data.model

data class Property(
    val id: Int = 0,
    val propertyName: String,
    val location: String,
    val propertyType: String,
    val monthlyRent: Double,
    val caretakerPhone: String,
    val isOccupied: Boolean
) {
    fun summary(): String {
        val status = if (isOccupied) "Occupied" else "Vacant"
        val formattedRent = String.format(java.util.Locale.US, "%,.2f", monthlyRent)
        return "Property: $propertyName\n" +
               "Location: $location\n" +
               "Type: $propertyType\n" +
               "Monthly Rent: KSh $formattedRent\n" +
               "Caretaker Phone: $caretakerPhone\n" +
               "Status: $status"
    }
}
