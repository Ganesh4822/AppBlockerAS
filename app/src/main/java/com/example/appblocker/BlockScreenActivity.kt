package com.example.appblocker

import android.app.Activity
import android.app.ActivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView

class BlockScreenActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_screen)
        // Kill the blocked app

        val messageTextView: TextView = findViewById(R.id.blockMessage)
        val closeButton: Button = findViewById(R.id.closeButton)
//        Log.d("AppBlocker", "inside app blocker schedule")
        messageTextView.text = "This app is blocked based on your schedule."
        closeButton.setOnClickListener {
            finishAffinity()

        }

    }

    private fun forceStopApp(packageName: String) {
        try {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses(packageName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
