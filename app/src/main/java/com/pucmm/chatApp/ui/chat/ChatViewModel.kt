package com.pucmm.chatApp.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pucmm.chatApp.data.model.Message
import com.pucmm.chatApp.data.repository.AuthRepository
import com.pucmm.chatApp.data.repository.ChatRepository
import com.pucmm.chatApp.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val pendingImageUri: Uri? = null,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository,
    private val conversationId: String,
    private val otherUserId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val currentUserId: String = authRepository.getCurrentUser()?.uid.orEmpty()

    init {
        viewModelScope.launch {
            chatRepository.observeMessages(conversationId)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudieron cargar los mensajes"
                    )
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages.sortedBy { message -> message.sentAt },
                        isLoading = false
                    )
                }
        }
    }

    fun setPendingImage(uri: Uri) {
        if (_uiState.value.isSending) return
        _uiState.value = _uiState.value.copy(pendingImageUri = uri)
    }

    fun clearPendingImage() {
        if (_uiState.value.isSending) return
        _uiState.value = _uiState.value.copy(pendingImageUri = null)
    }

    fun sendMessage(text: String): Boolean {
        val trimmed = text.trim()
        val pendingImage = _uiState.value.pendingImageUri
        if (_uiState.value.isSending) return false
        if (trimmed.isEmpty() && pendingImage == null) return false

        val user = authRepository.getCurrentUser() ?: return false
        val senderName = user.displayName.ifBlank { user.email }.ifBlank { "Usuario" }
        _uiState.value = _uiState.value.copy(isSending = true)

        viewModelScope.launch {
            val imageUrl = if (pendingImage == null) {
                null
            } else {
                val upload = storageRepository.uploadChatImage(pendingImage, conversationId)
                upload.getOrElse { error ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = error.message ?: "No se pudo subir la imagen"
                    )
                    return@launch
                }
            }

            val result = chatRepository.sendMessage(
                conversationId,
                Message(
                    senderId = user.uid,
                    senderName = senderName,
                    receiverId = otherUserId,
                    text = trimmed,
                    imageUrl = imageUrl
                )
            )
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        pendingImageUri = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = error.message ?: "No se pudo enviar el mensaje"
                    )
                }
            )
        }
        return true
    }

    fun consumeError() {
        if (_uiState.value.errorMessage == null) return
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    companion object {
        fun factory(
            chatRepository: ChatRepository,
            authRepository: AuthRepository,
            storageRepository: StorageRepository,
            conversationId: String,
            otherUserId: String
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(
                        chatRepository,
                        authRepository,
                        storageRepository,
                        conversationId,
                        otherUserId
                    ) as T
                }
            }
        }
    }
}
