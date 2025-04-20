package com.example.appblocker.models

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.appblocker.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsViewModel(application: Application) : AndroidViewModel(application) {

//    private val _allApps = MutableLiveData<List<AppModel>>()
//    val allApps: LiveData<List<AppModel>> get() = _allApps
//
//    private val _blockedApps = MutableLiveData<List<AppModel>>()
//    val blockedApps: LiveData<List<AppModel>> get() = _blockedApps
//
//    fun loadApps() {
//        if (_allApps.value != null && _blockedApps.value != null) return
//
//        viewModelScope.launch(Dispatchers.IO) {
//            val context = getApplication<Application>().applicationContext
//            val all = AppUtils.getAllApps(context)
//            //val blocked = AppUtils.getBlockedApps(context)
//            _allApps.postValue(all)
//            //_blockedApps.postValue(blocked)
//        }
//    }
    private val _apps = MutableLiveData<List<AppModel>>()
    val apps: LiveData<List<AppModel>> get() = _apps

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadApps(context: Context) {
        Log.d("Appcheck", "Loading apps")
       x
        viewModelScope.launch(Dispatchers.IO) {
            val appList =  AppUtils.getAllApps(context) // Replace with your loader
            withContext(Dispatchers.Main) {
                _apps.value = appList
                _isLoading.value = false
            }
        }
    }
}