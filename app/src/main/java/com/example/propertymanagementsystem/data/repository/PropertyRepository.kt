package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.PropertyDao
import com.example.propertymanagementsystem.data.model.Property

class PropertyRepository(private val propertyDao: PropertyDao) {

    val allProperties: LiveData<List<Property>> = propertyDao.getAllProperties()

    fun getPropertiesByUser(email: String): LiveData<List<Property>> {
        return propertyDao.getPropertiesByUser(email)
    }

    suspend fun addProperty(property: Property) {
        propertyDao.insertProperty(property)
    }

    suspend fun updateProperty(property: Property) {
        propertyDao.updateProperty(property)
    }

    suspend fun deleteProperty(property: Property) {
        propertyDao.deleteProperty(property)
    }
}
