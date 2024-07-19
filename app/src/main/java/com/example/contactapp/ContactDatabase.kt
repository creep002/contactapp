package com.example.contactapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Contact::class], version = 4, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {

    // Abstract method to get the DAO for the Contact
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: ContactDatabase? = null

        fun getDatabase(context: Context): ContactDatabase {
            // If instance is null, it will create a new instance of databases.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java,
                    "contact_database"
                )
                    .addMigrations(MIGRATION_2_4)
                    .fallbackToDestructiveMigration() // Delete old data if necessary
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                //Here for add column isFavorite to the table contacts with default value is 0
                db.execSQL("ALTER TABLE contacts ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}