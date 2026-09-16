package com.pucmm.chatApp.data.repository

import com.pucmm.chatApp.data.model.Message
import com.pucmm.chatApp.data.remote.FirestoreDataSource

class ChatRepository(
    private val firestoreDataSource: FirestoreDataSource
) {
    fun observeMessages(conversationId: String): List<Message> {
        return firestoreDataSource.observeMessages(conversationId)
    }

    suspend fun sendMessage(message: Message): Result<Unit> {
        return firestoreDataSource.sendMessage(message)
    }

    suspend fun createConversation(participantIds: List<String>): Result<String> {
        return firestoreDataSource.createConversation(participantIds)
    }
}
