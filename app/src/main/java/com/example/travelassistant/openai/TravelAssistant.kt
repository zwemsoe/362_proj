package com.example.travelassistant.openai

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.example.travelassistant.BuildConfig
import kotlin.time.Duration.Companion.seconds

class TravelAssistant {
    private val config = OpenAIConfig(
        token = BuildConfig.OPENAI_KEY,
        timeout = Timeout(socket = 60.seconds),
    )
    private val openAI = OpenAI(config)


}