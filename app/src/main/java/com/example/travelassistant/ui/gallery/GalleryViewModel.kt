package com.example.travelassistant.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelassistant.openai.TravelAssistant
import com.example.travelassistant.openai.TravelAssistantChat
import com.example.travelassistant.openai.TravelAssistantConstants.INSTRUCTIONS_TODO_ITEM
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val maxSuggestionCount = 3
    private val _suggestedTodoList = MutableLiveData<List<String>>()
    val suggestedTodoList: LiveData<List<String>> = _suggestedTodoList

    fun generateSuggestions() {
        val location = "4123 W 10th Ave, Vancouver, BC V6R 2H2"
        TravelAssistantChat.updateUserLocationKnowledge(location)
        var responseText = ""
        viewModelScope.launch {
            TravelAssistant.ask(INSTRUCTIONS_TODO_ITEM).collect {
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
        val suggestions = extractTodoItems(response)
        suggestions.forEach {
            println(it)
        }
        _suggestedTodoList.value = suggestions.take(maxSuggestionCount)
    }

    private fun extractTodoItems(response: String): List<String> {
        val todoPattern = Regex("^\\d+\\.\\s+(.+)$", RegexOption.MULTILINE)
        return todoPattern.findAll(response).map { it.groupValues[1] }.toList()
    }
}