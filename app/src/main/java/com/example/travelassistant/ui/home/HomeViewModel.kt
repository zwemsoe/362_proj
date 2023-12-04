package com.example.travelassistant.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelassistant.openai.TravelAssistant
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val maxSuggestionCount = 3
    private val _suggestedQuestionList = MutableLiveData<List<String>>()
    val suggestedQuestionList: LiveData<List<String>> = _suggestedQuestionList
    private val _questionAnswer = MutableLiveData("")
    val questionAnswer get() = _questionAnswer
    private val _question = MutableLiveData("")
    val question get() = _question

    init {
        generateSuggestions()
    }

    fun generateSuggestions() {
        var responseText = ""
        viewModelScope.launch {
            TravelAssistant.askQuestionSuggestions().collect {
                it.choices.forEach { chatChoice ->
                    if (chatChoice.delta.content == null) {
                        extractSuggestions(responseText)
                        return@collect
                    }
                    responseText += chatChoice.delta.content
                }
            }
        }
    }

    private fun extractSuggestions(response: String) {
        val suggestions = extractQuestionList(response)
        suggestions.forEach {
            println(it)
        }
        _suggestedQuestionList.value = suggestions.take(maxSuggestionCount)
    }

    private fun extractQuestionList(response: String): List<String> {
        val todoPattern = Regex("^\\d+\\.\\s+(.+)$", RegexOption.MULTILINE)
        return todoPattern.findAll(response).map { it.groupValues[1] }.toList()
    }

    /**
     * Must ensure validity of the string before
     * calling this function
     */
    fun submitQuestion(q: String) {
        _question.value = q
        _questionAnswer.value = ""
        viewModelScope.launch {
            TravelAssistant.ask(q).collect {
                it.choices.forEach { chatChoice ->
                    if (chatChoice.delta.content == null) {
                        return@collect
                    }
                    _questionAnswer.value += chatChoice.delta.content
                }
            }
        }
    }
}