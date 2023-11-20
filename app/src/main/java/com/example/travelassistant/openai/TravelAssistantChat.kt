package com.example.travelassistant.openai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole

object TravelAssistantChat {
    private val chatMessageInitial = ChatMessage(
        role = ChatRole.Assistant, content = TravelAssistantConstants.INSTRUCTIONS
    )
    private var chatMessageInitialList = listOf(chatMessageInitial)
    private val _chatMessages = MutableLiveData(chatMessageInitialList)
    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages

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

    fun addChatMessage(chatMessage: ChatMessage) {
        chatMessageInitialList = listOf(chatMessageInitial, chatMessage)
        _chatMessages.value = chatMessageInitialList
    }
}