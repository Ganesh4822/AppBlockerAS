package com.example.appblocker.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appblocker.BlockingOptionsActivity
import com.example.appblocker.R
import com.yourpackage.BlockedAppsAdapter


class AppsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    data class AppInfo(val name: String, val packageName: String, val icon: Drawable) {

    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var adapter: BlockedAppsAdapter
    private lateinit var appList: List<ApplicationInfo>
    private lateinit var appListNames: List<String>
    //private lateinit var appList: List<AppInfo>
    private lateinit var scheduleActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_apps, container, false)
        Log.d("AppCheck", "loading app fragment")
        searchBar = view.findViewById<EditText>(R.id.search_bar)!!
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)!!

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        scheduleActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                adapter.notifyDataSetChanged() // ✅ Refresh UI when returning from ScheduleActivity
            }
        }
        appList = getInstalledApps()
        appListNames = appList.map { it.name }
       // Log.d("AppCheck", "loading apps , ${appList.map { it.name }}")
        adapter = BlockedAppsAdapter(requireContext(),appList) { selectedApp ->
            // When user clicks on an app, navigate to ScheduleActivity
            val intent = Intent(requireContext(), BlockingOptionsActivity::class.java).apply {
                putExtra("APP_NAME", selectedApp.name)
                putExtra("PACKAGE_NAME", selectedApp.packageName)
            }
            scheduleActivityLauncher.launch(intent)
        }
        Log.d("AppCheck", "adapter loaded successfully")
        recyclerView.adapter = adapter

        searchBar.addTextChangedListener { text ->
            val filteredList = appList.filter { requireContext().packageManager.getApplicationLabel(it)
                .toString().contains(text.toString(), ignoreCase = true) }

            adapter.updateList(filteredList)
        }

        return view
    }

    private fun getInstalledApps(): List<ApplicationInfo> {

        val pm: PackageManager = requireContext().packageManager
        appList = pm.getInstalledApplications(PackageManager.GET_META_DATA).filter {
            pm.getLaunchIntentForPackage(it.packageName) != null // Only apps with launch intent
        }
        return appList
    }
}