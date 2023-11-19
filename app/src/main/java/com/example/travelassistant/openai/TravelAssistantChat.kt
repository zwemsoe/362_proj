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
    private val chatMessageInitialList = mutableListOf(chatMessageInitial)
    private val _chatMessages = MutableLiveData(chatMessageInitialList.toList())
    val chatMessages: LiveData<List<ChatMessage>> get() = _chatMessages
    fun addChatMessage(chatMessage: ChatMessage) {
        chatMessageInitialList.add(chatMessage)
        _chatMessages.value = chatMessageInitialList
    }

    fun updateUserLocationKnowledge(location: String) {
        val update = "User is now located at $location."
        val infoUpdate = ChatMessage(role = ChatRole.Assistant, content = update)

        addChatMessage(infoUpdate)
    }
}