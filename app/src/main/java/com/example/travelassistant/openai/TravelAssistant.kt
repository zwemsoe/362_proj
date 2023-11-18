package com.example.travelassistant.openai

import androidx.lifecycle.asFlow
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.exception.OpenAIAPIException
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.RetryStrategy
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
        retry = RetryStrategy(maxRetries = 1)
    )
    private val openAI = OpenAI(config)
    private val chatMessages = TravelAssistantChat.chatMessages.asFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val chatCompletions: Flow<ChatCompletionChunk> =
        chatMessages.flatMapLatest { chatMessages ->
            val chatCompletionRequest = ChatCompletionRequest(
                model = modelId, messages = chatMessages
            )
            openAI.chatCompletions(chatCompletionRequest)
        }

    fun ask(prompt: String): Flow<ChatCompletionChunk> {
        val messagePayload = ChatMessage(role = ChatRole.User, content = prompt)
        TravelAssistantChat.addChatMessage(messagePayload)
        return chatCompletions
    }

    suspend fun askOnce(prompt: String, personality: String?): ChatCompletion? {
        val personalityFinal = personality ?: TravelAssistantConstants.INSTRUCTIONS
        val instruction = ChatMessage(role = ChatRole.System, content = personalityFinal)
        val payload = ChatMessage(role = ChatRole.User, content = prompt)
        val messages = listOf(
            instruction, payload
        )
        return try {
            val req = ChatCompletionRequest(model = modelId, messages = messages)
            openAI.chatCompletion(req)
        } catch (e: OpenAIAPIException) {
            println("Cannot complete ChatCompletionRequest: ${e.printStackTrace()}")
            null
        }
    }

    suspend fun example() {
        val res = askOnce(
            "Hello! What is 1+1",
            "You are a helpful assistant!"
        )
        if (res == null) {
            println("Example response for TravelAssistant returned null")
            return
        }
        println("Example response for TravelAssistant:")
        res.choices.forEach { chatChoice ->
            println("role: ${chatChoice.message.role}")
            println("content: ${chatChoice.message.content}")
        }
    }
}