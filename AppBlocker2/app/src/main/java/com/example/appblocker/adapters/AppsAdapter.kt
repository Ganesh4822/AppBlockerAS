package com.example.appblocker.adapters

import android.adservices.ondevicepersonalization.AppInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appblocker.R
import com.example.appblocker.models.AppModel

class AppsAdapter(private val apps: List<AppModel>,
                  private val onItemClick: (AppModel) -> Unit) :
    RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {
    private var filteredApps: List<AppModel> = apps

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        val appName: TextView = itemView.findViewById(R.id.appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filteredApps[position]
        holder.itemView.setOnClickListener {
            onItemClick(app)
        }
        holder.appName.text = app.appName
        holder.appIcon.setImageDrawable(app.icon)
    }

    override fun getItemCount(): Int = filteredApps.size

    fun filter(query: String) {
        filteredApps = if (query.isEmpty()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}