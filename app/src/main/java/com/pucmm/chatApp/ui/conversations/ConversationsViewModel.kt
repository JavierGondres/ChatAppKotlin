package com.pucmm.chatApp.ui.conversations

import com.pucmm.chatApp.data.repository.AuthRepository
import com.pucmm.chatApp.data.repository.ChatRepository

class ConversationsViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) {
    private val conversationsList: List<ConversationUiModel> = listOf(
        ConversationUiModel(
            conversationId = "1",
            otherUserName = "Javier",
            lastMessage = "Cuídate",
            time = "10:00",
            unreadCount = 1
        ),
        ConversationUiModel(
            conversationId = "2",
            otherUserName = "Albania",
            lastMessage = "Consigue galletas",
            time = "3:00 PM",
            unreadCount = 2
        )
    )

    fun getConversations(): List<ConversationUiModel> {
        return conversationsList
    }

    fun signOut() {
        authRepository.signOut()
    }
}
