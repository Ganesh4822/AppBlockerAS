package com.example.appblocker.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appblocker.R
import com.example.appblocker.databinding.FragmentAppDetailsBinding

/**
 * A simple [Fragment] subclass.
 * Use the [AppDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppDetailsFragment : Fragment() {

    private var _binding: FragmentAppDetailsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentAppDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val appName = arguments?.getString("APP_NAME") ?: "App"
        binding.tvAppName.text = appName
        Log.d("NavDebug", "App clicked: ${appName}")
        // Load usage data and chart (we’ll implement this in the next steps)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}