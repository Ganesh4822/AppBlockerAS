package com.example.appblocker


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class BlockingOptionsActivity : AppCompatActivity() {
    private lateinit var scheduleSwitch: Switch
    private lateinit var timeSwitch: Switch
    private lateinit var scheduleButton: Button
    private lateinit var timeLimitInput: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var selectedAppName: String
    private lateinit var selectedPackageName: String
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocking_options)
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        sharedPreferences = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
        scheduleSwitch = findViewById(R.id.schedule_blocking_switch)
        timeSwitch = findViewById(R.id.time_blocking_switch)
        scheduleButton = findViewById(R.id.schedule_selection_button)
        timeLimitInput = findViewById(R.id.time_limit_input)
        saveButton = findViewById(R.id.save_button)

        selectedAppName = intent.getStringExtra("APP_NAME") ?: "Unknown App"
        selectedPackageName = intent.getStringExtra("PACKAGE_NAME") ?: ""
        Log.d("Appcheck","share pref are ${sharedPreferences.all}")
        Log.d("Appcheck","share pref are ${sharedPreferencesHelper.getBlockedAppData(selectedPackageName)}")

        loadBlockingSettings()

        // Manage visibility based on switches
        scheduleButton.visibility = if (scheduleSwitch.isChecked) View.VISIBLE else View.GONE
        timeLimitInput.visibility = if (timeSwitch.isChecked) View.VISIBLE else View.GONE

        scheduleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                deleteScheduleForApp(selectedPackageName)
            }
            scheduleButton.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

//        timeSwitch.setOnCheckedChangeListener { _, isChecked ->
//            if (!isChecked) {
//                deleteTimeLimitForApp(selectedPackageName)
//            }
//            timeLimitInput.visibility = if (isChecked) View.VISIBLE else View.GONE
//        }

        saveButton.setOnClickListener {
            savePreferences()
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
        }
        scheduleButton.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java).apply {
                putExtra("APP_NAME", selectedAppName)
                putExtra("PACKAGE_NAME", selectedPackageName)
            }
            startActivity(intent)
        }
    }

    private fun savePreferences() {
        val timeLimit = timeLimitInput.text.toString().toIntOrNull() ?: 0
        val appData = sharedPreferencesHelper.getBlockedAppData(selectedPackageName) ?: BlockedAppData(
            isBlockingEnabled = false,
            isScheduleBasedBlockingEnabled = false,
            isTimeBasedBlockingEnabled = false,
            schedules = mutableListOf(),
            timeLimit = 0
        )
        val updatedData = appData.copy(
            isScheduleBasedBlockingEnabled = scheduleSwitch.isChecked,
            isTimeBasedBlockingEnabled = timeSwitch.isChecked,
            timeLimit = if (timeSwitch.isChecked) timeLimit else 0
        )
        if (!updatedData.isScheduleBasedBlockingEnabled && !updatedData.isTimeBasedBlockingEnabled) {
            sharedPreferencesHelper.deleteBlockedAppData(selectedPackageName)
        } else {
            sharedPreferencesHelper.saveBlockedAppData(selectedPackageName, updatedData)
        }
        finish()

    }

    private fun loadBlockingSettings() {
        val appData = sharedPreferencesHelper.getBlockedAppData(selectedPackageName)

        if (appData != null) {
            scheduleSwitch.isChecked = appData.isScheduleBasedBlockingEnabled
            timeSwitch.isChecked = appData.isTimeBasedBlockingEnabled
            timeLimitInput.setText(if (appData.timeLimit > 0) appData.timeLimit.toString() else "")
        } else {
            scheduleSwitch.isChecked = false
            timeSwitch.isChecked = false
            timeLimitInput.setText("")
        }
        scheduleButton.visibility = if (scheduleSwitch.isChecked) View.VISIBLE else View.GONE
        timeLimitInput.visibility = if (timeSwitch.isChecked) View.VISIBLE else View.GONE
    }

    private fun deleteScheduleForApp(packageName: String) {
        val appData = sharedPreferencesHelper.getBlockedAppData(selectedPackageName)

    }

//    private fun deleteTimeLimitForApp(packageName: String) {
//        sharedPreferences.edit().remove("${packageName}_time_limit").apply()
//    }
}
