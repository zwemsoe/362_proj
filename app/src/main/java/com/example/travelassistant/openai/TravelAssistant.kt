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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlin.time.Duration.Companion.seconds


object TravelAssistant {
    private const val modelRawId = "gpt-3.5-turbo-1106"
    private var modelId: ModelId?
    private var config: OpenAIConfig?
    private var openAI: OpenAI?
//    private val chatMessages = TravelAssistantChat.chatMessages.asFlow()

    private var todoSuggestionsRequest: ChatCompletionRequest? = null
    private var questionSuggestionsRequest: ChatCompletionRequest? = null

    private var todoSuggestionChunks: Flow<ChatCompletionChunk>? = null
    private var questionSuggestionChunks: Flow<ChatCompletionChunk>? = null

    init {
        try {
            println("CHECKPOINT 1")
            modelId = ModelId(modelRawId)
            println("CHECKPOINT 2")
            config = OpenAIConfig(
                token = "BuildConfig.OPENAI_KEY",
                timeout = Timeout(socket = 60.seconds),
                retry = RetryStrategy(maxRetries = 1)
            )
            println("CHECKPOINT 3")
            openAI = OpenAI(config!!)
            println("CHECKPOINT 4")
            todoSuggestionsRequest = ChatCompletionRequest(
                model = modelId!!, messages = TravelAssistantChat.todoSuggestionsInitial
            )
            println("CHECKPOINT 5")
            questionSuggestionsRequest = ChatCompletionRequest(
                model = modelId!!, messages = TravelAssistantChat.questionSuggestionsInitial
            )
            println("CHECKPOINT 6")
            todoSuggestionChunks = getChatCompletions(todoSuggestionsRequest!!)
            questionSuggestionChunks = getChatCompletions(questionSuggestionsRequest!!)
            println("CHECKPOINT 7")
        } catch (e: Exception) {
            println("CHECKPOINT 8")
            printError(e)
            modelId = null
            openAI = null
            config = null
            println("CHECKPOINT 9")
        }
    }

    private fun printError(e: Exception) {
        println("Cannot complete ChatCompletionRequest:")
        println(e.message)
        println(e.printStackTrace())
    }

    private fun getChatCompletions(chatCompletionRequest: ChatCompletionRequest): Flow<ChatCompletionChunk> {
        if (openAI == null) {
            return emptyFlow()
        }
        return try {
            openAI!!.chatCompletions(chatCompletionRequest).catch {
                println("CHECKPOINT 13")
            }
        } catch (e: Exception) {
            printError(e)
            emptyFlow()
        }
    }

    fun askTodoSuggestions(): Flow<ChatCompletionChunk> {
        println("CHECKPOINT 10")
        return todoSuggestionChunks!!
    }

    fun askQuestionSuggestions(): Flow<ChatCompletionChunk> {
        println("CHECKPOINT 11")
        try {
            return questionSuggestionChunks?.catch {
                println("CHECKPOINT 12")
            } ?: emptyFlow()
        } catch (e: Exception) {
            println("CHECKPOINT 12")
            printError(e)
        }
        return emptyFlow()
    }
}