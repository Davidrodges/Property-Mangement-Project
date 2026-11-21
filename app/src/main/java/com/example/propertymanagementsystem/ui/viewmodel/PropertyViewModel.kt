package com.example.propertymanagementsystem.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.data.local.PropertyDatabase
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.repository.PropertyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class PropertyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PropertyRepository
    val allProperties: LiveData<List<Property>>

    // 1. Total property count LiveData transformation
    val propertyCountText: LiveData<String>

    // 2. Occupancy summary LiveData transformation
    val occupancySummaryText: LiveData<String>

    // 3. Total monthly rent expected LiveData transformation
    val totalMonthlyRentText: LiveData<String>

    // 4. All properties readable summary LiveData transformation
    val allPropertiesText: LiveData<String>

    init {
        val dao = PropertyDatabase.getDatabase(application).propertyDao()
        repository = PropertyRepository(dao)
        allProperties = repository.allProperties

        propertyCountText = allProperties.map { list ->
            val count = list?.size ?: 0
            "Total Properties: $count"
        }

        occupancySummaryText = allProperties.map { list ->
            val safeList = list.orEmpty()
            val occupiedCount = safeList.count { it.isOccupied }
            val vacantCount = safeList.size - occupiedCount
            "Occupied: $occupiedCount | Vacant: $vacantCount"
        }

        totalMonthlyRentText = allProperties.map { list ->
            val safeList = list.orEmpty()
            val totalRent = safeList.sumOf { it.monthlyRent }
            val formattedRent = String.format(Locale.US, "%,.2f", totalRent)
            "Total Monthly Rent: KSh $formattedRent"
        }

        allPropertiesText = allProperties.map { list ->
            if (list.isNullOrEmpty()) {
                "No properties registered yet. Tap 'Add Property' to begin."
            } else {
                list.joinToString(separator = "\n\n-------------------\n\n") { it.summary() }
            }
        }
    }

    /**
     * Validates property registration inputs and adds a new Property via Room repository if valid.
     * Launches a coroutine on Dispatchers.IO to persist data across app restarts.
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

        viewModelScope.launch(Dispatchers.IO) {
            repository.addProperty(newProperty)
        }

        return Pair(true, "Property added successfully")
    }

    fun updateProperty(property: Property) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProperty(property)
        }
    }

    fun deleteProperty(property: Property) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProperty(property)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("PROPERTY_LIFECYCLE", "PropertyViewModel cleared")
    }
}
