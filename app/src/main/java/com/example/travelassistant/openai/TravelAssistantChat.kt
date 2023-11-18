package com.example.travelassistant.openai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole

private const val INSTRUCTIONS =
    "You are a travel assistant. You will help users with any questions they may have about their nearby location. Give them suggestions, help with their TODO list and events when asked."

object TravelAssistantChat {
    private const val assistantInstruction = INSTRUCTIONS
    private val chatMessageInitial = ChatMessage(
        role = ChatRole.Assistant, content = assistantInstruction
    )
    private val _chatMessages = MutableLiveData(listOf(chatMessageInitial))
    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages
    fun addChatMessage(chatMessage: ChatMessage) {
        val currList = _chatMessages.value ?: return
        val updatedList = currList.toMutableList()
        updatedList.add(chatMessage)
        _chatMessages.value = updatedList
    }
}