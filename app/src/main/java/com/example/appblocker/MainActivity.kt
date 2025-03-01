package com.example.appblocker

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourpackage.BlockedAppsAdapter


//class MainActivity : ComponentActivity() {
class MainActivity : ComponentActivity() {

    data class AppInfo(val name: String, val packageName: String, val icon: Drawable) {

    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlockedAppsAdapter
    private lateinit var appList: List<AppInfo>
    private lateinit var scheduleActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val enableServiceButton: Button = findViewById(R.id.enableServiceButton)
        enableServiceButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        scheduleActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                adapter.notifyDataSetChanged() // ✅ Refresh UI when returning from ScheduleActivity
            }
        }
        //disableBatteryOptimization()
        //promptEnableAccessibility()
        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        appList  = getInstalledApps()

        //adapter = BlockedAppsAdapter(this, appList)
        adapter = BlockedAppsAdapter(this,appList) { selectedApp ->
            // When user clicks on an app, navigate to ScheduleActivity
            val intent = Intent(this, ScheduleActivity::class.java).apply {
                putExtra("APP_NAME", selectedApp.name)
                putExtra("PACKAGE_NAME", selectedApp.packageName)
            }
            scheduleActivityLauncher.launch(intent)
        }

        recyclerView.adapter = adapter
//        setContent {
//            AppBlockerTheme {
//                Button(onClick = { openAccessibilitySettings() }) {
//                    Text("Enable Accessibility Service")
//                }
//
//            }
//        }
    }

    private fun getInstalledApps(): List<AppInfo> {

        val pm = packageManager
        val apps = mutableListOf<AppInfo>()

        // Fetch installed applications
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (appInfo in packages) {
            val packageName = appInfo.packageName
            val appName = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)

            Log.d("AppCheck", "Detected: $appName ($packageName)")

            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                apps.add(AppInfo(appName, packageName, icon))
            }
        }

        Log.d("AppCheck", "Final App List: ${apps.map { it.name }}")
        return apps.sortedBy { it.name.lowercase() }
    }
}


