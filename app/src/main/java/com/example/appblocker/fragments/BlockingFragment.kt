package com.example.appblocker.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appblocker.R
import com.yourpackage.BlockedAppsAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [BlockingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlockingFragment : Fragment() {
    private lateinit var searchBar : EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlockedAppsAdapter
    private lateinit var appList: List<ApplicationInfo>
    private lateinit var appListNames: List<String>
    private lateinit var tabItem: MenuItem
    private lateinit var scheduleActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_blocking, container, false)
        searchBar = view.findViewById<EditText>(R.id.blocking_search_bar)
//        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_blocking_apps)


       // recyclerView.layoutManager = LinearLayoutManager(requireContext())

        scheduleActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                adapter.notifyDataSetChanged() // ✅ Refresh UI when returning from ScheduleActivity
            }
        }

        return view
    }


}