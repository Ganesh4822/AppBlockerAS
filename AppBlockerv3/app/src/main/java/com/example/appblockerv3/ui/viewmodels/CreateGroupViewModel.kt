package com.example.appblockerv3.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appblockerv3.data.db.entities.ScheduleEntity
import com.example.appblockerv3.data.repository.BlockingRepository
import kotlinx.coroutines.launch

class CreateGroupViewModel(private val repository: BlockingRepository) : ViewModel() {

    fun saveGroup(
        groupName: String,
        appList: List<String>,
        schedules: List<ScheduleEntity>,
        usageLimitHours: Int,
        usageLimitMinutes: Int
    ) {
        viewModelScope.launch {
            try {
                // Call to the repository function to save the group and its related data
                val newGroupId = repository.saveNewGroup(
                    groupName,
                    appList,
                    schedules,
                    usageLimitHours,
                    usageLimitMinutes
                )


                // Optionally, you can perform actions after successful save, e.g.,
                // navigate back, show a success message, update UI state.
                Log.d("CreateGroupViewModel", "Group saved successfully with ID: $newGroupId")
            } catch (e: Exception) {
                // Handle any errors during saving (e.g., log, show error message)
                println("Error saving group: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}