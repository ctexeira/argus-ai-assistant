package com.argus.app.ai

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

interface AIService {
    suspend fun sendMessage(message: String, conversationHistory: List<String> = emptyList()): Flow<String>
    suspend fun getQuickResponse(message: String): String
}

class LocalAIService : AIService {
    
    private val responses = mapOf(
        "greeting" to listOf(
            "Hello! I'm Argus, your AI assistant. How can I help you today?",
            "Hi there! What would you like to talk about?",
            "Greetings! I'm here to assist you with anything you need."
        ),
        "help" to listOf(
            "I'm here to help! You can ask me questions, have conversations, get information, or just chat. What do you need assistance with?",
            "I can help you with various tasks like answering questions, providing information, having conversations, and more. What would you like to do?",
            "Feel free to ask me anything! I can assist with questions, provide explanations, help with tasks, or just have a friendly chat."
        ),
        "weather" to listOf(
            "I don't have access to real-time weather data yet, but that's a feature we're planning to add! For now, you can check your local weather app.",
            "Weather integration is coming soon! In the meantime, I'd recommend checking a weather app or website for current conditions."
        ),
        "time" to listOf(
            "The current time is ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}",
            "Right now it's ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())}"
        ),
        "capabilities" to listOf(
            "I can help with conversations, answer questions, provide explanations, assist with tasks, and more. I'm constantly learning and improving!",
            "My capabilities include chatting, answering questions, helping with various tasks, and providing information. What would you like to explore?"
        ),
        "technology" to listOf(
            "I'm built using advanced AI technology to provide helpful and engaging conversations. I'm designed to be your intelligent assistant!",
            "I use modern AI techniques to understand and respond to your messages. I'm here to make your digital experience more helpful and enjoyable."
        ),
        "default" to listOf(
            "That's interesting! I'm still learning and will have more advanced capabilities soon. What else would you like to talk about?",
            "I find that fascinating! While I'm continuously improving, I'd love to hear more about what you're thinking.",
            "Thanks for sharing that with me! I'm always eager to learn and discuss new topics. What else is on your mind?",
            "That's a great point! I enjoy our conversation and I'm here to help with whatever you need."
        )
    )
    
    override suspend fun sendMessage(message: String, conversationHistory: List<String>): Flow<String> = flow {
        val category = categorizeMessage(message)
        val response = getResponseForCategory(category)
        
        // Simulate streaming response
        val words = response.split(" ")
        var currentResponse = ""
        
        for (word in words) {
            delay(50) // Simulate typing speed
            currentResponse += "$word "
            emit(currentResponse.trim())
        }
    }
    
    override suspend fun getQuickResponse(message: String): String {
        val category = categorizeMessage(message)
        return getResponseForCategory(category)
    }
    
    private fun categorizeMessage(message: String): String {
        val lowerMessage = message.lowercase()
        
        return when {
            lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("hey") -> "greeting"
            lowerMessage.contains("help") || lowerMessage.contains("assist") -> "help"
            lowerMessage.contains("weather") || lowerMessage.contains("temperature") || lowerMessage.contains("rain") -> "weather"
            lowerMessage.contains("time") || lowerMessage.contains("clock") -> "time"
            lowerMessage.contains("what can you do") || lowerMessage.contains("capabilities") || lowerMessage.contains("features") -> "capabilities"
            lowerMessage.contains("how do you work") || lowerMessage.contains("technology") || lowerMessage.contains("ai") -> "technology"
            else -> "default"
        }
    }
    
    private fun getResponseForCategory(category: String): String {
        val categoryResponses = responses[category] ?: responses["default"]!!
        return categoryResponses.random()
    }
}

// Future implementation for real AI APIs
class OpenAIService(private val apiKey: String) : AIService {
    
    override suspend fun sendMessage(message: String, conversationHistory: List<String>): Flow<String> = flow {
        // TODO: Implement OpenAI API integration
        // This would make actual API calls to OpenAI's GPT models
        emit("OpenAI integration coming soon!")
    }
    
    override suspend fun getQuickResponse(message: String): String {
        // TODO: Implement OpenAI API integration
        return "OpenAI integration coming soon!"
    }
}

class GeminiService(private val apiKey: String) : AIService {
    
    override suspend fun sendMessage(message: String, conversationHistory: List<String>): Flow<String> = flow {
        // TODO: Implement Google Gemini API integration
        emit("Gemini integration coming soon!")
    }
    
    override suspend fun getQuickResponse(message: String): String {
        // TODO: Implement Google Gemini API integration
        return "Gemini integration coming soon!"
    }
}