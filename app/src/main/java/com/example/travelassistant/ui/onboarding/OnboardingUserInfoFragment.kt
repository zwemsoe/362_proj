package com.example.travelassistant.ui.onboarding

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.travelassistant.R
import com.example.travelassistant.viewModels.OnboardingViewModel
import com.google.firebase.auth.FirebaseAuth

class OnboardingUserInfoFragment : Fragment() {
    private lateinit var view: View
    private lateinit var confirmButton: Button
    private lateinit var nameInput: EditText
    private lateinit var onboardingViewModel: OnboardingViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        view = inflater.inflate(R.layout.fragment_onboarding_user_info, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingViewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)

        nameInput = view.findViewById(R.id.name_input)
        confirmButton = view.findViewById(R.id.onboarding_user_info_confirm_button)
        nameInput.text = Editable.Factory.getInstance().newEditable(auth.currentUser!!.displayName)

        confirmButton.setOnClickListener {
            val displayName = nameInput.text.toString()
            if (displayName.isEmpty()) {
                nameInput.error = "Please enter your name"
            } else {
                onboardingViewModel.updateDisplayName(displayName)
                findNavController().navigate(R.id.action_onboardingUserInfoFragment_to_nav_settings)
            }

        }
    }
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar?.hide()
    }
}