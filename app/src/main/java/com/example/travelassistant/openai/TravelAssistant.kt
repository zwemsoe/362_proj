package com.example.travelassistant.openai

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
import com.example.travelassistant.models.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
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
    private var user: User? = null


    private fun getUserLocationInstruction(): ChatMessage {
        val noLocation = "No user location provided, generalize your answers."
        val location = user?.currentLocation
        if (location == null) {
            println("User location not provided before making request from AI")
            return ChatMessage(
                role = ChatRole.Assistant, content = noLocation
            )
        }
        return ChatMessage(
            role = ChatRole.Assistant, content = "User current location is $location."
        )
    }

    private fun printError(e: OpenAIAPIException) {
        println("Cannot complete ChatCompletionRequest:")
        println(e.printStackTrace())
    }

    private fun getChatCompletions(chatCompletionRequest: ChatCompletionRequest): Flow<ChatCompletionChunk> {
        return try {
            openAI.chatCompletions(chatCompletionRequest).catch {
                println("OpenAI.chatCompletions cannot be completed")
                it.printStackTrace()
                this.emitAll(emptyFlow())
            }
        } catch (e: OpenAIAPIException) {
            printError(e)
            emptyFlow()
        }
    }

    fun askTodoSuggestions(): Flow<ChatCompletionChunk> {
        val messages =
            listOf(TravelAssistantChat.todoSuggestionsInitial, getUserLocationInstruction())
        val todoSuggestionsRequest = ChatCompletionRequest(
            model = modelId, messages = messages
        )
        return getChatCompletions(todoSuggestionsRequest)
    }

    fun askQuestionSuggestions(): Flow<ChatCompletionChunk> {
        val messages =
            listOf(TravelAssistantChat.questionSuggestionsInitial, getUserLocationInstruction())
        val questionSuggestionsRequest = ChatCompletionRequest(
            model = modelId, messages = messages
        )
        return getChatCompletions(questionSuggestionsRequest)
    }

    fun ask(question: String): Flow<ChatCompletionChunk> {
        val chatMessage = ChatMessage(role = ChatRole.User, content = question)
        val messages = listOf(
            TravelAssistantChat.chatMessageInitial, getUserLocationInstruction(), chatMessage
        )
        val request = ChatCompletionRequest(model = modelId, messages = messages)
        return getChatCompletions(request)
    }

    fun setUser(user: User) {
        this.user = user
    }
}