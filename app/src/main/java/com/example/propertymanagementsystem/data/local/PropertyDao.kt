package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Property

/**
 * Data Access Object (DAO) for interacting with the SQLite `properties` table via Room.
 * Provides suspending database operations and LiveData reactive queries.
 */
@Dao
interface PropertyDao {
    /**
     * Inserts a property into the database, replacing on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property)

    /**
     * Updates an existing property record in the database.
     */
    @Update
    suspend fun updateProperty(property: Property)

    /**
     * Deletes a property record from the database.
     */
    @Delete
    suspend fun deleteProperty(property: Property)

    /**
     * Retrieves all properties ordered alphabetically by property name as LiveData.
     */
    @Query("SELECT * FROM properties ORDER BY propertyName ASC")
    fun getAllProperties(): LiveData<List<Property>>

    /**
     * Retrieves properties associated with a specific user email ordered alphabetically.
     */
    @Query("SELECT * FROM properties WHERE userEmail = :email ORDER BY propertyName ASC")
    fun getPropertiesByUser(email: String): LiveData<List<Property>>

    /**
     * Deletes all property records from the database.
     */
    @Query("DELETE FROM properties")
    suspend fun deleteAllProperties()
}
