package com.example.travelassistant.ui.myevents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travelassistant.R

class MyEventsFragment : Fragment() {
    private lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_myevents, container, false)
        val myEventsViewModel = ViewModelProvider(this)[MyEventsViewModel::class.java]
        return view
    }

    override fun onResume() {
        super.onResume()
        // Show the ActionBar when the Fragment is resumed
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }
}