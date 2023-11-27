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
import com.aallam.openai.client.Chat
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.RetryStrategy
import com.example.travelassistant.BuildConfig
import com.example.travelassistant.openai.TravelAssistantChat.getChatMessageList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
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
//    private val chatMessages = TravelAssistantChat.chatMessages.asFlow()

    private val todoSuggestionsRequest = ChatCompletionRequest(
        model = modelId, messages = TravelAssistantChat.todoSuggestionsInitial
    )
    private val questionSuggestionsRequest = ChatCompletionRequest(
        model = modelId, messages = TravelAssistantChat.questionSuggestionsInitial
    )

    private val todoSuggestionChunks = getChatCompletions(todoSuggestionsRequest)
    private val questionSuggestionChunks = getChatCompletions(questionSuggestionsRequest)

    private fun printError(e: OpenAIAPIException) {
        println("Cannot complete ChatCompletionRequest:")
        println(e.printStackTrace())
    }

    private fun getChatCompletions(chatCompletionRequest: ChatCompletionRequest): Flow<ChatCompletionChunk> {
        return try {
            openAI.chatCompletions(chatCompletionRequest).catch {
                println("OpenAI.chatCompletions cannot be completed")
                it.printStackTrace()
            }
        } catch (e: OpenAIAPIException) {
            printError(e)
            emptyFlow()
        }
    }

    fun askTodoSuggestions(): Flow<ChatCompletionChunk> {
        return todoSuggestionChunks
    }

    fun askQuestionSuggestions(): Flow<ChatCompletionChunk> {
        return questionSuggestionChunks
    }

    fun ask(question: String): Flow<ChatCompletionChunk> {
        val chatMessage = ChatMessage(role = ChatRole.User, content = question)
        val request =
            ChatCompletionRequest(model = modelId, messages = getChatMessageList(chatMessage))
        return getChatCompletions(request)
    }
}