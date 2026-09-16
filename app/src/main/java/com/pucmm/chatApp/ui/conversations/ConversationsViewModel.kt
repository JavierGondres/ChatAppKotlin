package com.pucmm.chatApp.ui.conversations

import com.pucmm.chatApp.data.model.Conversation
import com.pucmm.chatApp.data.repository.ChatRepository

class ConversationsViewModel(
    private val chatRepository: ChatRepository
) {
    fun getConversations(): List<Conversation> {
        return emptyList()
    }
}
