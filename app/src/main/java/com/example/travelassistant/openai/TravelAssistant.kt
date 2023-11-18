package com.example.travelassistant.openai

import androidx.lifecycle.asFlow
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.example.travelassistant.BuildConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.time.Duration.Companion.seconds


object TravelAssistant {
    private const val modelRawId = "gpt-3.5-turbo-1106"
    private val modelId = ModelId(modelRawId)
    private val config = OpenAIConfig(
        token = BuildConfig.OPENAI_KEY,
        timeout = Timeout(socket = 60.seconds),
    )
    private val openAI = OpenAI(config)
    private val chatMessages = TravelAssistantChat.chatMessages.asFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatCompletions: Flow<ChatCompletionChunk> = chatMessages.flatMapLatest { chatMessages ->
        val chatCompletionRequest = ChatCompletionRequest(
            model = modelId, messages = chatMessages
        )
        openAI.chatCompletions(chatCompletionRequest)
    }

    private fun ask(prompt: String) {
        val messagePayload = ChatMessage(
            role = ChatRole.User,
            content = prompt
        )
    }
}