package com.pucmm.chatApp.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pucmm.chatApp.data.model.Conversation
import com.pucmm.chatApp.data.model.User
import com.pucmm.chatApp.data.repository.AuthRepository
import com.pucmm.chatApp.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ConversationsUiState(
    val conversations: List<ConversationUiModel> = emptyList(),
    val availableUsers: List<User> = emptyList(),
    val currentUserName: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val openedConversation: ConversationUiModel? = null
)

class ConversationsViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val currentUser = authRepository.getCurrentUser()
    private val currentUid = currentUser?.uid.orEmpty()

    private val _uiState = MutableStateFlow(
        ConversationsUiState(
            currentUserName = currentUser?.visibleName().orEmpty(),
            isLoading = currentUid.isNotBlank()
        )
    )
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    private var isCreatingConversation = false

    init {
        if (currentUid.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Inicia sesión para ver tus chats"
            )
        } else {
            viewModelScope.launch {
                combine(
                    chatRepository.observeUsers(),
                    chatRepository.observeConversations(currentUid)
                ) { users, conversations ->
                    users to conversations
                }.catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: "No se pudieron cargar las conversaciones"
                    )
                }.collect { (users, conversations) ->
                    val usersById = users.associateBy { it.uid }
                    val partnerIds = conversations
                        .flatMap { conversation -> conversation.participantIds }
                        .filter { participantId -> participantId != currentUid }
                        .toSet()
                    val availableUsers = users
                        .filter { user -> user.uid != currentUid && user.uid !in partnerIds }
                        .sortedBy { user -> user.visibleName().lowercase() }

                    _uiState.value = _uiState.value.copy(
                        conversations = conversations.toUiModels(currentUid, usersById),
                        availableUsers = availableUsers,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createConversation(user: User) {
        if (isCreatingConversation || currentUid.isBlank() || user.uid.isBlank()) return

        isCreatingConversation = true
        viewModelScope.launch {
            val result = chatRepository.createConversation(currentUid, user.uid)
            isCreatingConversation = false
            result.fold(
                onSuccess = { conversationId ->
                    _uiState.value = _uiState.value.copy(
                        openedConversation = ConversationUiModel(
                            conversationId = conversationId,
                            otherUserId = user.uid,
                            otherUserName = user.visibleName()
                        )
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "No se pudo crear el chat"
                    )
                }
            )
        }
    }

    fun consumeOpenedConversation() {
        if (_uiState.value.openedConversation == null) return
        _uiState.value = _uiState.value.copy(openedConversation = null)
    }

    fun consumeError() {
        if (_uiState.value.errorMessage == null) return
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun signOut() {
        authRepository.signOut()
    }

    private fun List<Conversation>.toUiModels(
        currentUid: String,
        usersById: Map<String, User>
    ): List<ConversationUiModel> {
        return sortedByDescending { conversation -> conversation.updatedAt }
            .map { conversation ->
                val otherUserId = conversation.participantIds
                    .firstOrNull { participantId -> participantId != currentUid }
                    .orEmpty()
                val otherUser = usersById[otherUserId]
                ConversationUiModel(
                    conversationId = conversation.conversationId,
                    otherUserId = otherUserId,
                    otherUserName = otherUser?.visibleName() ?: "Usuario",
                    lastMessage = conversation.lastMessage.ifBlank { "Sin mensajes" },
                    time = formatConversationTime(conversation.updatedAt),
                    unreadCount = 0
                )
            }
    }

    private fun formatConversationTime(updatedAt: Long): String {
        if (updatedAt <= 0L) return ""
        val messageDay = Calendar.getInstance().apply { timeInMillis = updatedAt }
        val today = Calendar.getInstance()
        val sameDay = messageDay.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            messageDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        val pattern = if (sameDay) "h:mm a" else "dd/MM/yyyy"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(updatedAt))
    }

    companion object {
        fun factory(
            chatRepository: ChatRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ConversationsViewModel(chatRepository, authRepository) as T
                }
            }
        }
    }
}

private fun User.visibleName(): String {
    return displayName.ifBlank { email }.ifBlank { "Usuario" }
}
