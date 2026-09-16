package com.pucmm.chatApp.data.repository

import com.pucmm.chatApp.data.remote.StorageDataSource

class StorageRepository(
    private val storageDataSource: StorageDataSource
) {
    suspend fun uploadChatImage(imageUri: String, conversationId: String): Result<String> {
        return storageDataSource.uploadChatImage(imageUri, conversationId)
    }
}
