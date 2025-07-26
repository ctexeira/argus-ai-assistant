package com.argus.app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argus.app.ai.AIService
import com.argus.app.ai.LocalAIService
import com.argus.app.data.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatViewModel(private val aiService: AIService = LocalAIService()) : ViewModel() {
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages
    
    private val _currentMessage = mutableStateOf("")
    val currentMessage = _currentMessage
    
    private val _isAiTyping = mutableStateOf(false)
    val isAiTyping = _isAiTyping
    
    private val _streamingResponse = mutableStateOf("")
    val streamingResponse = _streamingResponse
    
    private val _isStreaming = mutableStateOf(false)
    val isStreaming = _isStreaming
    
    init {
        // Add a welcome message
        _messages.add(
            ChatMessage(
                content = "Hello! I'm Argus, your AI assistant. How can I help you today?",
                isFromUser = false
            )
        )
    }
    
    fun updateCurrentMessage(message: String) {
        _currentMessage.value = message
    }
    
    fun sendMessage() {
        val messageText = _currentMessage.value.trim()
        if (messageText.isNotEmpty()) {
            // Add user message
            _messages.add(
                ChatMessage(
                    content = messageText,
                    isFromUser = true
                )
            )
            
            // Clear input
            _currentMessage.value = ""
            
            // Simulate AI response
            simulateAiResponse(messageText)
        }
    }
    
    private fun simulateAiResponse(userMessage: String) {
        viewModelScope.launch {
            _isAiTyping.value = true
            _isStreaming.value = true
            _streamingResponse.value = ""
            
            // Get conversation history for context
            val conversationHistory = _messages.takeLast(10).map { it.content }
            
            try {
                aiService.sendMessage(userMessage, conversationHistory)
                    .catch { e ->
                        // Handle errors gracefully
                        _streamingResponse.value = "Sorry, I encountered an error. Please try again."
                        delay(1000)
                        finalizeStreamingResponse()
                    }
                    .collect { partialResponse ->
                        _streamingResponse.value = partialResponse
                    }
                
                // Finalize the streaming response
                finalizeStreamingResponse()
                
            } catch (e: Exception) {
                _streamingResponse.value = "Sorry, I encountered an error. Please try again."
                delay(1000)
                finalizeStreamingResponse()
            }
        }
    }
    
    private fun finalizeStreamingResponse() {
        if (_streamingResponse.value.isNotEmpty()) {
            _messages.add(
                ChatMessage(
                    content = _streamingResponse.value,
                    isFromUser = false
                )
            )
        }
        
        _streamingResponse.value = ""
        _isStreaming.value = false
        _isAiTyping.value = false
    }
    
    fun getQuickSuggestions(): List<String> {
        return listOf(
            "Hello! How are you?",
            "What can you help me with?",
            "Tell me about your capabilities",
            "What time is it?",
            "How do you work?"
        )
    }
    
    fun sendQuickMessage(message: String) {
        updateCurrentMessage(message)
        sendMessage()
    }
    
    fun clearMessages() {
        _messages.clear()
        _streamingResponse.value = ""
        _isStreaming.value = false
        _isAiTyping.value = false
        
        // Re-add welcome message
        _messages.add(
            ChatMessage(
                content = "Hello! I'm Argus, your AI assistant. How can I help you today?",
                isFromUser = false
            )
        )
    }
    
    fun stopStreaming() {
        _isStreaming.value = false
        _isAiTyping.value = false
    }
}