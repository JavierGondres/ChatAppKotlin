package com.pucmm.chatApp.data.remote

import com.pucmm.chatApp.data.model.Message

class FirestoreDataSource {
    fun observeMessages(conversationId: String): List<Message> {
        // TODO: escuchar mensajes desde Firestore con snapshots
        return emptyList()
    }

    suspend fun sendMessage(message: Message): Result<Unit> {
        return runCatching {
            // TODO: guardar mensaje en Firestore
            Unit
        }
    }

    suspend fun createConversation(participantIds: List<String>): Result<String> {
        return runCatching {
            // TODO: crear documento de conversación en Firestore
            participantIds.joinToString(separator = "-")
        }
    }
}
