package com.example.travelassistant.ui.home


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.travelassistant.R
import com.example.travelassistant.models.user.User
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.utils.CoordinatesUtil.getAddressFromLocation
import com.example.travelassistant.utils.multilineDone
import com.example.travelassistant.utils.shakeAnimation
import com.example.travelassistant.viewModels.UserViewModel
import com.example.travelassistant.viewModels.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val MAX_QUESTION_LEN = 150

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
    private lateinit var micBtn: ImageButton
    private var isListening = false
    private lateinit var speechRecognizer: SpeechRecognizer
    private var userPromptCount = 0


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
        setMaxQuestionLength(MAX_QUESTION_LEN)
        listenCurrentUserChanges()
        listenQuestionAnswer()
        setupSuggestions()

        micBtn = view.findViewById(R.id.mic_fab)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        setupSpeechToText()

        micBtn.setOnClickListener {
            toggleSpeechRecognition()
        }

        if (qnaSessionExists()) {
            switchToQnAMode()
        }

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

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
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
            requireActivity() /* Do not change to "this" */,
            UserViewModelFactory(userRepository)
        )[UserViewModel::class.java]

        userViewModel.getUser(auth.currentUser!!.uid)
    }


    private fun setMaxNumOfQuestions(maxNumOfQuestions: Int) {
        val questionLimitTextView = view.findViewById<TextView>(R.id.question_limit_text)
        val qStr = if (maxNumOfQuestions == 1) "question" else "questions"
        val str = "You have $maxNumOfQuestions $qStr left"
        questionLimitTextView.text = str
    }

    private fun setMaxQuestionLength(maxCharLimit: Int) {
        val charCountTextView = view.findViewById<TextView>(R.id.char_count_text)
        charCountTextView.text = "0/$maxCharLimit"
        questionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val curr = s?.length ?: 0
                charCountTextView.text = "$curr/$maxCharLimit"
            }

        })
        questionEditText.filters = arrayOf(InputFilter.LengthFilter(maxCharLimit))
    }

    private fun initEditTextListener() {
        questionEditText.multilineDone { v, _, _ ->
            handleSubmitQuestion(v.text.toString())
            true
        }
    }

    private fun handleSubmitQuestion(question: String) {
        if (question.isEmpty()) {
            return
        }
        if (userPromptCount == 0) {
            view.findViewById<ConstraintLayout>(R.id.question_limit_container).shakeAnimation()
            return
        }
        userViewModel.decreasePromptCount(auth.currentUser!!.uid)
        switchToQnAMode()
        questionAnswerTextView.text = ""
        homeViewModel.submitQuestion(question)
    }

    private fun qnaSessionExists(): Boolean {
        val qExists = homeViewModel.question.value?.isNotEmpty() == true
        val aExists = homeViewModel.questionAnswer.value?.isNotEmpty() == true
        return qExists && aExists
    }

    private fun switchToQnAMode() {
        hideSuggestionsOnQuestionSubmit()
        displayAnswerContainer()
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
            if (user == null) {
                return@observe
            }
            if (user.currentLocation != null) {
                setUserLocationText(user)
            }
            userPromptCount = user.promptCount
            setMaxNumOfQuestions(user.promptCount)
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
                val suggestionView = createSuggestion(suggestion)
                suggestionsContainer.addView(suggestionView)
            }
        }
    }

    private fun createSuggestion(text: String): View {
        val suggestionView = inflater.inflate(
            R.layout.question_suggestion_bubble, suggestionsContainer, false
        )
        val suggestionTextView =
            suggestionView.findViewById<TextView>(R.id.textview_question_suggestion)
        suggestionTextView.text = "\"$text\""
        val suggestionClickable =
            suggestionView.findViewById<ConstraintLayout>(R.id.suggestion_clickable)
        suggestionClickable.setOnClickListener {
            questionEditText.setText(text)
            handleSubmitQuestion(text)
        }
        return suggestionView
    }


    private fun toggleSpeechRecognition() {
        if (isListening) {
            questionEditText.isEnabled = true
            speechRecognizer.stopListening()
            micBtn.setImageResource(R.drawable.ic_mic_off)
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer.startListening(intent)
            questionEditText.isEnabled = false
            micBtn.setImageResource(R.drawable.ic_mic_on)
            Toast.makeText(context, "Started listening", Toast.LENGTH_SHORT).show()
        }
        isListening = !isListening
    }

    private fun setupSpeechToText() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                results?.let {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        questionEditText.setText(matches[0])
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.let {
                    val matches =
                        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        questionEditText.setText(matches[0])
                    }
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                questionEditText.isEnabled = true
                speechRecognizer.stopListening()
                micBtn.setImageResource(R.drawable.ic_mic_off)
                micBtn.setBackgroundResource(com.google.android.libraries.places.R.color.quantum_googred600)
            }

            override fun onError(error: Int) {
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    Toast.makeText(
                        context,
                        "I didn't catch that. Please try speaking again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                questionEditText.isEnabled = true
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (this::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }

    }
}