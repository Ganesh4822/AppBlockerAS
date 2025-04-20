package com.example.appblocker

import android.content.Context
import android.content.SharedPreferences
import com.example.appblocker.BlockedAppData
import com.google.gson.Gson

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppBlockingPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveBlockedAppData(packageName: String, data: BlockedAppData) {
        sharedPreferences.edit()
            .putString(packageName, BlockedAppData.toJson(data))
            .apply()
    }

    fun getBlockedAppData(packageName: String): BlockedAppData? {
        val json = sharedPreferences.getString(packageName, null)
        return BlockedAppData.fromJson(json)
    }

    fun deleteBlockedAppData(packageName: String) {
        sharedPreferences.edit()
            .remove(packageName)
            .apply()
    }

    fun deleteBlockedAppDataForAKey(packageName: String, key: String) {
        sharedPreferences.edit()
            .remove(packageName)
            .apply()
    }
}
