package com.example.travelassistant.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.travelassistant.R
import com.example.travelassistant.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var view: View
    private lateinit var viewModel: HomeViewModel
    private lateinit var inflater: LayoutInflater
    private lateinit var suggestionsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        this.inflater = inflater
        view = inflater.inflate(R.layout.fragment_home, container, false)
        suggestionsContainer = view.findViewById(R.id.question_suggestions_container)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSuggestions()
    }

    private fun setupSuggestions() {
        val loadingOrFail = TextView(requireContext())
        loadingOrFail.text = "Loading..."
        suggestionsContainer.addView(loadingOrFail)

        viewModel.suggestedQuestionList.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isEmpty()) {
                loadingOrFail.text = "Sorry, cannot give any suggestions at the moment"
                return@observe
            }
            suggestionsContainer.removeAllViews()
            suggestions.forEach { suggestion ->
                val todoView =
                    inflater.inflate(
                        R.layout.question_suggestion_bubble,
                        suggestionsContainer,
                        false
                    )
                val suggestionTextView = todoView.findViewById<TextView>(
                    R.id.textview_question_suggestion
                )
                suggestionTextView.text = "\"$suggestion\""
                suggestionsContainer.addView(todoView)
            }
        }
        viewModel.generateSuggestions()
    }
}