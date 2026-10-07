package com.pucmm.chatApp.ui.conversations

// Esta clase solo necesita los datos que la pantalla va a mostrar
// Se necesita para mostrar todos los valores del XML.
data class ConversationUiModel (
    val conversationId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val time: String = "",
    val unreadCount: Int = 0,
)