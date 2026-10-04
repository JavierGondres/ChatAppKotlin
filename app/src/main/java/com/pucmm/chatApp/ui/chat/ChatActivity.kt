package com.pucmm.chatApp.ui.chat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pucmm.chatApp.R
import com.pucmm.chatApp.di.AppModule

class ChatActivity : AppCompatActivity() {
    private val viewModel: ChatViewModel by lazy {
        ChatViewModel(AppModule.chatRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val conversationId = intent.getStringExtra("conversationId").orEmpty()
        val otherUserName = intent.getStringExtra("otherUserName").orEmpty()

        val messages = viewModel.observeMessages(conversationId)

        val editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        val buttonSend = findViewById<Button>(R.id.buttonSend)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ChatAdapter(messages, currentUserId = "current-user", otherUserName)

        buttonSend.setOnClickListener {
            val text = editTextMessage.text.toString().trim()

            if (text.isEmpty()) {
                return@setOnClickListener // si se pone un return normal, sale de la funcion OnCreate
                // por ello, se usa @setOnClickListener. Salir de la funcion lambda.
            }

            val updatedMessages = viewModel.sendMessage(
                conversationId = conversationId,
                text = text
            )

            adapter.updateMessages(updatedMessages)

            editTextMessage.text.clear()

            recyclerView.scrollToPosition(adapter.itemCount - 1)
        }

        recyclerView.adapter = adapter

    }
}
