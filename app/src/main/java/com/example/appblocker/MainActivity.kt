package com.example.appblocker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView



//class MainActivity : ComponentActivity() {
class MainActivity : AppCompatActivity() {
    private lateinit var scheduleActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val enableServiceButton: Button = findViewById(R.id.enableServiceButton)
//        enableServiceButton.setOnClickListener {
//            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//            startActivity(intent)
//        }
//        scheduleActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                adapter.notifyDataSetChanged() // ✅ Refresh UI when returning from ScheduleActivity
//            }
//        }

       // recyclerView = findViewById<RecyclerView>(R.id.recyclerViewApps)
        //recyclerView.layoutManager = LinearLayoutManager(this)

       // appList  = getInstalledApps()

        //adapter = BlockedAppsAdapter(this, appList)
//        adapter = BlockedAppsAdapter(this,appList) { selectedApp ->
//            // When user clicks on an app, navigate to ScheduleActivity
//            val intent = Intent(this, BlockingOptionsActivity::class.java).apply {
//                putExtra("APP_NAME", selectedApp.name)
//                putExtra("PACKAGE_NAME", selectedApp.packageName)
//            }
//            scheduleActivityLauncher.launch(intent)
//        }

//        recyclerView.adapter = adapter
//        setContent {
//            AppBlockerTheme {
//                Button(onClick = { openAccessibilitySettings() }) {
//                    Text("Enable Accessibility Service")
//                }
//
//            }
//        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        loadFragment(AppsFragment())

        bottomNavigationView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_apps -> loadFragment(AppsFragment())
                R.id.nav_schedules -> loadFragment(SchedulesFragment())
                R.id.nav_analytics -> loadFragment(AnalyticsFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}


