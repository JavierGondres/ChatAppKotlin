package com.pucmm.chatApp.data.model

data class Conversation(
    val conversationId: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
