package com.pucmm.chatApp.ui.chat

import com.pucmm.chatApp.data.model.Message
import com.pucmm.chatApp.data.repository.ChatRepository

class ChatViewModel(
    private val chatRepository: ChatRepository
) {

    // se puso mutable para probar el envio de mensajes.
    private val messageList: MutableList<Message> = mutableListOf(
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
        return messageList.toList() //momentaneamente.
    }

    fun sendMessage(conversationId: String, text: String): List<Message> {
        // Se hace manual para probar. Cambiar logica para aplicar Firebase
        // TODO: Cambiar logica para conectar con Firebase
        val message = Message(
            id = "$conversationId-${System.currentTimeMillis()}",
            senderId = "current-user",
            receiverId = "other-user",
            text = text
        )

        messageList.add(message)
        return messageList.toList()
    }
}
