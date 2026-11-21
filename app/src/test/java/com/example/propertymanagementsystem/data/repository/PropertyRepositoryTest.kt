package com.example.propertymanagementsystem.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.propertymanagementsystem.data.local.PropertyDao
import com.example.propertymanagementsystem.data.model.Property
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PropertyRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private class TestPropertyDao : PropertyDao {
        val insertedList = mutableListOf<Property>()
        val updatedList = mutableListOf<Property>()
        val deletedList = mutableListOf<Property>()
        val liveData = MutableLiveData<List<Property>>(emptyList())

        override suspend fun insertProperty(property: Property) {
            insertedList.add(property)
            liveData.value = liveData.value.orEmpty() + property
        }

        override suspend fun updateProperty(property: Property) {
            updatedList.add(property)
        }

        override suspend fun deleteProperty(property: Property) {
            deletedList.add(property)
        }

        override fun getAllProperties(): LiveData<List<Property>> = liveData

        override fun getPropertiesByUser(email: String): LiveData<List<Property>> = liveData

        override suspend fun deleteAllProperties() {
            insertedList.clear()
            liveData.value = emptyList()
        }
    }

    private lateinit var fakeDao: TestPropertyDao
    private lateinit var repository: PropertyRepository

    @Before
    fun setUp() {
        fakeDao = TestPropertyDao()
        repository = PropertyRepository(fakeDao)
    }

    @Test
    fun addProperty_delegatesToDao() = runBlocking {
        val property = Property(id = 1, userEmail = "test@example.com", propertyName = "Oak Villa", location = "Kileleshwa", propertyType = "Residential", monthlyRent = 50000.0, caretakerPhone = "0711223344", isOccupied = true)
        repository.addProperty(property)

        assertEquals(1, fakeDao.insertedList.size)
        assertEquals(property, fakeDao.insertedList[0])
    }

    @Test
    fun updateProperty_delegatesToDao() = runBlocking {
        val property = Property(id = 1, userEmail = "test@example.com", propertyName = "Oak Villa Updated", location = "Kileleshwa", propertyType = "Residential", monthlyRent = 55000.0, caretakerPhone = "0711223344", isOccupied = true)
        repository.updateProperty(property)

        assertEquals(1, fakeDao.updatedList.size)
        assertEquals(property, fakeDao.updatedList[0])
    }

    @Test
    fun deleteProperty_delegatesToDao() = runBlocking {
        val property = Property(id = 1, userEmail = "test@example.com", propertyName = "Oak Villa", location = "Kileleshwa", propertyType = "Residential", monthlyRent = 50000.0, caretakerPhone = "0711223344", isOccupied = true)
        repository.deleteProperty(property)

        assertEquals(1, fakeDao.deletedList.size)
        assertEquals(property, fakeDao.deletedList[0])
    }

    @Test
    fun allProperties_exposesDaoLiveData() {
        var observedList: List<Property>? = null
        repository.allProperties.observeForever { observedList = it }

        assertEquals(emptyList<Property>(), observedList)

        val property = Property(id = 1, userEmail = "test@example.com", propertyName = "Palm Plaza", location = "Westlands", propertyType = "Commercial", monthlyRent = 120000.0, caretakerPhone = "0722334455", isOccupied = false)
        runBlocking {
            repository.addProperty(property)
        }

        assertEquals(1, observedList?.size)
        assertEquals(property, observedList?.first())
    }
}
