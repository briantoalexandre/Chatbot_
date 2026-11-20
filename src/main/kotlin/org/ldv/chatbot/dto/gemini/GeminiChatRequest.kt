package org.ldv.chatbot.dto.gemini

data class GeminiChatRequest(
    val model: String,
    val messages: List<GeminiMessage>
)