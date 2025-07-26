package com.argus.app.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiService(private val apiKey: String) : AIService {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<String>
    ): Flow<String> = flow {
        try {
            val request = createRequest(message, conversationHistory)
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    when (response.code) {
                        400 -> emit("Invalid API key. Please check your Gemini API key in settings.")
                        403 -> emit("API key access denied. Please check your Gemini API key permissions.")
                        429 -> emit("Rate limit exceeded. Please try again in a moment.")
                        500, 502, 503, 504 -> emit("Gemini service is temporarily unavailable. Please try again later.")
                        else -> emit("Error ${response.code}: ${response.message}. Please check your API key and try again.")
                    }
                    return@flow
                }
                
                val responseBody = response.body?.string()
                if (responseBody == null) {
                    emit("Sorry, I didn't receive a proper response. Please try again.")
                    return@flow
                }
                
                try {
                    val geminiResponse = json.decodeFromString<GeminiResponse>(responseBody)
                    val content = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    
                    if (content != null) {
                        // Simulate streaming for better UX
                        val words = content.split(" ")
                        var accumulatedText = ""
                        
                        for (word in words) {
                            accumulatedText += if (accumulatedText.isEmpty()) word else " $word"
                            emit(accumulatedText)
                            delay(100) // Simulate typing effect
                        }
                    } else {
                        emit("I'm sorry, I couldn't generate a response. Please try rephrasing your question.")
                    }
                } catch (e: Exception) {
                    emit("Error parsing response. Please try again.")
                }
            }
        } catch (e: IOException) {
            emit("Network error: Please check your internet connection and try again.")
        } catch (e: Exception) {
            emit("Error: ${e.message ?: "An unexpected error occurred. Please check your API key and try again."}")  
        }
    }
    
    override suspend fun getQuickResponse(message: String): String {
        return try {
            val request = createNonStreamingRequest(message, emptyList())
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    when (response.code) {
                        400 -> "Invalid API key. Please check your Gemini API key in settings."
                        403 -> "API key access denied. Please check your Gemini API key permissions."
                        429 -> "Rate limit exceeded. Please try again in a moment."
                        500, 502, 503, 504 -> "Gemini service is temporarily unavailable. Please try again later."
                        else -> "Error ${response.code}: ${response.message}"
                    }
                } else {
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        "No response received"
                    } else {
                        try {
                            val geminiResponse = json.decodeFromString<GeminiResponse>(responseBody)
                            val content = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            content ?: "No response received"
                        } catch (e: Exception) {
                            "Error parsing response: ${e.message}"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    private fun createNonStreamingRequest(currentMessage: String, history: List<String>): Request {
        val contents = buildContents(currentMessage, history)
        
        val requestBody = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 500
            )
        )
        
        val jsonBody = json.encodeToString(GeminiRequest.serializer(), requestBody)
        
        return Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
    }
    
    private fun createRequest(currentMessage: String, history: List<String>): Request {
        val contents = buildContents(currentMessage, history)
        
        val requestBody = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 500
            )
        )
        
        val jsonBody = json.encodeToString(GeminiRequest.serializer(), requestBody)
        
        return Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
    }
    
    private fun buildContents(currentMessage: String, history: List<String>): List<Content> {
        val contents = mutableListOf<Content>()
        
        // Add system instruction as first user message
        contents.add(
            Content(
                parts = listOf(
                    Part(
                        text = "You are Argus, a helpful AI assistant integrated into an Android app. " +
                                "Provide concise, helpful responses. Be friendly and conversational. " +
                                "If asked about your capabilities, mention that you can help with questions, " +
                                "provide information, and have conversations."
                    )
                ),
                role = "user"
            )
        )
        
        // Add a model acknowledgment
        contents.add(
            Content(
                parts = listOf(Part(text = "Understood! I'm Argus, your helpful AI assistant. How can I help you today?")),
                role = "model"
            )
        )
        
        // Add conversation history (last 8 messages for context)
        val recentHistory = history.takeLast(8)
        var isUser = recentHistory.size % 2 == 1
        
        recentHistory.forEach { message ->
            contents.add(
                Content(
                    parts = listOf(Part(text = message)),
                    role = if (isUser) "user" else "model"
                )
            )
            isUser = !isUser
        }
        
        // Add current message
        contents.add(
            Content(
                parts = listOf(Part(text = currentMessage)),
                role = "user"
            )
        )
        
        return contents
    }
}

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 500
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)