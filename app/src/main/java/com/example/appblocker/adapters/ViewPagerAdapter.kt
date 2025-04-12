package com.example.appblocker.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val AppsFragment: Fragment,
    private val BlockedAppsFragment: Fragment
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) AppsFragment else BlockedAppsFragment
    }
}
