package com.pucmm.chatApp.data.repository

import android.net.Uri
import com.pucmm.chatApp.data.remote.StorageDataSource

class StorageRepository(
    private val storageDataSource: StorageDataSource
) {
    suspend fun uploadChatImage(imageUri: Uri, conversationId: String): Result<String> {
        return storageDataSource.uploadChatImage(imageUri, conversationId)
    }
}
