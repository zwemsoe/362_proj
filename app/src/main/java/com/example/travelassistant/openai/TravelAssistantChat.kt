package com.example.travelassistant.openai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole

object TravelAssistantChat {
    val chatMessageInitial = ChatMessage(
        role = ChatRole.Assistant, content = TravelAssistantConstants.INSTRUCTIONS
    )

    val todoSuggestionsInitial = ChatMessage(
        role = ChatRole.Assistant, content = TravelAssistantConstants.INSTRUCTIONS_TODO_ITEM
    )

    val questionSuggestionsInitial = ChatMessage(
        role = ChatRole.Assistant,
        content = TravelAssistantConstants.INSTRUCTIONS_EXAMPLE_QUESTIONS
    )
}