package com.example.appblocker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AppBlockerService : AccessibilityService() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AppCheck", "Accessibility Service Connected")

        sharedPreferences = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)

        sharedPreferencesHelper = SharedPreferencesHelper(this)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        val BlockData = sharedPreferencesHelper.getBlockedAppData(packageName)
        val isTimeBlockingEnabled = sharedPreferences.getBoolean("${packageName}_time_blocking", false)
        val timeLimitMinutes = sharedPreferences.getInt("${packageName}_time_limit", 0)
        Log.d("AppCheck", "App Launched: $packageName")

        if (isTimeBlockingEnabled) {
            val totalUsage = getTotalUsageTime(packageName) / 60000 // Convert ms to minutes
            if (totalUsage >= timeLimitMinutes) {
                showBlockScreen(packageName)
                performGlobalAction(GLOBAL_ACTION_HOME)
                return
            }
        }
        if (isAppBlocked(packageName)) {
            Log.d("AppCheck", "Blocking app: $packageName")

            showBlockScreen(packageName)

            performGlobalAction(GLOBAL_ACTION_HOME)
            Handler(Looper.getMainLooper()).postDelayed({
                exitPipMode()
            }, 300)
        }
    }

    override fun onInterrupt() {
        Log.d("AppCheck", "Service Interrupted")
    }

    private fun isAppBlocked(packageName: String): Boolean {
        val schedules = sharedPreferencesHelper.getBlockedAppData(packageName)?.schedules ?: return false

        for(schedule in schedules){
            var (timeRange, days) = schedule.split("|")
            days = days.split(",").toString()
            val (startTime, endTime) = timeRange.split("-")
            val startTimeMin =  timeToMinutes(startTime)
            val endTimeMin =  timeToMinutes(endTime)

            val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val currentTimeMin =  timeToMinutes(currentTime)

            if (!days.contains(currentDay)){
                continue
            } // Not a blocked day
            val isWithinSchedule = if (startTimeMin < endTimeMin) {
                currentTimeMin in startTimeMin..endTimeMin
            } else {
                currentTimeMin >= startTimeMin || currentTimeMin <= endTimeMin
            }
            return isWithinSchedule
        }
        return false
    }
    fun timeToMinutes(time: String): Int {

        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun showBlockScreen(packageName: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, BlockScreenActivity::class.java).apply {
                putExtra("APP_NAME", packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }, 500) // 500ms delay
    }

    private fun getTotalUsageTime(packageName: String): Long {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)  // Check usage in the last 24 hours

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        var totalTime: Long = 0
        for (usageStat in stats) {
            if (usageStat.packageName == packageName) {
                totalTime += usageStat.totalTimeInForeground
            }
        }
        return totalTime // Return total time in milliseconds
    }

    private fun exitPipMode() {
        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningTasks = am.appTasks

            for (task in runningTasks) {
                task.moveToFront() // Bring app to the foreground
                task.finishAndRemoveTask() // Close the app
            }
            Log.d("AppBlocker", "Exited Picture-in-Picture Mode")
        } catch (e: Exception) {
            Log.e("AppBlocker", "Failed to exit PiP mode: ${e.message}")
        }
    }
}
