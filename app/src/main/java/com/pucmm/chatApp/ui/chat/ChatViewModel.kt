package com.pucmm.chatApp.ui.chat

import com.pucmm.chatApp.data.model.Message
import com.pucmm.chatApp.data.repository.ChatRepository

class ChatViewModel(
    private val chatRepository: ChatRepository
) {
    fun observeMessages(conversationId: String): List<Message> {
        return chatRepository.observeMessages(conversationId)
    }

    suspend fun sendMessage(message: Message): Result<Unit> {
        return chatRepository.sendMessage(message)
    }
}
