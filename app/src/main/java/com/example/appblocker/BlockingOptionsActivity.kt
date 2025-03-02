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
import androidx.appcompat.widget.SwitchCompat

class BlockingOptionsActivity : AppCompatActivity() {
    private lateinit var scheduleSwitch: Switch
    private lateinit var timeSwitch: Switch
    private lateinit var scheduleButton: Button
    private lateinit var timeLimitInput: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var selectedAppName: String
    private lateinit var selectedPackageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocking_options)
        sharedPreferences = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
        scheduleSwitch = findViewById(R.id.schedule_blocking_switch)
        timeSwitch = findViewById(R.id.time_blocking_switch)
        scheduleButton = findViewById(R.id.schedule_selection_button)
        timeLimitInput = findViewById(R.id.time_limit_input)
        saveButton = findViewById(R.id.save_button)

        selectedAppName = intent.getStringExtra("APP_NAME") ?: "Unknown App"
        selectedPackageName = intent.getStringExtra("PACKAGE_NAME") ?: ""
        Log.d("Appcheck","share pref are ${sharedPreferences.all}")

        loadBlockingSettings()

        // Manage visibility based on switches
        scheduleButton.visibility = if (scheduleSwitch.isChecked) View.VISIBLE else View.GONE
        timeLimitInput.visibility = if (timeSwitch.isChecked) View.VISIBLE else View.GONE

        scheduleSwitch.setOnCheckedChangeListener { _, isChecked ->
            scheduleButton.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        timeSwitch.setOnCheckedChangeListener { _, isChecked ->
            timeLimitInput.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

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
        val editor = sharedPreferences.edit()
        editor.putBoolean("${selectedPackageName}_schedule_blocking", scheduleSwitch.isChecked)
        editor.putBoolean("${selectedPackageName}_time_blocking", timeSwitch.isChecked)
        editor.putInt("${selectedPackageName}_time_limit", timeLimitInput.text.toString().toIntOrNull() ?: 0)
        editor.apply()
        finish() // Close activity after saving
    }

    private fun loadBlockingSettings() {
        val schedule = sharedPreferences.getString("SCHEDULE_${selectedPackageName}",null)
        val isScheduleBlockingEnabled = sharedPreferences.getBoolean("${selectedPackageName}_schedule_blocking", false)
        Log.d("Appcheck","Is shcedule blocking enabled for $selectedPackageName $isScheduleBlockingEnabled and scedule is $schedule")
        val isTimeBlockingEnabled = sharedPreferences.getBoolean("${selectedPackageName}_time_blocking", false)
        val timeLimit = sharedPreferences.getInt("${selectedPackageName}_time_limit", 0)

        timeSwitch.isChecked = isTimeBlockingEnabled
        scheduleSwitch.isChecked = isScheduleBlockingEnabled
        timeLimitInput.setText(if (timeLimit > 0) timeLimit.toString() else "")
        timeLimitInput.visibility = if (isTimeBlockingEnabled) View.VISIBLE else View.GONE
    }
}
