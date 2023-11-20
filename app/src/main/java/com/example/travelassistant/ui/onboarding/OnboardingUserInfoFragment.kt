package com.example.travelassistant.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.travelassistant.R

class OnboardingUserInfoFragment : Fragment() {
    private lateinit var view: View
    private lateinit var confirmButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_onboarding_user_info, container, false)
        confirmButton = view.findViewById(R.id.onboarding_user_info_confirm_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        confirmButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingUserInfoFragment_to_nav_settings)
        }
    }
}