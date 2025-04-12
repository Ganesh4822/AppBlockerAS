package com.example.appblocker.adapters

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appblocker.R

class BlockedAppsAdapter(
    private val context: Context,
    private var appList: List<ApplicationInfo>,
    private val onAppClick: (ApplicationInfo) -> Unit,// List of installed apps
) : RecyclerView.Adapter<BlockedAppsAdapter.ViewHolder>() {

    private val sharedPreferences = context.getSharedPreferences("AppBlockerPrefs", Context.MODE_PRIVATE)
    private var blockedApps = sharedPreferences.getStringSet("blocked_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val scheduleTextView: TextView = view.findViewById(R.id.scheduleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    fun updateList(newList: List<ApplicationInfo>) {
        appList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appList[position]
        holder.appName.text = holder.itemView.context.packageManager.getApplicationLabel(app).toString()


        val schedule = sharedPreferences.getString("SCHEDULE_${app.packageName}",null)
        holder.itemView.setOnClickListener {
            onAppClick(app) // Pass the clicked app to the listener
        }
        //Old version to block the apps that are marked by the checkmark.
        if(schedule != null){
            val formattedSchedule = schedule.replace("|", " (") + ")"
            holder.scheduleTextView.text = "blocked: $formattedSchedule"
            holder.scheduleTextView.visibility = View.VISIBLE
        }
        else{
            holder.scheduleTextView.visibility = View.GONE
        }
        val isBlocked = blockedApps.contains(app.packageName)

        //holder.appIcon.setImageDrawable(app.icon)
        Log.d("AppCheck", "Blocked apps: $blockedApps)")
    }

    override fun getItemCount() = appList.size
}
