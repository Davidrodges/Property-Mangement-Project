package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Property


@Dao
interface PropertyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property)


    @Update
    suspend fun updateProperty(property: Property)


    @Delete
    suspend fun deleteProperty(property: Property)


    @Query("SELECT * FROM properties ORDER BY propertyName ASC")
    fun getAllProperties(): LiveData<List<Property>>

    /**
     * Retrieves properties associated with a specific user email ordered alphabetically.
     */
    @Query("SELECT * FROM properties WHERE userEmail = :email ORDER BY propertyName ASC")
    fun getPropertiesByUser(email: String): LiveData<List<Property>>

    /**
     * Deletes all property the records from the property database.
     */
    @Query("DELETE FROM properties")
    suspend fun deleteAllProperties()
}
