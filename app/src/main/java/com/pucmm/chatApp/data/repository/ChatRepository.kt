package com.pucmm.chatApp.data.repository

import com.pucmm.chatApp.data.model.Conversation
import com.pucmm.chatApp.data.model.Message
import com.pucmm.chatApp.data.model.User
import com.pucmm.chatApp.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val firestoreDataSource: FirestoreDataSource
) {
    fun observeUsers(): Flow<List<User>> {
        return firestoreDataSource.observeUsers()
    }

    fun observeConversations(currentUid: String): Flow<List<Conversation>> {
        return firestoreDataSource.observeConversations(currentUid)
    }

    fun observeMessages(conversationId: String): Flow<List<Message>> {
        return firestoreDataSource.observeMessages(conversationId)
    }

    suspend fun sendMessage(conversationId: String, message: Message): Result<Unit> {
        return firestoreDataSource.sendMessage(conversationId, message)
    }

    suspend fun createConversation(currentUid: String, otherUid: String): Result<String> {
        return firestoreDataSource.createConversation(currentUid, otherUid)
    }
}
