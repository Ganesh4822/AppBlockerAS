package com.example.appblockerv3.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appblockerv3.data.repository.BlockingRepository

class AppViewModelFactory(private val repository: BlockingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(CreateGroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateGroupViewModel(repository) as T
        }
        // Add other ViewModels here
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}