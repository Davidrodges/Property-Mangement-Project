package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.PropertyDao
import com.example.propertymanagementsystem.data.model.Property

/**
 * Repository class that abstracts access to the local database [PropertyDao].
 * Provides a clean API for ViewModel to fetch, insert, update, and delete properties.
 */
class PropertyRepository(private val propertyDao: PropertyDao) {

    /**
     * Observable LiveData stream of all properties.
     */
    val allProperties: LiveData<List<Property>> = propertyDao.getAllProperties()

    /**
     * Retrieves observable properties filtered by user email.
     */
    fun getPropertiesByUser(email: String): LiveData<List<Property>> {
        return propertyDao.getPropertiesByUser(email)
    }

    /**
     * Adds a new property via DAO.
     */
    suspend fun addProperty(property: Property) {
        propertyDao.insertProperty(property)
    }

    /**
     * Updates an existing property via DAO.
     */
    suspend fun updateProperty(property: Property) {
        propertyDao.updateProperty(property)
    }

    /**
     * Deletes a property via DAO.
     */
    suspend fun deleteProperty(property: Property) {
        propertyDao.deleteProperty(property)
    }
}
