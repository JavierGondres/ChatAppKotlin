package com.pucmm.chatApp.ui.chat

import com.pucmm.chatApp.data.model.Message
import com.pucmm.chatApp.data.repository.ChatRepository

class ChatViewModel(
    private val chatRepository: ChatRepository
) {

    private val messageList: List<Message> = listOf(
        Message(
            id = "message-1",
            senderId = "current-user",
            receiverId = "user-javier",
            text = "Hola Javier"
        ),
        Message(
            id = "message-2",
            senderId = "user-javier",
            receiverId = "current-user",
            text = "Como ta mi brodel"
        ),
        Message(
            id = "message-3",
            senderId = "current-user",
            receiverId = "user-javier",
            text = "Cuidate mio"
        )
    )

    fun observeMessages(conversationId: String): List<Message> {
        //return chatRepository.observeMessages(conversationId)
        return messageList //momentaneamente.
    }

    suspend fun sendMessage(message: Message): Result<Unit> {
        return chatRepository.sendMessage(message)
    }
}
