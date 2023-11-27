package com.example.travelassistant.openai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole

object TravelAssistantChat {
    private val chatMessageInitial = ChatMessage(
        role = ChatRole.Assistant, content = TravelAssistantConstants.INSTRUCTIONS
    )

    val todoSuggestionsInitial = listOf(
        ChatMessage(
            role = ChatRole.Assistant, content = TravelAssistantConstants.INSTRUCTIONS_TODO_ITEM
        )
    )

    val questionSuggestionsInitial = listOf(
        ChatMessage(
            role = ChatRole.Assistant,
            content = TravelAssistantConstants.INSTRUCTIONS_EXAMPLE_QUESTIONS
        )
    )

    fun getChatMessageList(chatMessage: ChatMessage?): List<ChatMessage> {
        if (chatMessage == null) {
            return listOf(chatMessageInitial)
        }
        return listOf(chatMessageInitial, chatMessage)
    }
}