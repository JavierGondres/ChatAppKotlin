package com.pucmm.chatApp.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.pucmm.chatApp.BuildConfig
import com.pucmm.chatApp.data.util.Constants
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(BuildConfig.FIREBASE_STORAGE_BUCKET)
) {
    suspend fun uploadChatImage(imageUri: Uri, conversationId: String): Result<String> {
        if (conversationId.isBlank()) {
            return Result.failure(Exception("No se pudo subir la imagen"))
        }

        return runCatching {
            val imageRef = storage.reference
                .child(Constants.STORAGE_CHAT_IMAGES_PATH)
                .child(conversationId)
                .child("${UUID.randomUUID()}.jpg")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Result.failure(Exception(error.toStorageMessage(), error))
            }
        )
    }

    private fun Throwable.toStorageMessage(): String {
        val code = (this as? StorageException)?.errorCode
            ?: (cause as? StorageException)?.errorCode
        return when (code) {
            StorageException.ERROR_NOT_AUTHORIZED ->
                "No hay permiso para subir la imagen. Revisa las reglas de Storage."
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
                "Revisa tu conexión a internet"
            else -> "No se pudo subir la imagen"
        }
    }
}
