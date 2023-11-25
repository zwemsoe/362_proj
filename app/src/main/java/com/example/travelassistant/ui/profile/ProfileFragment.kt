package com.example.travelassistant.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travelassistant.R
import com.example.travelassistant.viewModels.OnboardingViewModel

class ProfileFragment : Fragment() {
    // declare ViewModel instance in fragment
    private lateinit var onboardingViewModel: OnboardingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize ViewModel
        onboardingViewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]

        val profileNameTextView = view.findViewById<TextView>(R.id.profile_name)

        onboardingViewModel.displayName.observe(viewLifecycleOwner) { displayName ->
            profileNameTextView.text = displayName ?: "Unknown"
        }
    }
}