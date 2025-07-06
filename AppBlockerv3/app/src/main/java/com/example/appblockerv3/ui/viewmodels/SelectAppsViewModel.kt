// com.example.appblockerv3.ui.viewmodels.SelectAppsViewModel.kt
package com.example.appblockerv3.ui.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appblockerv3.AppBlockerApplication
import com.example.appblockerv3.data.db.entities.AppBlockingStatus
import com.example.appblockerv3.data.repository.BlockingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Data class to represent an app in the UI, including its blocking status
data class AppInfoWithStatus(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    var isSelected: Boolean = false, // Current selection state for UI
    val isIndividuallyBlocked: Boolean = false,
    val groupCount: Int = 0
) {
    // Helper to determine if the app is currently blocked by any rule
    val isBlocked: Boolean
        get() = isIndividuallyBlocked || groupCount > 0

    // Rules for disabling checkbox/selection:
    // 1. If individually blocked, it cannot be in a group (disabled).
    // 2. If it's in 2 groups, it cannot be in another group (disabled).
    val isDisabled: Boolean
        get() = isIndividuallyBlocked || groupCount >= 1
}


class SelectAppsViewModel(application: Application, private val repository: BlockingRepository) : AndroidViewModel(application) {

    private val _allAppsWithStatus = MutableStateFlow<List<AppInfoWithStatus>>(emptyList())
    val allAppsWithStatus: StateFlow<List<AppInfoWithStatus>> = _allAppsWithStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredApps: StateFlow<List<AppInfoWithStatus>> = combine(
        _allAppsWithStatus,
        _searchQuery
    ) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.appName.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Start sharing when subscribers appear, stop after 5s inactivity
        initialValue = emptyList() // Initial value before the first emission
    )

    init {
        loadInstalledAppsAndBlockingStatus()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun loadInstalledAppsAndBlockingStatus() {
        viewModelScope.launch {
            val packageManager = getApplication<AppBlockerApplication>().packageManager
            val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)

            val appBlockingStatusMap = repository.getAllAppsBlockingStatus().first() // Get current blocking status once

            val tempAppList = mutableListOf<AppInfoWithStatus>()
            for (packageInfo in installedPackages) {
                try {
                    val applicationInfo = packageInfo.applicationInfo
                    if (applicationInfo != null) {
                        // Filter out system apps, but include updated system apps
                        if ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                            (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                            val appName = applicationInfo.loadLabel(packageManager).toString()
                            val appIcon = try { applicationInfo.loadIcon(packageManager) } catch (e: Exception) { null }
                            val packageName = packageInfo.packageName

                            val status = appBlockingStatusMap[packageName] ?: AppBlockingStatus(packageName, false, 0)

                            tempAppList.add(
                                AppInfoWithStatus(
                                    packageName = packageName,
                                    appName = appName,
                                    icon = appIcon,
                                    isIndividuallyBlocked = status.isIndividuallyBlocked,
                                    groupCount = status.groupCount
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("SelectAppsViewModel", "Error loading app: ${e.message}")
                }
            }
            _allAppsWithStatus.value = tempAppList.sortedBy { it.appName }
        }
    }

    // Function to update the selection state of an app in the UI
    fun updateAppSelection(packageName: String, isSelected: Boolean) {
        _allAppsWithStatus.value = _allAppsWithStatus.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(isSelected = isSelected)
            } else {
                app
            }
        }
    }
}

// ViewModel Factory for AndroidViewModel
class SelectAppsViewModelFactory(private val application: Application, private val repository: BlockingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectAppsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectAppsViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}