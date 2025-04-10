package com.example.appblocker.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appblocker.fragments.AppsFragment
import com.example.appblocker.fragments.BlockedAppsFragment

class ViewPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int = 2 // Two tabs

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) AppsFragment() else BlockedAppsFragment()
    }
}
