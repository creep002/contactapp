package com.example.contactapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


// ViewModel class that interacts with the Repository to manage user interface data.
class ContactViewModel(private val repository: ContactRepository): ViewModel() {

    //LiveData to observe all contacts from the repository.
    val allContacts: LiveData<List<Contact>> = repository.allContacts.asLiveData()
    val favoriteContacts: LiveData<List<Contact>> = repository.favoriteContacts// Like above, but for favorite contacts
    val nonFavoriteContacts: LiveData<List<Contact>> = repository.nonFavoriteContacts//for non favorite contacts

    // Function to add a contact.
    fun addContact(image: String, name: String, phoneNumber: String, email: String){
        viewModelScope.launch {
            val contact = Contact(0, image = image, name = name, phoneNumber = phoneNumber, email = email)
            repository.insert(contact)
        }
    }

    // Function to delete an existing contact.
    fun deleteContact(contact: Contact){
        viewModelScope.launch {
            repository.delete(contact)
        }
    }
    // Function to update a contact
    fun updateContact(contact: Contact){
        viewModelScope.launch {
            repository.update(contact)
        }
    }

    // Function to know a contact have favorite yes or no
    fun toggleFavorite(contact: Contact) {
        val updatedContact = contact.copy(isFavorite = !contact.isFavorite)
        updateContact(updatedContact)
    }

    // Factory class for creating instances of ContactViewModel with the repository.
    class  ContactViewModelFactory(private val repository: ContactRepository): ViewModelProvider.Factory{

        // Create a ViewModel instance. Checks if the requested ViewModel class matches ContactViewModel.
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContactViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return ContactViewModel(repository) as T
            } else{
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}