package com.example.travelassistant.ui.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelassistant.models.user.TodoItem
import com.example.travelassistant.openai.TravelAssistant
import kotlinx.coroutines.launch

class TodoViewModel : ViewModel() {
    private val maxSuggestionCount = 3
    private val _suggestedTodoList = MutableLiveData<List<String>>()
    val suggestedTodoList: LiveData<List<String>> = _suggestedTodoList
    private val _todoList = MutableLiveData<List<TodoItem>>()
    val todoList get() = _todoList

    init {
        generateSuggestions()
    }

    fun generateSuggestions() {
        var responseText = ""
        viewModelScope.launch {
            TravelAssistant.askTodoSuggestions().collect {
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

    fun setTodoList(todoList: List<TodoItem>) {
        _todoList.value = todoList
    }
}