package com.example.appblocker


import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private lateinit var appNameTextView: TextView
    private lateinit var startTimeButton: Button
    private lateinit var endTimeButton: Button
    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private lateinit var saveScheduleButton: Button
    private lateinit var clearScheduleButton: Button

    private lateinit var monCheckBox: CheckBox
    private lateinit var tueCheckBox: CheckBox
    private lateinit var wedCheckBox: CheckBox
    private lateinit var thuCheckBox: CheckBox
    private lateinit var friCheckBox: CheckBox
    private lateinit var satCheckBox: CheckBox
    private lateinit var sunCheckBox: CheckBox
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""
    private var selectedDays = mutableSetOf<String>()

    //todo
    //Need to deprecate
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        // Initialize UI components
        appNameTextView = findViewById(R.id.appNameTextView)
        startTimeButton = findViewById(R.id.startTimeButton)
        endTimeButton = findViewById(R.id.endTimeButton)
        startTimeTextView = findViewById(R.id.startTimeTextView)
        endTimeTextView = findViewById(R.id.endTimeTextView)
        saveScheduleButton = findViewById(R.id.saveScheduleButton)
        clearScheduleButton = findViewById(R.id.clearScheduleButton)

        monCheckBox = findViewById(R.id.monCheckBox)
        tueCheckBox = findViewById(R.id.tueCheckBox)
        wedCheckBox = findViewById(R.id.wedCheckBox)
        thuCheckBox = findViewById(R.id.thuCheckBox)
        friCheckBox = findViewById(R.id.friCheckBox)
        satCheckBox = findViewById(R.id.satCheckBox)
        sunCheckBox = findViewById(R.id.sunCheckBox)


        sharedPreferences = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
        // Get the app name passed from previous activity
        val appName = intent.getStringExtra("APP_NAME") ?: "Unknown App"
        val packageName = intent.getStringExtra("PACKAGE_NAME") ?: ""



        appNameTextView.text = "Blocking Schedule for: $appName"

        // Handle Start Time Selection
        startTimeButton.setOnClickListener {
            showTimePicker { time ->
                selectedStartTime = time
                startTimeTextView.text = "Start Time: $selectedStartTime"
            }
        }

        // Handle End Time Selection
        endTimeButton.setOnClickListener {
            showTimePicker { selectedTime ->
                selectedEndTime = selectedTime
                endTimeButton.text = "End: $selectedEndTime"
            }
        }

        // Handle Save Schedule
        saveScheduleButton.setOnClickListener {
            selectedDays.clear()
            if (monCheckBox.isChecked) selectedDays.add("Monday")
            if (tueCheckBox.isChecked) selectedDays.add("Tuesday")
            if (wedCheckBox.isChecked) selectedDays.add("Wednesday")
            if (thuCheckBox.isChecked) selectedDays.add("Thursday")
            if (friCheckBox.isChecked) selectedDays.add("Friday")
            if (satCheckBox.isChecked) selectedDays.add("Saturday")
            if (sunCheckBox.isChecked) selectedDays.add("Sunday")
            saveSchedule(packageName)

            Toast.makeText(this, "Schedule Saved! for $packageName", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }

        packageName.let { pkg ->
            val scheduleExists = sharedPreferences.contains("SCHEDULE_$pkg")
            clearScheduleButton.visibility = if (scheduleExists) View.VISIBLE else View.GONE
        }

        clearScheduleButton.setOnClickListener {
            packageName.let { pkg ->
                sharedPreferences.edit().remove("SCHEDULE_$pkg").apply()
                Toast.makeText(this, "Schedule cleared!", Toast.LENGTH_SHORT).show()
                clearScheduleButton.visibility = View.GONE
            }
        }
    }


    // Function to show Time Picker Dialog
    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    // Function to save schedule (to be implemented)
    private fun saveSchedule(packageName: String) {
        val timePart = if (selectedStartTime.isNotEmpty() && selectedEndTime.isNotEmpty()) {
            "$selectedStartTime-$selectedEndTime"
        } else {
            "00:00-23:59"  // Default to full-day blocking
        }
        if (selectedDays.isEmpty()) {
            selectedDays.addAll(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"))
        }
        val scheduleData = "$timePart|${selectedDays.joinToString(",")}"

        val appData = sharedPreferencesHelper.getBlockedAppData(packageName) ?: BlockedAppData(
            isBlockingEnabled = false,
            isScheduleBasedBlockingEnabled = false,
            isTimeBasedBlockingEnabled = false,
            schedules = mutableListOf(),
            timeLimit = 0,

        )
        appData.isBlockingEnabled = true
        appData.isScheduleBasedBlockingEnabled = true
        appData.schedules.add(scheduleData)
        sharedPreferencesHelper.saveBlockedAppData(packageName, appData)

    }
}
