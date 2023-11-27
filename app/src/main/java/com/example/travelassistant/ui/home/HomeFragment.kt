package com.example.travelassistant.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.travelassistant.R
import com.example.travelassistant.models.user.User
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.utils.CoordinatesUtil.getAddressFromLocation
import com.example.travelassistant.viewModels.UserViewModel
import com.example.travelassistant.viewModels.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private lateinit var view: View
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var inflater: LayoutInflater
    private lateinit var suggestionsContainer: LinearLayout
    private lateinit var userLocationTextView: TextView
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel
    private lateinit var questionEditText: EditText
    private lateinit var answerContainer: LinearLayout
    private lateinit var suggestionsOuterContainer: LinearLayout
    private lateinit var questionAnswerTextView: TextView
    private lateinit var answerOptionsContainer: ConstraintLayout
    private lateinit var copyAnswerButton: ImageButton
    private lateinit var showAnswerOnMapButton: ImageButton
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        this.inflater = inflater
        view = inflater.inflate(R.layout.fragment_home, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVars()
        initEditTextListener()
        listenCurrentUserChanges()
        listenQuestionAnswer()
        setupSuggestions()

        copyAnswerButton.setOnClickListener {
            val clipboardManager =
                it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = questionAnswerTextView.text.toString()
            if (text.isEmpty()) {
                return@setOnClickListener
            }
            val clip = ClipData.newPlainText("Travel Assistant", text)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(it.context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initVars() {
        suggestionsContainer = view.findViewById(R.id.question_suggestions_container)
        userLocationTextView = view.findViewById(R.id.home_user_location_text)
        questionEditText = view.findViewById(R.id.home_question_input)
        answerContainer = view.findViewById(R.id.answer_scroll_view)
        suggestionsOuterContainer = view.findViewById(R.id.home_suggestions)
        questionAnswerTextView = view.findViewById(R.id.answer_text_view)
        answerOptionsContainer = view.findViewById(R.id.answer_options_container)
        copyAnswerButton = view.findViewById(R.id.copy_answer_button)
        showAnswerOnMapButton = view.findViewById(R.id.answer_map_button)
        showAnswerOnMapButton.isEnabled = false // TODO

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        userRepository = UserRepository()
        userViewModel = ViewModelProvider(
            this, UserViewModelFactory(userRepository)
        )[UserViewModel::class.java]

        userViewModel.getUser(auth.currentUser!!.uid)
    }

    private fun initEditTextListener() {
        questionEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = v.text
                if (text.isEmpty()) {
                    true
                }
                hideSuggestionsOnQuestionSubmit()
                displayAnswerContainer()
                questionAnswerTextView.text = ""
                homeViewModel.submitQuestion(text.toString())
                true
            }
            false
        }
    }

    private fun hideSuggestionsOnQuestionSubmit() {
        suggestionsOuterContainer.visibility = View.GONE
    }

    private fun displayAnswerContainer() {
        answerContainer.visibility = View.VISIBLE
        answerOptionsContainer.visibility = View.VISIBLE

        val displayMetrics = Resources.getSystem().displayMetrics
        val screenHeight = displayMetrics.heightPixels
        answerContainer.layoutParams.height = screenHeight / 2

    }

    private fun listenQuestionAnswer() {
        homeViewModel.questionAnswer.observe(viewLifecycleOwner) {
            questionAnswerTextView.text = it
        }
    }

    private fun listenCurrentUserChanges() {
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user.currentLocation == null) {
                return@observe
            }
            setUserLocationText(user)
        }
    }

    private fun setUserLocationText(user: User) {
        lifecycleScope.launch {
            val addressList = getAddressFromLocation(requireContext(), user.currentLocation!!)
            withContext(Dispatchers.Main) {
                if (addressList.isNotEmpty()) {
                    userLocationTextView.text = addressList[0].getAddressLine(0)
                }
            }
        }
    }

    private fun setupSuggestions() {
        val loadingOrFail = TextView(requireContext())
        loadingOrFail.text = "Loading..."
        suggestionsContainer.addView(loadingOrFail)

        homeViewModel.suggestedQuestionList.observe(viewLifecycleOwner) { suggestions ->
            if (suggestions.isEmpty()) {
                loadingOrFail.text = "Sorry, cannot give any suggestions at the moment"
                return@observe
            }
            suggestionsContainer.removeAllViews()
            suggestions.forEach { suggestion ->
                val suggestionView = inflater.inflate(
                    R.layout.question_suggestion_bubble, suggestionsContainer, false
                )
                val suggestionTextView = suggestionView.findViewById<TextView>(
                    R.id.textview_question_suggestion
                )
                suggestionTextView.text = "\"$suggestion\""
                suggestionsContainer.addView(suggestionView)
            }
        }
        homeViewModel.generateSuggestions()
    }
}