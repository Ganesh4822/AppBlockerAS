package com.example.appblocker.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appblocker.R
import com.example.appblocker.adapters.AppsAdapter
import com.example.appblocker.databinding.FragmentAppsBinding
import com.example.appblocker.models.AppModel
import com.example.appblocker.utils.AppUtils


//class AppsFragment : Fragment(){
//
//    data class AppInfo(val name: String, val packageName: String, val icon: Drawable) {
//
//    }
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var searchBar: EditText
//    private lateinit var adapter: BlockedAppsAdapter
//    private lateinit var appList: List<ApplicationInfo>
//    private lateinit var appListNames: List<String>
//    //private lateinit var appList: List<AppInfo>
//    private lateinit var scheduleActivityLauncher: ActivityResultLauncher<Intent>
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view =  inflater.inflate(R.layout.fragment_apps, container, false)
//        Log.d("AppCheck", "loading app fragment")
////        searchBar = view.findViewById<EditText>(R.id.search_bar)!!
//        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)!!
//
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        scheduleActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                adapter.notifyDataSetChanged() // ✅ Refresh UI when returning from ScheduleActivity
//            }
//        }
//        appList = getInstalledApps()
//        appListNames = appList.map { it.name }
//       // Log.d("AppCheck", "loading apps , ${appList.map { it.name }}")
//        adapter = BlockedAppsAdapter(requireContext(),appList) { selectedApp ->
//            // When user clicks on an app, navigate to ScheduleActivity
//            val intent = Intent(requireContext(), BlockingOptionsActivity::class.java).apply {
//                putExtra("APP_NAME", selectedApp.name)
//                putExtra("PACKAGE_NAME", selectedApp.packageName)
//            }
//            scheduleActivityLauncher.launch(intent)
//        }
//        Log.d("AppCheck", "adapter loaded successfully")
//        recyclerView.adapter = adapter
//
//        searchBar.addTextChangedListener { text ->
//            val filteredList = appList.filter { requireContext().packageManager.getApplicationLabel(it)
//                .toString().contains(text.toString(), ignoreCase = true) }
//
//            adapter.updateList(filteredList)
//        }
//
//        return view
//    }
//
//    private fun getInstalledApps(): List<ApplicationInfo> {
//
//        val pm: PackageManager = requireContext().packageManager
//        appList = pm.getInstalledApplications(PackageManager.GET_META_DATA).filter {
//            pm.getLaunchIntentForPackage(it.packageName) != null // Only apps with launch intent
//        }
//        return appList
//    }
//}

class AppsFragment : Fragment() {

    private lateinit var binding: FragmentAppsBinding
    private lateinit var adapter: AppsAdapter
    private var apps: List<AppModel> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppsBinding.inflate(inflater, container, false)

        //apps = AppUtils.getAllApps(requireContext())
        adapter = AppsAdapter(apps)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    fun setAppList(apps: List<AppModel>) {
        this.apps = apps
        if (::adapter.isInitialized) {
            adapter = AppsAdapter(apps)
            binding.recyclerView.adapter = adapter
        }
    }

    fun filter(query: String) {
        if (::adapter.isInitialized) {
            adapter.filter(query)
        }
    }
}