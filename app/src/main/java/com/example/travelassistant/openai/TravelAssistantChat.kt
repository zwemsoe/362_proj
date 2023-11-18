package com.example.travelassistant.openai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole

object TravelAssistantChat {
    private const val assistantInstruction = TravelAssistantConstants.INSTRUCTIONS
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