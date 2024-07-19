package com.example.contactapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.LiveData

@Dao
interface ContactDao {
    // Insert a contact into the db. If the contact you inserted already exists, it will be ignored
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact)

    //Update the existing contact
    @Update
    suspend fun update(contact: Contact)

    //Delete a existing contact
    @Delete
    suspend fun delete(contact: Contact)

    //Get all contacts from db
    @Query("SELECT * FROM contacts")
    fun getAllcontacts(): Flow<List<Contact>>

    //Get all favorite contracts from db
    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name")
    fun getFavoriteContacts(): LiveData<List<Contact>>

    //Get all non favorite contracts from db
    @Query("SELECT * FROM contacts WHERE isFavorite = 0 ORDER BY name")
    fun getNonFavoriteContacts(): LiveData<List<Contact>>
}