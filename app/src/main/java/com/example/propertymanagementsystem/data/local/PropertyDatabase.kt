package com.example.propertymanagementsystem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.propertymanagementsystem.data.model.Property

@Database(entities = [Property::class], version = 1, exportSchema = false)
abstract class PropertyDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao

    companion object {
        @Volatile
        private var INSTANCE: PropertyDatabase? = null

        fun getDatabase(context: Context): PropertyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PropertyDatabase::class.java,
                    "property_management_db"
                ).build().also { INSTANCE = it }
            }

        fun setTestInstance(database: PropertyDatabase?) {
            INSTANCE = database
        }
    }
}
