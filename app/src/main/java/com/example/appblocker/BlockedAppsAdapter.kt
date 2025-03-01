package com.yourpackage

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appblocker.MainActivity
import com.example.appblocker.R

class BlockedAppsAdapter(
    private val context: Context,
    private val appList: List<MainActivity.AppInfo>,
    private val onAppClick: (MainActivity.AppInfo) -> Unit,// List of installed apps
) : RecyclerView.Adapter<BlockedAppsAdapter.ViewHolder>() {

    private val sharedPreferences = context.getSharedPreferences("AppBlockerPrefs", Context.MODE_PRIVATE)
    private var blockedApps = sharedPreferences.getStringSet("blocked_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
//        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val scheduleTextView: TextView = view.findViewById(R.id.scheduleTextView)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appList[position]
        holder.appName.text = app.name


        val schedule = sharedPreferences.getString("SCHEDULE_${app.packageName}",null)
        holder.itemView.setOnClickListener {
            onAppClick(app) // Pass the clicked app to the listener
        }
        //Old version to block the apps that are marked by the checkmark.
        holder.checkBox.setOnCheckedChangeListener(null)
        if(schedule != null){
            val formattedSchedule = schedule.replace("|", " (") + ")"
            holder.scheduleTextView.text = "blocked: $formattedSchedule"
            holder.scheduleTextView.visibility = View.VISIBLE
        }
        else{
            holder.scheduleTextView.visibility = View.GONE
        }
        val isBlocked = blockedApps.contains(app.packageName)
        holder.checkBox.isChecked = isBlocked

        //holder.appIcon.setImageDrawable(app.icon)
        Log.d("AppCheck", "Blockd apps: $blockedApps)")
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                blockedApps.add(app.packageName)
                Log.d("AppCheck", "Detected: $app)")
            } else {
                blockedApps.remove(app.packageName)
            }
            // ✅ Save updated blocked apps list
            sharedPreferences.edit().putStringSet("blocked_apps", blockedApps).apply()
        }
    }

    override fun getItemCount() = appList.size
}
