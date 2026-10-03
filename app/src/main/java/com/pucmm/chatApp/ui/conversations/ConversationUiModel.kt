package com.pucmm.chatApp.ui.conversations

// Esta clase solo necesita los datos que la pantalla va a mostrar
data class ConversationUiModel (
    val conversationId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val time: String = "",
    val unreadCount: Int = 0,
)