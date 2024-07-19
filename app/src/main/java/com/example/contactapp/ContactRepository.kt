package com.example.contactapp

import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<Contact>> = contactDao.getAllcontacts()
    val favoriteContacts: LiveData<List<Contact>> = contactDao.getFavoriteContacts()
    val nonFavoriteContacts: LiveData<List<Contact>> = contactDao.getNonFavoriteContacts()

    //function to insert a contact into the database
    suspend fun insert(contact: Contact){
        contactDao.insert(contact)
    }

    //function to update a contact into the database
    suspend fun update(contact: Contact){
        contactDao.update(contact)
    }

    //function to delete a contact into the database
    suspend fun delete(contact: Contact){
        contactDao.delete(contact)
    }
}