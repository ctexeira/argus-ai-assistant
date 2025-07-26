package com.argus.app.ai

import android.content.Context
import com.argus.app.utils.ApiKeyManager

class AIServiceFactory(private val context: Context) {
    
    private val apiKeyManager = ApiKeyManager(context)
    
    fun createAIService(): AIService {
        return createAIService(apiKeyManager.getSelectedService())
    }
    
    fun createAIService(serviceType: String): AIService {
        return when (serviceType) {
            ApiKeyManager.SERVICE_OPENAI -> {
                val apiKey = apiKeyManager.getOpenAIKey()
                if (apiKey != null) {
                    OpenAIService(apiKey)
                } else {
                    LocalAIService() // Fallback to local if no API key
                }
            }
            ApiKeyManager.SERVICE_GEMINI -> {
                val apiKey = apiKeyManager.getGeminiKey()
                if (apiKey != null) {
                    GeminiService(apiKey)
                } else {
                    LocalAIService() // Fallback to local if no API key
                }
            }
            else -> LocalAIService()
        }
    }
    
    fun getAvailableServices(): List<AIServiceInfo> {
        return listOf(
            AIServiceInfo(
                id = ApiKeyManager.SERVICE_LOCAL,
                name = "Local AI (Demo)",
                description = "Built-in responses for testing",
                requiresApiKey = false,
                isAvailable = true
            ),
            AIServiceInfo(
                id = ApiKeyManager.SERVICE_OPENAI,
                name = "OpenAI GPT",
                description = "Advanced AI powered by OpenAI",
                requiresApiKey = true,
                isAvailable = apiKeyManager.hasOpenAIKey()
            ),
            AIServiceInfo(
                id = ApiKeyManager.SERVICE_GEMINI,
                name = "Google Gemini",
                description = "Google's advanced AI model",
                requiresApiKey = true,
                isAvailable = apiKeyManager.hasGeminiKey()
            )
        )
    }
    
    fun getCurrentServiceInfo(): AIServiceInfo {
        val currentService = apiKeyManager.getSelectedService()
        return getAvailableServices().find { it.id == currentService }
            ?: getAvailableServices().first { it.id == ApiKeyManager.SERVICE_LOCAL }
    }
}

data class AIServiceInfo(
    val id: String,
    val name: String,
    val description: String,
    val requiresApiKey: Boolean,
    val isAvailable: Boolean,
    val type: String = id
)