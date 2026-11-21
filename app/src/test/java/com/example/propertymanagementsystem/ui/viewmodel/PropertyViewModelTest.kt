package com.example.propertymanagementsystem.ui.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.propertymanagementsystem.data.local.PropertyDao
import com.example.propertymanagementsystem.data.local.PropertyDatabase
import com.example.propertymanagementsystem.data.model.Property
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PropertyViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private class TestApplication : Application()

    private class FakePropertyDao(private val liveData: LiveData<List<Property>>) : PropertyDao {
        override suspend fun insertProperty(property: Property) {}
        override suspend fun updateProperty(property: Property) {}
        override suspend fun deleteProperty(property: Property) {}
        override fun getAllProperties(): LiveData<List<Property>> = liveData
        override fun getPropertiesByUser(email: String): LiveData<List<Property>> = liveData
        override suspend fun deleteAllProperties() {}
    }

    private class FakePropertyDatabase(private val dao: PropertyDao) : PropertyDatabase() {
        override fun propertyDao(): PropertyDao = dao
        override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper {
            throw UnsupportedOperationException()
        }
        override fun createInvalidationTracker(): InvalidationTracker {
            return InvalidationTracker(this, "properties")
        }
        override fun clearAllTables() {}
    }

    private lateinit var application: Application
    private lateinit var liveDataProperties: MutableLiveData<List<Property>>
    private lateinit var viewModel: PropertyViewModel

    @Before
    fun setUp() {
        application = TestApplication()
        liveDataProperties = MutableLiveData(emptyList())
        val fakeDao = FakePropertyDao(liveDataProperties)
        val fakeDb = FakePropertyDatabase(fakeDao)

        PropertyDatabase.setTestInstance(fakeDb)

        viewModel = PropertyViewModel(application)
        viewModel.setCurrentUser("test@example.com")
        viewModel.propertyCountText.observeForever {}
        viewModel.occupancySummaryText.observeForever {}
        viewModel.totalMonthlyRentText.observeForever {}
        viewModel.allPropertiesText.observeForever {}
    }

    @Test
    fun initial_state_is_empty() {
        assertEquals("Total Properties: 0", viewModel.propertyCountText.value)
        assertEquals("Occupied: 0 | Vacant: 0", viewModel.occupancySummaryText.value)
        assertEquals("Total Monthly Rent: KSh 0.00", viewModel.totalMonthlyRentText.value)
        assertEquals("No properties registered yet. Tap 'Add Property' to begin.", viewModel.allPropertiesText.value)
    }

    @Test
    fun validateAndAddProperty_success() {
        val result = viewModel.validateAndAddProperty(
            name = "Sunset Apartments",
            location = "Nairobi",
            type = "Residential",
            rentInput = "25000",
            phone = "0712345678",
            isOccupied = true
        )

        assertTrue(result.first)
        assertEquals("Property added successfully", result.second)
    }

    @Test
    fun validateAndAddProperty_validations() {
        // Blank name
        var result = viewModel.validateAndAddProperty("", "Nairobi", "Residential", "25000", "0712345678", true)
        assertFalse(result.first)
        assertEquals("Property name is required.", result.second)

        // Blank location
        result = viewModel.validateAndAddProperty("Sunset", "", "Residential", "25000", "0712345678", true)
        assertFalse(result.first)
        assertEquals("Property location is required.", result.second)

        // Blank type
        result = viewModel.validateAndAddProperty("Sunset", "Nairobi", "", "25000", "0712345678", true)
        assertFalse(result.first)
        assertEquals("Property type is required.", result.second)

        // Blank phone
        result = viewModel.validateAndAddProperty("Sunset", "Nairobi", "Residential", "25000", "", true)
        assertFalse(result.first)
        assertEquals("Caretaker phone number is required.", result.second)

        // Invalid rent
        result = viewModel.validateAndAddProperty("Sunset", "Nairobi", "Residential", "abc", "0712345678", true)
        assertFalse(result.first)
        assertEquals("Enter a valid monthly rent.", result.second)

        // Zero rent
        result = viewModel.validateAndAddProperty("Sunset", "Nairobi", "Residential", "0", "0712345678", true)
        assertFalse(result.first)
        assertEquals("Monthly rent must be greater than zero.", result.second)

        // Negative rent
        result = viewModel.validateAndAddProperty("Sunset", "Nairobi", "Residential", "-100", "0712345678", true)
        assertFalse(result.first)
        assertEquals("Monthly rent must be greater than zero.", result.second)
    }

    @Test
    fun portfolio_statistics_multiple_properties() {
        val list = listOf(
            Property(id = 1, userEmail = "test@example.com", propertyName = "Prop 1", location = "Loc 1", propertyType = "Res", monthlyRent = 30000.0, caretakerPhone = "111", isOccupied = true),
            Property(id = 2, userEmail = "test@example.com", propertyName = "Prop 2", location = "Loc 2", propertyType = "Com", monthlyRent = 45000.0, caretakerPhone = "222", isOccupied = true),
            Property(id = 3, userEmail = "test@example.com", propertyName = "Prop 3", location = "Loc 3", propertyType = "Res", monthlyRent = 50000.0, caretakerPhone = "333", isOccupied = false),
            Property(id = 4, userEmail = "test@example.com", propertyName = "Prop 4", location = "Loc 4", propertyType = "Res", monthlyRent = 20000.0, caretakerPhone = "444", isOccupied = false)
        )
        liveDataProperties.value = list

        assertEquals("Total Properties: 4", viewModel.propertyCountText.value)
        assertEquals("Occupied: 2 | Vacant: 2", viewModel.occupancySummaryText.value)
        assertEquals("Total Monthly Rent: KSh 145,000.00", viewModel.totalMonthlyRentText.value)
    }
}
