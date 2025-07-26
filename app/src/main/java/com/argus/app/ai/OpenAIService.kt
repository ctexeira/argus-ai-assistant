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

class OpenAIService(private val apiKey: String) : AIService {
    
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
            val messages = buildMessageHistory(message, conversationHistory)
            val request = createStreamingRequest(messages)
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    when (response.code) {
                        401 -> emit("Invalid API key. Please check your OpenAI API key in settings.")
                        429 -> emit("Rate limit exceeded. Please try again in a moment.")
                        500, 502, 503, 504 -> emit("OpenAI service is temporarily unavailable. Please try again later.")
                        else -> emit("Error ${response.code}: ${response.message}. Please check your API key and try again.")
                    }
                    return@flow
                }
                
                val source = response.body?.source()
                if (source == null) {
                    emit("Sorry, I didn't receive a proper response. Please try again.")
                    return@flow
                }
                
                var accumulatedResponse = ""
                
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    
                    if (line.startsWith("data: ")) {
                        val data = line.substring(6)
                        
                        if (data == "[DONE]") {
                            break
                        }
                        
                        try {
                            val streamResponse = json.decodeFromString<OpenAIStreamResponse>(data)
                            val content = streamResponse.choices.firstOrNull()?.delta?.content
                            
                            if (content != null) {
                                accumulatedResponse += content
                                emit(accumulatedResponse)
                                delay(50) // Smooth streaming effect
                            }
                        } catch (e: Exception) {
                            // Skip malformed JSON chunks
                            continue
                        }
                    }
                }
                
                if (accumulatedResponse.isEmpty()) {
                    emit("I'm sorry, I couldn't generate a response. Please try rephrasing your question.")
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
            val messages = buildMessageHistory(message, emptyList())
            val request = createNonStreamingRequest(messages)
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    when (response.code) {
                        401 -> "Invalid API key. Please check your OpenAI API key in settings."
                        429 -> "Rate limit exceeded. Please try again in a moment."
                        500, 502, 503, 504 -> "OpenAI service is temporarily unavailable. Please try again later."
                        else -> "Error ${response.code}: ${response.message}"
                    }
                } else {
                    val responseBody = response.body?.string() ?: ""
                    val jsonResponse = json.decodeFromString<OpenAINonStreamResponse>(responseBody)
                    jsonResponse.choices.firstOrNull()?.message?.content ?: "No response received"
                }
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    private fun buildMessageHistory(currentMessage: String, history: List<String>): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        
        // Add system message
        messages.add(
            ChatMessage(
                role = "system",
                content = "You are Argus, a helpful AI assistant integrated into an Android app. " +
                        "Provide concise, helpful responses. Be friendly and conversational. " +
                        "If asked about your capabilities, mention that you can help with questions, " +
                        "provide information, and have conversations."
            )
        )
        
        // Add conversation history (last 10 messages for context)
        val recentHistory = history.takeLast(10)
        var isUser = recentHistory.size % 2 == 1
        
        recentHistory.forEach { message ->
            messages.add(
                ChatMessage(
                    role = if (isUser) "user" else "assistant",
                    content = message
                )
            )
            isUser = !isUser
        }
        
        // Add current message
        messages.add(
            ChatMessage(
                role = "user",
                content = currentMessage
            )
        )
        
        return messages
    }
    
    private fun createStreamingRequest(messages: List<ChatMessage>): Request {
        val requestBody = OpenAIRequest(
            model = "gpt-3.5-turbo",
            messages = messages,
            stream = true,
            maxTokens = 500,
            temperature = 0.7
        )
        
        val jsonBody = json.encodeToString(OpenAIRequest.serializer(), requestBody)
        
        return Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
    }
    
    private fun createNonStreamingRequest(messages: List<ChatMessage>): Request {
        val requestBody = OpenAIRequest(
            model = "gpt-3.5-turbo",
            messages = messages,
            stream = false,
            maxTokens = 500,
            temperature = 0.7
        )
        
        val jsonBody = json.encodeToString(OpenAIRequest.serializer(), requestBody)
        
        return Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
    }
}

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
    val maxTokens: Int? = null,
    val temperature: Double = 0.7
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIStreamResponse(
    val choices: List<StreamChoice>
)

@Serializable
data class StreamChoice(
    val delta: StreamDelta
)

@Serializable
data class StreamDelta(
    val content: String? = null
)

@Serializable
data class OpenAINonStreamResponse(
    val choices: List<NonStreamChoice>
)

@Serializable
data class NonStreamChoice(
    val message: ChatMessage
)