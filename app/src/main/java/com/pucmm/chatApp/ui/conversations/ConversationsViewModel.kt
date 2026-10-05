package com.pucmm.chatApp.ui.conversations

import com.pucmm.chatApp.data.model.Conversation
import com.pucmm.chatApp.data.repository.AuthRepository
import com.pucmm.chatApp.data.repository.ChatRepository

class ConversationsViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) {
    fun getConversations(): List<Conversation> {
        return emptyList()
    }

    fun signOut() {
        authRepository.signOut()
    }
}
