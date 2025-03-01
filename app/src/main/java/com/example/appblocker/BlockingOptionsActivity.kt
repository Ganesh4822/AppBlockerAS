import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appblocker.R

class BlockingOptionsActivity : AppCompatActivity() {
    private lateinit var scheduleSwitch: Switch
    private lateinit var timeSwitch: Switch
    private lateinit var scheduleButton: Button
    private lateinit var timeLimitInput: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedPrefs by lazy { getSharedPreferences("BlockingPrefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocking_options)

        scheduleSwitch = findViewById(R.id.schedule_blocking_switch)
        timeSwitch = findViewById(R.id.time_blocking_switch)
        scheduleButton = findViewById(R.id.schedule_selection_button)
        timeLimitInput = findViewById(R.id.time_limit_input)
        saveButton = findViewById(R.id.save_button)

        sharedPreferences = getSharedPreferences("BlockingPrefs", MODE_PRIVATE)

        // Load saved preferences
        loadTimeBlockingSettings()
//        scheduleSwitch.isChecked = sharedPreferences.getBoolean("schedule_blocking", false)
//        timeSwitch.isChecked = sharedPreferences.getBoolean("time_blocking", false)
//        timeLimitInput.setText(sharedPreferences.getInt("time_limit", 0).toString())

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
            val isTimeBlockingEnabled = timeSwitch.isChecked
            val timeLimit = timeLimitInput.text.toString().toIntOrNull() ?: 0

            saveTimeBlockingSettings(isTimeBlockingEnabled, timeLimit)
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
            savePreferences()
        }
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("schedule_blocking", scheduleSwitch.isChecked)
        editor.putBoolean("time_blocking", timeSwitch.isChecked)
        editor.putInt("time_limit", timeLimitInput.text.toString().toIntOrNull() ?: 0)
        editor.apply()
        finish() // Close activity after saving
    }

    private fun saveTimeBlockingSettings(enabled: Boolean, limit: Int) {
        sharedPrefs.edit().apply {
            putBoolean("TIME_BLOCKING_ENABLED", enabled)
            putInt("TIME_BLOCKING_LIMIT", limit)
            apply()
        }
    }

    private fun loadTimeBlockingSettings() {
        val isEnabled = sharedPrefs.getBoolean("TIME_BLOCKING_ENABLED", false)
        val timeLimit = sharedPrefs.getInt("TIME_BLOCKING_LIMIT", 0)

        timeSwitch.isChecked = isEnabled
        timeLimitInput.setText(if (timeLimit > 0) timeLimit.toString() else "")
        timeLimitInput.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }
}
