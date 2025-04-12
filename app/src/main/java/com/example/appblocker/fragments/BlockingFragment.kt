package com.example.appblocker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.appblocker.adapters.ViewPagerAdapter
import com.example.appblocker.databinding.FragmentBlockingBinding
import com.example.appblocker.models.AppsViewModel
import com.example.appblocker.utils.AppUtils
import com.google.android.material.tabs.TabLayoutMediator

/**
 * A simple [Fragment] subclass.
 * Use the [BlockingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlockingFragment : Fragment() {

    private lateinit var binding: FragmentBlockingBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var allAppsFragment: AppsFragment
    private lateinit var blockedAppsFragment: BlockedAppsFragment

    private val appsViewModel: AppsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentBlockingBinding.inflate(inflater, container, false)

        allAppsFragment = AppsFragment()
        blockedAppsFragment = BlockedAppsFragment()

        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle,allAppsFragment,blockedAppsFragment)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "All Apps (${AppUtils.getAllApps(requireContext()).size})"
            else "Blocked Apps"
        }.attach()

        appsViewModel.loadApps()

        appsViewModel.allApps.observe(viewLifecycleOwner) { list ->
            allAppsFragment.setAppList(list)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                when (binding.viewPager.currentItem) {
                    0 -> allAppsFragment.filter(newText.orEmpty())
                    //1 -> blockedAppsFragment.filter(newText.orEmpty())
                }
                return true
            }
        })

        return binding.root
    }

}