package com.example.propertymanagementsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.Locale

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
    val isOccupied: Boolean
) : Serializable {
    fun summary(): String {
        val status = if (isOccupied) "Occupied" else "Vacant"
        val formattedRent = String.format(Locale.US, "%,.2f", monthlyRent)
        return "Property: $propertyName\n" +
            "Location: $location\n" +
            "Type: $propertyType\n" +
            "Monthly Rent: KSh $formattedRent\n" +
            "Caretaker Phone: $caretakerPhone\n" +
            "Status: $status"
    }
}
