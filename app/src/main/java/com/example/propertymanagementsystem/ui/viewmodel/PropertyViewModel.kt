package com.example.propertymanagementsystem.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.propertymanagementsystem.data.model.Property
import java.util.Locale

class PropertyViewModel(application: Application) : AndroidViewModel(application) {

    // Temporary in-memory implementation for Stage 2.
    // This will be replaced by PropertyRepository with Room database integration in Stage 5.
    private val _properties = MutableLiveData<List<Property>>(emptyList())
    val properties: LiveData<List<Property>> get() = _properties

    // 1. Total property count LiveData transformation
    val propertyCountText: LiveData<String> = _properties.map { list ->
        "Total Properties: ${list.size}"
    }

    // 2. Occupancy summary LiveData transformation
    val occupancySummaryText: LiveData<String> = _properties.map { list ->
        val occupiedCount = list.count { it.isOccupied }
        val vacantCount = list.size - occupiedCount
        "Occupied: $occupiedCount | Vacant: $vacantCount"
    }

    // 3. Total monthly rent expected LiveData transformation
    val totalMonthlyRentText: LiveData<String> = _properties.map { list ->
        val totalRent = list.sumOf { it.monthlyRent }
        val formattedRent = String.format(Locale.US, "%,.2f", totalRent)
        "Total Monthly Rent: KSh $formattedRent"
    }

    // 4. All properties readable summary LiveData transformation
    val allPropertiesText: LiveData<String> = _properties.map { list ->
        if (list.isEmpty()) {
            "No properties registered yet. Tap 'Add Property' to begin."
        } else {
            list.joinToString(separator = "\n\n-------------------\n\n") { it.summary() }
        }
    }

    /**
     * Validates property registration inputs and adds a new Property if valid.
     * Preserves existing registered properties and updates LiveData observers automatically.
     */
    fun validateAndAddProperty(
        name: String,
        location: String,
        type: String,
        rentInput: String,
        phone: String,
        isOccupied: Boolean
    ): Pair<Boolean, String> {
        val trimmedName = name.trim()
        val trimmedLocation = location.trim()
        val trimmedType = type.trim()
        val trimmedRent = rentInput.trim()
        val trimmedPhone = phone.trim()

        if (trimmedName.isEmpty()) {
            return Pair(false, "Property name is required.")
        }
        if (trimmedLocation.isEmpty()) {
            return Pair(false, "Property location is required.")
        }
        if (trimmedType.isEmpty()) {
            return Pair(false, "Property type is required.")
        }
        if (trimmedPhone.isEmpty()) {
            return Pair(false, "Caretaker phone number is required.")
        }

        val rent = trimmedRent.toDoubleOrNull()
        if (rent == null) {
            return Pair(false, "Enter a valid monthly rent.")
        }
        if (rent <= 0.0) {
            return Pair(false, "Monthly rent must be greater than zero.")
        }

        val newProperty = Property(
            id = 0,
            propertyName = trimmedName,
            location = trimmedLocation,
            propertyType = trimmedType,
            monthlyRent = rent,
            caretakerPhone = trimmedPhone,
            isOccupied = isOccupied
        )

        val currentList = _properties.value.orEmpty()
        _properties.value = currentList + newProperty

        return Pair(true, "Property added successfully")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("PROPERTY_LIFECYCLE", "PropertyViewModel cleared")
    }
}
