package com.example.appblocker.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.appblocker.models.AppModel

object AppUtils {

    fun getAllApps(context: Context): List<AppModel> {
        val packageManager = context.packageManager
        val appsList = mutableListOf<AppModel>()

        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in packages) {
            val appName = packageManager.getApplicationLabel(app).toString()
            val packageName = app.packageName
            val icon = packageManager.getApplicationIcon(app)
            if (packageManager.getLaunchIntentForPackage(packageName) != null){
                appsList.add(AppModel(appName, packageName, icon, isBlocked = false))
            }
        }
        return appsList
    }

//    fun getBlockedApps(context: Context): List<AppModel> {
//        val allApps = getAllApps(context)
//
//        // Get blocked apps from SharedPreferences or Database
//        val blockedApps = AppPreferences.getBlockedApps(context)
//
//        return allApps.filter { blockedApps.contains(it.packageName) }
//    }
}