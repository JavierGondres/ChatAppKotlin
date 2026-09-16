package com.pucmm.chatApp.data.remote

class StorageDataSource {
    suspend fun uploadChatImage(imageUri: String, conversationId: String): Result<String> {
        return runCatching {
            // TODO: subir imagen a Firebase Storage
            "https://example.com/$conversationId/$imageUri"
        }
    }
}
