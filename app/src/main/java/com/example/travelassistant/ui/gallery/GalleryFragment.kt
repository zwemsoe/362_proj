package com.example.travelassistant.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.travelassistant.R
import com.example.travelassistant.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {
    private lateinit var view: View
    private lateinit var inflater: LayoutInflater
    private lateinit var viewModel: GalleryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        this.inflater = inflater
        view = inflater.inflate(R.layout.fragment_gallery, container, false)
        viewModel = ViewModelProvider(this)[GalleryViewModel::class.java]


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSuggestions()
    }

    private fun setupSuggestions() {
        val container = view.findViewById<LinearLayout>(R.id.todo_suggestions_container)
        val loadingOrFail = TextView(requireContext())
        loadingOrFail.text = "Loading..."
        container.addView(loadingOrFail)

        viewModel.suggestedTodoList.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isEmpty()) {
                loadingOrFail.text = "Sorry, cannot give any suggestions at the moment"
                return@observe
            }
            container.removeView(loadingOrFail)

            suggestions.forEach { suggestion ->
                val todoView = inflater.inflate(R.layout.todo_item, container, false)
                todoView.findViewById<TextView>(R.id.textview_todo).text = suggestion
                container.addView(todoView)
            }
        }
        viewModel.generateSuggestions()
    }
}