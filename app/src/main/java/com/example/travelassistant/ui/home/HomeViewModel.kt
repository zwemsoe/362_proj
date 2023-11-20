package com.example.travelassistant.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelassistant.openai.TravelAssistant
import com.example.travelassistant.openai.TravelAssistantChat
import com.example.travelassistant.openai.TravelAssistantConstants
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val maxSuggestionCount = 3
    private val _suggestedQuestionList = MutableLiveData<List<String>>()
    val suggestedQuestionList: LiveData<List<String>> = _suggestedQuestionList

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
}