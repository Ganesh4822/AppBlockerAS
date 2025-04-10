package com.example.appblocker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appblocker.adapters.ViewPagerAdapter
import com.example.appblocker.databinding.FragmentBlockingBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment

        binding = FragmentBlockingBinding.inflate(inflater, container, false)
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "All Apps (${AppUtils.getAllApps(requireContext()).size})"
            else "Blocked Apps"
        }.attach()
        return binding.root
    }


}