package com.pucmm.chatApp.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val sentAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
