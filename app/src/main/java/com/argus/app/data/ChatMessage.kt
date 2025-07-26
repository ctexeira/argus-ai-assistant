package com.argus.app.data

import java.util.Date
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date = Date(),
    val isTyping: Boolean = false
)

enum class MessageType {
    USER,
    AI,
    SYSTEM
}