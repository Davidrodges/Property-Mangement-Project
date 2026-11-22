package com.example.propertymanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Locale

/**
 * Entity data class representing a real estate property in the local Room database.
 * Holds details such as ownership (userEmail), property name, location, type, rent, and occupancy statistics.
 */
@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String = "",
    val propertyName: String,
    val location: String,
    val propertyType: String,
    val monthlyRent: Double,
    val caretakerPhone: String,
    val isOccupied: Boolean,
    val lastVacantDate: String = "N/A",
    val totalUnits: Int = 1,
    val vacantUnits: Int = 1
) : Serializable {
    /**
     * Returns a formatted multiline summary string of the property details for UI display.
     */
    fun summary(): String {
        val status = if (isOccupied) "Occupied" else "Vacant"
        val formattedRent = String.format(Locale.US, "%,.2f", monthlyRent)
        return "Property: $propertyName\n" +
            "Location: $location\n" +
            "Type: $propertyType\n" +
            "Monthly Rent: KSh $formattedRent\n" +
            "Caretaker Phone: $caretakerPhone\n" +
            "Status: $status\n" +
            "Vacant Units: $vacantUnits / $totalUnits\n" +
            "Last Vacant: $lastVacantDate"
    }
}
